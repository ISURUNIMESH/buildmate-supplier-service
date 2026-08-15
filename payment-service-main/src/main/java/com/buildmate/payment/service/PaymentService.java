package com.buildmate.payment.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.buildmate.payment.dto.PaymentCompletedEvent;
import com.buildmate.payment.model.MonthlyReport;
import com.buildmate.payment.model.Payment;
import com.buildmate.payment.model.RevenueReport;
import com.buildmate.payment.model.TopCustomerReport;
import com.buildmate.payment.producer.PaymentEventPublisher;
import com.buildmate.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentEventPublisher paymentEventPublisher) {

        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    // Create Payment
    public Payment createPayment(Payment payment) {

        logger.info("Creating payment for Order ID: {}", payment.getOrderId());

        payment.setCreatedAt(LocalDateTime.now());

        if (payment.getStatus() == null || payment.getStatus().isBlank()) {
            payment.setStatus("PENDING");
        }

        Payment savedPayment = paymentRepository.save(payment);

        logger.info("Payment created successfully with ID: {}", savedPayment.getId());

        publishIfCompleted(savedPayment);

        return savedPayment;
    }

    // Get All Payments
    public List<Payment> getAllPayments() {

        logger.info("Fetching all payments");

        return paymentRepository.findAll();
    }

    // Get Payment by ID
    public Optional<Payment> getPaymentById(String id) {

        logger.info("Fetching payment with ID: {}", id);

        return paymentRepository.findById(id);
    }

    // Payment History
    public List<Payment> getPaymentHistory(String userId) {

        logger.info("Fetching payment history for User ID: {}", userId);

        return paymentRepository.findByUserId(userId);
    }

    // Pending Payments
    public List<Payment> getPendingPayments() {

        logger.info("Fetching pending payments");

        return paymentRepository.findByStatus("PENDING");
    }

    // Payments By Status
    public List<Payment> getPaymentsByStatus(String status) {

        logger.info("Fetching payments with status: {}", status);

        return paymentRepository.findByStatus(status);
    }

    // Update Status
    public Optional<Payment> updatePaymentStatus(String id, String status) {

        Optional<Payment> paymentOptional = paymentRepository.findById(id);

        if (paymentOptional.isPresent()) {

            Payment payment = paymentOptional.get();

            payment.setStatus(status);

            Payment updatedPayment = paymentRepository.save(payment);

            logger.info("Payment {} status updated to {}", id, status);

            publishIfCompleted(updatedPayment);

            return Optional.of(updatedPayment);
        }

        return Optional.empty();
    }

    // Refund Payment
    public Optional<Payment> refundPayment(String id) {

        Optional<Payment> paymentOptional = paymentRepository.findById(id);

        if (paymentOptional.isEmpty()) {
            return Optional.empty();
        }

        Payment payment = paymentOptional.get();

        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {

            logger.warn("Refund rejected. Payment {} is {}",
                    id,
                    payment.getStatus());

            return Optional.empty();
        }

        payment.setStatus("REFUNDED");

        Payment refundedPayment = paymentRepository.save(payment);

        logger.info("Payment refunded successfully: {}",
                refundedPayment.getId());

        return Optional.of(refundedPayment);
    }

    // Retry Payment
    public Optional<Payment> retryPayment(String id) {

        Optional<Payment> paymentOptional = paymentRepository.findById(id);

        if (paymentOptional.isEmpty()) {
            return Optional.empty();
        }

        Payment payment = paymentOptional.get();

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {

            logger.warn("Retry rejected. Payment {} is already SUCCESS", id);

            return Optional.empty();
        }

        if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {

            logger.warn("Retry rejected. Payment {} is REFUNDED", id);

            return Optional.empty();
        }

        payment.setStatus("PENDING");

        Payment updatedPayment = paymentRepository.save(payment);

        logger.info("Retry initiated for payment {}", id);

        return Optional.of(updatedPayment);
    }

    // Revenue Report
    public RevenueReport getRevenueReport() {

        List<Payment> payments = paymentRepository.findByStatus("SUCCESS");

        BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RevenueReport(
                totalRevenue,
                payments.size()
        );
    }

    // Monthly Report
    public MonthlyReport getMonthlyReport() {

        List<Payment> payments = paymentRepository.findByStatus("SUCCESS");

        BigDecimal revenue = payments.stream()
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyReport(
                YearMonth.now().toString(),
                revenue,
                payments.size()
        );
    }

    // Top Customer Report
    public TopCustomerReport getTopCustomerReport() {

        List<Payment> payments = paymentRepository.findByStatus("SUCCESS");

        Map<String, List<Payment>> grouped =
                payments.stream()
                        .collect(Collectors.groupingBy(Payment::getUserId));

        if (grouped.isEmpty()) {
            return new TopCustomerReport("", BigDecimal.ZERO, 0);
        }

        Map.Entry<String, List<Payment>> topCustomer =
                grouped.entrySet()
                        .stream()
                        .max(Comparator.comparingDouble(entry ->
                                entry.getValue().stream()
                                        .map(Payment::getAmount)
                                        .filter(amount -> amount != null)
                                        .mapToDouble(BigDecimal::doubleValue)
                                        .sum()))
                        .orElseThrow();

        BigDecimal totalSpent =
                topCustomer.getValue().stream()
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TopCustomerReport(
                topCustomer.getKey(),
                totalSpent,
                topCustomer.getValue().size()
        );
    }

    private void publishIfCompleted(Payment payment) {
        if (payment == null || !isCompletedStatus(payment.getStatus())) {
            return;
        }

        Instant paidAt = payment.getCreatedAt() != null
                ? payment.getCreatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();

        paymentEventPublisher.publishPaymentCompleted(new PaymentCompletedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                paidAt));
    }

    private boolean isCompletedStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "COMPLETED".equals(normalized)
                || "SUCCESS".equals(normalized)
                || "PAID".equals(normalized);
    }

}
