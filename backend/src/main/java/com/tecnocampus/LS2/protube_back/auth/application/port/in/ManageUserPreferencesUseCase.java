package com.tecnocampus.LS2.protube_back.auth.application.port.in;

import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;

import java.util.UUID;

public interface ManageUserPreferencesUseCase {
    UserPreferences get(UUID userId);

    UserPreferences update(UUID userId, boolean darkMode);

    class AuthenticationRequiredException extends RuntimeException {
        public AuthenticationRequiredException() {
            super("Authentication required.");
        }
    }
}
