package com.buildmate.auth.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Loads a persistent RSA key from disk, or creates and stores one for JWT signing.
 * Sharing this key with Authorization Server JWKS allows the Gateway to validate tokens.
 */
public final class RsaKeyManager {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyManager.class);

    private RsaKeyManager() {
    }

    public static JWKSource<SecurityContext> loadOrCreate(Path keyPath, String keyId) {
        try {
            Files.createDirectories(keyPath.getParent());
            RSAKey rsaKey;
            if (Files.exists(keyPath)) {
                String json = Files.readString(keyPath);
                rsaKey = RSAKey.parse(json);
                log.info("Loaded RSA key from {}", keyPath);
            } else {
                rsaKey = generateRsa(keyId);
                Files.writeString(keyPath, rsaKey.toJSONString());
                log.info("Generated and stored RSA key at {}", keyPath);
            }
            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load/create RSA signing key", ex);
        }
    }

    private static RSAKey generateRsa(String keyId) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(keyId != null ? keyId : UUID.randomUUID().toString())
                .build();
    }
}
