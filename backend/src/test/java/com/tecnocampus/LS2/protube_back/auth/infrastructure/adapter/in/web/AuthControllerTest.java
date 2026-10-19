package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.service.UserRegistrationService;
import com.tecnocampus.LS2.protube_back.auth.application.service.UserLoginService;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    private final UserRegistrationService service = mock(UserRegistrationService.class);
    private final UserLoginService loginService = mock(UserLoginService.class);
    private final AuthController controller = new AuthController(service, loginService);
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

    @Test
    void logsInAndStoresUserInSession() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", "hash");
        when(loginService.authenticate("user@example.com", "secret123")).thenReturn(user);
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);

        var response = controller.login(
                new AuthController.LoginRequest("user@example.com", "secret123"), request);

        assertEquals(200, response.getStatusCode().value());
        verify(session).setAttribute("authenticatedUserId", user.getId());
    }

    @Test
    void rejectsInvalidCredentialsWithoutCreatingSession() {
        when(loginService.authenticate("user@example.com", "wrongpass"))
                .thenThrow(new UserLoginService.InvalidCredentialsException("Invalid email or password"));
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);

        var response = assertThrows(
                UserLoginService.InvalidCredentialsException.class,
                () -> controller.login(new AuthController.LoginRequest("user@example.com", "wrongpass"), request));

        assertEquals("Invalid email or password", response.getMessage());
        verify(request, never()).getSession(true);
    }

    @ParameterizedTest
    @MethodSource("invalidLogins")
    void returnsValidationDetailsForInvalidLogin(
            AuthController.LoginRequest loginRequest,
            String field
    ) {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);

        var response = controller.login(loginRequest, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(field, ((java.util.Map<?, ?>) response.getBody()).get("field"));
        verifyNoInteractions(loginService);
    }

    private static Stream<Arguments> invalidLogins() {
        return Stream.of(
                Arguments.of(new AuthController.LoginRequest(null, "secret123"), "email"),
                Arguments.of(new AuthController.LoginRequest("", "secret123"), "email"),
                Arguments.of(new AuthController.LoginRequest("user@example.com", null), "password"),
                Arguments.of(new AuthController.LoginRequest("user@example.com", ""), "password")
        );
    }

    @Test
    void logsOutAndInvalidatesExistingSession() {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        var response = controller.logout(request);

        assertEquals(204, response.getStatusCode().value());
        verify(session).invalidate();
    }

    @Test
    void logsOutWithoutCreatingSessionWhenNoneExists() {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        var response = controller.logout(request);

        assertEquals(204, response.getStatusCode().value());
        verify(request).getSession(false);
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
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", null), "password"),
                Arguments.of(new AuthController.RegistrationRequest(" ", "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", " ", "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", "x".repeat(73)), "password")
        );
    }
}
