package com.buildmate.supplier.config;

import com.buildmate.supplier.repository.ApiKeyRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Supplier security follows Payment + Material patterns:
 * Spring Security is stateless; {@link ApiKeyFilter} enforces {@code X-API-KEY}
 * for business endpoints (Swagger/Actuator excluded inside the filter).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public ApiKeyFilter apiKeyFilter(ApiKeyRepository apiKeyRepository) {
        return new ApiKeyFilter(apiKeyRepository);
    }

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter apiKeyFilter) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(apiKeyFilter);
        // Registered in the SecurityFilterChain below; disable servlet auto-registration.
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/**",
                                "/health"
                        ).permitAll()
                        // Business endpoints: authentication is enforced by ApiKeyFilter (Payment pattern).
                        // Spring Security cannot see the API-key principal unless the filter sets it,
                        // and AuthorizationFilter runs before UsernamePasswordAuthenticationFilter by default.
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
