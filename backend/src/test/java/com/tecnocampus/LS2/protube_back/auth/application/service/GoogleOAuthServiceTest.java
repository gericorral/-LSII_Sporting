package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.GoogleAccountPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleOAuthServiceTest {
    private final GoogleAccountPort accounts = mock(GoogleAccountPort.class);
    private final GoogleOAuthService service = new GoogleOAuthService(accounts);

    @Test
    void delegatesValidGoogleIdentity() {
        User user = new User(UUID.randomUUID(), "google@example.com", "google_user", null);
        when(accounts.provisionOrFind("subject", "google@example.com", "Google User")).thenReturn(user);

        assertSame(user, service.authenticate("subject", "google@example.com", "Google User"));
    }

    @Test
    void rejectsIncompleteIdentity() {
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("", "google@example.com", "Google User"));
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate(null, "google@example.com", "Google User"));
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate(" ", "google@example.com", "Google User"));
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("subject", null, "Google User"));
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("subject", "", "Google User"));
        verifyNoInteractions(accounts);
    }
}
