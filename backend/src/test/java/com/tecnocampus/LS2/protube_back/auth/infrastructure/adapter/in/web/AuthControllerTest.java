package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.*;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    private final RegistrationUseCase registration = mock(RegistrationUseCase.class);
    private final LoginUseCase login = mock(LoginUseCase.class);
    private final LogoutUseCase logout = mock(LogoutUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final AuthController controller = new AuthController(registration, login, logout, session);

    @Test
    void registersAndAuthenticatesUser() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", "hash");
        when(registration.register("user@example.com", "player_1", "secret123")).thenReturn(user);
        var response = controller.register(new AuthController.RegistrationRequest(
                "user@example.com", "player_1", "secret123"));
        assertEquals(201, response.getStatusCode().value());
        verify(session).authenticate(user.getId());
    }

    @Test
    void logsInAndAuthenticatesUser() {
        User user = new User(UUID.randomUUID(), "user@example.com", "player_1", "hash");
        when(login.authenticate("user@example.com", "secret123")).thenReturn(user);
        var response = controller.login(new AuthController.LoginRequest("user@example.com", "secret123"));
        assertEquals(200, response.getStatusCode().value());
        verify(session).authenticate(user.getId());
    }

    @Test
    void rejectsInvalidCredentialsWithoutAuthenticating() {
        when(login.authenticate("user@example.com", "wrongpass"))
                .thenThrow(new LoginUseCase.InvalidCredentialsException("Invalid email or password"));
        assertThrows(LoginUseCase.InvalidCredentialsException.class,
                () -> controller.login(new AuthController.LoginRequest("user@example.com", "wrongpass")));
        verifyNoInteractions(session);
    }

    @Test
    void delegatesLogoutToUseCase() {
        assertEquals(204, controller.logout().getStatusCode().value());
        verify(logout).logout();
    }

    @Test
    void mapsDuplicateUserError() {
        var duplicate = new RegistrationUseCase.DuplicateUserException("email", "Email is already registered");
        var response = controller.duplicate(duplicate);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("email", response.getBody().get("field"));
    }

    @Test
    void mapsInvalidCredentialsError() {
        var response = controller.invalidCredentials();
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @ParameterizedTest
    @MethodSource("invalidRegistrations")
    void validatesRegistrationRequest(AuthController.RegistrationRequest request, String field) {
        var response = controller.register(request);
        assertEquals(400, response.getStatusCode().value());
        assertEquals(field, ((java.util.Map<?, ?>) response.getBody()).get("field"));
        verifyNoInteractions(registration);
    }

    private static Stream<Arguments> invalidRegistrations() {
        return Stream.of(
                Arguments.of(new AuthController.RegistrationRequest(null, "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest(" ", "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest("bad", "player_1", "secret123"), "email"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", null, "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", " ", "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "ab", "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "bad-name", "secret123"), "username"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", null), "password"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", "short"), "password"),
                Arguments.of(new AuthController.RegistrationRequest("user@example.com", "player_1", "x".repeat(73)), "password"),
                Arguments.of(new AuthController.RegistrationRequest("x".repeat(321) + "@example.com", "player_1", "secret123"), "email")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidLogins")
    void validatesLoginRequest(AuthController.LoginRequest request, String field) {
        var response = controller.login(request);
        assertEquals(400, response.getStatusCode().value());
        assertEquals(field, ((java.util.Map<?, ?>) response.getBody()).get("field"));
        verifyNoInteractions(login);
    }

    private static Stream<Arguments> invalidLogins() {
        return Stream.of(
                Arguments.of(new AuthController.LoginRequest(null, "secret123"), "email"),
                Arguments.of(new AuthController.LoginRequest("", "secret123"), "email"),
                Arguments.of(new AuthController.LoginRequest("user@example.com", null), "password"),
                Arguments.of(new AuthController.LoginRequest("user@example.com", ""), "password")
        );
    }
}
