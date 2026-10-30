package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.model.CommentLike;
import com.tecnocampus.LS2.protube_back.comment.domain.port.out.CommentPersistencePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
        return findByVideoId(videoId, null);
    }

    @Override
    public List<Comment> findByVideoId(String videoId, UUID viewerId) {
        RowMapper<Comment> mapper = (rs, rowNum) -> map(rs.getObject("id", UUID.class),
                rs.getString("video_id"),
                rs.getObject("author_id", UUID.class),
                rs.getString("username"),
                rs.getString("content"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("parent_comment_id", UUID.class),
                rs.getLong("like_count"),
                rs.getBoolean("liked_by_current_user"));
        if (viewerId == null) {
            return jdbcTemplate.query("""
                    SELECT c.id, c.video_id, c.author_id, u.username, c.content, c.created_at,
                           c.parent_comment_id,
                           (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.id) AS like_count,
                           FALSE AS liked_by_current_user
                    FROM comments c
                    JOIN users u ON u.id = c.author_id
                    WHERE c.video_id = ?
                    ORDER BY c.created_at DESC, c.id DESC
                    """, mapper, videoId);
        }
        return jdbcTemplate.query("""
                SELECT c.id, c.video_id, c.author_id, u.username, c.content, c.created_at,
                       c.parent_comment_id,
                       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.id) AS like_count,
                       EXISTS (SELECT 1 FROM comment_likes vl
                               WHERE vl.comment_id = c.id AND vl.user_id = ?) AS liked_by_current_user
                FROM comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.video_id = ?
                ORDER BY c.created_at DESC, c.id DESC
                """, mapper, viewerId, videoId);
    }

    @Override
    public Comment save(String videoId, UUID authorId, String content) {
        return save(videoId, authorId, null, content);
    }

    @Override
    public Comment save(String videoId, UUID authorId, UUID parentCommentId, String content) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO comments (id, video_id, author_id, parent_comment_id, content)
                VALUES (?, ?, ?, ?, ?)
                """, id, videoId, authorId, parentCommentId, content);
        return jdbcTemplate.queryForObject("""
                SELECT c.id, c.video_id, c.author_id, u.username, c.content, c.created_at,
                       c.parent_comment_id, 0 AS like_count, FALSE AS liked_by_current_user
                FROM comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.id = ?
                """, (rs, rowNum) -> map(rs.getObject("id", UUID.class),
                rs.getString("video_id"),
                rs.getObject("author_id", UUID.class),
                rs.getString("username"),
                rs.getString("content"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("parent_comment_id", UUID.class),
                rs.getLong("like_count"),
                rs.getBoolean("liked_by_current_user")), id);
    }

    @Override
    public boolean existsInVideo(UUID commentId, String videoId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM comments WHERE id = ? AND video_id = ?
                """, Integer.class, commentId, videoId);
        return count != null && count > 0;
    }

    @Override
    public CommentLike toggleLike(UUID commentId, UUID userId) {
        Integer existing = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM comment_likes WHERE comment_id = ? AND user_id = ?
                """, Integer.class, commentId, userId);
        boolean liked;
        if (existing != null && existing > 0) {
            jdbcTemplate.update("DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?", commentId, userId);
            liked = false;
        } else {
            jdbcTemplate.update("""
                    INSERT INTO comment_likes (comment_id, user_id) VALUES (?, ?)
                    """, commentId, userId);
            liked = true;
        }
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comment_likes WHERE comment_id = ?", Long.class, commentId);
        return new CommentLike(commentId, count == null ? 0 : count, liked);
    }

    private Comment map(
            UUID id,
            String videoId,
            UUID authorId,
            String authorName,
            String content,
            LocalDateTime createdAt,
            UUID parentCommentId,
            long likeCount,
            boolean likedByCurrentUser
    ) {
        return new Comment(id, videoId, authorId, authorName, content, createdAt,
                parentCommentId, likeCount, likedByCurrentUser);
    }
}
