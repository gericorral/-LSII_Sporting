package com.tecnocampus.LS2.protube_back.auth.application.port.in;

import java.util.UUID;

public interface ProfileUseCase {
    Profile get(UUID userId);

    Profile update(UUID userId, ProfileUpdate update);

    record Profile(
            UUID userId,
            String username,
            String displayName,
            String description,
            String avatarPath,
            String website,
            String location,
            boolean publicProfile
    ) {}

    record ProfileUpdate(
            String displayName,
            String description,
            String avatarPath,
            String website,
            String location,
            boolean publicProfile
    ) {}

    class ProfileNotFoundException extends RuntimeException {
        public ProfileNotFoundException(UUID userId) {
            super("Profile not found: " + userId);
        }
    }
}
