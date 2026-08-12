package com.realconstruction.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realconstruction.payment.model.MonthlyReport;
import com.realconstruction.payment.model.RevenueReport;
import com.realconstruction.payment.model.TopCustomerReport;
import com.realconstruction.payment.service.PaymentService;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final PaymentService paymentService;

    public ReportController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueReport> getRevenueReport() {

        return ResponseEntity.ok(
                paymentService.getRevenueReport()
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReport> getMonthlyReport() {

        return ResponseEntity.ok(
                paymentService.getMonthlyReport()
        );
    }

    @GetMapping("/top-customers")
    public ResponseEntity<TopCustomerReport> getTopCustomers() {

        return ResponseEntity.ok(
                paymentService.getTopCustomerReport()
        );
    }

}