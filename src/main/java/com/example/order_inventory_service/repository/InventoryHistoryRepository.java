package com.example.order_inventory_service.repository;

import com.example.order_inventory_service.model.InventoryHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryHistoryRepository extends MongoRepository<InventoryHistory, String> {
}
