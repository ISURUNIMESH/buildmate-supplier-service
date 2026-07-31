package com.construction.materialservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkStockUpdateRequest {

    private String id;
    private Integer stock;
}