package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserPersistencePort;
import com.tecnocampus.LS2.protube_back.auth.application.port.in.RegistrationUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserRegistrationServiceTest {
    private final UserPersistencePort repository = mock(UserPersistencePort.class);
    private final UserRegistrationService service = new UserRegistrationService(repository);

    @Test
    void createsUserWithNormalizedEmailAndHashedPassword() {
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.register("  User@Example.COM ", "player_1", "secret123");

        assertEquals("user@example.com", user.getEmail());
        assertEquals("player_1", user.getUsername());
        assertNotEquals("secret123", user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("pbkdf2$120000$"));
    }

    @Test
    void rejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        RegistrationUseCase.DuplicateUserException exception = assertThrows(
                RegistrationUseCase.DuplicateUserException.class,
                () -> service.register("user@example.com", "player_1", "secret123")
        );

        assertEquals("email", exception.getField());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDuplicateUsername() {
        when(repository.existsByUsernameIgnoreCase("player_1")).thenReturn(true);

        RegistrationUseCase.DuplicateUserException exception = assertThrows(
                RegistrationUseCase.DuplicateUserException.class,
                () -> service.register("user@example.com", "player_1", "secret123")
        );

        assertEquals("username", exception.getField());
        verify(repository, never()).save(any());
    }

    @Test
    void translatesConcurrentDuplicateIntoConflict() {
        when(repository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        RegistrationUseCase.DuplicateUserException exception = assertThrows(
                RegistrationUseCase.DuplicateUserException.class,
                () -> service.register("user@example.com", "player_1", "secret123")
        );

        assertEquals("account", exception.getField());
        assertEquals("duplicate", exception.getCause().getMessage());
    }

    @Test
    void exposesAllUserFields() {
        User user = new User(
                java.util.UUID.randomUUID(),
                "user@example.com",
                "player_1",
                "hash"
        );

        assertNotNull(user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("player_1", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
    }
}
