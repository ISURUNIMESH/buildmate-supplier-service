package com.buildmate.material.producer;

import com.buildmate.material.config.RabbitMQConfig;
import com.buildmate.material.events.MaterialEvent;
import com.buildmate.material.model.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MaterialEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MaterialEventPublisher publisher;

    @Test
    void publishCreatedSendsCorrectPayload() {
        Material material = sampleMaterial();

        publisher.publishCreated(material);

        ArgumentCaptor<MaterialEvent> captor = ArgumentCaptor.forClass(MaterialEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATERIAL_CREATED_ROUTING_KEY),
                captor.capture());
        assertEquals("MATERIAL_CREATED", captor.getValue().getEventType());
        assertEquals("m1", captor.getValue().getMaterialId());
        assertEquals("Cement", captor.getValue().getName());
        assertEquals(100, captor.getValue().getStock());
        assertNotNull(captor.getValue().getOccurredAt());
    }

    @Test
    void publishStockUpdatedUsesStockRoutingKey() {
        Material material = sampleMaterial();
        material.setStock(55);

        publisher.publishStockUpdated(material);

        ArgumentCaptor<MaterialEvent> captor = ArgumentCaptor.forClass(MaterialEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATERIAL_STOCK_UPDATED_ROUTING_KEY),
                captor.capture());
        assertEquals("MATERIAL_STOCK_UPDATED", captor.getValue().getEventType());
        assertEquals(55, captor.getValue().getStock());
    }

    @Test
    void publishDeletedSendsMaterialId() {
        Material material = sampleMaterial();

        publisher.publishDeleted(material);

        ArgumentCaptor<MaterialEvent> captor = ArgumentCaptor.forClass(MaterialEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.MATERIAL_DELETED_ROUTING_KEY),
                captor.capture());
        assertEquals("MATERIAL_DELETED", captor.getValue().getEventType());
        assertEquals("m1", captor.getValue().getMaterialId());
    }

    private static Material sampleMaterial() {
        Material material = new Material();
        material.setId("m1");
        material.setName("Cement");
        material.setCategory("Cement");
        material.setPrice(2450.0);
        material.setStock(100);
        material.setUnit("Bag");
        material.setSupplierId("s1");
        return material;
    }
}
