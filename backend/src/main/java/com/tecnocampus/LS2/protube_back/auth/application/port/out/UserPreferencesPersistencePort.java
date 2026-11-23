package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesPersistencePort {
    Optional<UserPreferences> findByUserId(UUID userId);

    void save(UUID userId, boolean darkMode);
}
