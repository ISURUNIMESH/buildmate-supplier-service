package com.buildmate.supplier.repository;

import com.buildmate.supplier.model.SupplierReview;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SupplierReviewRepository extends MongoRepository<SupplierReview, String> {
    List<SupplierReview> findBySupplierIdOrderByCreatedAtDesc(String supplierId);
}
