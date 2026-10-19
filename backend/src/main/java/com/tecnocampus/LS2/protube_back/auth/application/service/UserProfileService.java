package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileService {
    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Profile get(UUID userId) {
        return repository.findByUserId(userId).orElseThrow(() -> new ProfileNotFoundException(userId));
    }

    @Transactional
    public Profile update(UUID userId, ProfileUpdate update) {
        repository.save(userId, update);
        return get(userId);
    }

    public record Profile(
            UUID userId,
            String username,
            String displayName,
            String description,
            String avatarPath,
            String website,
            String location,
            boolean publicProfile
    ) {}

    public record ProfileUpdate(
            String displayName,
            String description,
            String avatarPath,
            String website,
            String location,
            boolean publicProfile
    ) {}

    public static class ProfileNotFoundException extends RuntimeException {
        public ProfileNotFoundException(UUID userId) {
            super("Profile not found: " + userId);
        }
    }
}
