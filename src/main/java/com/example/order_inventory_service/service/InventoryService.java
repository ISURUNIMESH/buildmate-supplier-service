package com.example.order_inventory_service.service;

import com.example.order_inventory_service.model.Inventory;
import com.example.order_inventory_service.model.InventoryHistory;
import com.example.order_inventory_service.repository.InventoryHistoryRepository;
import com.example.order_inventory_service.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryHistoryRepository inventoryHistoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory reserveInventory(String materialId, Integer quantity, String reference) {
        Inventory inventory = inventoryRepository.findByMaterialId(materialId)
                .orElseGet(() -> {
                    Inventory created = new Inventory();
                    created.setMaterialId(materialId);
                    created.setAvailableQuantity(0);
                    created.setReservedQuantity(0);
                    created.setMinimumStock(0);
                    return inventoryRepository.save(created);
                });
        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient inventory for material " + materialId);
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        logHistory(materialId, "RESERVED", quantity, reference);
        return saved;
    }

    public Inventory releaseInventory(String materialId, Integer quantity, String reference) {
        Inventory inventory = inventoryRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        logHistory(materialId, "RELEASED", quantity, reference);
        return saved;
    }

    public void handleStockUpdated(String materialId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByMaterialId(materialId)
                .orElseGet(() -> {
                    Inventory created = new Inventory();
                    created.setMaterialId(materialId);
                    created.setAvailableQuantity(0);
                    created.setReservedQuantity(0);
                    created.setMinimumStock(0);
                    return inventoryRepository.save(created);
                });
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventoryRepository.save(inventory);
        logHistory(materialId, "STOCK_UPDATED", quantity, "external");
    }

    public List<InventoryHistory> getHistory() {
        return inventoryHistoryRepository.findAll();
    }

    private void logHistory(String materialId, String action, Integer quantity, String reference) {
        InventoryHistory history = new InventoryHistory();
        history.setMaterialId(materialId);
        history.setAction(action);
        history.setQuantity(quantity);
        history.setDate(Instant.now());
        history.setReference(reference);
        inventoryHistoryRepository.save(history);
    }
}
