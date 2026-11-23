package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.domain.port.out.SubscriptionPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JdbcSubscriptionAdapter implements SubscriptionPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcSubscriptionAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean targetExists(UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE id = ? AND status = 'ACTIVE')",
                Boolean.class, userId));
    }

    @Override
    public boolean exists(UUID subscriberId, UUID subscribedToId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM subscriptions WHERE subscriber_id = ? AND subscribed_to_id = ?)",
                Boolean.class, subscriberId, subscribedToId));
    }

    @Override
    public void add(UUID subscriberId, UUID subscribedToId) {
        jdbcTemplate.update("""
                INSERT INTO subscriptions (subscriber_id, subscribed_to_id)
                VALUES (?, ?)
                ON CONFLICT (subscriber_id, subscribed_to_id) DO NOTHING
                """, subscriberId, subscribedToId);
    }

    @Override
    public void remove(UUID subscriberId, UUID subscribedToId) {
        jdbcTemplate.update(
                "DELETE FROM subscriptions WHERE subscriber_id = ? AND subscribed_to_id = ?",
                subscriberId, subscribedToId);
    }

    @Override
    public List<ProfileUseCase.Profile> findSubscriptions(UUID subscriberId) {
        return jdbcTemplate.query("""
                SELECT u.id, u.username, p.display_name, p.description, p.avatar_path,
                       p.website, p.location, COALESCE(p.is_public, TRUE) AS is_public
                FROM subscriptions s
                JOIN users u ON u.id = s.subscribed_to_id AND u.status = 'ACTIVE'
                LEFT JOIN user_profiles p ON p.user_id = u.id
                WHERE s.subscriber_id = ?
                ORDER BY s.created_at DESC
                """, (rs, rowNum) -> new ProfileUseCase.Profile(
                rs.getObject("id", UUID.class),
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getString("description"),
                rs.getString("avatar_path"),
                rs.getString("website"),
                rs.getString("location"),
                rs.getBoolean("is_public")), subscriberId);
    }
}
