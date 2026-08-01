package com.buildmate.supplier.repository;

import com.buildmate.supplier.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {
    List<Document> findBySupplierId(String supplierId);
    List<Document> findByDocumentType(String documentType);
}
