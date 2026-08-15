package com.buildmate.orderinventory.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.buildmate.orderinventory.config.RabbitMQConfig;
import com.buildmate.orderinventory.events.OrderCreatedEvent;

/**
 * Publishes order domain events to RabbitMQ.
 * Failures are logged and do not roll back the persisted order (graceful degrade).
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    event);

            log.info(
                    "Published OrderCreatedEvent to exchange={} routingKey={} orderId={} userId={} totalAmount={} status={}",
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    event.getOrderId(),
                    event.getUserId(),
                    event.getTotalAmount(),
                    event.getStatus());
        } catch (Exception ex) {
            log.error(
                    "Failed to publish OrderCreatedEvent for orderId={}. Order was saved; message not delivered. Cause: {}",
                    event != null ? event.getOrderId() : null,
                    ex.getMessage(),
                    ex);
        }
    }
}
