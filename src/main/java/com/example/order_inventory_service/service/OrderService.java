package com.example.order_inventory_service.service;

import com.example.order_inventory_service.dto.CreateOrderRequest;
import com.example.order_inventory_service.dto.OrderResponse;
import com.example.order_inventory_service.model.Order;
import com.example.order_inventory_service.model.OrderHistory;
import com.example.order_inventory_service.repository.OrderHistoryRepository;
import com.example.order_inventory_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository,
                        OrderHistoryRepository orderHistoryRepository,
                        InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.inventoryService = inventoryService;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
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

        for (Order.OrderItem item : saved.getItems()) {
            inventoryService.reserveInventory(item.getMaterialId(), item.getQuantity(), saved.getId());
        }

        addHistory(saved.getId(), saved.getStatus(), "Order created");
        return toResponse(saved);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrder(String id) {
        return orderRepository.findById(id).map(this::toResponse).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public OrderResponse updateStatus(String id, String status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        order.setUpdatedDate(Instant.now());
        Order saved = orderRepository.save(order);
        addHistory(saved.getId(), saved.getStatus(), "Status updated");
        if (Order.Status.CONFIRMED.name().equals(status)) {
            addHistory(saved.getId(), saved.getStatus(), "Order confirmed");
        }
        if (Order.Status.CANCELLED.name().equals(status)) {
            for (Order.OrderItem item : saved.getItems()) {
                inventoryService.releaseInventory(item.getMaterialId(), item.getQuantity(), saved.getId());
            }
        }
        return toResponse(saved);
    }

    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

    public List<OrderResponse> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void markConfirmed(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!Order.Status.PENDING.name().equals(order.getStatus())) {
            return;
        }
        order.setStatus(Order.Status.CONFIRMED.name());
        order.setUpdatedDate(Instant.now());
        orderRepository.save(order);
        addHistory(orderId, order.getStatus(), "Payment completed");
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
