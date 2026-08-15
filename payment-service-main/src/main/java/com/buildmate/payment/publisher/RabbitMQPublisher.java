package com.buildmate.payment.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.buildmate.payment.config.RabbitMQConfig;
import com.buildmate.payment.event.PaymentCreatedEvent;
import com.buildmate.payment.event.PaymentRefundedEvent;

@Component
public class RabbitMQPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCreated(PaymentCreatedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_ROUTING_KEY,
                event);

        logger.info("PaymentCreatedEvent published for Payment ID: {}", event.getPaymentId());
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_ROUTING_KEY,
                event);

        logger.info("Payment refunded event published for payment {}",
                event.getPaymentId());
    }
}
