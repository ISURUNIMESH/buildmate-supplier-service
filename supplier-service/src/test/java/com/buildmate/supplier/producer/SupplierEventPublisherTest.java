package com.buildmate.supplier.producer;

import com.buildmate.supplier.config.RabbitMQConfig;
import com.buildmate.supplier.events.SupplierDeletedEvent;
import com.buildmate.supplier.events.SupplierEvent;
import com.buildmate.supplier.events.SupplierStatusChangedEvent;
import com.buildmate.supplier.model.Supplier;
import com.buildmate.supplier.model.SupplierStatus;
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
class SupplierEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SupplierEventPublisher publisher;

    @Test
    void publishCreatedSendsCorrectPayload() {
        Supplier supplier = Supplier.builder()
                .id("s1")
                .supplierCode("SUP-1")
                .companyName("Acme")
                .email("a@b.com")
                .status(SupplierStatus.PENDING)
                .build();

        publisher.publishCreated(supplier);

        ArgumentCaptor<SupplierEvent> captor = ArgumentCaptor.forClass(SupplierEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.SUPPLIER_CREATED_ROUTING_KEY),
                captor.capture());
        assertEquals("SUPPLIER_CREATED", captor.getValue().getEventType());
        assertEquals("s1", captor.getValue().getSupplierId());
        assertEquals("SUP-1", captor.getValue().getSupplierCode());
        assertNotNull(captor.getValue().getOccurredAt());
    }

    @Test
    void publishStatusChangedSendsPreviousAndNewStatus() {
        Supplier supplier = Supplier.builder()
                .id("s1")
                .supplierCode("SUP-1")
                .status(SupplierStatus.APPROVED)
                .build();

        publisher.publishStatusChanged(supplier, "PENDING");

        ArgumentCaptor<SupplierStatusChangedEvent> captor =
                ArgumentCaptor.forClass(SupplierStatusChangedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.SUPPLIER_STATUS_CHANGED_ROUTING_KEY),
                captor.capture());
        assertEquals("PENDING", captor.getValue().getPreviousStatus());
        assertEquals("APPROVED", captor.getValue().getNewStatus());
    }

    @Test
    void publishDeletedSendsSupplierId() {
        publisher.publishDeleted("s1", "SUP-1");

        ArgumentCaptor<SupplierDeletedEvent> captor = ArgumentCaptor.forClass(SupplierDeletedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.SUPPLIER_DELETED_ROUTING_KEY),
                captor.capture());
        assertEquals("s1", captor.getValue().getSupplierId());
        assertEquals("SUPPLIER_DELETED", captor.getValue().getEventType());
    }
}
