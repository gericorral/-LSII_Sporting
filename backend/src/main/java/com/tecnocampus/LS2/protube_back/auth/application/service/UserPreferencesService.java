package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ManageUserPreferencesUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserPreferencesPersistencePort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserPreferencesService implements ManageUserPreferencesUseCase {
    private final UserPreferencesPersistencePort persistencePort;

    public UserPreferencesService(UserPreferencesPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public UserPreferences get(UUID userId) {
        requireUser(userId);
        return persistencePort.findByUserId(userId)
                .orElseGet(() -> new UserPreferences(userId, false));
    }

    @Override
    public UserPreferences update(UUID userId, boolean darkMode) {
        requireUser(userId);
        persistencePort.save(userId, darkMode);
        return new UserPreferences(userId, darkMode);
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new AuthenticationRequiredException();
        }
    }
}
