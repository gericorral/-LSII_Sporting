ALTER TABLE video_ratings DROP CONSTRAINT IF EXISTS fk_video_ratings_video;
ALTER TABLE video_ratings ALTER COLUMN video_id TYPE VARCHAR(255) USING video_id::text;
