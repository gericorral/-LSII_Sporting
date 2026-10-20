package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserProfilePersistencePort;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {
    private final UserProfilePersistencePort repository = mock(UserProfilePersistencePort.class);
    private final UserProfileService service = new UserProfileService(repository);
    private final UUID userId = UUID.randomUUID();
    private final ProfileUseCase.Profile profile = new ProfileUseCase.Profile(
            userId, "player_1", "Player One", "About me", null, null, null, true);

    @Test
    void returnsExistingProfile() {
        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertEquals(profile, service.get(userId));
    }

    @Test
    void rejectsMissingProfile() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileUseCase.ProfileNotFoundException.class, () -> service.get(userId));
    }

    @Test
    void savesChangesAndReturnsUpdatedProfile() {
        ProfileUseCase.ProfileUpdate update =
                new ProfileUseCase.ProfileUpdate("Updated", null, null, null, null, false);
        ProfileUseCase.Profile updated = new ProfileUseCase.Profile(
                userId, "player_1", "Updated", null, null, null, null, false);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(updated));

        assertEquals(updated, service.update(userId, update));
        verify(repository).save(userId, update);
    }
}
