package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.VideoFile;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoPublicationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
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
    public VideoSummary publish(String id, String title, String description, UUID ownerId, VideoFile video) {
        List<Path> published = List.of(
                storeDirectory.resolve(id + ".mp4"),
                storeDirectory.resolve(id + ".png"),
                storeDirectory.resolve(id + ".json")
        );
        List<Path> temporary = List.of(
                temporaryPath(id, ".mp4"),
                temporaryPath(id, ".png"),
                temporaryPath(id, ".json")
        );
        try {
            Files.createDirectories(storeDirectory);
            try (var content = video.content()) {
                Files.copy(content, temporary.get(0), StandardCopyOption.REPLACE_EXISTING);
            }
            writeThumbnail(temporary.get(1));
            objectMapper.writeValue(temporary.get(2).toFile(),
                    new UploadMetadata(title, new Metadata(description), ownerId));
            for (int index = 0; index < temporary.size(); index++) {
                moveAtomically(temporary.get(index), published.get(index));
            }
            return new VideoSummary(
                    id, title, description, null, "/media/" + id + ".png", "/media/" + id + ".mp4"
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
            } catch (IOException cleanupException) {
                throw new IllegalStateException("Unable to clean up video publication", cleanupException);
            }
        });
    }

    private record UploadMetadata(String title, Metadata meta, UUID ownerId) {
    }

    private record Metadata(String description) {
    }
}
