package com.buildmate.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
@Schema(description = "Payment record")
public class Payment {

    @Id
    @Schema(description = "MongoDB payment ID (backend ID, not a friendly display ID)", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank(message = "Order ID is required")
    @Schema(description = "MongoDB/backend order ID associated with this payment", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;

    @NotBlank(message = "User ID is required")
    @Schema(description = "MongoDB/backend user ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Schema(description = "Payment amount", requiredMode = Schema.RequiredMode.REQUIRED, example = "1500.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Schema(description = "Currency code", requiredMode = Schema.RequiredMode.REQUIRED, example = "LKR")
    private String currency;

    @NotBlank(message = "Payment method is required")
    @Schema(description = "Payment method", requiredMode = Schema.RequiredMode.REQUIRED, example = "CARD")
    private String paymentMethod;

    @Schema(description = "Payment status (for example PENDING, SUCCESS, REFUNDED). Defaults to PENDING on create when omitted.")
    private String status;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
