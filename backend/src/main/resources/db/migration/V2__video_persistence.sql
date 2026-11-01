CREATE TABLE IF NOT EXISTS videos (
    id UUID PRIMARY KEY,
    owner_id UUID,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    slug VARCHAR(280) NOT NULL UNIQUE,
    source_type VARCHAR(30) NOT NULL DEFAULT 'USER_UPLOAD',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    duration_seconds INTEGER,
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_videos_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS video_assets (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    storage_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    storage_key VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_video_assets_video_type UNIQUE (video_id, asset_type),
    CONSTRAINT fk_video_assets_video FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS video_categories (
    video_id UUID NOT NULL REFERENCES videos (id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    PRIMARY KEY (video_id, category_id)
);

CREATE TABLE IF NOT EXISTS video_tags (
    video_id UUID NOT NULL REFERENCES videos (id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (video_id, tag_id)
);

CREATE TABLE IF NOT EXISTS video_views (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL REFERENCES videos (id) ON DELETE CASCADE,
    user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS video_likes (
    video_id UUID NOT NULL REFERENCES videos (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (video_id, user_id)
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

CREATE INDEX IF NOT EXISTS idx_videos_published_at ON videos (published_at DESC);
CREATE INDEX IF NOT EXISTS idx_videos_owner_id ON videos (owner_id);
CREATE INDEX IF NOT EXISTS idx_video_assets_video_id ON video_assets (video_id);
CREATE INDEX IF NOT EXISTS idx_video_views_video_created_at ON video_views (video_id, created_at);

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
