package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@Profile("prod")
public class VideoStoreDatabaseImporter implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(VideoStoreDatabaseImporter.class);

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public VideoStoreDatabaseImporter(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper,
            JdbcTemplate jdbcTemplate
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!Files.isDirectory(storeDirectory)) {
            LOG.info("Video store directory does not exist; skipping database import: {}", storeDirectory);
            return;
        }

        try (Stream<Path> metadataFiles = Files.list(storeDirectory)) {
            metadataFiles
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(this::importMetadata);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan video store: " + storeDirectory, exception);
        }
    }

    private void importMetadata(Path metadataPath) {
        String fileId = metadataPath.getFileName().toString()
                .substring(0, metadataPath.getFileName().toString().length() - ".json".length());
        Path videoPath = storeDirectory.resolve(fileId + ".mp4");
        Path thumbnailPath = Files.isRegularFile(storeDirectory.resolve(fileId + ".webp"))
                ? storeDirectory.resolve(fileId + ".webp")
                : storeDirectory.resolve(fileId + ".png");

        if (!Files.isRegularFile(videoPath) || !Files.isRegularFile(thumbnailPath)) {
            LOG.warn("Skipping video {} because its media files are incomplete", fileId);
            return;
        }

        JsonNode metadata;
        try {
            metadata = objectMapper.readTree(metadataPath.toFile());
        } catch (IOException exception) {
            LOG.warn("Skipping invalid video metadata {}", metadataPath, exception);
            return;
        }
        String title = metadata.path("title").asText("").trim();
        if (title.isEmpty()) {
            LOG.warn("Skipping video {} because its title is missing", fileId);
            return;
        }
        if (metadata.hasNonNull("ownerId")) {
            LOG.debug("Skipping user-uploaded video {} during legacy store import", fileId);
            return;
        }

        UUID videoId = UUID.nameUUIDFromBytes(("video-store:" + fileId).getBytes(StandardCharsets.UTF_8));
        String description = metadata.path("meta").path("description").asText("");
        Integer duration = metadata.hasNonNull("duration") ? metadata.get("duration").asInt() : null;

        jdbcTemplate.update("""
                    INSERT INTO videos (id, owner_id, title, description, slug, source_type,
                                        status, visibility, duration_seconds, published_at)
                    VALUES (?, NULL, ?, ?, ?, 'VIDEO_GRABBER', 'PUBLISHED', 'PUBLIC', ?, CURRENT_TIMESTAMP)
                    ON CONFLICT (id) DO UPDATE SET
                        title = EXCLUDED.title,
                        description = EXCLUDED.description,
                        duration_seconds = EXCLUDED.duration_seconds,
                        status = EXCLUDED.status,
                        visibility = EXCLUDED.visibility
                    """, videoId, title, description, "video-store-" + fileId, duration);

        jdbcTemplate.update("""
                    INSERT INTO video_assets (id, video_id, asset_type, storage_provider, storage_key, mime_type)
                    VALUES (?, ?, 'VIDEO', 'LOCAL', ?, 'video/mp4')
                    ON CONFLICT (video_id, asset_type) DO UPDATE SET
                        storage_key = EXCLUDED.storage_key,
                        mime_type = EXCLUDED.mime_type
                    """, UUID.randomUUID(), videoId, fileId + ".mp4");

        jdbcTemplate.update("""
                    INSERT INTO video_assets (id, video_id, asset_type, storage_provider, storage_key, mime_type)
                    VALUES (?, ?, 'THUMBNAIL', 'LOCAL', ?, ?)
                    ON CONFLICT (video_id, asset_type) DO UPDATE SET
                        storage_key = EXCLUDED.storage_key,
                        mime_type = EXCLUDED.mime_type
                    """, UUID.randomUUID(), videoId, thumbnailPath.getFileName().toString(),
                    thumbnailPath.toString().endsWith(".webp") ? "image/webp" : "image/png");

        importTags(videoId, metadata.path("tags"));
        importCategory(videoId, metadata.path("category").asText(""));
    }

    private void importTags(UUID videoId, JsonNode tags) {
        jdbcTemplate.update("DELETE FROM video_tags WHERE video_id = ?", videoId);
        tags.forEach(tagNode -> {
            String name = tagNode.asText("").trim();
            if (name.isEmpty()) {
                return;
            }
            UUID tagId = jdbcTemplate.queryForObject("""
                    INSERT INTO tags (id, name, slug) VALUES (?, ?, ?)
                    ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                    RETURNING id
                    """, UUID.class, UUID.randomUUID(), name, slug(name));
            jdbcTemplate.update(
                    "INSERT INTO video_tags (video_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    videoId, tagId
            );
        });
    }

    private void importCategory(UUID videoId, String category) {
        jdbcTemplate.update("DELETE FROM video_categories WHERE video_id = ?", videoId);
        if (category.isBlank()) {
            return;
        }
        String normalized = category.trim();
        UUID categoryId = jdbcTemplate.queryForObject("""
                INSERT INTO categories (id, name, slug) VALUES (?, ?, ?)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id
                """, UUID.class, UUID.randomUUID(), normalized, slug(normalized));
        jdbcTemplate.update(
                "INSERT INTO video_categories (video_id, category_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                videoId, categoryId
        );
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
