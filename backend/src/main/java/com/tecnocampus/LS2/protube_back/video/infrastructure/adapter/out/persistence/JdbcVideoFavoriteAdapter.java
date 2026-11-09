package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoFavoritePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JdbcVideoFavoriteAdapter implements VideoFavoritePort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoFavoriteAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(String videoId, UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM favorites WHERE video_id = ? AND user_id = ?)",
                Boolean.class, videoId, userId));
    }

    @Override
    public void add(String videoId, UUID userId) {
        jdbcTemplate.update("""
                INSERT INTO favorites (video_id, user_id)
                VALUES (?, ?)
                ON CONFLICT (video_id, user_id) DO NOTHING
                """, videoId, userId);
    }

    @Override
    public void remove(String videoId, UUID userId) {
        jdbcTemplate.update("DELETE FROM favorites WHERE video_id = ? AND user_id = ?", videoId, userId);
    }

    @Override
    public List<String> findVideoIds(UUID userId) {
        return jdbcTemplate.query(
                "SELECT video_id FROM favorites WHERE user_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> rs.getString("video_id"), userId);
    }
}
