package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.out.persistence;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcCommentRepositoryTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcCommentRepository repository;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:comments-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new JdbcCommentRepository(jdbcTemplate);
        authorId = UUID.randomUUID();

        jdbcTemplate.execute("""
                CREATE TABLE users (
                    id UUID PRIMARY KEY,
                    username VARCHAR(50) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE comments (
                    id UUID PRIMARY KEY,
                    video_id VARCHAR(255) NOT NULL,
                    author_id UUID NOT NULL,
                    parent_comment_id UUID,
                    content VARCHAR(2000) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE comment_likes (
                    comment_id UUID NOT NULL,
                    user_id UUID NOT NULL,
                    PRIMARY KEY (comment_id, user_id)
                )
                """);
        jdbcTemplate.update("INSERT INTO users (id, username) VALUES (?, ?)", authorId, "player_1");
    }

    @Test
    void savesAndReadsCommentsWithAuthorDetails() {
        var saved = repository.save("video-1", authorId, "Hello");
        var comments = repository.findByVideoId("video-1");

        assertEquals(saved.id(), comments.get(0).id());
        assertEquals("player_1", comments.get(0).authorName());
        assertEquals("Hello", comments.get(0).content());
    }

    @Test
    void togglesLikesAndReadsReplyMetadata() {
        var root = repository.save("video-1", authorId, "Root");
        var reply = repository.save("video-1", authorId, root.id(), "Reply");

        assertEquals(root.id(), reply.parentCommentId());
        assertEquals(1, repository.toggleLike(root.id(), authorId).likeCount());
        assertEquals(1, repository.findByVideoId("video-1", authorId).stream()
                .filter(comment -> comment.id().equals(root.id()))
                .findFirst().orElseThrow().likeCount());
        assertEquals(false, repository.toggleLike(root.id(), authorId).likedByCurrentUser());
    }
}
