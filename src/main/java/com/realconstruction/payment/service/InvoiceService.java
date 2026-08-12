package com.realconstruction.payment.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.realconstruction.payment.model.Invoice;
import com.realconstruction.payment.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private static final Logger logger =
            LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice createInvoice(Invoice invoice) {

        logger.info("Creating invoice for Order ID: {}", invoice.getOrderId());

        invoice.setCreatedAt(LocalDateTime.now());

        if (invoice.getStatus() == null || invoice.getStatus().isBlank()) {
            invoice.setStatus("GENERATED");
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        logger.info("Invoice created successfully with ID: {}", savedInvoice.getId());

        return savedInvoice;
    }

    public Optional<Invoice> getInvoiceById(String id) {

        logger.info("Fetching invoice with ID: {}", id);

        return invoiceRepository.findById(id);
    }

}