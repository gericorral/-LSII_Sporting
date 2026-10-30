package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.out.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.DependsOn;

import jakarta.annotation.PostConstruct;

@Component
@DependsOn("userSchemaInitializer")
public class CommentSchemaInitializer {
    private final JdbcTemplate jdbcTemplate;

    public CommentSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void initialize() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS comments (
                    id UUID PRIMARY KEY,
                    video_id VARCHAR(255) NOT NULL,
                    author_id UUID NOT NULL,
                    content VARCHAR(2000) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_comments_author FOREIGN KEY (author_id)
                        REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT ck_comments_content CHECK (length(trim(content)) > 0),
                    CONSTRAINT ck_comments_content_length CHECK (length(content) <= 2000)
                )
                """);
        migrateLegacyTextColumn();
        ensureColumn("parent_comment_id", "ALTER TABLE comments ADD COLUMN parent_comment_id UUID");
        ensureConstraint(
                "fk_comments_author",
                """
                ALTER TABLE comments ADD CONSTRAINT fk_comments_author
                    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
                """
        );
        ensureConstraint(
                "ck_comments_content",
                """
                ALTER TABLE comments ADD CONSTRAINT ck_comments_content
                    CHECK (length(trim(content)) > 0)
                """
        );
        ensureConstraint(
                "ck_comments_content_length",
                """
                ALTER TABLE comments ADD CONSTRAINT ck_comments_content_length
                    CHECK (length(content) <= 2000)
                """
        );
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_comments_video_created
                ON comments (video_id, created_at DESC, id DESC)
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS comment_likes (
                    comment_id UUID NOT NULL,
                    user_id UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (comment_id, user_id),
                    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id)
                        REFERENCES comments(id) ON DELETE CASCADE,
                    CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id)
                        REFERENCES users(id) ON DELETE CASCADE
                )
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_comment_likes_comment
                ON comment_likes (comment_id)
                """);
        ensureConstraint(
                "fk_comments_parent",
                """
                ALTER TABLE comments ADD CONSTRAINT fk_comments_parent
                    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
                """
        );
    }

    private void migrateLegacyTextColumn() {
        boolean hasText = hasColumn("text");
        boolean hasContent = hasColumn("content");
        if (hasText && hasContent) {
            throw new IllegalStateException(
                    "Comments schema conflict: both comments.text and comments.content exist; migration aborted."
            );
        }
        if (hasText) {
            jdbcTemplate.execute("ALTER TABLE comments RENAME COLUMN text TO content");
        }
    }

    private boolean hasColumn(String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE LOWER(table_name) = 'comments'
                  AND LOWER(column_name) = ?
                """, Integer.class, columnName.toLowerCase());
        return count != null && count > 0;
    }

    private void ensureColumn(String columnName, String sql) {
        if (!hasColumn(columnName)) {
            jdbcTemplate.execute(sql);
        }
    }

    private void ensureConstraint(String constraintName, String sql) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE LOWER(table_name) = 'comments'
                  AND LOWER(constraint_name) = ?
                """, Integer.class, constraintName.toLowerCase());
        if (count == null || count == 0) {
            jdbcTemplate.execute(sql);
        }
    }
}
