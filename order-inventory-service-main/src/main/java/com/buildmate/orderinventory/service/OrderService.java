package com.buildmate.orderinventory.service;

import com.buildmate.orderinventory.dto.CreateOrderRequest;
import com.buildmate.orderinventory.dto.OrderResponse;
import com.buildmate.orderinventory.events.OrderCreatedEvent;
import com.buildmate.orderinventory.exception.BusinessConflictException;
import com.buildmate.orderinventory.exception.ResourceNotFoundException;
import com.buildmate.orderinventory.model.Order;
import com.buildmate.orderinventory.model.OrderHistory;
import com.buildmate.orderinventory.producer.OrderEventPublisher;
import com.buildmate.orderinventory.repository.OrderHistoryRepository;
import com.buildmate.orderinventory.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final InventoryService inventoryService;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            OrderHistoryRepository orderHistoryRepository,
            InventoryService inventoryService,
            OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.inventoryService = inventoryService;
        this.orderEventPublisher = orderEventPublisher;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        List<InventoryService.OrderReservation> reservations = request.getItems().stream()
                .map(item -> new InventoryService.OrderReservation(item.getMaterialId(), item.getQuantity()))
                .toList();
        inventoryService.precheckAvailability(reservations);

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(Order.Status.PENDING.name());
        order.setCreatedDate(Instant.now());
        order.setUpdatedDate(Instant.now());
        order.setItems(request.getItems().stream().map(item -> {
            Order.OrderItem orderItem = new Order.OrderItem();
            orderItem.setMaterialId(item.getMaterialId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            return orderItem;
        }).collect(Collectors.toList()));
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        List<Order.OrderItem> reservedItems = new ArrayList<>();
        try {
            for (Order.OrderItem item : saved.getItems()) {
                inventoryService.reserveInventory(item.getMaterialId(), item.getQuantity(), saved.getId());
                reservedItems.add(item);
            }
            addHistory(saved.getId(), saved.getStatus(), "Order created");
            orderEventPublisher.publishOrderCreated(new OrderCreatedEvent(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getTotalPrice(),
                    saved.getStatus(),
                    saved.getCreatedDate()));
            return toResponse(saved);
        } catch (RuntimeException ex) {
            for (Order.OrderItem item : reservedItems) {
                try {
                    inventoryService.releaseInventory(item.getMaterialId(), item.getQuantity(), saved.getId());
                } catch (RuntimeException ignored) {
                }
            }
            orderRepository.delete(saved);
            throw ex;
        }
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrder(String id) {
        return toResponse(findOrder(id));
    }

    public OrderResponse updateStatus(String id, Order.Status status) {
        Order order = findOrder(id);
        order.setStatus(status.name());
        order.setUpdatedDate(Instant.now());
        Order saved = orderRepository.save(order);
        addHistory(saved.getId(), saved.getStatus(), "Status updated");
        if (Order.Status.CANCELLED == status) {
            for (Order.OrderItem item : saved.getItems()) {
                inventoryService.releaseInventory(item.getMaterialId(), item.getQuantity(), saved.getId());
            }
        }
        return toResponse(saved);
    }

    public void deleteOrder(String id) {
        Order order = findOrder(id);
        if (Order.Status.CANCELLED != Order.Status.valueOf(order.getStatus())) {
            for (Order.OrderItem item : order.getItems()) {
                inventoryService.releaseInventory(item.getMaterialId(), item.getQuantity(), order.getId());
            }
        }
        orderRepository.delete(order);
    }

    public List<OrderResponse> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void markConfirmed(String orderId) {
        Order order = findOrder(orderId);
        if (!Order.Status.PENDING.name().equals(order.getStatus())) {
            return;
        }
        order.setStatus(Order.Status.CONFIRMED.name());
        order.setUpdatedDate(Instant.now());
        orderRepository.save(order);
        addHistory(orderId, order.getStatus(), "Payment completed");
    }

    /**
     * Phase 2: mark order PAID after PaymentCompletedEvent.
     * Idempotent if already PAID; only transitions from PENDING/CONFIRMED.
     */
    public void markPaid(String orderId) {
        Order order = findOrder(orderId);
        String current = order.getStatus();
        if (Order.Status.PAID.name().equals(current)) {
            return;
        }
        if (Order.Status.CANCELLED.name().equals(current)
                || Order.Status.DELIVERED.name().equals(current)) {
            throw new BusinessConflictException(
                    "Cannot mark order PAID from status " + current);
        }
        order.setStatus(Order.Status.PAID.name());
        order.setUpdatedDate(Instant.now());
        orderRepository.save(order);
        addHistory(orderId, order.getStatus(), "Payment completed event received");
    }

    private Order findOrder(String id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void addHistory(String orderId, String status, String remarks) {
        OrderHistory history = new OrderHistory();
        history.setOrderId(orderId);
        history.setStatus(status);
        history.setDate(Instant.now());
        history.setRemarks(remarks);
        orderHistoryRepository.save(history);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalPrice(order.getTotalPrice());
        response.setCreatedDate(order.getCreatedDate());
        response.setUpdatedDate(order.getUpdatedDate());
        response.setItems(order.getItems().stream().map(item -> {
            OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
            itemResponse.setMaterialId(item.getMaterialId());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            return itemResponse;
        }).collect(Collectors.toList()));
        return response;
    }
}
