package com.buildmate.payment.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.buildmate.payment.config.OpenApiConfig;
import com.buildmate.payment.model.Invoice;
import com.buildmate.payment.service.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Create and retrieve invoices")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @Operation(summary = "Create invoice")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice created",
                    content = @Content(schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {

        Invoice savedInvoice = invoiceService.createInvoice(invoice);

        return ResponseEntity.status(201).body(savedInvoice);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice found",
                    content = @Content(schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Invoice> getInvoiceById(
            @Parameter(description = "MongoDB invoice ID", required = true) @PathVariable String id) {

        Optional<Invoice> invoice = invoiceService.getInvoiceById(id);

        return invoice.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
