package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.service.UserRegistrationService;
import com.tecnocampus.LS2.protube_back.auth.application.service.UserLoginService;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String SESSION_USER_ID = "authenticatedUserId";
    private final UserRegistrationService registrationService;
    private final UserLoginService loginService;

    public AuthController(UserRegistrationService registrationService) {
        this(registrationService, null);
    }

    @Autowired
    public AuthController(UserRegistrationService registrationService, UserLoginService loginService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        Map<String, String> validationError = request.validationError();
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        User user = loginService.authenticate(request.email(), request.password());
        httpRequest.getSession(true).setAttribute(SESSION_USER_ID, user.getId());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest request,
            HttpSession session
    ) {
        Map<String, String> validationError = request.validationError();
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        User user = registrationService.register(request.email(), request.username(), request.password());
        session.setAttribute(SESSION_USER_ID, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @ExceptionHandler(UserRegistrationService.DuplicateUserException.class)
    ResponseEntity<Map<String, String>> duplicate(UserRegistrationService.DuplicateUserException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("field", exception.getField(), "message", exception.getMessage()));
    }

    @ExceptionHandler(UserLoginService.InvalidCredentialsException.class)
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
