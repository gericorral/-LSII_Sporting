package com.tecnocampus.LS2.protube_back.auth.domain.model;

import java.util.UUID;

public record UserPreferences(UUID userId, boolean darkMode) {
}
