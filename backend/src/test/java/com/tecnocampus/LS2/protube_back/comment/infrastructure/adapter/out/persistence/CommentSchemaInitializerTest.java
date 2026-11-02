package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.out.persistence;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentSchemaInitializerTest {
    @Test
    void migratesLegacyTextColumnAndPreservesRowsAcrossRepeatedStartup() {
        JdbcTemplate jdbcTemplate = jdbcTemplate("legacy");
        UUID authorId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        createUsersTable(jdbcTemplate);
        jdbcTemplate.update("INSERT INTO users (id, username) VALUES (?, ?)", authorId, "player");
        jdbcTemplate.execute("""
                CREATE TABLE comments (
                    id UUID PRIMARY KEY,
                    video_id VARCHAR(255) NOT NULL,
                    author_id UUID NOT NULL,
                    text VARCHAR(2000) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.update(
                "INSERT INTO comments (id, video_id, author_id, text, created_at) VALUES (?, ?, ?, ?, ?)",
                commentId, "video-1", authorId, "Existing comment", LocalDateTime.now());

        CommentSchemaInitializer initializer = new CommentSchemaInitializer(jdbcTemplate);
        initializer.initialize();
        initializer.initialize();

        assertEquals(0, count(jdbcTemplate, "text"));
        assertEquals(1, count(jdbcTemplate, "content"));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE id = ? AND content = ?",
                Integer.class, commentId, "Existing comment"));
        assertEquals(1, countConstraint(jdbcTemplate, "ck_comments_content"));
        assertEquals(1, countIndex(jdbcTemplate, "idx_comments_video_created"));

        JdbcCommentRepository repository = new JdbcCommentRepository(jdbcTemplate);
        var saved = repository.save("video-1", authorId, "New comment");
        assertEquals(2, repository.findByVideoId("video-1").size());
        assertEquals("New comment", repository.findByVideoId("video-1").stream()
                .filter(comment -> comment.id().equals(saved.id()))
                .findFirst()
                .orElseThrow()
                .content());
    }

    @Test
    void keepsAlreadyUpdatedSchemaUsable() {
        JdbcTemplate jdbcTemplate = jdbcTemplate("current");
        createUsersTable(jdbcTemplate);
        jdbcTemplate.execute("""
                CREATE TABLE comments (
                    id UUID PRIMARY KEY,
                    video_id VARCHAR(255) NOT NULL,
                    author_id UUID NOT NULL,
                    content VARCHAR(2000) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        new CommentSchemaInitializer(jdbcTemplate).initialize();

        assertEquals(1, count(jdbcTemplate, "content"));
        assertEquals(0, count(jdbcTemplate, "text"));
        assertEquals(1, countConstraint(jdbcTemplate, "fk_comments_author"));
    }

    @Test
    void abortsWhenLegacyAndCurrentColumnsExistTogether() {
        JdbcTemplate jdbcTemplate = jdbcTemplate("conflict");
        createUsersTable(jdbcTemplate);
        jdbcTemplate.execute("""
                CREATE TABLE comments (
                    id UUID PRIMARY KEY,
                    video_id VARCHAR(255) NOT NULL,
                    author_id UUID NOT NULL,
                    text VARCHAR(2000),
                    content VARCHAR(2000),
                    created_at TIMESTAMP NOT NULL
                )
                """);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new CommentSchemaInitializer(jdbcTemplate).initialize()
        );

        assertEquals(
                "Comments schema conflict: both comments.text and comments.content exist; migration aborted.",
                exception.getMessage()
        );
    }

    private JdbcTemplate jdbcTemplate(String name) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:comments-schema-" + name + "-" + UUID.randomUUID()
                + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        return new JdbcTemplate(dataSource);
    }

    private void createUsersTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE users (
                    id UUID PRIMARY KEY,
                    username VARCHAR(50) NOT NULL
                )
                """);
    }

    private int count(JdbcTemplate jdbcTemplate, String columnName) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE LOWER(table_name) = 'comments' AND LOWER(column_name) = ?
                """, Integer.class, columnName);
    }

    private int countConstraint(JdbcTemplate jdbcTemplate, String constraintName) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.table_constraints
                WHERE LOWER(table_name) = 'comments' AND LOWER(constraint_name) = ?
                """, Integer.class, constraintName);
    }

    private int countIndex(JdbcTemplate jdbcTemplate, String indexName) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.indexes
                WHERE LOWER(table_name) = 'comments' AND LOWER(index_name) = ?
                """, Integer.class, indexName);
    }
}
