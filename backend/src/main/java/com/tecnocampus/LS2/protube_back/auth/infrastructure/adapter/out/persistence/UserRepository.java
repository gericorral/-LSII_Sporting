package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserPersistencePort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UUID>, UserPersistencePort {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
}
