CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY,
    display_name VARCHAR(100),
    description TEXT,
    avatar_path VARCHAR(500),
    website VARCHAR(500),
    location VARCHAR(200),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE auth_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_auth_accounts_provider_account UNIQUE (provider, provider_account_id),
    CONSTRAINT fk_auth_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_auth_accounts_provider CHECK (provider IN ('LOCAL', 'GOOGLE'))
);

CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    CONSTRAINT uq_user_sessions_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE user_preferences (
    user_id UUID PRIMARY KEY,
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    accessibility_settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_categories_name UNIQUE (name),
    CONSTRAINT uq_categories_slug UNIQUE (slug)
);

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tags_name UNIQUE (name),
    CONSTRAINT uq_tags_slug UNIQUE (slug)
);

CREATE TABLE videos (
    id UUID PRIMARY KEY,
    owner_id UUID,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    slug VARCHAR(280) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_external_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    duration_seconds INTEGER,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    search_vector TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('simple', COALESCE(title, '') || ' ' || COALESCE(description, ''))
    ) STORED,
    CONSTRAINT uq_videos_slug UNIQUE (slug),
    CONSTRAINT fk_videos_owner
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_videos_source_type CHECK (source_type IN ('USER_UPLOAD', 'VIDEO_GRABBER', 'EXTERNAL')),
    CONSTRAINT ck_videos_status CHECK (status IN ('PROCESSING', 'PUBLISHED', 'PRIVATE', 'BLOCKED', 'FAILED', 'DELETED')),
    CONSTRAINT ck_videos_visibility CHECK (visibility IN ('PUBLIC', 'UNLISTED', 'PRIVATE')),
    CONSTRAINT ck_videos_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
);

CREATE TABLE video_assets (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    storage_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    storage_key VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(500),
    mime_type VARCHAR(255),
    file_size_bytes BIGINT,
    checksum VARCHAR(128),
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_video_assets_video_type UNIQUE (video_id, asset_type),
    CONSTRAINT fk_video_assets_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT ck_video_assets_type CHECK (asset_type IN ('VIDEO', 'THUMBNAIL', 'METADATA')),
    CONSTRAINT ck_video_assets_provider CHECK (storage_provider IN ('LOCAL', 'S3', 'EXTERNAL')),
    CONSTRAINT ck_video_assets_file_size CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0)
);

CREATE TABLE video_categories (
    video_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (video_id, category_id),
    CONSTRAINT fk_video_categories_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_video_categories_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

CREATE TABLE video_tags (
    video_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (video_id, tag_id),
    CONSTRAINT fk_video_tags_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_video_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL,
    author_id UUID,
    parent_comment_id UUID,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_comments_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    CONSTRAINT ck_comments_status CHECK (status IN ('VISIBLE', 'EDITED', 'DELETED', 'MODERATED')),
    CONSTRAINT ck_comments_body CHECK (length(btrim(body)) > 0)
);

CREATE TABLE comment_likes (
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id, user_id),
    CONSTRAINT fk_comment_likes_comment
        FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_likes_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE video_views (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL,
    user_id UUID,
    session_id VARCHAR(255),
    ip_hash VARCHAR(128),
    watched_seconds INTEGER NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_views_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_video_views_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_video_views_watched_seconds CHECK (watched_seconds >= 0)
);

CREATE TABLE video_likes (
    video_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (video_id, user_id),
    CONSTRAINT fk_video_likes_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_video_likes_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE video_ratings (
    video_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (video_id, user_id),
    CONSTRAINT fk_video_ratings_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_video_ratings_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_video_ratings_value CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE favorites (
    video_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (video_id, user_id),
    CONSTRAINT fk_favorites_video
        FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE subscriptions (
    subscriber_id UUID NOT NULL,
    subscribed_to_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (subscriber_id, subscribed_to_id),
    CONSTRAINT fk_subscriptions_subscriber
        FOREIGN KEY (subscriber_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_subscribed_to
        FOREIGN KEY (subscribed_to_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_subscriptions_not_self CHECK (subscriber_id <> subscribed_to_id)
);

CREATE INDEX idx_auth_accounts_user_id ON auth_accounts (user_id);
CREATE INDEX idx_user_sessions_user_id ON user_sessions (user_id);
CREATE INDEX idx_user_preferences_language ON user_preferences (language);
CREATE INDEX idx_videos_owner_id ON videos (owner_id);
CREATE INDEX idx_videos_status_visibility ON videos (status, visibility);
CREATE INDEX idx_videos_published_at ON videos (published_at DESC);
CREATE INDEX idx_videos_search_vector ON videos USING GIN (search_vector);
CREATE INDEX idx_video_assets_video_id ON video_assets (video_id);
CREATE INDEX idx_video_categories_category_id ON video_categories (category_id);
CREATE INDEX idx_video_tags_tag_id ON video_tags (tag_id);
CREATE INDEX idx_comments_video_created_at ON comments (video_id, created_at);
CREATE INDEX idx_comments_parent_comment_id ON comments (parent_comment_id);
CREATE INDEX idx_comments_author_id ON comments (author_id);
CREATE INDEX idx_comment_likes_user_id ON comment_likes (user_id);
CREATE INDEX idx_video_views_video_created_at ON video_views (video_id, created_at);
CREATE INDEX idx_video_views_user_id ON video_views (user_id);
CREATE INDEX idx_video_likes_user_id ON video_likes (user_id);
CREATE INDEX idx_video_ratings_user_id ON video_ratings (user_id);
CREATE INDEX idx_favorites_user_id ON favorites (user_id);
CREATE INDEX idx_subscriptions_subscribed_to_id ON subscriptions (subscribed_to_id);

CREATE VIEW video_analytics_summary AS
WITH view_counts AS (
    SELECT video_id, COUNT(*)::BIGINT AS view_count
    FROM video_views
    GROUP BY video_id
),
comment_counts AS (
    SELECT video_id, COUNT(*)::BIGINT AS comment_count
    FROM comments
    WHERE status IN ('VISIBLE', 'EDITED')
    GROUP BY video_id
),
like_counts AS (
    SELECT video_id, COUNT(*)::BIGINT AS like_count
    FROM video_likes
    GROUP BY video_id
)
SELECT
    v.id AS video_id,
    v.owner_id,
    COALESCE(vc.view_count, 0)::BIGINT AS view_count,
    COALESCE(cc.comment_count, 0)::BIGINT AS comment_count,
    COALESCE(lc.like_count, 0)::BIGINT AS like_count
FROM videos v
LEFT JOIN view_counts vc ON vc.video_id = v.id
LEFT JOIN comment_counts cc ON cc.video_id = v.id
LEFT JOIN like_counts lc ON lc.video_id = v.id;
