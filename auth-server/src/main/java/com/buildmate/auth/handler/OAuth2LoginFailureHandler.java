package com.buildmate.auth.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    private final String frontendFailureUrl;

    public OAuth2LoginFailureHandler(
            @Value("${buildmate.auth.frontend-failure-url}") String frontendFailureUrl) {
        this.frontendFailureUrl = frontendFailureUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        log.warn("Google login failed: {}", exception.getMessage());
        String message = URLEncoder.encode(
                exception.getMessage() != null ? exception.getMessage() : "Google login failed",
                StandardCharsets.UTF_8);
        response.sendRedirect(frontendFailureUrl + "?error=" + message);
    }
}
