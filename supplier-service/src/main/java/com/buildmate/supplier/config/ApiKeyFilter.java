package com.buildmate.supplier.config;

import com.buildmate.supplier.repository.ApiKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Centralized API-key validation (Material + Payment pattern).
 * Validates {@code X-API-KEY} against MongoDB {@code api_keys} (active keys),
 * with optional env fallback {@code SUPPLIER_API_KEY} / {@code supplier.api-key.default}.
 * Registered only via {@link SecurityConfig} (not as a servlet {@code @Component}) to avoid 403s.
 */
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final ApiKeyRepository apiKeyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${supplier.api-key.default:}")
    private String configuredApiKey;

    public ApiKeyFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        return path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.equals("/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        boolean valid = requestApiKey != null
                && !requestApiKey.isBlank()
                && apiKeyRepository.existsByKeyValueAndActiveTrue(requestApiKey);

        if (!valid && configuredApiKey != null && !configuredApiKey.isBlank()) {
            valid = configuredApiKey.equals(requestApiKey);
        }

        if (!valid) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", HttpServletResponse.SC_UNAUTHORIZED,
                    "error", "Unauthorized",
                    "message", "Invalid or missing API key",
                    "path", request.getRequestURI()
            ));
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "api-client",
                null,
                AuthorityUtils.createAuthorityList("ROLE_API_CLIENT"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
