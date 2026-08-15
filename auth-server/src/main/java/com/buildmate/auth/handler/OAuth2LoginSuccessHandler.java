package com.buildmate.auth.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.buildmate.auth.dto.TokenResponse;
import com.buildmate.auth.entity.User;
import com.buildmate.auth.service.JwtService;
import com.buildmate.auth.service.UserService;

/**
 * After Google authentication succeeds:
 * 1) upsert BuildMate user in MongoDB
 * 2) issue BuildMate RSA-signed JWT (not Google's token)
 * 3) redirect React to /oauth/callback with the token
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final String frontendSuccessUrl;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            JwtService jwtService,
            @Value("${buildmate.auth.frontend-success-url}") String frontendSuccessUrl) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String providerId = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        log.info("Google login success for email={}", email);

        User user = userService.upsertGoogleUser(providerId, email, name, picture);
        TokenResponse token = jwtService.issueToken(user);

        String redirect = frontendSuccessUrl
                + "?token=" + URLEncoder.encode(token.accessToken(), StandardCharsets.UTF_8)
                + "&expires_in=" + token.expiresIn();

        response.sendRedirect(redirect);
    }
}
