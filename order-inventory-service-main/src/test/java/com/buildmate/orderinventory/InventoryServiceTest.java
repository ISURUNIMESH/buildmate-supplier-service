package com.buildmate.orderinventory;

import com.buildmate.orderinventory.exception.BusinessConflictException;
import com.buildmate.orderinventory.model.Inventory;
import com.buildmate.orderinventory.repository.InventoryHistoryRepository;
import com.buildmate.orderinventory.repository.InventoryRepository;
import com.buildmate.orderinventory.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        when(inventoryRepository.findAllByMaterialId("MAT-100")).thenReturn(List.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.reserveInventory("MAT-100", 4, "order-test");

        assertEquals(6, updated.getAvailableQuantity());
        assertEquals(4, updated.getReservedQuantity());
    }

    @Test
    void reserveInventoryRejectsNonPositiveQuantity() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);

        assertThrows(IllegalArgumentException.class, () -> inventoryService.reserveInventory("MAT-100", 0, "order-test"));
    }

    @Test
    void releaseInventoryRejectsReleaseGreaterThanReserved() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);

        Inventory inventory = new Inventory();
        inventory.setMaterialId("MAT-100");
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(5);

        when(inventoryRepository.findAllByMaterialId("MAT-100")).thenReturn(List.of(inventory));

        assertThrows(IllegalArgumentException.class, () -> inventoryService.releaseInventory("MAT-100", 100, "order-test"));
    }

    @Test
    void createInventoryRejectsDuplicateMaterial() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);

        Inventory existing = inventory("MAT-100", 10, 0);
        Inventory duplicate = inventory("MAT-100", 5, 0);
        when(inventoryRepository.findAllByMaterialId("MAT-100")).thenReturn(List.of(existing));

        assertThrows(BusinessConflictException.class, () -> inventoryService.createInventory(duplicate));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void precheckRejectsAmbiguousDuplicateInventoryRecords() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);

        when(inventoryRepository.findAllByMaterialId("MAT-100")).thenReturn(List.of(
                inventory("MAT-100", 10, 0),
                inventory("MAT-100", 5, 0)));

        assertThrows(BusinessConflictException.class, () -> inventoryService.precheckAvailability(
                List.of(new InventoryService.OrderReservation("MAT-100", 1))));
    }

    @Test
    void precheckRejectsDuplicateMaterialReservations() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryHistoryRepository inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        InventoryService inventoryService = new InventoryService(inventoryRepository, inventoryHistoryRepository);

        when(inventoryRepository.findAllByMaterialId("MAT-100"))
                .thenReturn(List.of(inventory("MAT-100", 10, 0)));

        assertThrows(IllegalArgumentException.class, () -> inventoryService.precheckAvailability(List.of(
                new InventoryService.OrderReservation("MAT-100", 1),
                new InventoryService.OrderReservation("MAT-100", 2))));
    }

    private Inventory inventory(String materialId, int available, int reserved) {
        Inventory inventory = new Inventory();
        inventory.setMaterialId(materialId);
        inventory.setAvailableQuantity(available);
        inventory.setReservedQuantity(reserved);
        return inventory;
    }
}
