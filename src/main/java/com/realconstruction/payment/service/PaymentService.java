package com.realconstruction.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.realconstruction.payment.event.PaymentCreatedEvent;
import com.realconstruction.payment.event.PaymentRefundedEvent;
import com.realconstruction.payment.model.MonthlyReport;
import com.realconstruction.payment.model.Payment;
import com.realconstruction.payment.model.RevenueReport;
import com.realconstruction.payment.model.TopCustomerReport;
import com.realconstruction.payment.publisher.RabbitMQPublisher;
import com.realconstruction.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RabbitMQPublisher rabbitMQPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          RabbitMQPublisher rabbitMQPublisher) {

        this.paymentRepository = paymentRepository;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    // Create Payment
    public Payment createPayment(Payment payment) {

        logger.info("Creating payment for Order ID: {}", payment.getOrderId());

        payment.setCreatedAt(LocalDateTime.now());

        if (payment.getStatus() == null || payment.getStatus().isBlank()) {
            payment.setStatus("PENDING");
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getAmount(),
                savedPayment.getStatus()
        );

        rabbitMQPublisher.publishPaymentCreated(event);

        logger.info("Payment created successfully with ID: {}", savedPayment.getId());

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

        PaymentRefundedEvent event = new PaymentRefundedEvent(
                refundedPayment.getId(),
                refundedPayment.getOrderId(),
                refundedPayment.getUserId(),
                refundedPayment.getAmount(),
                refundedPayment.getStatus());

        rabbitMQPublisher.publishPaymentRefunded(event);

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

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                updatedPayment.getId(),
                updatedPayment.getOrderId(),
                updatedPayment.getUserId(),
                updatedPayment.getAmount(),
                updatedPayment.getStatus()
        );

        rabbitMQPublisher.publishPaymentCreated(event);

        logger.info("Retry initiated for payment {}", id);

        return Optional.of(updatedPayment);
    }

    // Revenue Report
    public RevenueReport getRevenueReport() {

        List<Payment> payments = paymentRepository.findByStatus("SUCCESS");

        double totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        return new RevenueReport(
                totalRevenue,
                payments.size()
        );
    }

    // Monthly Report
    public MonthlyReport getMonthlyReport() {

        List<Payment> payments = paymentRepository.findByStatus("SUCCESS");

        double revenue = payments.stream()
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

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
            return new TopCustomerReport("", 0, 0);
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

        double totalSpent =
                topCustomer.getValue().stream()
                        .map(Payment::getAmount)
                        .filter(amount -> amount != null)
                        .mapToDouble(BigDecimal::doubleValue)
                        .sum();

        return new TopCustomerReport(
                topCustomer.getKey(),
                totalSpent,
                topCustomer.getValue().size()
        );
    }

}