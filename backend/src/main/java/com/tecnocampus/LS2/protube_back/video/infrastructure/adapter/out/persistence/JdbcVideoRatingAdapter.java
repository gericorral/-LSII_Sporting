package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoRatingPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class JdbcVideoRatingAdapter implements VideoRatingPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoRatingAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public VideoRating find(String videoId, UUID userId) {
        return jdbcTemplate.queryForObject("""
                SELECT COALESCE(AVG(rating), 0) AS average_rating,
                       COUNT(*) AS rating_count,
                       MAX(CASE WHEN user_id = ? THEN rating END) AS user_rating
                FROM video_ratings
                WHERE video_id = ?
                """, (rs, rowNum) -> new VideoRating(
                videoId,
                rs.getBigDecimal("average_rating").setScale(2),
                rs.getLong("rating_count"),
                toInteger(rs.getObject("user_rating"))
        ), userId, videoId);
    }

    private Integer toInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    @Override
    public void save(String videoId, UUID userId, int rating) {
        jdbcTemplate.update("""
                INSERT INTO video_ratings (video_id, user_id, rating)
                VALUES (?, ?, ?)
                ON CONFLICT (video_id, user_id)
                DO UPDATE SET rating = EXCLUDED.rating, updated_at = CURRENT_TIMESTAMP
                """, videoId, userId, rating);
    }
}
