package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.VideoFile;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.ThumbnailFile;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoPublicationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Component
public class FileVideoPublicationAdapter implements VideoPublicationPort {
    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public FileVideoPublicationAdapter(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
    }

    @Override
    public VideoSummary publish(String id, String title, String description, UUID ownerId, VideoFile video,
                                ThumbnailFile thumbnail) {
        String thumbnailExtension = extensionFor(thumbnail.contentType());
        String thumbnailFileName = id + "." + thumbnailExtension;
        List<Path> published = List.of(
                storeDirectory.resolve(id + ".mp4"),
                storeDirectory.resolve(thumbnailFileName),
                storeDirectory.resolve(id + ".json")
        );
        List<Path> temporary = List.of(
                temporaryPath(id, ".mp4"),
                temporaryPath(id, "." + thumbnailExtension),
                temporaryPath(id, ".json")
        );
        try {
            Files.createDirectories(storeDirectory);
            try (var content = video.content()) {
                Files.copy(content, temporary.get(0), StandardCopyOption.REPLACE_EXISTING);
            }
            try (var content = thumbnail.content()) {
                Files.copy(content, temporary.get(1), StandardCopyOption.REPLACE_EXISTING);
            }
            objectMapper.writeValue(temporary.get(2).toFile(),
                    new UploadMetadata(title, new Metadata(description), ownerId, new Analytics(0, 0, 0)));
            for (int index = 0; index < temporary.size(); index++) {
                moveAtomically(temporary.get(index), published.get(index));
            }
            return new VideoSummary(
                    id, title, description, null, "/media/" + thumbnailFileName, "/media/" + id + ".mp4"
            );
        } catch (IOException | RuntimeException exception) {
            deleteFiles(temporary);
            deleteFiles(published);
            throw new IllegalStateException("Unable to publish the video", exception);
        }
    }

    private Path temporaryPath(String id, String extension) {
        return storeDirectory.resolve("." + id + extension + ".uploading");
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase(java.util.Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "png";
        };
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target);
        }
    }

    private void deleteFiles(List<Path> files) {
        files.forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException cleanupException) {
                throw new IllegalStateException("Unable to clean up video publication", cleanupException);
            }
        });
    }

    private record UploadMetadata(String title, Metadata meta, UUID ownerId, Analytics analytics) {
    }

    private record Metadata(String description) {
    }

    private record Analytics(long views, long comments, long likes) {
    }
}
