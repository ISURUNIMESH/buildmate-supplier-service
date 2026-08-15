package com.buildmate.supplier.controller;

import com.buildmate.supplier.config.OpenApiConfig;
import com.buildmate.supplier.dto.ApiErrorResponse;
import com.buildmate.supplier.dto.DocumentUploadRequest;
import com.buildmate.supplier.model.Document;
import com.buildmate.supplier.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers/{id}/documents")
@RequiredArgsConstructor
@Tag(name = "Supplier Documents", description = "Supplier document metadata (JSON path/URL, not multipart)")
@SecurityRequirement(name = OpenApiConfig.API_KEY_SCHEME)
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    @PostMapping
    @Operation(
            summary = "Upload document metadata for a supplier",
            description = "Accepts JSON with documentName, documentType, and filePath/URL. "
                    + "Does not accept multipart file upload. Path supplier id is applied to the request.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document metadata stored",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Document> uploadDocument(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id,
            @Valid @RequestBody DocumentUploadRequest request) {
        logger.info("Received request to upload document for supplier: {}", id);
        request.setSupplierId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(request));
    }

    @GetMapping
    @Operation(summary = "List documents for a supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Document.class)))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<Document>> getDocumentsBySupplier(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id) {
        logger.info("Received request to fetch documents for supplier: {}", id);
        return ResponseEntity.ok(documentService.getDocumentsBySupplier(id));
    }
}
