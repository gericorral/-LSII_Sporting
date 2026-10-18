package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.service.UserRegistrationService;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    private final UserRegistrationService service = mock(UserRegistrationService.class);
    private final AuthController controller = new AuthController(service);
    private final HttpSession session = mock(HttpSession.class);

    @Test
    void registersAndStoresAuthenticatedUserInSession() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", "hash");
        when(service.register("user@example.com", "player_1", "secret123")).thenReturn(user);

        var response = controller.register(
                new AuthController.RegistrationRequest("user@example.com", "player_1", "secret123"),
                session
        );

        assertEquals(201, response.getStatusCode().value());
        assertEquals(user.getId(), ((AuthController.UserResponse) response.getBody()).id());
        verify(session).setAttribute("authenticatedUserId", user.getId());
    }

    @Test
    void returnsConflictDetailsForDuplicate() {
        var duplicate = new UserRegistrationService.DuplicateUserException("email", "Email is already registered");

        var response = controller.duplicate(duplicate);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("email", response.getBody().get("field"));
    }

    @ParameterizedTest
    @MethodSource("invalidRegistrations")
    void returnsValidationDetailsForInvalidRegistration(
            AuthController.RegistrationRequest request,
            String field
    ) {
        var response = controller.register(request, session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(field, ((java.util.Map<?, ?>) response.getBody()).get("field"));
        verifyNoInteractions(service);
    }

    private static Stream<Arguments> invalidRegistrations() {
        return Stream.of(
                Arguments.of(new AuthController.RegistrationRequest(null, "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest("", "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest("invalid", "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", null, "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "ab", "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player-1", "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", "short"), "password"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", null), "password")
        );
    }
}
