package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;

import java.util.Optional;
import java.util.UUID;

public interface UserProfilePersistencePort {
    Optional<ProfileUseCase.Profile> findByUserId(UUID userId);

    void save(UUID userId, ProfileUseCase.ProfileUpdate update);
}
