package com.buildmate.supplier.repository;

import com.buildmate.supplier.model.Supplier;
import com.buildmate.supplier.model.SupplierStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends MongoRepository<Supplier, String> {
    Optional<Supplier> findByEmail(String email);
    Optional<Supplier> findBySupplierCode(String supplierCode);
    List<Supplier> findByStatus(SupplierStatus status);
    List<Supplier> findByDistrict(String district);
    List<Supplier> findByCompanyNameContainingIgnoreCase(String companyName);
    List<Supplier> findByRatingGreaterThanEqual(Double rating);
    List<Supplier> findTop10ByOrderByRatingDesc();
    boolean existsByEmail(String email);
    boolean existsBySupplierCode(String supplierCode);
    boolean existsByBusinessRegistrationNo(String businessRegistrationNo);
}
