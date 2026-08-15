package com.buildmate.payment.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.buildmate.payment.config.RabbitMQConfig;
import com.buildmate.payment.dto.OrderCreatedEvent;

/**
 * Phase 1 consumer: receives OrderCreatedEvent and logs it.
 * Does not create payments yet (deferred to a later phase).
 */
@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info(
                "Received OrderCreated Event: orderId={}, userId={}, totalAmount={}, status={}, createdAt={}",
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount(),
                event.getStatus(),
                event.getCreatedAt());
    }
}
