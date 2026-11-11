DO $$
BEGIN
    IF to_regclass('public.favorites') IS NOT NULL THEN
        ALTER TABLE favorites DROP CONSTRAINT IF EXISTS fk_favorites_video;

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
    END IF;
END $$;
