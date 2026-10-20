package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserProfilePersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileService implements ProfileUseCase {
    private final UserProfilePersistencePort repository;

    public UserProfileService(UserProfilePersistencePort repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ProfileUseCase.Profile get(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ProfileUseCase.ProfileNotFoundException(userId));
    }

    @Transactional
    public ProfileUseCase.Profile update(UUID userId, ProfileUseCase.ProfileUpdate update) {
        repository.save(userId, update);
        return get(userId);
    }

}
