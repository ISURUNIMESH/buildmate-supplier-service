package com.realconstruction.payment.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.realconstruction.payment.model.Invoice;
import com.realconstruction.payment.service.InvoiceService;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {

        Invoice savedInvoice = invoiceService.createInvoice(invoice);

        return ResponseEntity.ok(savedInvoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable String id) {

        Optional<Invoice> invoice = invoiceService.getInvoiceById(id);

        return invoice.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}