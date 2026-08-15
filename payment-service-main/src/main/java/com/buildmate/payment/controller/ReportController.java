package com.buildmate.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildmate.payment.config.OpenApiConfig;
import com.buildmate.payment.model.MonthlyReport;
import com.buildmate.payment.model.RevenueReport;
import com.buildmate.payment.model.TopCustomerReport;
import com.buildmate.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Payment revenue and customer reports based on SUCCESS payments")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class ReportController {

    private final PaymentService paymentService;

    public ReportController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue report")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revenue report",
                    content = @Content(schema = @Schema(implementation = RevenueReport.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<RevenueReport> getRevenueReport() {

        return ResponseEntity.ok(
                paymentService.getRevenueReport()
        );
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly report")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly report for the current YearMonth",
                    content = @Content(schema = @Schema(implementation = MonthlyReport.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<MonthlyReport> getMonthlyReport() {

        return ResponseEntity.ok(
                paymentService.getMonthlyReport()
        );
    }

    @GetMapping("/top-customers")
    @Operation(summary = "Get top customer report")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top customer by total spent among SUCCESS payments",
                    content = @Content(schema = @Schema(implementation = TopCustomerReport.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<TopCustomerReport> getTopCustomers() {

        return ResponseEntity.ok(
                paymentService.getTopCustomerReport()
        );
    }

}
