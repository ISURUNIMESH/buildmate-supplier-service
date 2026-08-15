package com.buildmate.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.buildmate.auth.dto.AuthUserResponse;
import com.buildmate.auth.dto.TokenResponse;
import com.buildmate.auth.entity.User;

@Service
public class JwtTokenGenerator {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long ttlMinutes;

    public JwtTokenGenerator(
            JwtEncoder jwtEncoder,
            @Value("${buildmate.auth.issuer}") String issuer,
            @Value("${buildmate.auth.access-token-ttl-minutes:60}") long ttlMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public TokenResponse generate(User user) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(ttlMinutes * 60);

        List<String> roles = user.getRoles() == null ? List.of("ROLE_USER") : user.getRoles();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expires)
                .subject(user.getId())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("picture", user.getProfileImageUrl())
                .claim("roles", roles)
                .claim("scope", roles.stream()
                        .map(r -> r.replace("ROLE_", "").toLowerCase())
                        .collect(Collectors.joining(" ")))
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        AuthUserResponse userResponse = new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getProfileImageUrl(),
                roles
        );

        return new TokenResponse(token, "Bearer", ttlMinutes * 60, userResponse);
    }
}
