package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierReviewResponse", description = "Stored supplier review")
public class SupplierReviewResponse {
    @Schema(description = "Review identifier")
    private String id;

    @Schema(description = "Supplier identifier")
    private String supplierId;

    @Schema(description = "Reviewer display name")
    private String reviewerName;

    @Schema(description = "Review comment")
    private String comment;

    @Schema(description = "Review rating")
    private Double rating;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
}
