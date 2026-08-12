package com.realconstruction.payment.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.realconstruction.payment.model.Invoice;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    List<Invoice> findByUserId(String userId);

    List<Invoice> findByOrderId(String orderId);

}