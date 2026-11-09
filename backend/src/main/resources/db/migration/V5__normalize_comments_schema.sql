CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    video_id VARCHAR(255) NOT NULL,
    author_id UUID NOT NULL,
    content VARCHAR(2000) NOT NULL,
    parent_comment_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id)
        REFERENCES comments (id) ON DELETE CASCADE,
    CONSTRAINT ck_comments_content CHECK (length(trim(content)) > 0),
    CONSTRAINT ck_comments_content_length CHECK (length(content) <= 2000)
);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'comments' AND column_name = 'body')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'comments' AND column_name = 'content') THEN
        ALTER TABLE comments RENAME COLUMN body TO content;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name = 'comments' AND column_name = 'body')
          AND EXISTS (SELECT 1 FROM information_schema.columns
                      WHERE table_name = 'comments' AND column_name = 'content') THEN
        RAISE EXCEPTION 'Comments schema conflict: both comments.body and comments.content exist';
    END IF;
END $$;

CREATE OR REPLACE VIEW video_analytics_summary AS
SELECT v.id AS video_id,
       v.owner_id,
       COUNT(DISTINCT vv.id)::BIGINT AS view_count,
       COUNT(DISTINCT c.id)::BIGINT AS comment_count,
       COUNT(DISTINCT vl.user_id)::BIGINT AS like_count
FROM videos v
LEFT JOIN video_views vv ON vv.video_id = v.id
LEFT JOIN comments c ON c.video_id::text = v.id::text
LEFT JOIN video_likes vl ON vl.video_id = v.id
GROUP BY v.id, v.owner_id;
