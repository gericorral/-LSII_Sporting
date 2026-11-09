package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCommentPort;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
@Profile("prod")
public class JdbcVideoCommentAdapter implements VideoCommentPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoCommentAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean incrementComments(String videoId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM videos WHERE id = ?", Long.class, UUID.fromString(videoId)) > 0;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
