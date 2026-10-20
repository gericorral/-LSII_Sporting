package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileRepositoryTest {
    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserProfileRepository repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:profiles-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new UserProfileRepository(jdbcTemplate);
        userId = UUID.randomUUID();
        jdbcTemplate.execute("""
                CREATE TABLE users (
                    id UUID PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    status VARCHAR(20) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE user_profiles (
                    user_id UUID PRIMARY KEY,
                    display_name VARCHAR(100),
                    description VARCHAR(2000),
                    avatar_path VARCHAR(500),
                    website VARCHAR(500),
                    location VARCHAR(200),
                    is_public BOOLEAN NOT NULL DEFAULT TRUE,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.update("INSERT INTO users (id, username, status) VALUES (?, ?, ?)",
                userId, "player_1", "ACTIVE");
    }

    @Test
    void readsUserWithoutProfileUsingDefaultsFromLeftJoin() {
        var profile = repository.findByUserId(userId);

        assertTrue(profile.isPresent());
        assertEquals(userId, profile.get().userId());
        assertEquals("player_1", profile.get().username());
        assertEquals(false, profile.get().publicProfile());
    }

    @Test
    void updatesExistingProfile() {
        ProfileUseCase.ProfileUpdate update = new ProfileUseCase.ProfileUpdate(
                "Player One", "About me", null, "https://example.com", "Barcelona", true);

        repository.save(userId, update);

        var profile = repository.findByUserId(userId);
        assertTrue(profile.isPresent());
        assertEquals("Player One", profile.get().displayName());
        assertEquals("Barcelona", profile.get().location());
        assertTrue(profile.get().publicProfile());
    }
}
