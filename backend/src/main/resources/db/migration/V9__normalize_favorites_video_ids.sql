ALTER TABLE favorites DROP CONSTRAINT IF EXISTS fk_favorites_video;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE LOWER(table_name) = 'favorites'
          AND LOWER(column_name) = 'video_id'
          AND LOWER(data_type) = 'uuid'
    ) THEN
        ALTER TABLE favorites
            ALTER COLUMN video_id TYPE VARCHAR(255)
            USING video_id::text;
    END IF;
END $$;
