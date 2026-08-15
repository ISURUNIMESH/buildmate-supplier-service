package com.buildmate.supplier.service;

import com.buildmate.supplier.dto.DocumentUploadRequest;
import com.buildmate.supplier.model.Document;
import com.buildmate.supplier.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document uploadDocument(DocumentUploadRequest request) {
        logger.info("Uploading document for supplier: {}", request.getSupplierId());
        Document document = new Document();
        document.setSupplierId(request.getSupplierId());
        document.setDocumentName(request.getDocumentName());
        document.setDocumentType(request.getDocumentType());
        document.setFilePath(request.getFilePath());
        document.setUploadedAt(LocalDateTime.now());

        Document savedDocument = documentRepository.save(document);
        logger.info("Document uploaded successfully: {}", savedDocument.getId());
        return savedDocument;
    }

    public List<Document> getDocumentsBySupplier(String supplierId) {
        logger.info("Fetching documents for supplier: {}", supplierId);
        return documentRepository.findBySupplierId(supplierId);
    }
}
