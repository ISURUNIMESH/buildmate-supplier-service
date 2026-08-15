package com.buildmate.orderinventory.consumer;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.buildmate.orderinventory.config.RabbitMQConfig;
import com.buildmate.orderinventory.events.MaterialEvent;
import com.buildmate.orderinventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumes Material domain events for inventory projection.
 * Does not invent catalog fields beyond Inventory.materialId / quantities.
 */
@Component
public class MaterialEventListener {

    private static final Logger log = LoggerFactory.getLogger(MaterialEventListener.class);

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    public MaterialEventListener(ObjectMapper objectMapper, InventoryService inventoryService) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitMQConfig.MATERIAL_EVENTS_QUEUE)
    public void onMaterialEvent(
            Message message,
            @Header(name = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
        if (message == null || message.getBody() == null || message.getBody().length == 0) {
            log.error("Rejected empty material event message routingKey={}", routingKey);
            throw new AmqpRejectAndDontRequeueException("Empty material event body");
        }

        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String key = routingKey != null ? routingKey : "";

        try {
            MaterialEvent event = objectMapper.readValue(body, MaterialEvent.class);
            if (event.getMaterialId() == null || event.getMaterialId().isBlank()) {
                throw new IllegalArgumentException("materialId is required");
            }

            switch (key) {
                case RabbitMQConfig.MATERIAL_CREATED_ROUTING_KEY -> {
                    inventoryService.ensureInventoryForMaterial(event.getMaterialId(), event.getStock());
                    log.info(
                            "Consumed material.created materialId={} name={} stock={} — inventory ensured",
                            event.getMaterialId(),
                            event.getName(),
                            event.getStock());
                }
                case RabbitMQConfig.MATERIAL_UPDATED_ROUTING_KEY -> {
                    // Catalog update: if stock is present, keep inventory availableQuantity aligned.
                    if (event.getStock() != null) {
                        inventoryService.ensureInventoryForMaterial(event.getMaterialId(), event.getStock());
                        inventoryService.syncAvailableQuantityFromMaterial(event.getMaterialId(), event.getStock());
                    }
                    log.info(
                            "Consumed material.updated materialId={} name={} stock={} — catalog noted; stock synced when present",
                            event.getMaterialId(),
                            event.getName(),
                            event.getStock());
                }
                case RabbitMQConfig.MATERIAL_STOCK_UPDATED_ROUTING_KEY -> {
                    inventoryService.syncAvailableQuantityFromMaterial(event.getMaterialId(), event.getStock());
                    log.info(
                            "Consumed material.stock.updated materialId={} stock={} — inventory availableQuantity synced",
                            event.getMaterialId(),
                            event.getStock());
                }
                case RabbitMQConfig.MATERIAL_DELETED_ROUTING_KEY -> {
                    boolean hasInventory = inventoryService.hasInventoryForMaterial(event.getMaterialId());
                    log.warn(
                            "Consumed material.deleted materialId={} name={} inventoryExists={} — "
                                    + "inventory NOT cascade-deleted",
                            event.getMaterialId(),
                            event.getName(),
                            hasInventory);
                }
                default -> {
                    log.error("Rejected material event with unknown routingKey={} body={}", key, body);
                    throw new AmqpRejectAndDontRequeueException("Unknown material routing key: " + key);
                }
            }
        } catch (AmqpRejectAndDontRequeueException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "Failed to process material event routingKey={}. Cause: {}",
                    key,
                    ex.getMessage(),
                    ex);
            throw new AmqpRejectAndDontRequeueException("Malformed or invalid material event", ex);
        }
    }
}
