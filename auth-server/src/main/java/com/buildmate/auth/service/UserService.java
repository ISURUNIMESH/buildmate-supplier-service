package com.buildmate.auth.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.buildmate.auth.entity.User;
import com.buildmate.auth.exception.AuthException;
import com.buildmate.auth.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User upsertGoogleUser(String providerId, String email, String fullName, String profileImageUrl) {
        if (email == null || email.isBlank()) {
            throw new AuthException("Google account did not provide an email address");
        }

        Instant now = Instant.now();
        User user = userRepository.findByEmail(email.toLowerCase())
                .or(() -> userRepository.findByProviderAndProviderId("google", providerId))
                .orElseGet(User::new);

        boolean isNew = user.getId() == null;
        user.setEmail(email.toLowerCase());
        user.setFullName(fullName != null && !fullName.isBlank() ? fullName : email);
        user.setProfileImageUrl(profileImageUrl);
        user.setProvider("google");
        user.setProviderId(providerId);
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);

        if (isNew) {
            user.setRoles(List.of("ROLE_USER"));
            user.setCreatedAt(now);
        } else if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        return userRepository.save(user);
    }

    public User requireById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthException("User not found"));
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }
}
