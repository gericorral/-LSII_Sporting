package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserPreferencesPersistencePort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserPreferencesServiceTest {
    private final UserPreferencesPersistencePort persistence = mock(UserPreferencesPersistencePort.class);
    private final UserPreferencesService service = new UserPreferencesService(persistence);
    private final UUID userId = UUID.randomUUID();

    @Test
    void readsStoredPreferenceAndDefaultsToLightMode() {
        when(persistence.findByUserId(userId)).thenReturn(Optional.of(new UserPreferences(userId, true)));
        assertTrue(service.get(userId).darkMode());

        when(persistence.findByUserId(userId)).thenReturn(Optional.empty());
        assertFalse(service.get(userId).darkMode());
    }

    @Test
    void savesRequestedMode() {
        assertTrue(service.update(userId, true).darkMode());
        verify(persistence).save(userId, true);
    }

    @Test
    void requiresAuthentication() {
        assertThrows(UserPreferencesService.AuthenticationRequiredException.class, () -> service.get(null));
        assertThrows(UserPreferencesService.AuthenticationRequiredException.class, () -> service.update(null, true));
    }
}
