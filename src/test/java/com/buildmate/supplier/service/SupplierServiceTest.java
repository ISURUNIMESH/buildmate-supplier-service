package com.buildmate.supplier.service;

import com.buildmate.supplier.dto.SupplierLoginRequest;
import com.buildmate.supplier.dto.SupplierRegisterRequest;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierStatusUpdateRequest;
import com.buildmate.supplier.model.ApiKey;
import com.buildmate.supplier.model.Supplier;
import com.buildmate.supplier.model.SupplierStatus;
import com.buildmate.supplier.repository.ApiKeyRepository;
import com.buildmate.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

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
                .email("[email protected]")
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
                .email("[email protected]")
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
    void validateApiKey_acceptsActiveKey() {
        ApiKey apiKey = ApiKey.builder()
                .keyValue("valid-key")
                .clientName("BuildMate")
                .active(true)
                .build();
        when(apiKeyRepository.findByKeyValue("valid-key")).thenReturn(Optional.of(apiKey));

        assertDoesNotThrow(() -> supplierService.validateApiKey("valid-key"));
        verify(apiKeyRepository).findByKeyValue("valid-key");
    }

    @Test
    void validateApiKey_rejectsInactiveOrMissingKey() {
        when(apiKeyRepository.findByKeyValue("invalid-key")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supplierService.validateApiKey("invalid-key"));
        assertEquals("Invalid API Key", exception.getMessage());
        verify(apiKeyRepository).findByKeyValue("invalid-key");
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

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supplierService.getSupplierById("missing"));
        assertEquals("Supplier not found with id: missing", exception.getMessage());
        verify(supplierRepository).findById("missing");
    }

    @Test
    void registerSupplier_savesNewSupplier() {
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
    }

    @Test
    void updateSupplier_updatesExistingSupplier() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierRegisterRequest updateRequest = SupplierRegisterRequest.builder()
                .supplierCode("SUP-001")
                .companyName("Updated BuildMate")
                .ownerName("Jane Doe")
                .email("[email protected]")
                .password("password123")
                .phone("0779876543")
                .address("456 Updated Street")
                .district("Gampaha")
                .businessRegistrationNo("BRN-001")
                .build();

        SupplierResponse response = supplierService.updateSupplier("supplier-1", updateRequest);

        assertEquals("Updated BuildMate", response.getCompanyName());
        assertEquals("Jane Doe", response.getOwnerName());
        assertEquals("Gampaha", response.getDistrict());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void deleteSupplier_deletesExistingSupplier() {
        when(supplierRepository.existsById("supplier-1")).thenReturn(true);

        supplierService.deleteSupplier("supplier-1");

        verify(supplierRepository).deleteById("supplier-1");
    }

    @Test
    void deleteSupplier_throwsWhenMissing() {
        when(supplierRepository.existsById("missing")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supplierService.deleteSupplier("missing"));
        assertEquals("Supplier not found with id: missing", exception.getMessage());
        verify(supplierRepository, never()).deleteById(anyString());
    }

    @Test
    void updateSupplierStatus_updatesStatus() {
        when(supplierRepository.findById("supplier-1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierStatusUpdateRequest request = SupplierStatusUpdateRequest.builder()
                .status(SupplierStatus.APPROVED)
                .build();

        SupplierResponse response = supplierService.updateSupplierStatus("supplier-1", request);

        assertEquals(SupplierStatus.APPROVED, response.getStatus());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void loginSupplier_succeedsWithValidCredentials() {
        when(supplierRepository.findByEmail("[email protected]")).thenReturn(Optional.of(supplier));

        SupplierLoginRequest request = SupplierLoginRequest.builder()
                .email("[email protected]")
                .password("password123")
                .build();

        SupplierResponse response = supplierService.loginSupplier(request);

        assertEquals("supplier-1", response.getId());
        verify(supplierRepository).findByEmail("[email protected]");
    }

    @Test
    void loginSupplier_failsWithInvalidPassword() {
        when(supplierRepository.findByEmail("[email protected]")).thenReturn(Optional.of(supplier));

        SupplierLoginRequest request = SupplierLoginRequest.builder()
                .email("[email protected]")
                .password("wrong-password")
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supplierService.loginSupplier(request));
        assertEquals("Invalid credentials", exception.getMessage());
        verify(supplierRepository).findByEmail("[email protected]");
    }
}
