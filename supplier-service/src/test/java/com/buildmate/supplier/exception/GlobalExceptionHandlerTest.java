package com.buildmate.supplier.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/suppliers");
    }

    @Test
    void validationReturns400WithFieldDetails() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Validation failed", response.getBody().get("message"));
        assertEquals("/suppliers", response.getBody().get("path"));
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertEquals("must not be blank", details.get("email"));
        assertFalse(response.getBody().toString().contains("Exception"));
    }

    @Test
    void notFoundReturns404() {
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(
                new SupplierNotFoundException("Supplier not found with id: x"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Supplier not found with id: x", response.getBody().get("message"));
    }

    @Test
    void unauthorizedReturns401() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorized(
                new UnauthorizedException("Invalid credentials"), request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Unauthorized", response.getBody().get("error"));
    }

    @Test
    void conflictReturns409() {
        ResponseEntity<Map<String, Object>> response = handler.handleConflict(
                new DuplicateSupplierException("Email is already registered"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("status"));
    }

    @Test
    void unexpectedReturnsSafe500WithoutInternalDetails() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(
                new RuntimeException("secret db password leak"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertFalse(response.getBody().toString().contains("secret"));
        assertFalse(response.getBody().toString().contains("password"));
    }
}
