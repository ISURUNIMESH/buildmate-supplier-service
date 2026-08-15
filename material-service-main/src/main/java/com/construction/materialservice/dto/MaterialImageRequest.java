package com.buildmate.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "MaterialImageRequest",
        description = "JSON metadata for a material image. Image is referenced by URL (not multipart upload).")
public class MaterialImageRequest {

    @NotBlank(message = "fileName is required")
    @Schema(description = "Original or display file name", example = "cement-bag.jpg")
    private String fileName;

    @NotBlank(message = "imageUrl is required")
    @Schema(description = "Public http(s) image URL", example = "https://cdn.example.com/materials/cement-bag.jpg")
    private String imageUrl;
}
