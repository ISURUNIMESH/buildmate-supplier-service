package com.buildmate.material.consumer;

import com.buildmate.material.config.RabbitMQConfig;
import com.buildmate.material.model.Material;
import com.buildmate.material.repository.MaterialRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierEventListenerTest {

    @Mock
    private MaterialRepository materialRepository;

    private SupplierEventListener listener;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new SupplierEventListener(objectMapper, materialRepository);
    }

    @Test
    void consumesSupplierCreatedWithoutMutatingMaterials() {
        String json = """
                {"eventType":"SUPPLIER_CREATED","supplierId":"s1","supplierCode":"SUP-1","status":"PENDING"}
                """;
        listener.onSupplierEvent(message(json), RabbitMQConfig.SUPPLIER_CREATED_ROUTING_KEY);
    }

    @Test
    void consumesSupplierDeletedAndLogsLinkedMaterials() {
        Material material = new Material();
        material.setId("m1");
        material.setName("Cement");
        material.setSupplierId("s1");
        when(materialRepository.findBySupplierId("s1")).thenReturn(List.of(material));

        String json = """
                {"eventType":"SUPPLIER_DELETED","supplierId":"s1","supplierCode":"SUP-1"}
                """;
        listener.onSupplierEvent(message(json), RabbitMQConfig.SUPPLIER_DELETED_ROUTING_KEY);

        verify(materialRepository).findBySupplierId("s1");
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onSupplierEvent(message("{not-json"), RabbitMQConfig.SUPPLIER_CREATED_ROUTING_KEY));
    }

    @Test
    void rejectsUnknownRoutingKey() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onSupplierEvent(message("{\"supplierId\":\"s1\"}"), "supplier.unknown"));
    }

    private static Message message(String body) {
        return new Message(body.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }
}
