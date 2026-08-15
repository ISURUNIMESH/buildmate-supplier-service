package com.buildmate.supplier.config;

import com.buildmate.supplier.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private FilterChain filterChain;

    private ApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyFilter(apiKeyRepository);
        ReflectionTestUtils.setField(filter, "configuredApiKey", "");
    }

    @Test
    void missingKeyReturns401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/suppliers");
        request.setServletPath("/suppliers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or missing API key"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void invalidKeyReturns401() throws ServletException, IOException {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("bad")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/suppliers");
        request.setServletPath("/suppliers");
        request.addHeader("X-API-KEY", "bad");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void inactiveKeyTreatedAsInvalid() throws ServletException, IOException {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("inactive")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/suppliers");
        request.setServletPath("/suppliers");
        request.addHeader("X-API-KEY", "inactive");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void validKeyAllowsRequest() throws ServletException, IOException {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good")).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/suppliers");
        request.setServletPath("/suppliers");
        request.addHeader("X-API-KEY", "good");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void swaggerPathsAreNotFiltered() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        request.setServletPath("/v3/api-docs");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void envFallbackAcceptsConfiguredKey() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "configuredApiKey", "env-key");
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("env-key")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/suppliers");
        request.setServletPath("/suppliers");
        request.addHeader("X-API-KEY", "env-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
