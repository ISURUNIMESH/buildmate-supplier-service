package com.example.order_inventory_service.repository;

import com.example.order_inventory_service.model.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {
}
