package com.buildmate.auth.config;

import java.nio.file.Path;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

import com.buildmate.auth.util.RsaKeyManager;

@Configuration
public class JwtKeyConfig {

    @Bean
    public JWKSource<SecurityContext> jwkSource(
            @Value("${buildmate.auth.rsa-key-path}") String keyPath,
            @Value("${buildmate.auth.rsa-key-id}") String keyId) {
        return RsaKeyManager.loadOrCreate(Path.of(keyPath), keyId);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
