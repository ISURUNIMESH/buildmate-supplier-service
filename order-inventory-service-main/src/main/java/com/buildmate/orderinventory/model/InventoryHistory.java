package com.buildmate.orderinventory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "inventory_history")
@Schema(description = "Inventory movement history entry")
public class InventoryHistory {
    @Id
    @Schema(description = "MongoDB history ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    @Schema(description = "MongoDB/backend material ID")
    private String materialId;
    @Schema(description = "Action performed", example = "RESERVED")
    private String action;
    @Schema(description = "Quantity moved")
    private Integer quantity;
    @Schema(description = "Event timestamp")
    private Instant date;
    @Schema(description = "Reference such as order ID or api")
    private String reference;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
