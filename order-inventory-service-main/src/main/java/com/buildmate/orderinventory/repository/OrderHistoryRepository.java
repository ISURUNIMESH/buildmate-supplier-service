package com.buildmate.orderinventory.repository;

import com.buildmate.orderinventory.model.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {
}
