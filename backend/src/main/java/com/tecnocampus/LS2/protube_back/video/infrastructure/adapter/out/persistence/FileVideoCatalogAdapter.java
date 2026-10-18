package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class FileVideoCatalogAdapter implements VideoCatalogPort {

    private static final Logger LOG = LoggerFactory.getLogger(FileVideoCatalogAdapter.class);

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public FileVideoCatalogAdapter(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
    }

    @Override
    public List<VideoSummary> findAll() {
        if (!Files.isDirectory(storeDirectory)) {
            LOG.warn("Video store directory does not exist: {}", storeDirectory);
            return List.of();
        }

        try (Stream<Path> files = Files.list(storeDirectory)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::readVideo)
                    .flatMap(java.util.Optional::stream)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read video store: " + storeDirectory, exception);
        }
    }

    @Override
    public Optional<VideoSummary> findById(String id) {
        if (id == null || id.isBlank() || id.contains("/") || id.contains("\\") || id.contains("..")) {
            return Optional.empty();
        }

        Path metadataPath = storeDirectory.resolve(id + ".json").normalize();
        if (!metadataPath.getParent().equals(storeDirectory.normalize())) {
            return Optional.empty();
        }
        return Files.isRegularFile(metadataPath) ? readVideo(metadataPath) : Optional.empty();
    }

    private java.util.Optional<VideoSummary> readVideo(Path metadataPath) {
        String fileName = metadataPath.getFileName().toString();
        String id = fileName.substring(0, fileName.length() - ".json".length());
        Path videoPath = storeDirectory.resolve(id + ".mp4");
        Path thumbnailPath = storeDirectory.resolve(id + ".webp");

        if (!Files.isRegularFile(videoPath) || !Files.isRegularFile(thumbnailPath)) {
            LOG.warn("Skipping video {} because its media files are incomplete", id);
            return java.util.Optional.empty();
        }

        try {
            JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
            String title = requiredText(metadata, "title");
            String description = metadata.path("meta").path("description").asText("");
            Integer duration = metadata.hasNonNull("duration") ? metadata.get("duration").asInt() : null;
            return java.util.Optional.of(new VideoSummary(
                    id,
                    title,
                    description,
                    duration,
                    "/media/" + id + ".webp",
                    "/media/" + id + ".mp4"
            ));
        } catch (IOException | IllegalArgumentException exception) {
            LOG.warn("Skipping invalid video metadata {}", metadataPath, exception);
            return java.util.Optional.empty();
        }
    }

    private String requiredText(JsonNode metadata, String field) {
        String value = metadata.path(field).asText("").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing required metadata field: " + field);
        }
        return value;
    }
}
