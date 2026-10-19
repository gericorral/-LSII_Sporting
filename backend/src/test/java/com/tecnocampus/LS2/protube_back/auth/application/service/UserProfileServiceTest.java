package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence.UserProfileRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final UserProfileService service = new UserProfileService(repository);
    private final UUID userId = UUID.randomUUID();
    private final UserProfileService.Profile profile = new UserProfileService.Profile(
            userId, "player_1", "Player One", "About me", null, null, null, true);

    @Test
    void returnsExistingProfile() {
        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertEquals(profile, service.get(userId));
    }

    @Test
    void rejectsMissingProfile() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(UserProfileService.ProfileNotFoundException.class, () -> service.get(userId));
    }

    @Test
    void savesChangesAndReturnsUpdatedProfile() {
        UserProfileService.ProfileUpdate update =
                new UserProfileService.ProfileUpdate("Updated", null, null, null, null, false);
        UserProfileService.Profile updated = new UserProfileService.Profile(
                userId, "player_1", "Updated", null, null, null, null, false);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(updated));

        assertEquals(updated, service.update(userId, update));
        verify(repository).save(userId, update);
    }
}
