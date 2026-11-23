package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ManageUserPreferencesUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {
    private final ManageUserPreferencesUseCase preferencesUseCase;
    private final SessionPort sessionPort;

    public UserPreferencesController(
            ManageUserPreferencesUseCase preferencesUseCase,
            SessionPort sessionPort
    ) {
        this.preferencesUseCase = preferencesUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping("/me")
    public ResponseEntity<?> get() {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(preferencesUseCase.get(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<?> update(@RequestBody PreferencesRequest request) {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(preferencesUseCase.update(userId, request != null && request.darkMode()));
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
    }

    public record PreferencesRequest(boolean darkMode) {
    }
}
