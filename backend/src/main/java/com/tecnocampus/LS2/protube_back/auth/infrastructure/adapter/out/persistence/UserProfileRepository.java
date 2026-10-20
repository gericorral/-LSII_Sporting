package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserProfilePersistencePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserProfileRepository implements UserProfilePersistencePort {
    private final JdbcTemplate jdbcTemplate;

    public UserProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProfileUseCase.Profile> findByUserId(UUID userId) {
        return jdbcTemplate.query("""
                SELECT u.id, u.username, p.display_name, p.description, p.avatar_path,
                       p.website, p.location, p.is_public
                FROM users u
                LEFT JOIN user_profiles p ON p.user_id = u.id
                WHERE u.id = ? AND u.status = 'ACTIVE'
                """,
                rs -> rs.next()
                        ? Optional.of(new ProfileUseCase.Profile(
                                rs.getObject("id", UUID.class),
                                rs.getString("username"),
                                rs.getString("display_name"),
                                rs.getString("description"),
                                rs.getString("avatar_path"),
                                rs.getString("website"),
                                rs.getString("location"),
                                rs.getBoolean("is_public")))
                        : Optional.empty(),
                userId);
    }

    @Override
    public void save(UUID userId, ProfileUseCase.ProfileUpdate update) {
        int updated = jdbcTemplate.update("""
                UPDATE user_profiles
                SET display_name = ?, description = ?, avatar_path = ?, website = ?, location = ?,
                    is_public = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                """,
                update.displayName(), update.description(), update.avatarPath(), update.website(),
                update.location(), update.publicProfile(), userId);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO user_profiles
                        (user_id, display_name, description, avatar_path, website, location, is_public)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    userId, update.displayName(), update.description(), update.avatarPath(),
                    update.website(), update.location(), update.publicProfile());
        }
    }
}
