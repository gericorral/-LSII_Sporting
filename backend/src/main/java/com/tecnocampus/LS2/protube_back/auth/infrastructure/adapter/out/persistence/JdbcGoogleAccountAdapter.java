package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.GoogleAccountPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.UUID;

@Repository
public class JdbcGoogleAccountAdapter implements GoogleAccountPort {
    private final JdbcTemplate jdbc;
    private final UserRepository users;

    public JdbcGoogleAccountAdapter(JdbcTemplate jdbc, UserRepository users) {
        this.jdbc = jdbc;
        this.users = users;
    }

    @Override
    public User provisionOrFind(String subject, String email, String displayName) {
        User existing = findBySubject(subject);
        if (existing != null) return existing;
        if (users.findByEmailIgnoreCase(email.trim()).isPresent()) {
            throw new GoogleAccountConflictException();
        }

        String username = uniqueUsername(email, displayName);
        User user = users.saveAndFlush(
                new User(UUID.randomUUID(), email.trim().toLowerCase(Locale.ROOT), username, null));
        try {
            jdbc.update("""
                    INSERT INTO auth_accounts (id, user_id, provider, provider_account_id, provider_email)
                    VALUES (?, ?, 'GOOGLE', ?, ?)
                    """, UUID.randomUUID(), user.getId(), subject, user.getEmail());
            jdbc.update("""
                    INSERT INTO user_profiles (user_id, display_name, is_public)
                    VALUES (?, ?, TRUE)
                    ON CONFLICT (user_id) DO NOTHING
                    """, user.getId(), blankToNull(displayName));
            return user;
        } catch (DataIntegrityViolationException exception) {
            User concurrent = findBySubject(subject);
            if (concurrent != null) return concurrent;
            throw exception;
        }
    }

    private User findBySubject(String subject) {
        return jdbc.query("""
                SELECT u.id, u.email, u.username, u.password_hash
                FROM users u JOIN auth_accounts a ON a.user_id = u.id
                WHERE a.provider = 'GOOGLE' AND a.provider_account_id = ?
                """, rs -> rs.next()
                ? new User(rs.getObject("id", UUID.class), rs.getString("email"),
                rs.getString("username"), rs.getString("password_hash"))
                : null, subject);
    }

    private String uniqueUsername(String email, String displayName) {
        String base = (displayName == null ? email.substring(0, email.indexOf('@')) : displayName)
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_");
        if (base.length() < 3) base = "google_user";
        base = base.substring(0, Math.min(42, base.length()));
        String candidate = base;
        int suffix = 1;
        while (users.existsByUsernameIgnoreCase(candidate)) {
            String tail = "_" + suffix++;
            candidate = base.substring(0, Math.min(50 - tail.length(), base.length())) + tail;
        }
        return candidate;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static class GoogleAccountConflictException extends RuntimeException {
        public GoogleAccountConflictException() {
            super("A local account already uses this email. Sign in with your password; Google was not linked.");
        }
    }
}
