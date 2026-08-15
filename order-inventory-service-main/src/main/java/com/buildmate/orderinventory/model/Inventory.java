package com.buildmate.orderinventory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventory")
@Schema(description = "Inventory stock record for a material")
public class Inventory {
    @Id
    @Schema(description = "MongoDB inventory ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    @Schema(description = "MongoDB/backend material ID")
    private String materialId;
    @Schema(description = "Available quantity")
    private Integer availableQuantity;
    @Schema(description = "Reserved quantity")
    private Integer reservedQuantity;
    @Schema(description = "Minimum stock threshold")
    private Integer minimumStock;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
}
