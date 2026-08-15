package com.buildmate.orderinventory.repository;

import com.buildmate.orderinventory.model.InventoryHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryHistoryRepository extends MongoRepository<InventoryHistory, String> {
}
