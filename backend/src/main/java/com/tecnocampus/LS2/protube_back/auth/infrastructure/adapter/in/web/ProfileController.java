package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.service.UserProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private static final String SESSION_USER_ID = "authenticatedUserId";
    private final UserProfileService profileService;

    public ProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getOwnProfile(HttpSession session) {
        UUID userId = authenticatedUser(session);
        if (userId == null) return unauthorized();
        return ResponseEntity.ok(ProfileResponse.from(profileService.get(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateOwnProfile(@RequestBody ProfileRequest request, HttpSession session) {
        UUID userId = authenticatedUser(session);
        if (userId == null) return unauthorized();
        Map<String, String> error = request.validationError();
        if (error != null) return ResponseEntity.badRequest().body(error);
        return ResponseEntity.ok(ProfileResponse.from(profileService.update(userId, request.toUpdate())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID userId) {
        UserProfileService.Profile profile = profileService.get(userId);
        return profile.publicProfile()
                ? ResponseEntity.ok(ProfileResponse.from(profile))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private UUID authenticatedUser(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER_ID);
        return value instanceof UUID ? (UUID) value : null;
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Authentication required"));
    }

    @ExceptionHandler(UserProfileService.ProfileNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    public record ProfileRequest(
            String displayName, String description, String avatarPath,
            String website, String location, Boolean publicProfile
    ) {
        Map<String, String> validationError() {
            if (displayName != null && displayName.length() > 100)
                return Map.of("field", "displayName", "message", "Display name must be at most 100 characters.");
            if (description != null && description.length() > 2000)
                return Map.of("field", "description", "message", "Description must be at most 2000 characters.");
            if (website != null && website.length() > 500)
                return Map.of("field", "website", "message", "Website must be at most 500 characters.");
            if (location != null && location.length() > 200)
                return Map.of("field", "location", "message", "Location must be at most 200 characters.");
            return null;
        }

        UserProfileService.ProfileUpdate toUpdate() {
            return new UserProfileService.ProfileUpdate(displayName, description, avatarPath, website, location,
                    publicProfile == null || publicProfile);
        }
    }

    public record ProfileResponse(
            UUID userId, String username, String displayName, String description,
            String avatarPath, String website, String location, boolean publicProfile
    ) {
        static ProfileResponse from(UserProfileService.Profile profile) {
            return new ProfileResponse(profile.userId(), profile.username(), profile.displayName(),
                    profile.description(), profile.avatarPath(), profile.website(), profile.location(),
                    profile.publicProfile());
        }
    }
}
