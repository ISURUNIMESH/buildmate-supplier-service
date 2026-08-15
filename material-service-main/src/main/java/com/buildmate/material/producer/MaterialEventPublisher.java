package com.buildmate.material.producer;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.buildmate.material.config.RabbitMQConfig;
import com.buildmate.material.events.MaterialEvent;
import com.buildmate.material.model.Material;

/**
 * Publishes material domain events to RabbitMQ.
 * Failures are logged and do not roll back the persisted material change.
 */
@Component
public class MaterialEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MaterialEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public MaterialEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(Material material) {
        publish(RabbitMQConfig.MATERIAL_CREATED_ROUTING_KEY, "MATERIAL_CREATED", material);
    }

    public void publishUpdated(Material material) {
        publish(RabbitMQConfig.MATERIAL_UPDATED_ROUTING_KEY, "MATERIAL_UPDATED", material);
    }

    public void publishStockUpdated(Material material) {
        publish(RabbitMQConfig.MATERIAL_STOCK_UPDATED_ROUTING_KEY, "MATERIAL_STOCK_UPDATED", material);
    }

    public void publishDeleted(Material material) {
        publish(RabbitMQConfig.MATERIAL_DELETED_ROUTING_KEY, "MATERIAL_DELETED", material);
    }

    private void publish(String routingKey, String eventType, Material material) {
        if (material == null || material.getId() == null) {
            log.error("Skipped publishing {} — material or materialId is null", eventType);
            return;
        }

        MaterialEvent event = new MaterialEvent(
                eventType,
                material.getId(),
                material.getName(),
                material.getCategory(),
                material.getPrice(),
                material.getStock(),
                material.getUnit(),
                material.getSupplierId(),
                Instant.now());

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info(
                    "Published {} to exchange={} routingKey={} materialId={} stock={}",
                    eventType,
                    RabbitMQConfig.EXCHANGE_NAME,
                    routingKey,
                    event.getMaterialId(),
                    event.getStock());
        } catch (Exception ex) {
            log.error(
                    "Failed to publish {} for materialId={}. Cause: {}",
                    eventType,
                    event.getMaterialId(),
                    ex.getMessage(),
                    ex);
        }
    }
}
