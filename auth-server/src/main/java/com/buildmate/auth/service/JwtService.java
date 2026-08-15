package com.buildmate.auth.service;

import org.springframework.stereotype.Service;

import com.buildmate.auth.dto.TokenResponse;
import com.buildmate.auth.entity.User;

/**
 * High-level JWT service used by OAuth success handler and controllers.
 */
@Service
public class JwtService {

    private final JwtTokenGenerator jwtTokenGenerator;

    public JwtService(JwtTokenGenerator jwtTokenGenerator) {
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    public TokenResponse issueToken(User user) {
        return jwtTokenGenerator.generate(user);
    }
}
