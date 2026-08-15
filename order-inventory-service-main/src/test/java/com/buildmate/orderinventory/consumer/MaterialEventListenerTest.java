package com.buildmate.orderinventory.consumer;

import com.buildmate.orderinventory.config.RabbitMQConfig;
import com.buildmate.orderinventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    private MaterialEventListener listener;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new MaterialEventListener(objectMapper, inventoryService);
    }

    @Test
    void materialCreatedEnsuresInventory() {
        String json = """
                {"eventType":"MATERIAL_CREATED","materialId":"m1","name":"Cement","stock":100}
                """;
        listener.onMaterialEvent(message(json), RabbitMQConfig.MATERIAL_CREATED_ROUTING_KEY);
        verify(inventoryService).ensureInventoryForMaterial("m1", 100);
    }

    @Test
    void materialStockUpdatedSyncsAvailableQuantity() {
        String json = """
                {"eventType":"MATERIAL_STOCK_UPDATED","materialId":"m1","stock":55}
                """;
        listener.onMaterialEvent(message(json), RabbitMQConfig.MATERIAL_STOCK_UPDATED_ROUTING_KEY);
        verify(inventoryService).syncAvailableQuantityFromMaterial("m1", 55);
    }

    @Test
    void materialDeletedDoesNotCascadeDelete() {
        when(inventoryService.hasInventoryForMaterial("m1")).thenReturn(true);
        String json = """
                {"eventType":"MATERIAL_DELETED","materialId":"m1","name":"Cement"}
                """;
        listener.onMaterialEvent(message(json), RabbitMQConfig.MATERIAL_DELETED_ROUTING_KEY);
        verify(inventoryService).hasInventoryForMaterial("m1");
    }

    @Test
    void rejectsMissingMaterialId() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMaterialEvent(
                        message("{\"eventType\":\"MATERIAL_CREATED\",\"stock\":1}"),
                        RabbitMQConfig.MATERIAL_CREATED_ROUTING_KEY));
    }

    private static Message message(String body) {
        return new Message(body.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }
}
