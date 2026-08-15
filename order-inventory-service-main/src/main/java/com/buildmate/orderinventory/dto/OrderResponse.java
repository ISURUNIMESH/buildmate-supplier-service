package com.buildmate.orderinventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Order response")
public class OrderResponse {
    @Schema(description = "MongoDB order ID (backend ID, not a friendly display ID)")
    private String id;
    @Schema(description = "MongoDB/backend user ID")
    private String userId;
    @Schema(description = "Order status", example = "PENDING")
    private String status;
    @Schema(description = "Total order price")
    private BigDecimal totalPrice;
    @Schema(description = "Creation timestamp")
    private Instant createdDate;
    @Schema(description = "Last update timestamp")
    private Instant updatedDate;
    @Schema(description = "Order line items")
    private List<OrderItemResponse> items;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }
    public Instant getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(Instant updatedDate) { this.updatedDate = updatedDate; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    @Schema(description = "Order line item in response")
    public static class OrderItemResponse {
        @Schema(description = "MongoDB/backend material ID")
        private String materialId;
        @Schema(description = "Quantity ordered")
        private Integer quantity;
        @Schema(description = "Unit price")
        private BigDecimal price;
        public String getMaterialId() { return materialId; }
        public void setMaterialId(String materialId) { this.materialId = materialId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}
