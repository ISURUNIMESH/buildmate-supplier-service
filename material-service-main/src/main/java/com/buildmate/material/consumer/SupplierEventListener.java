package com.buildmate.material.consumer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.buildmate.material.config.RabbitMQConfig;
import com.buildmate.material.events.SupplierDeletedEvent;
import com.buildmate.material.events.SupplierEvent;
import com.buildmate.material.events.SupplierStatusChangedEvent;
import com.buildmate.material.model.Material;
import com.buildmate.material.repository.MaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumes Supplier domain events.
 * Material domain only stores {@code supplierId} — no supplier projection/cache to mutate.
 * Events are acknowledged after safe logging; deleted suppliers do not cascade-delete materials.
 */
@Component
public class SupplierEventListener {

    private static final Logger log = LoggerFactory.getLogger(SupplierEventListener.class);

    private final ObjectMapper objectMapper;
    private final MaterialRepository materialRepository;

    public SupplierEventListener(ObjectMapper objectMapper, MaterialRepository materialRepository) {
        this.objectMapper = objectMapper;
        this.materialRepository = materialRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.MATERIAL_SUPPLIER_EVENTS_QUEUE)
    public void onSupplierEvent(
            Message message,
            @Header(name = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
        if (message == null || message.getBody() == null || message.getBody().length == 0) {
            log.error("Rejected empty supplier event message routingKey={}", routingKey);
            throw new AmqpRejectAndDontRequeueException("Empty supplier event body");
        }

        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String key = routingKey != null ? routingKey : "";

        try {
            switch (key) {
                case RabbitMQConfig.SUPPLIER_CREATED_ROUTING_KEY -> handleCreated(body);
                case RabbitMQConfig.SUPPLIER_UPDATED_ROUTING_KEY -> handleUpdated(body);
                case RabbitMQConfig.SUPPLIER_STATUS_CHANGED_ROUTING_KEY -> handleStatusChanged(body);
                case RabbitMQConfig.SUPPLIER_DELETED_ROUTING_KEY -> handleDeleted(body);
                default -> {
                    log.error("Rejected supplier event with unknown routingKey={} body={}", key, body);
                    throw new AmqpRejectAndDontRequeueException("Unknown supplier routing key: " + key);
                }
            }
        } catch (AmqpRejectAndDontRequeueException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "Failed to process supplier event routingKey={}. Cause: {}",
                    key,
                    ex.getMessage(),
                    ex);
            throw new AmqpRejectAndDontRequeueException("Malformed or invalid supplier event", ex);
        }
    }

    private void handleCreated(String body) throws Exception {
        SupplierEvent event = objectMapper.readValue(body, SupplierEvent.class);
        if (event.getSupplierId() == null || event.getSupplierId().isBlank()) {
            throw new IllegalArgumentException("supplierId is required");
        }
        log.info(
                "Consumed supplier.created supplierId={} supplierCode={} status={} — "
                        + "no Material projection to update; acknowledged",
                event.getSupplierId(),
                event.getSupplierCode(),
                event.getStatus());
    }

    private void handleUpdated(String body) throws Exception {
        SupplierEvent event = objectMapper.readValue(body, SupplierEvent.class);
        if (event.getSupplierId() == null || event.getSupplierId().isBlank()) {
            throw new IllegalArgumentException("supplierId is required");
        }
        int linked = materialRepository.findBySupplierId(event.getSupplierId()).size();
        log.info(
                "Consumed supplier.updated supplierId={} supplierCode={} linkedMaterials={} — "
                        + "Material records keep supplierId reference only; acknowledged",
                event.getSupplierId(),
                event.getSupplierCode(),
                linked);
    }

    private void handleStatusChanged(String body) throws Exception {
        SupplierStatusChangedEvent event = objectMapper.readValue(body, SupplierStatusChangedEvent.class);
        if (event.getSupplierId() == null || event.getSupplierId().isBlank()) {
            throw new IllegalArgumentException("supplierId is required");
        }
        int linked = materialRepository.findBySupplierId(event.getSupplierId()).size();
        log.info(
                "Consumed supplier.status.changed supplierId={} {} -> {} linkedMaterials={} — "
                        + "Material has no supplier-status field; materials left unchanged; acknowledged",
                event.getSupplierId(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                linked);
    }

    private void handleDeleted(String body) throws Exception {
        SupplierDeletedEvent event = objectMapper.readValue(body, SupplierDeletedEvent.class);
        if (event.getSupplierId() == null || event.getSupplierId().isBlank()) {
            throw new IllegalArgumentException("supplierId is required");
        }
        List<Material> linked = materialRepository.findBySupplierId(event.getSupplierId());
        log.warn(
                "Consumed supplier.deleted supplierId={} supplierCode={} orphanMaterialCount={} — "
                        + "materials NOT deleted; supplierId references preserved",
                event.getSupplierId(),
                event.getSupplierCode(),
                linked.size());
        for (Material material : linked) {
            log.warn(
                    "Orphan material reference materialId={} name={} supplierId={}",
                    material.getId(),
                    material.getName(),
                    material.getSupplierId());
        }
    }
}
