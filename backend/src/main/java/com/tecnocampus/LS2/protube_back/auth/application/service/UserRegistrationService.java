package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserRegistrationService {
    private final UserRepository userRepository;

    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(String email, String username, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedUsername = username.trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateUserException("email", "Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new DuplicateUserException("username", "Username is already taken");
        }

        try {
            return userRepository.save(new User(
                    UUID.randomUUID(),
                    normalizedEmail,
                    normalizedUsername,
                    hashPassword(password)
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException("account", "Email or username is already registered", exception);
        }
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec).getEncoded();
            return "pbkdf2$120000$" + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash password", exception);
        }
    }

    public static class DuplicateUserException extends RuntimeException {
        private final String field;

        public DuplicateUserException(String field, String message) {
            super(message);
            this.field = field;
        }

        public DuplicateUserException(String field, String message, Throwable cause) {
            super(message, cause);
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
}
