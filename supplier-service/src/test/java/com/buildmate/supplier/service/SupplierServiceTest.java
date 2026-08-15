package com.buildmate.supplier.service;

import com.buildmate.supplier.client.MaterialServiceClient;
import com.buildmate.supplier.dto.SupplierLoginRequest;
import com.buildmate.supplier.dto.SupplierRatingUpdateRequest;
import com.buildmate.supplier.dto.SupplierRegisterRequest;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierReviewCreateRequest;
import com.buildmate.supplier.dto.SupplierStatusUpdateRequest;
import com.buildmate.supplier.dto.SupplierUpdateRequest;
import com.buildmate.supplier.exception.DuplicateSupplierException;
import com.buildmate.supplier.exception.SupplierNotFoundException;
import com.buildmate.supplier.exception.UnauthorizedException;
import com.buildmate.supplier.model.Supplier;
import com.buildmate.supplier.model.SupplierReview;
import com.buildmate.supplier.model.SupplierStatus;
import com.buildmate.supplier.producer.SupplierEventPublisher;
import com.buildmate.supplier.repository.SupplierRepository;
import com.buildmate.supplier.repository.SupplierReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierReviewRepository supplierReviewRepository;

    @Mock
    private SupplierEventPublisher supplierEventPublisher;

    @Mock
    private MaterialServiceClient materialServiceClient;

    @InjectMocks
    private SupplierService supplierService;

    private SupplierRegisterRequest registerRequest;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        registerRequest = SupplierRegisterRequest.builder()
                .supplierCode("SUP-001")
                .companyName("BuildMate Supplies")
                .ownerName("John Doe")
                .email("owner@example.com")
                .password("password123")
                .phone("0771234567")
                .address("123 Main Street")
                .district("Colombo")
                .businessRegistrationNo("BRN-001")
                .build();

        supplier = Supplier.builder()
                .id("supplier-1")
                .supplierCode("SUP-001")
                .companyName("BuildMate Supplies")
                .ownerName("John Doe")
                .email("owner@example.com")
                .password("password123")
                .phone("0771234567")
                .address("123 Main Street")
                .district("Colombo")
                .businessRegistrationNo("BRN-001")
                .rating(4.5)
                .status(SupplierStatus.PENDING)
                .build();
    }

    @Test
    void getAllSuppliers_returnsMappedSuppliers() {
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));

        List<SupplierResponse> responses = supplierService.getAllSuppliers();

        assertEquals(1, responses.size());
        assertEquals("supplier-1", responses.get(0).getId());
        assertEquals("SUP-001", responses.get(0).getSupplierCode());
        verify(supplierRepository).findAll();
    }

    @Test
    void getSupplierById_returnsSupplierWhenFound() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));

        SupplierResponse response = supplierService.getSupplierById("supplier-1");

        assertEquals("supplier-1", response.getId());
        assertEquals("BuildMate Supplies", response.getCompanyName());
        verify(supplierRepository).findById("supplier-1");
    }

    @Test
    void getSupplierById_throwsWhenMissing() {
        when(supplierRepository.findById("missing")).thenReturn(Optional.empty());

        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class,
                () -> supplierService.getSupplierById("missing"));
        assertEquals("Supplier not found with id: missing", exception.getMessage());
        verify(supplierRepository).findById("missing");
    }

    @Test
    void registerSupplier_savesNewSupplierAndPublishesEvent() {
        when(supplierRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(supplierRepository.existsBySupplierCode(registerRequest.getSupplierCode())).thenReturn(false);
        when(supplierRepository.existsByBusinessRegistrationNo(registerRequest.getBusinessRegistrationNo())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier saved = invocation.getArgument(0);
            saved.setId("supplier-1");
            return saved;
        });

        SupplierResponse response = supplierService.registerSupplier(registerRequest);

        assertEquals("supplier-1", response.getId());
        assertEquals(SupplierStatus.PENDING, response.getStatus());
        verify(supplierRepository).save(any(Supplier.class));
        verify(supplierEventPublisher).publishCreated(any(Supplier.class));
    }

    @Test
    void registerSupplier_throwsOnDuplicateEmail() {
        when(supplierRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateSupplierException.class, () -> supplierService.registerSupplier(registerRequest));
        verify(supplierRepository, never()).save(any());
        verifyNoInteractions(supplierEventPublisher);
    }

    @Test
    void updateSupplier_updatesExistingSupplierAndPublishesEvent() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierUpdateRequest updateRequest = SupplierUpdateRequest.builder()
                .companyName("Updated BuildMate")
                .ownerName("Jane Doe")
                .phone("0779876543")
                .address("456 Updated Street")
                .district("Gampaha")
                .build();

        SupplierResponse response = supplierService.updateSupplier("supplier-1", updateRequest);

        assertEquals("Updated BuildMate", response.getCompanyName());
        assertEquals("Jane Doe", response.getOwnerName());
        assertEquals("Gampaha", response.getDistrict());
        verify(supplierRepository).save(any(Supplier.class));
        verify(supplierEventPublisher).publishUpdated(any(Supplier.class));
    }

    @Test
    void deleteSupplier_deletesExistingSupplierAndPublishesEvent() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));

        supplierService.deleteSupplier("supplier-1");

        verify(supplierRepository).deleteById("supplier-1");
        verify(supplierEventPublisher).publishDeleted(eq("supplier-1"), eq("SUP-001"));
    }

    @Test
    void deleteSupplier_throwsWhenMissing() {
        when(supplierRepository.findById("missing")).thenReturn(Optional.empty());

        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class,
                () -> supplierService.deleteSupplier("missing"));
        assertEquals("Supplier not found with id: missing", exception.getMessage());
        verify(supplierRepository, never()).deleteById(anyString());
        verifyNoInteractions(supplierEventPublisher);
    }

    @Test
    void updateSupplierStatus_updatesStatusAndPublishesEvent() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierStatusUpdateRequest request = SupplierStatusUpdateRequest.builder()
                .status(SupplierStatus.APPROVED)
                .build();

        SupplierResponse response = supplierService.updateSupplierStatus("supplier-1", request);

        assertEquals(SupplierStatus.APPROVED, response.getStatus());
        verify(supplierRepository).save(any(Supplier.class));
        verify(supplierEventPublisher).publishStatusChanged(any(Supplier.class), eq("PENDING"));
    }

    @Test
    void loginSupplier_succeedsWithValidCredentials() {
        when(supplierRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(supplier));

        SupplierLoginRequest request = SupplierLoginRequest.builder()
                .email("owner@example.com")
                .password("password123")
                .build();

        SupplierResponse response = supplierService.loginSupplier(request);

        assertEquals("supplier-1", response.getId());
        verify(supplierRepository).findByEmail("owner@example.com");
    }

    @Test
    void loginSupplier_failsWithInvalidPassword() {
        when(supplierRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(supplier));

        SupplierLoginRequest request = SupplierLoginRequest.builder()
                .email("owner@example.com")
                .password("wrong-password")
                .build();

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> supplierService.loginSupplier(request));
        assertEquals("Invalid credentials", exception.getMessage());
        verify(supplierRepository).findByEmail("owner@example.com");
    }

    @Test
    void updateSupplierRating_updatesRatingAndPublishesEvent() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierRatingUpdateRequest request = SupplierRatingUpdateRequest.builder()
                .rating(4.8)
                .build();

        SupplierResponse response = supplierService.updateSupplierRating("supplier-1", request);

        assertEquals(4.8, response.getRating());
        verify(supplierRepository).save(any(Supplier.class));
        verify(supplierEventPublisher).publishUpdated(any(Supplier.class));
    }

    @Test
    void getTopRatedSuppliers_returnsHighestRatedFirst() {
        when(supplierRepository.findTop10ByOrderByRatingDesc()).thenReturn(List.of(supplier));

        List<SupplierResponse> response = supplierService.getTopRatedSuppliers();

        assertEquals(1, response.size());
        assertEquals("supplier-1", response.get(0).getId());
        verify(supplierRepository).findTop10ByOrderByRatingDesc();
    }

    @Test
    void searchSuppliers_returnsMatches() {
        when(supplierRepository.findByCompanyNameContainingIgnoreCase("Build"))
                .thenReturn(List.of(supplier));
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));

        List<SupplierResponse> response = supplierService.searchSuppliers("Build");

        assertEquals(1, response.size());
        assertEquals("SUP-001", response.get(0).getSupplierCode());
    }

    @Test
    void searchSuppliers_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> supplierService.searchSuppliers("  "));
    }

    @Test
    void verifySupplier_setsApproved() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierResponse response = supplierService.verifySupplier("supplier-1");

        assertEquals(SupplierStatus.APPROVED, response.getStatus());
        verify(supplierEventPublisher).publishStatusChanged(any(Supplier.class), eq("PENDING"));
    }

    @Test
    void addSupplierRating_savesReviewAndUpdatesAverage() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierReviewRepository.save(any(SupplierReview.class))).thenAnswer(inv -> inv.getArgument(0));
        when(supplierReviewRepository.findBySupplierIdOrderByCreatedAtDesc("supplier-1")).thenReturn(List.of(
                SupplierReview.builder().supplierId("supplier-1").rating(5.0).build(),
                SupplierReview.builder().supplierId("supplier-1").rating(3.0).build()
        ));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierResponse response = supplierService.addSupplierRating(
                "supplier-1",
                SupplierReviewCreateRequest.builder()
                        .rating(4.0)
                        .comment("Good")
                        .reviewerName("Alice")
                        .build());

        assertEquals(4.0, response.getRating());
        verify(supplierReviewRepository).save(any(SupplierReview.class));
    }

    @Test
    void getSupplierReviews_missingSupplier_throws() {
        when(supplierRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(SupplierNotFoundException.class, () -> supplierService.getSupplierReviews("missing"));
    }

    @Test
    void getSupplierMaterials_delegatesToClient() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(materialServiceClient.getMaterialsBySupplierId("supplier-1"))
                .thenReturn(List.of(Map.of("id", "m1", "name", "Cement")));

        List<Map<String, Object>> materials = supplierService.getSupplierMaterials("supplier-1");

        assertEquals(1, materials.size());
        assertEquals("Cement", materials.get(0).get("name"));
    }

    @Test
    void getSupplierMaterials_missingSupplier_throws() {
        when(supplierRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(SupplierNotFoundException.class, () -> supplierService.getSupplierMaterials("missing"));
    }
}
