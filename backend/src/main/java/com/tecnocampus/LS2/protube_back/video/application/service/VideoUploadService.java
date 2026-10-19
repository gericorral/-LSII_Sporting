package com.tecnocampus.LS2.protube_back.video.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VideoUploadService {
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;
    private static final String MP4_CONTENT_TYPE = "video/mp4";

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public VideoUploadService(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
    }

    public VideoSummary upload(String title, String description, MultipartFile file, UUID ownerId) {
        validate(title, description, file);
        String id = UUID.randomUUID().toString();
        List<Path> temporaryFiles = new ArrayList<>();
        List<Path> publishedFiles = List.of(
                storeDirectory.resolve(id + ".mp4"),
                storeDirectory.resolve(id + ".png"),
                storeDirectory.resolve(id + ".json")
        );

        try {
            Files.createDirectories(storeDirectory);
            Path videoTemporary = temporaryPath(id, ".mp4");
            Path thumbnailTemporary = temporaryPath(id, ".png");
            Path metadataTemporary = temporaryPath(id, ".json");
            temporaryFiles.addAll(List.of(videoTemporary, thumbnailTemporary, metadataTemporary));

            file.transferTo(videoTemporary);
            writeThumbnail(thumbnailTemporary);
            objectMapper.writeValue(metadataTemporary.toFile(), new UploadMetadata(
                    title.trim(), new Metadata(description.trim()), ownerId
            ));

            moveAtomically(videoTemporary, publishedFiles.get(0));
            moveAtomically(thumbnailTemporary, publishedFiles.get(1));
            moveAtomically(metadataTemporary, publishedFiles.get(2));
            return new VideoSummary(
                    id, title.trim(), description.trim(), null,
                    "/media/" + id + ".png", "/media/" + id + ".mp4"
            );
        } catch (IOException | RuntimeException exception) {
            deleteFiles(temporaryFiles);
            deleteFiles(publishedFiles);
            throw new UploadFailedException("Unable to publish the video", exception);
        }
    }

    private void validate(String title, String description, MultipartFile file) {
        if (title == null || title.isBlank() || title.trim().length() > 255) {
            throw new InvalidUploadException("Title is required and must contain at most 255 characters.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidUploadException("Description is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidUploadException("A video file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidUploadException("Video files must not exceed 100 MB.");
        }
        if (!MP4_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
            throw new InvalidUploadException("Only MP4 video files are supported.");
        }
    }

    private Path temporaryPath(String id, String extension) {
        return storeDirectory.resolve("." + id + extension + ".uploading");
    }

    private void writeThumbnail(Path path) throws IOException {
        BufferedImage image = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(40, 44, 52));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(new Color(97, 218, 251));
            graphics.fillOval(280, 130, 80, 80);
        } finally {
            graphics.dispose();
        }
        if (!ImageIO.write(image, "png", path.toFile())) {
            throw new IOException("PNG thumbnail writer is unavailable");
        }
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
            } catch (IOException ignored) {
                // The original publication error is more useful to the caller.
            }
        });
    }

    private record UploadMetadata(String title, Metadata meta, UUID ownerId) {
    }

    private record Metadata(String description) {
    }

    public static class InvalidUploadException extends RuntimeException {
        public InvalidUploadException(String message) {
            super(message);
        }
    }

    public static class UploadFailedException extends RuntimeException {
        public UploadFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
