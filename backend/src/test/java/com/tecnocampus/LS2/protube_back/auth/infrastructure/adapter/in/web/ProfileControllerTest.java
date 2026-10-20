package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProfileControllerTest {
    private final ProfileUseCase profileUseCase = mock(ProfileUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final ProfileController controller = new ProfileController(profileUseCase, session);
    private final UUID userId = UUID.randomUUID();
    private final ProfileUseCase.Profile profile = new ProfileUseCase.Profile(
            userId, "player_1", "Player One", "About me", null, "https://example.com", "Barcelona", true);

    @Test
    void rejectsOwnProfileWithoutAuthentication() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.getOwnProfile().getStatusCode().value());
        verifyNoInteractions(profileUseCase);
    }

    @Test
    void returnsOwnProfileForAuthenticatedUser() {
        when(session.currentUserId()).thenReturn(userId);
        when(profileUseCase.get(userId)).thenReturn(profile);
        var response = controller.getOwnProfile();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("player_1", ((ProfileController.ProfileResponse) response.getBody()).username());
    }

    @Test
    void updatesOwnProfile() {
        when(session.currentUserId()).thenReturn(userId);
        when(profileUseCase.update(eq(userId), any())).thenReturn(profile);
        var response = controller.updateOwnProfile(new ProfileController.ProfileRequest(
                "Player One", "About me", null, null, null, true));
        assertEquals(200, response.getStatusCode().value());
        verify(profileUseCase).update(eq(userId), any(ProfileUseCase.ProfileUpdate.class));
    }

    @Test
    void rejectsUnauthenticatedUpdate() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.updateOwnProfile(new ProfileController.ProfileRequest(
                "Player One", null, null, null, null, true)).getStatusCode().value());
        verifyNoInteractions(profileUseCase);
    }

    @Test
    void rejectsOverlongDescription() {
        when(session.currentUserId()).thenReturn(userId);
        var response = controller.updateOwnProfile(new ProfileController.ProfileRequest(
                null, "x".repeat(2001), null, null, null, true));
        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(profileUseCase);
    }

    @Test
    void hidesPrivateProfile() {
        when(profileUseCase.get(userId)).thenReturn(new ProfileUseCase.Profile(
                userId, "player_1", "Player One", null, null, null, null, false));
        assertEquals(404, controller.getPublicProfile(userId).getStatusCode().value());
    }

    @Test
    void returnsPublicProfile() {
        when(profileUseCase.get(userId)).thenReturn(profile);
        var response = controller.getPublicProfile(userId);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(userId, ((ProfileController.ProfileResponse) response.getBody()).userId());
    }

    @Test
    void validatesOtherProfileFields() {
        when(session.currentUserId()).thenReturn(userId);
        assertEquals(400, controller.updateOwnProfile(new ProfileController.ProfileRequest(
                "x".repeat(101), null, null, null, null, true)).getStatusCode().value());
        assertEquals(400, controller.updateOwnProfile(new ProfileController.ProfileRequest(
                null, null, null, "x".repeat(501), null, true)).getStatusCode().value());
        assertEquals(400, controller.updateOwnProfile(new ProfileController.ProfileRequest(
                null, null, null, null, "x".repeat(201), true)).getStatusCode().value());
    }

    @Test
    void mapsMissingProfileAndPublicFlag() {
        assertEquals(404, controller.notFound().getStatusCode().value());
        assertEquals(true, new ProfileController.ProfileRequest(null, null, null, null, null, null)
                .toUpdate().publicProfile());
        assertEquals(false, new ProfileController.ProfileRequest(null, null, null, null, null, false)
                .toUpdate().publicProfile());
    }
}
