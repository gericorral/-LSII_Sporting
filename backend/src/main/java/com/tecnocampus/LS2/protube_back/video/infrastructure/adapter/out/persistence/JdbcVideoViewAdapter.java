package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoViewPort;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class JdbcVideoViewAdapter implements VideoViewPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoViewAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean incrementViews(String videoId) {
        try {
            UUID id = UUID.fromString(videoId);
            return jdbcTemplate.update("""
                    INSERT INTO video_views (id, video_id) SELECT ?, id FROM videos WHERE id = ?
                    """, UUID.randomUUID(), id) == 1;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
