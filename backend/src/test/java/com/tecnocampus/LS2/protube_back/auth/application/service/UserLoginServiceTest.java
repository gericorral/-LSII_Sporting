package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserLoginServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final UserLoginService service = new UserLoginService(repository);

    @Test
    void authenticatesWithNormalizedEmailAndCorrectPassword() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", hash("secret123"));
        when(repository.findByEmailIgnoreCase("user@example.com")).thenReturn(java.util.Optional.of(user));

        assertEquals(user, service.authenticate(" User@Example.COM ", "secret123"));
    }

    @Test
    void rejectsUnknownUserAndWrongPasswordWithSameError() {
        when(repository.findByEmailIgnoreCase("missing@example.com")).thenReturn(java.util.Optional.empty());
        UserLoginService.InvalidCredentialsException missing = assertThrows(
                UserLoginService.InvalidCredentialsException.class,
                () -> service.authenticate("missing@example.com", "secret123")
        );

        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", hash("secret123"));
        when(repository.findByEmailIgnoreCase("user@example.com")).thenReturn(java.util.Optional.of(user));
        UserLoginService.InvalidCredentialsException wrong = assertThrows(
                UserLoginService.InvalidCredentialsException.class,
                () -> service.authenticate("user@example.com", "wrongpass")
        );

        assertEquals(missing.getMessage(), wrong.getMessage());
    }

    @Test
    void rejectsMalformedStoredHash() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", "not-a-hash");
        when(repository.findByEmailIgnoreCase("user@example.com")).thenReturn(java.util.Optional.of(user));

        assertThrows(UserLoginService.InvalidCredentialsException.class,
                () -> service.authenticate("user@example.com", "secret123"));
    }

    @Test
    void rejectsStoredHashWithInvalidBase64() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", "pbkdf2$120000$%%%$%%%");
        when(repository.findByEmailIgnoreCase("user@example.com")).thenReturn(java.util.Optional.of(user));

        assertThrows(UserLoginService.InvalidCredentialsException.class,
                () -> service.authenticate("user@example.com", "secret123"));
    }

    private static String hash(String password) {
        byte[] salt = new byte[16];
        java.util.Arrays.fill(salt, (byte) 7);
        try {
            var spec = new javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
            byte[] value = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return "pbkdf2$120000$" + Base64.getEncoder().encodeToString(salt)
                    + "$" + Base64.getEncoder().encodeToString(value);
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
