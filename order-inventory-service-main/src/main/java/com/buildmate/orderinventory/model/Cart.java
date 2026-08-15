package com.buildmate.orderinventory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "cart")
@Schema(description = "Shopping cart. Missing carts are returned as an empty cart with items=[] (not HTTP 404).")
public class Cart {
    @Id
    @Schema(description = "MongoDB cart ID (may be null for unsaved empty carts)", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    @Schema(description = "MongoDB/backend user ID (not a friendly display ID like U_001)")
    private String userId;
    @Schema(description = "Cart items; empty array when no cart exists yet")
    private List<CartItem> items = new ArrayList<>();
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    @Schema(description = "Cart line item")
    public static class CartItem {
        @Schema(description = "MongoDB/backend material ID")
        private String materialId;
        @Schema(description = "Item quantity")
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
