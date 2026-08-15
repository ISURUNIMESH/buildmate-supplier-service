package com.buildmate.orderinventory.repository;

import com.buildmate.orderinventory.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findAllByMaterialId(String materialId);
}
