package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthenticationPort {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findById(UUID id);
}
