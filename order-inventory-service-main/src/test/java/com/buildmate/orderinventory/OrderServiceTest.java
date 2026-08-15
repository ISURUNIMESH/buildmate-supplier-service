package com.buildmate.orderinventory;

import com.buildmate.orderinventory.dto.CreateOrderRequest;
import com.buildmate.orderinventory.dto.CreateOrderRequest.OrderItemRequest;
import com.buildmate.orderinventory.model.Inventory;
import com.buildmate.orderinventory.model.Order;
import com.buildmate.orderinventory.producer.OrderEventPublisher;
import com.buildmate.orderinventory.repository.InventoryHistoryRepository;
import com.buildmate.orderinventory.repository.InventoryRepository;
import com.buildmate.orderinventory.repository.OrderHistoryRepository;
import com.buildmate.orderinventory.repository.OrderRepository;
import com.buildmate.orderinventory.service.InventoryService;
import com.buildmate.orderinventory.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void createOrderDoesNotPersistOrderWhenInventoryPrecheckFails() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderHistoryRepository orderHistoryRepository = mock(OrderHistoryRepository.class);
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        OrderEventPublisher orderEventPublisher = mock(OrderEventPublisher.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);
        OrderService orderService = spy(new OrderService(
                orderRepository, orderHistoryRepository, inventoryService, orderEventPublisher));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user-1");
        OrderItemRequest item = new OrderItemRequest();
        item.setMaterialId("MAT-1");
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(10));
        request.setItems(List.of(item));

        when(inventoryRepository.findAllByMaterialId("MAT-1")).thenReturn(List.of());

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> orderService.createOrder(request));

        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreated(any());
    }

    @Test
    void createOrderPublishesOnlyAfterOrderIsSavedAndInventoryIsReserved() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderHistoryRepository orderHistoryRepository = mock(OrderHistoryRepository.class);
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        OrderEventPublisher orderEventPublisher = mock(OrderEventPublisher.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);
        OrderService orderService = new OrderService(
                orderRepository, orderHistoryRepository, inventoryService, orderEventPublisher);

        Inventory inventory = new Inventory();
        inventory.setMaterialId("MAT-1");
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        when(inventoryRepository.findAllByMaterialId("MAT-1")).thenReturn(List.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("order-1");
            return order;
        });

        orderService.createOrder(request("MAT-1"));

        InOrder inOrder = inOrder(orderRepository, inventoryRepository, orderHistoryRepository, orderEventPublisher);
        inOrder.verify(orderRepository).save(any(Order.class));
        inOrder.verify(inventoryRepository).save(any(Inventory.class));
        inOrder.verify(orderHistoryRepository).save(any());
        inOrder.verify(orderEventPublisher).publishOrderCreated(any());
    }

    @Test
    void createOrderRejectsDuplicateMaterialLinesBeforePersistence() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderHistoryRepository orderHistoryRepository = mock(OrderHistoryRepository.class);
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        OrderEventPublisher orderEventPublisher = mock(OrderEventPublisher.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);
        OrderService orderService = new OrderService(
                orderRepository, orderHistoryRepository, inventoryService, orderEventPublisher);

        CreateOrderRequest request = request("MAT-1");
        request.setItems(List.of(request.getItems().get(0), request.getItems().get(0)));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreated(any());
    }

    private CreateOrderRequest request(String materialId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user-1");
        OrderItemRequest item = new OrderItemRequest();
        item.setMaterialId(materialId);
        item.setQuantity(2);
        item.setPrice(BigDecimal.TEN);
        request.setItems(List.of(item));
        return request;
    }
}
