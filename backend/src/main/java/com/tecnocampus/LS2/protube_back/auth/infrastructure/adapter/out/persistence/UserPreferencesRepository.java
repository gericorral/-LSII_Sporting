package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserPreferencesPersistencePort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPreferencesRepository implements UserPreferencesPersistencePort {
    private final JdbcTemplate jdbcTemplate;

    public UserPreferencesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserPreferences> findByUserId(UUID userId) {
        return jdbcTemplate.query("""
                SELECT user_id, dark_mode
                FROM user_preferences
                WHERE user_id = ?
                """, rs -> rs.next()
                ? Optional.of(new UserPreferences(
                        rs.getObject("user_id", UUID.class), rs.getBoolean("dark_mode")))
                : Optional.empty(), userId);
    }

    @Override
    public void save(UUID userId, boolean darkMode) {
        jdbcTemplate.update("""
                INSERT INTO user_preferences (user_id, dark_mode)
                VALUES (?, ?)
                ON CONFLICT (user_id)
                DO UPDATE SET dark_mode = EXCLUDED.dark_mode, updated_at = CURRENT_TIMESTAMP
                """, userId, darkMode);
    }
}
