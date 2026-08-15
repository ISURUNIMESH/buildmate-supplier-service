package com.buildmate.orderinventory.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.buildmate.orderinventory.config.RabbitMQConfig;
import com.buildmate.orderinventory.events.PaymentCompletedEvent;
import com.buildmate.orderinventory.exception.BusinessConflictException;
import com.buildmate.orderinventory.exception.ResourceNotFoundException;
import com.buildmate.orderinventory.service.OrderService;

/**
 * PaymentCompletedEvent → mark order PAID.
 * Permanent business failures are rejected without requeue (no infinite retry).
 * Transient failures propagate so the broker can redeliver.
 */
@Component
public class PaymentCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedListener.class);

    private final OrderService orderService;

    public PaymentCompletedListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event == null || event.getOrderId() == null || event.getOrderId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException("payment.completed missing orderId");
        }

        log.info(
                "Received PaymentCompleted Event: paymentId={}, orderId={}, amount={}, paymentStatus={}, paidAt={}",
                event.getPaymentId(),
                event.getOrderId(),
                event.getAmount(),
                event.getPaymentStatus(),
                event.getPaidAt());

        try {
            orderService.markPaid(event.getOrderId());
            log.info("Order {} status updated to PAID after payment {}", event.getOrderId(), event.getPaymentId());
        } catch (ResourceNotFoundException | BusinessConflictException | IllegalArgumentException ex) {
            log.error(
                    "Permanent failure marking order PAID for orderId={} paymentId={}. Cause: {}",
                    event.getOrderId(),
                    event.getPaymentId(),
                    ex.getMessage());
            throw new AmqpRejectAndDontRequeueException("Permanent payment.completed failure", ex);
        }
    }
}
