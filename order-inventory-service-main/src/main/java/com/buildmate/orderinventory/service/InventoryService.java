package com.buildmate.orderinventory.service;

import com.buildmate.orderinventory.exception.BusinessConflictException;
import com.buildmate.orderinventory.exception.ResourceNotFoundException;
import com.buildmate.orderinventory.model.Inventory;
import com.buildmate.orderinventory.model.InventoryHistory;
import com.buildmate.orderinventory.repository.InventoryHistoryRepository;
import com.buildmate.orderinventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    public InventoryService(InventoryRepository inventoryRepository, InventoryHistoryRepository inventoryHistoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
    }

    public List<Inventory> getAllInventory() { return inventoryRepository.findAll(); }

    public Inventory createInventory(Inventory inventory) {
        if (inventory.getMaterialId() == null || inventory.getMaterialId().isBlank()) {
            throw new IllegalArgumentException("Material ID is required");
        }
        if (findUniqueInventory(inventory.getMaterialId()).isPresent()) {
            throw new BusinessConflictException(
                    "Inventory already exists for material " + inventory.getMaterialId());
        }
        Inventory saved = inventoryRepository.save(inventory);
        logHistory(saved.getMaterialId(), "CREATED", saved.getAvailableQuantity(), "api");
        return saved;
    }

    /**
     * Updates stock levels for an existing inventory row (one row per material).
     */
    public Inventory updateInventoryLevels(String materialId, Integer availableQuantity,
                                           Integer reservedQuantity, Integer minimumStock) {
        if (materialId == null || materialId.isBlank()) {
            throw new IllegalArgumentException("Material ID is required");
        }
        Inventory inventory = findUniqueInventory(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        if (availableQuantity != null) {
            if (availableQuantity < 0) {
                throw new IllegalArgumentException("availableQuantity must be non-negative");
            }
            inventory.setAvailableQuantity(availableQuantity);
        }
        if (reservedQuantity != null) {
            if (reservedQuantity < 0) {
                throw new IllegalArgumentException("reservedQuantity must be non-negative");
            }
            inventory.setReservedQuantity(reservedQuantity);
        }
        if (minimumStock != null) {
            if (minimumStock < 0) {
                throw new IllegalArgumentException("minimumStock must be non-negative");
            }
            inventory.setMinimumStock(minimumStock);
        }
        Inventory saved = inventoryRepository.save(inventory);
        logHistory(materialId, "UPDATED", saved.getAvailableQuantity(), "api");
        return saved;
    }

    /**
     * Idempotent: creates inventory for a material if missing.
     * Uses material catalog stock as initial availableQuantity when provided.
     */
    public Inventory ensureInventoryForMaterial(String materialId, Integer stock) {
        return findUniqueInventory(materialId).orElseGet(() -> {
            Inventory inventory = new Inventory();
            inventory.setMaterialId(materialId);
            inventory.setAvailableQuantity(stock != null ? stock : 0);
            inventory.setReservedQuantity(0);
            inventory.setMinimumStock(0);
            Inventory saved = inventoryRepository.save(inventory);
            logHistory(materialId, "CREATED_FROM_MATERIAL", saved.getAvailableQuantity(), "material.created");
            return saved;
        });
    }

    /**
     * Syncs inventory availableQuantity from material catalog stock when an inventory row exists.
     * No-op (logged) when inventory is absent — avoid inventing inventory outside material.created.
     */
    public void syncAvailableQuantityFromMaterial(String materialId, Integer stock) {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock must be non-negative");
        }
        findUniqueInventory(materialId).ifPresentOrElse(inventory -> {
            inventory.setAvailableQuantity(stock);
            inventoryRepository.save(inventory);
            logHistory(materialId, "SYNCED_FROM_MATERIAL", stock, "material.stock.updated");
        }, () -> {
            // No inventory projection yet — caller logs consumption; remain idempotent.
        });
    }

    public boolean hasInventoryForMaterial(String materialId) {
        return findUniqueInventory(materialId).isPresent();
    }

    public Inventory reserveInventory(String materialId, Integer quantity, String reference) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Inventory inventory = findUniqueInventory(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        if (inventory.getAvailableQuantity() == null || inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient inventory for material " + materialId);
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity((inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity()) + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        logHistory(materialId, "RESERVED", quantity, reference);
        return saved;
    }

    public Inventory releaseInventory(String materialId, Integer quantity, String reference) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Inventory inventory = findUniqueInventory(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        int reservedQuantity = inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("Release quantity exceeds reserved stock for material " + materialId);
        }
        inventory.setReservedQuantity(reservedQuantity - quantity);
        inventory.setAvailableQuantity((inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity()) + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        logHistory(materialId, "RELEASED", quantity, reference);
        return saved;
    }

    public Inventory findByMaterialId(String materialId) {
        return findUniqueInventory(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    public List<InventoryHistory> getHistory() { return inventoryHistoryRepository.findAll(); }

    public void precheckAvailability(List<OrderReservation> reservations) {
        Set<String> materialIds = new HashSet<>();
        for (OrderReservation reservation : reservations) {
            if (!materialIds.add(reservation.materialId())) {
                throw new IllegalArgumentException(
                        "Duplicate material in order: " + reservation.materialId());
            }
            Inventory inventory = findUniqueInventory(reservation.materialId())
                    .orElseThrow(() -> new IllegalArgumentException("Inventory not found for material " + reservation.materialId()));
            if (inventory.getAvailableQuantity() == null || inventory.getAvailableQuantity() < reservation.quantity()) {
                throw new IllegalArgumentException("Insufficient inventory for material " + reservation.materialId());
            }
        }
    }

    public record OrderReservation(String materialId, Integer quantity) {}

    private Optional<Inventory> findUniqueInventory(String materialId) {
        List<Inventory> matches = inventoryRepository.findAllByMaterialId(materialId);
        if (matches.size() > 1) {
            throw new BusinessConflictException(
                    "Multiple inventory records found for material " + materialId);
        }
        return matches.stream().findFirst();
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
