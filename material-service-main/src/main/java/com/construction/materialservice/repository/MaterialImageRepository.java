package com.buildmate.material.repository;

import com.buildmate.material.model.MaterialImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MaterialImageRepository extends MongoRepository<MaterialImage, String> {

    List<MaterialImage> findByMaterialId(String materialId);
}
