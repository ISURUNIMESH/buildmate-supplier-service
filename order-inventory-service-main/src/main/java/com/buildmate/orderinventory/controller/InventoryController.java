package com.buildmate.orderinventory.controller;

import com.buildmate.orderinventory.config.OpenApiConfig;
import com.buildmate.orderinventory.dto.ReserveInventoryRequest;
import com.buildmate.orderinventory.exception.ApiErrorResponse;
import com.buildmate.orderinventory.model.Inventory;
import com.buildmate.orderinventory.model.InventoryHistory;
import com.buildmate.orderinventory.service.InventoryService;
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
@RequestMapping("/inventory")
@Tag(name = "Inventory", description = "Inventory stock management and history")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "List all inventory records")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Inventory.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<Inventory> getAllInventory() { return inventoryService.getAllInventory(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create inventory record")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory created",
                    content = @Content(schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Inventory createInventory(@RequestBody Inventory inventory) { return inventoryService.createInventory(inventory); }

    @PutMapping("/{materialId}")
    @Operation(summary = "Update inventory stock levels for a material")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory updated",
                    content = @Content(schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "400", description = "Invalid stock values",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found for material",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Inventory updateInventory(
            @Parameter(description = "MongoDB/backend material ID", required = true) @PathVariable String materialId,
            @RequestBody Inventory inventory) {
        return inventoryService.updateInventoryLevels(
                materialId,
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getMinimumStock());
    }

    @PatchMapping("/{materialId}/reserve")
    @Operation(summary = "Reserve inventory", description = "Decrements available quantity and increments reserved quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory reserved",
                    content = @Content(schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found for material",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Inventory reserveInventory(
            @Parameter(description = "MongoDB/backend material ID", required = true) @PathVariable String materialId,
            @Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryService.reserveInventory(materialId, request.getQuantity(), "api");
    }

    @PatchMapping("/{materialId}/release")
    @Operation(summary = "Release reserved inventory", description = "Decrements reserved quantity and increments available quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory released",
                    content = @Content(schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or release exceeds reserved stock",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found for material",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Inventory releaseInventory(
            @Parameter(description = "MongoDB/backend material ID", required = true) @PathVariable String materialId,
            @Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryService.releaseInventory(materialId, request.getQuantity(), "api");
    }

    @GetMapping("/history")
    @Operation(summary = "List inventory history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory history entries",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = InventoryHistory.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public List<InventoryHistory> getInventoryHistory() { return inventoryService.getHistory(); }
}
