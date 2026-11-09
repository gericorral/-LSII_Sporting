DROP VIEW IF EXISTS video_analytics_summary;
ALTER TABLE comments DROP CONSTRAINT IF EXISTS fk_comments_video;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE LOWER(table_name) = 'comments'
          AND LOWER(column_name) = 'video_id'
          AND LOWER(data_type) = 'uuid'
    ) THEN
        ALTER TABLE comments
            ALTER COLUMN video_id TYPE VARCHAR(255)
            USING video_id::text;
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
