package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

import java.util.Optional;

public interface UserAuthenticationPort {
    Optional<User> findByEmailIgnoreCase(String email);
}
