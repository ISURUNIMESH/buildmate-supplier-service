package com.buildmate.orderinventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Create order request")
public class CreateOrderRequest {
    @NotBlank
    @Schema(description = "MongoDB/backend user ID (not a friendly display ID like U_001)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
    @NotEmpty
    @Valid
    @Schema(description = "Order line items", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    @Schema(description = "Order line item")
    public static class OrderItemRequest {
        @NotBlank
        @Schema(description = "MongoDB/backend material ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private String materialId;
        @Positive
        @Schema(description = "Quantity to order", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Integer quantity;
        @Positive
        @Schema(description = "Unit price", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal price;
        public String getMaterialId() { return materialId; }
        public void setMaterialId(String materialId) { this.materialId = materialId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}
