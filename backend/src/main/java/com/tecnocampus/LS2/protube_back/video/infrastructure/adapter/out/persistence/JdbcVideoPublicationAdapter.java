package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.VideoFile;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoPublicationPort;
import com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.storage.FileVideoPublicationAdapter;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Primary
public class JdbcVideoPublicationAdapter implements VideoPublicationPort {
    private final FileVideoPublicationAdapter fileStorage;
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoPublicationAdapter(FileVideoPublicationAdapter fileStorage, JdbcTemplate jdbcTemplate) {
        this.fileStorage = fileStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public VideoSummary publish(String id, String title, String description, UUID ownerId, VideoFile video,
                                UploadVideoUseCase.ThumbnailFile thumbnail) {
        UUID videoId = UUID.fromString(id);
        fileStorage.publish(id, title, description, ownerId, video, thumbnail);
        String thumbnailExtension = thumbnailExtension(thumbnail.contentType());
        try {
            jdbcTemplate.update("""
                    INSERT INTO videos (id, owner_id, title, description, slug, source_type,
                                        status, visibility, published_at)
                    VALUES (?, ?, ?, ?, ?, 'USER_UPLOAD', 'PUBLISHED', 'PUBLIC', CURRENT_TIMESTAMP)
                    """, videoId, ownerId, title, description, id);
            jdbcTemplate.update("""
                    INSERT INTO video_assets (id, video_id, asset_type, storage_provider, storage_key, mime_type)
                    VALUES (?, ?, 'VIDEO', 'LOCAL', ?, 'video/mp4'),
                           (?, ?, 'THUMBNAIL', 'LOCAL', ?, ?)
                    """, UUID.randomUUID(), videoId, id + ".mp4",
                    UUID.randomUUID(), videoId, id + "." + thumbnailExtension, thumbnail.contentType());
            return new VideoSummary(id, title, description, null,
                    "/media/" + id + "." + thumbnailExtension, "/media/" + id + ".mp4",
                    ownerId, java.util.List.of(), "");
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to persist video metadata: " + id, exception);
        }
    }

    private String thumbnailExtension(String contentType) {
        return switch (contentType.toLowerCase(java.util.Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "png";
        };
    }
}
