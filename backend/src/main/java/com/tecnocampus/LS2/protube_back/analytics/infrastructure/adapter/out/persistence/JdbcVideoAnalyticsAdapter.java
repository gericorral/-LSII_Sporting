package com.tecnocampus.LS2.protube_back.analytics.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.out.VideoAnalyticsPort;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Primary
public class JdbcVideoAnalyticsAdapter implements VideoAnalyticsPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoAnalyticsAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<VideoAnalytics> findByOwnerId(UUID ownerId) {
        if (ownerId == null) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT s.video_id, v.title, s.view_count, s.comment_count, s.like_count
                FROM video_analytics_summary s
                JOIN videos v ON v.id = s.video_id
                WHERE s.owner_id = ?
                ORDER BY v.published_at DESC, s.video_id
                """, (rs, rowNum) -> new VideoAnalytics(
                rs.getString("video_id"),
                rs.getString("title"),
                rs.getLong("view_count"),
                rs.getLong("comment_count"),
                rs.getLong("like_count")
        ), ownerId);
    }
}
