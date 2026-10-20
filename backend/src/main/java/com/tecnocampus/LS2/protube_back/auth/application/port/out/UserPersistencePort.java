package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

public interface UserPersistencePort extends UserAuthenticationPort {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    User save(User user);
}
