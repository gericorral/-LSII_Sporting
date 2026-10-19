package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.service.UserProfileService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProfileControllerTest {
    private final UserProfileService service = mock(UserProfileService.class);
    private final ProfileController controller = new ProfileController(service);
    private final HttpSession session = mock(HttpSession.class);
    private final UUID userId = UUID.randomUUID();
    private final UserProfileService.Profile profile = new UserProfileService.Profile(
            userId, "player_1", "Player One", "About me", null, "https://example.com", "Barcelona", true);

    @Test
    void rejectsOwnProfileWithoutAuthentication() {
        when(session.getAttribute("authenticatedUserId")).thenReturn(null);

        var response = controller.getOwnProfile(session);

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsOwnProfileWhenSessionValueHasWrongType() {
        when(session.getAttribute("authenticatedUserId")).thenReturn("not-a-uuid");

        var response = controller.getOwnProfile(session);

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(service);
    }

    @Test
    void returnsOwnProfileForAuthenticatedUser() {
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        when(service.get(userId)).thenReturn(profile);

        var response = controller.getOwnProfile(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("player_1", ((ProfileController.ProfileResponse) response.getBody()).username());
    }

    @Test
    void updatesOwnProfileAfterValidation() {
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        when(service.update(eq(userId), any())).thenReturn(profile);

        var response = controller.updateOwnProfile(
                new ProfileController.ProfileRequest("Player One", "About me", null, null, null, true),
                session);

        assertEquals(200, response.getStatusCode().value());
        verify(service).update(eq(userId), any(UserProfileService.ProfileUpdate.class));
    }

    @Test
    void rejectsProfileUpdateWithoutAuthentication() {
        when(session.getAttribute("authenticatedUserId")).thenReturn(null);

        var response = controller.updateOwnProfile(
                new ProfileController.ProfileRequest("Player One", null, null, null, null, true),
                session);

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsOverlongDescription() {
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        String description = "x".repeat(2001);

        var response = controller.updateOwnProfile(
                new ProfileController.ProfileRequest(null, description, null, null, null, true),
                session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("description", ((java.util.Map<?, ?>) response.getBody()).get("field"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("invalidProfiles")
    void rejectsOverlongProfileFields(
            ProfileController.ProfileRequest request,
            String field
    ) {
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);

        var response = controller.updateOwnProfile(request, session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(field, ((java.util.Map<?, ?>) response.getBody()).get("field"));
        verifyNoInteractions(service);
    }

    private static Stream<Arguments> invalidProfiles() {
        return Stream.of(
                Arguments.of(new ProfileController.ProfileRequest("x".repeat(101), null, null, null, null, true), "displayName"),
                Arguments.of(new ProfileController.ProfileRequest(null, null, null, "x".repeat(501), null, true), "website"),
                Arguments.of(new ProfileController.ProfileRequest(null, null, null, null, "x".repeat(201), true), "location")
        );
    }

    @Test
    void hidesPrivatePublicProfile() {
        when(service.get(userId)).thenReturn(new UserProfileService.Profile(
                userId, "player_1", "Player One", null, null, null, null, false));

        var response = controller.getPublicProfile(userId);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void returnsPublicProfile() {
        when(service.get(userId)).thenReturn(profile);

        var response = controller.getPublicProfile(userId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userId, ((ProfileController.ProfileResponse) response.getBody()).userId());
    }

    @Test
    void mapsMissingProfileExceptionToNotFound() {
        var response = controller.notFound();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void defaultsNullPublicFlagToPublic() {
        var update = new ProfileController.ProfileRequest(null, null, null, null, null, null).toUpdate();

        assertEquals(true, update.publicProfile());
    }

    @Test
    void preservesPrivatePublicFlag() {
        var update = new ProfileController.ProfileRequest(null, null, null, null, null, false).toUpdate();

        assertEquals(false, update.publicProfile());
    }
}
