package com.construction.materialservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialImageRequest {

    private String fileName;
    private String imageUrl;
}