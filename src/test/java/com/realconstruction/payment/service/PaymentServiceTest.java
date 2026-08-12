package com.realconstruction.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.realconstruction.payment.publisher.RabbitMQPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.realconstruction.payment.model.Payment;
import com.realconstruction.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RabbitMQPublisher rabbitMQPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {

        payment = Payment.builder()
                .id("PAY001")
                .orderId("ORD001")
                .userId("USR001")
                .amount(new BigDecimal("15000"))
                .currency("LKR")
                .paymentMethod("CARD")
                .status("SUCCESS")
                .build();
    }

    @Test
    void shouldCreatePayment() {

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        Payment savedPayment =
                paymentService.createPayment(payment);

        assertNotNull(savedPayment);
        assertEquals("PAY001", savedPayment.getId());

        verify(paymentRepository, times(1))
                .save(any(Payment.class));

        verify(rabbitMQPublisher, times(1))
                .publishPaymentCreated(any());
    }

    @Test
    void shouldReturnPaymentById() {

        when(paymentRepository.findById("PAY001"))
                .thenReturn(Optional.of(payment));

        Optional<Payment> result =
                paymentService.getPaymentById("PAY001");

        assertTrue(result.isPresent());
        assertEquals("PAY001", result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenPaymentNotFound() {

        when(paymentRepository.findById("UNKNOWN"))
                .thenReturn(Optional.empty());

        Optional<Payment> result =
                paymentService.getPaymentById("UNKNOWN");

        assertTrue(result.isEmpty());
    }
}