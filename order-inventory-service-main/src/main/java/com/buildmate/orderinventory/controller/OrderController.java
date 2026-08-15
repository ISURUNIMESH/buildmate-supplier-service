package com.buildmate.orderinventory.controller;

import com.buildmate.orderinventory.config.OpenApiConfig;
import com.buildmate.orderinventory.dto.CreateOrderRequest;
import com.buildmate.orderinventory.dto.OrderResponse;
import com.buildmate.orderinventory.exception.ApiErrorResponse;
import com.buildmate.orderinventory.model.Order;
import com.buildmate.orderinventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Create and manage orders. Successful creation publishes OrderCreatedEvent via RabbitMQ; PaymentCompletedEvent updates payment status.")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order", description = "Reserves inventory and publishes OrderCreatedEvent asynchronously via RabbitMQ after persistence.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or business input error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key"),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    @Operation(summary = "List all orders")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All orders",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<OrderResponse> getAllOrders() { return orderService.getAllOrders(); }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public OrderResponse getOrder(
            @Parameter(description = "MongoDB order ID (not a friendly display ID)", required = true)
            @PathVariable String id) {
        return orderService.getOrder(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Status must match Order.Status enum values. CANCELLED releases reserved inventory.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public OrderResponse updateStatus(
            @Parameter(description = "MongoDB order ID", required = true) @PathVariable String id,
            @Parameter(description = "New status (PENDING, CONFIRMED, PAID, CANCELLED, DELIVERED)", required = true, example = "CONFIRMED")
            @RequestParam String status) {
        return orderService.updateStatus(id, Order.Status.valueOf(status));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete order", description = "Deletes the order and releases reserved inventory unless already CANCELLED.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public void deleteOrder(
            @Parameter(description = "MongoDB order ID", required = true) @PathVariable String id) {
        orderService.deleteOrder(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List orders for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders for the user",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<OrderResponse> getOrdersByUser(
            @Parameter(description = "MongoDB/backend user ID (not a friendly display ID like U_001)", required = true)
            @PathVariable String userId) {
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List orders by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders for the given status",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<OrderResponse> getOrdersByStatus(
            @Parameter(description = "Order status filter", required = true, example = "PENDING")
            @PathVariable String status) {
        return orderService.getOrdersByStatus(status);
    }
}
