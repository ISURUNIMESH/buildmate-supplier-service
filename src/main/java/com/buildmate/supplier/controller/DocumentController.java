package com.buildmate.supplier.controller;

import com.buildmate.supplier.dto.DocumentUploadRequest;
import com.buildmate.supplier.model.Document;
import com.buildmate.supplier.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers/{id}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> uploadDocument(
            @RequestHeader("X-API-KEY") String apiKey,
            @PathVariable String id,
            @Valid @RequestBody DocumentUploadRequest request) {
        logger.info("Received request to upload document for supplier: {}", id);
        documentService.validateApiKey(apiKey);
        request.setSupplierId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(request));
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocumentsBySupplier(
            @RequestHeader("X-API-KEY") String apiKey,
            @PathVariable String id) {
        logger.info("Received request to fetch documents for supplier: {}", id);
        documentService.validateApiKey(apiKey);
        return ResponseEntity.ok(documentService.getDocumentsBySupplier(id));
    }
}
