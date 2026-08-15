package com.buildmate.material.config;

import com.buildmate.material.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
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
        SecurityContextHolder.clearContext();
        filter = new ApiKeyFilter(apiKeyRepository);
        ReflectionTestUtils.setField(filter, "configuredApiKey", "");
    }

    @Test
    void missingKeyReturns401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/materials");
        request.setServletPath("/materials");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or missing API key"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidKeyReturns401() throws ServletException, IOException {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("bad")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/materials");
        request.setServletPath("/materials");
        request.addHeader("X-API-KEY", "bad");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void inactiveKeyTreatedAsInvalid() throws ServletException, IOException {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("inactive")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/materials");
        request.setServletPath("/materials");
        request.addHeader("X-API-KEY", "inactive");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void validKeyAllowsRequestAndPopulatesSecurityContext() throws ServletException, IOException {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good")).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/materials");
        request.setServletPath("/materials");
        request.addHeader("X-API-KEY", "good");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_API_CLIENT".equals(a.getAuthority())));
    }

    @Test
    void swaggerPathsAreNotFiltered() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        request.setServletPath("/v3/api-docs");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void actuatorPathsAreNotFiltered() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.setServletPath("/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void envFallbackAcceptsConfiguredKey() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "configuredApiKey", "env-key");
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("env-key")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/materials");
        request.setServletPath("/materials");
        request.addHeader("X-API-KEY", "env-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
