package com.buildmate.material.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "material_images")
public class MaterialImage {

    @Id
    private String id;

    private String materialId;
    private String fileName;
    private String imageUrl;
    private LocalDateTime uploadedAt;
}
