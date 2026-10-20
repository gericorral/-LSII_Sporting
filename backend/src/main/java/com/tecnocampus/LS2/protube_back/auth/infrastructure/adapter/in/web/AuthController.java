package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.LoginUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.in.LogoutUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.in.RegistrationUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegistrationUseCase registrationUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final SessionPort sessionPort;

    public AuthController(
            RegistrationUseCase registrationUseCase,
            LoginUseCase loginUseCase,
            LogoutUseCase logoutUseCase,
            SessionPort sessionPort
    ) {
        this.registrationUseCase = registrationUseCase;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.sessionPort = sessionPort;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Map<String, String> validationError = request.validationError();
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        User user = loginUseCase.authenticate(request.email(), request.password());
        sessionPort.authenticate(user.getId());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        logoutUseCase.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        Map<String, String> validationError = request.validationError();
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        User user = registrationUseCase.register(request.email(), request.username(), request.password());
        sessionPort.authenticate(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @ExceptionHandler(RegistrationUseCase.DuplicateUserException.class)
    ResponseEntity<Map<String, String>> duplicate(RegistrationUseCase.DuplicateUserException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("field", exception.getField(), "message", exception.getMessage()));
    }

    @ExceptionHandler(LoginUseCase.InvalidCredentialsException.class)
    ResponseEntity<Map<String, String>> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
    }

    public record RegistrationRequest(String email, String username, String password) {
        private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

        Map<String, String> validationError() {
            if (email == null || email.isBlank() || email.length() > 320 || !EMAIL.matcher(email.trim()).matches()) {
                return Map.of("field", "email", "message", "Enter a valid email address.");
            }
            if (username == null || username.isBlank() || !USERNAME.matcher(username.trim()).matches()) {
                return Map.of("field", "username", "message", "Use 3-50 letters, numbers or underscores.");
            }
            if (password == null || password.length() < 8 || password.length() > 72) {
                return Map.of("field", "password", "message", "Password must contain 8-72 characters.");
            }
            return null;
        }
    }

    public record LoginRequest(String email, String password) {
        Map<String, String> validationError() {
            if (email == null || email.isBlank()) {
                return Map.of("field", "email", "message", "Enter your email address.");
            }
            if (password == null || password.isBlank()) {
                return Map.of("field", "password", "message", "Enter your password.");
            }
            return null;
        }
    }

    public record UserResponse(UUID id, String email, String username, boolean authenticated) {
        static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), true);
        }
    }
}
