package com.buildmate.supplier.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.buildmate.supplier.config.RabbitMQConfig;
import com.buildmate.supplier.events.SupplierDeletedEvent;
import com.buildmate.supplier.events.SupplierEvent;
import com.buildmate.supplier.events.SupplierStatusChangedEvent;
import com.buildmate.supplier.model.Supplier;

import java.time.Instant;

/**
 * Publishes supplier domain events.
 * Failures are logged and do not roll back the persisted supplier change (graceful degrade).
 */
@Component
public class SupplierEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SupplierEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public SupplierEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(Supplier supplier) {
        publishLifecycle(RabbitMQConfig.SUPPLIER_CREATED_ROUTING_KEY, "SUPPLIER_CREATED", supplier);
    }

    public void publishUpdated(Supplier supplier) {
        publishLifecycle(RabbitMQConfig.SUPPLIER_UPDATED_ROUTING_KEY, "SUPPLIER_UPDATED", supplier);
    }

    public void publishStatusChanged(Supplier supplier, String previousStatus) {
        SupplierStatusChangedEvent event = new SupplierStatusChangedEvent(
                supplier.getId(),
                supplier.getSupplierCode(),
                previousStatus,
                supplier.getStatus() != null ? supplier.getStatus().name() : null,
                Instant.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SUPPLIER_STATUS_CHANGED_ROUTING_KEY,
                    event);
            log.info(
                    "Published SupplierStatusChangedEvent supplierId={} previousStatus={} newStatus={}",
                    event.getSupplierId(),
                    event.getPreviousStatus(),
                    event.getNewStatus());
        } catch (Exception ex) {
            log.error(
                    "Failed to publish SupplierStatusChangedEvent for supplierId={}. Cause: {}",
                    event.getSupplierId(),
                    ex.getMessage(),
                    ex);
        }
    }

    public void publishDeleted(String supplierId, String supplierCode) {
        SupplierDeletedEvent event = new SupplierDeletedEvent(supplierId, supplierCode, Instant.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SUPPLIER_DELETED_ROUTING_KEY,
                    event);
            log.info("Published SupplierDeletedEvent supplierId={}", event.getSupplierId());
        } catch (Exception ex) {
            log.error(
                    "Failed to publish SupplierDeletedEvent for supplierId={}. Cause: {}",
                    event.getSupplierId(),
                    ex.getMessage(),
                    ex);
        }
    }

    private void publishLifecycle(String routingKey, String eventType, Supplier supplier) {
        SupplierEvent event = new SupplierEvent(
                eventType,
                supplier.getId(),
                supplier.getSupplierCode(),
                supplier.getCompanyName(),
                supplier.getEmail(),
                supplier.getStatus() != null ? supplier.getStatus().name() : null,
                Instant.now());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info(
                    "Published {} to exchange={} routingKey={} supplierId={}",
                    eventType,
                    RabbitMQConfig.EXCHANGE_NAME,
                    routingKey,
                    event.getSupplierId());
        } catch (Exception ex) {
            log.error(
                    "Failed to publish {} for supplierId={}. Cause: {}",
                    eventType,
                    event.getSupplierId(),
                    ex.getMessage(),
                    ex);
        }
    }
}
