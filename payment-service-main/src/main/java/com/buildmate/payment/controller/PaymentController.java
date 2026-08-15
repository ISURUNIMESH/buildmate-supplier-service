package com.buildmate.payment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildmate.payment.config.OpenApiConfig;
import com.buildmate.payment.exception.ErrorResponse;
import com.buildmate.payment.model.Payment;
import com.buildmate.payment.service.PaymentService;

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

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Create and manage payments. Completed payments publish PaymentCompletedEvent via RabbitMQ.")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Create payment", description = "Creates a payment. If status is omitted, defaults to PENDING. When status is completed (COMPLETED/SUCCESS/PAID), publishes PaymentCompletedEvent.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created",
                    content = @Content(schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment) {

        Payment savedPayment = paymentService.createPayment(payment);

        return ResponseEntity.status(201).body(savedPayment);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Payment> getPaymentById(
            @Parameter(description = "MongoDB payment ID", required = true) @PathVariable String id) {

        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get payment history for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment history",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<List<Payment>> getPaymentHistory(
            @Parameter(description = "MongoDB/backend user ID", required = true) @PathVariable String userId) {

        List<Payment> payments = paymentService.getPaymentHistory(userId);

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending payments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending payments",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<List<Payment>> getPendingPayments() {

        return ResponseEntity.ok(paymentService.getPendingPayments());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update payment status", description = "When updated status is completed (COMPLETED/SUCCESS/PAID), publishes PaymentCompletedEvent via RabbitMQ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Payment> updatePaymentStatus(
            @Parameter(description = "MongoDB payment ID", required = true) @PathVariable String id,
            @Parameter(description = "New payment status", required = true, example = "SUCCESS") @RequestParam String status) {

        return paymentService.updatePaymentStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List payments by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments for the given status",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<List<Payment>> getPaymentsByStatus(
            @Parameter(description = "Payment status filter", required = true, example = "PENDING") @PathVariable String status) {

        return ResponseEntity.ok(
            paymentService.getPaymentsByStatus(status));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment", description = "Refunds only when current status is SUCCESS. Otherwise returns 400.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment refunded",
                    content = @Content(schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "400", description = "Payment not found or not eligible for refund"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Payment> refundPayment(
            @Parameter(description = "MongoDB payment ID", required = true) @PathVariable String id) {

        return paymentService.refundPayment(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping
    @Operation(summary = "List all payments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All payments",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<List<Payment>> getAllPayments() {

        List<Payment> payments = paymentService.getAllPayments();

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List payments for a user", description = "Same data as payment history for the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments for the user",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<List<Payment>> getPaymentsByUser(
            @Parameter(description = "MongoDB/backend user ID", required = true) @PathVariable String userId) {

        return ResponseEntity.ok(
                paymentService.getPaymentHistory(userId));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry payment", description = "Sets status back to PENDING when eligible. Rejects SUCCESS or REFUNDED payments with 400.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retry initiated",
                    content = @Content(schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "400", description = "Payment not found or not eligible for retry"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public ResponseEntity<Payment> retryPayment(
            @Parameter(description = "MongoDB payment ID", required = true) @PathVariable String id) {

        return paymentService.retryPayment(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
