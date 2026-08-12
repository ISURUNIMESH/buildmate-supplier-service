package com.example.order_inventory_service;

import com.example.order_inventory_service.model.Inventory;
import com.example.order_inventory_service.repository.InventoryHistoryRepository;
import com.example.order_inventory_service.repository.InventoryRepository;
import com.example.order_inventory_service.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

    @Test
    void reserveInventoryShouldDecreaseAvailableAndIncreaseReserved() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);

        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);

        Inventory inventory = new Inventory();
        inventory.setMaterialId("MAT-100");
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setMinimumStock(3);

        when(inventoryRepository.findByMaterialId("MAT-100")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.reserveInventory("MAT-100", 4, "order-test");

        assertEquals(6, updated.getAvailableQuantity());
        assertEquals(4, updated.getReservedQuantity());
    }
}
