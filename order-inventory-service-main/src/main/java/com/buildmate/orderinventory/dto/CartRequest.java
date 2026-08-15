package com.buildmate.orderinventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Add item to cart request")
public class CartRequest {
    @NotBlank
    @Schema(description = "MongoDB/backend user ID (not a friendly display ID like U_001)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
    @NotBlank
    @Schema(description = "MongoDB/backend material ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialId;
    @Positive
    @Schema(description = "Quantity to add", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer quantity;
    @Positive
    @Schema(description = "Unit price", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.00")
    private BigDecimal price;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
