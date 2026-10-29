package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.port.out.CommentPersistencePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcCommentRepository implements CommentPersistencePort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findByVideoId(String videoId) {
        return jdbcTemplate.query("""
                SELECT c.id, c.video_id, c.author_id, u.username, c.content, c.created_at
                FROM comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.video_id = ?
                ORDER BY c.created_at DESC, c.id DESC
                """, (rs, rowNum) -> map(rs.getObject("id", UUID.class),
                rs.getString("video_id"),
                rs.getObject("author_id", UUID.class),
                rs.getString("username"),
                rs.getString("content"),
                rs.getObject("created_at", LocalDateTime.class)), videoId);
    }

    @Override
    public Comment save(String videoId, UUID authorId, String content) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO comments (id, video_id, author_id, content)
                VALUES (?, ?, ?, ?)
                """, id, videoId, authorId, content);
        return jdbcTemplate.queryForObject("""
                SELECT c.id, c.video_id, c.author_id, u.username, c.content, c.created_at
                FROM comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.id = ?
                """, (rs, rowNum) -> map(rs.getObject("id", UUID.class),
                rs.getString("video_id"),
                rs.getObject("author_id", UUID.class),
                rs.getString("username"),
                rs.getString("content"),
                rs.getObject("created_at", LocalDateTime.class)), id);
    }

    private Comment map(
            UUID id,
            String videoId,
            UUID authorId,
            String authorName,
            String content,
            LocalDateTime createdAt
    ) {
        return new Comment(id, videoId, authorId, authorName, content, createdAt);
    }
}
