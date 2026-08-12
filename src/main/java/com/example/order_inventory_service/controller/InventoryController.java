package com.example.order_inventory_service.controller;

import com.example.order_inventory_service.dto.ReserveInventoryRequest;
import com.example.order_inventory_service.model.Inventory;
import com.example.order_inventory_service.model.InventoryHistory;
import com.example.order_inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    @PatchMapping("/{materialId}/reserve")
    public Inventory reserveInventory(@PathVariable String materialId, @Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryService.reserveInventory(materialId, request.getQuantity(), "api");
    }

    @PatchMapping("/{materialId}/release")
    public Inventory releaseInventory(@PathVariable String materialId, @RequestBody ReserveInventoryRequest request) {
        return inventoryService.releaseInventory(materialId, request.getQuantity(), "api");
    }

    @GetMapping("/history")
    public List<InventoryHistory> getInventoryHistory() {
        return inventoryService.getHistory();
    }
}
