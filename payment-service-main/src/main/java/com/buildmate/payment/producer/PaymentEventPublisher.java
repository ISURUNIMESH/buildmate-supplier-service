package com.buildmate.payment.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.buildmate.payment.config.RabbitMQConfig;
import com.buildmate.payment.dto.PaymentCompletedEvent;

/**
 * Publishes payment domain events to RabbitMQ (Phase 2).
 * Failures are logged and do not roll back the saved payment (graceful degrade).
 */
@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BUILDMATE_EXCHANGE,
                    RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY,
                    event);

            log.info(
                    "Published PaymentCompletedEvent to exchange={} routingKey={} paymentId={} orderId={} amount={} paymentStatus={}",
                    RabbitMQConfig.BUILDMATE_EXCHANGE,
                    RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY,
                    event.getPaymentId(),
                    event.getOrderId(),
                    event.getAmount(),
                    event.getPaymentStatus());
        } catch (Exception ex) {
            log.error(
                    "Failed to publish PaymentCompletedEvent for paymentId={} orderId={}. Payment was saved; message not delivered. Cause: {}",
                    event != null ? event.getPaymentId() : null,
                    event != null ? event.getOrderId() : null,
                    ex.getMessage(),
                    ex);
        }
    }
}
