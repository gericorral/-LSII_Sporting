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
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.nio.file.StandardCopyOption;
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
                    .sorted(Comparator.comparing(this::videoModificationTime)
                            .reversed()
                            .thenComparing(path -> path.getFileName().toString()))
                    .map(this::readVideo)
                    .flatMap(java.util.Optional::stream)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read video store: " + storeDirectory, exception);
        }

    }

    @Override
    public List<VideoSummary> search(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return findAll();
        }
        return findAll().stream()
                .map(video -> new ScoredVideo(video, relevance(video, normalizedQuery)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredVideo::score).reversed()
                        .thenComparing(scored -> scored.video().title(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(scored -> scored.video().id()))
                .map(ScoredVideo::video)
                .toList();
    }

    private int relevance(VideoSummary video, String query) {
        String title = video.title().toLowerCase(java.util.Locale.ROOT);
        String description = video.description().toLowerCase(java.util.Locale.ROOT);
        int score = 0;
        if (title.equals(query)) score += 1000;
        else if (title.contains(query)) score += 500;
        if (description.contains(query)) score += 100;
        if (video.tags().stream().map(tag -> tag.toLowerCase(java.util.Locale.ROOT))
                .anyMatch(tag -> tag.contains(query))) {
            score += 50;
        }
        return score;
    }

    private record ScoredVideo(VideoSummary video, int score) {
    }

    private FileTime videoModificationTime(Path metadataPath) {
        Path videoPath = storeDirectory.resolve(
                metadataPath.getFileName().toString().replaceFirst("\\.json$", ".mp4")
        );
        try {
            return Files.getLastModifiedTime(videoPath);
        } catch (IOException exception) {
            return FileTime.fromMillis(Long.MIN_VALUE);
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
        if (!Files.isRegularFile(thumbnailPath)) {
            thumbnailPath = storeDirectory.resolve(id + ".png");
        }

        if (!Files.isRegularFile(videoPath) || !Files.isRegularFile(thumbnailPath)) {
            LOG.warn("Skipping video {} because its media files are incomplete", id);
            return java.util.Optional.empty();
        }

        try {
            JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
            String title = requiredText(metadata, "title");
            String description = metadata.path("meta").path("description").asText("");
            Integer duration = metadata.hasNonNull("duration") ? metadata.get("duration").asInt() : null;
            UUID ownerId = metadata.hasNonNull("ownerId")
                    ? UUID.fromString(metadata.get("ownerId").asText()) : null;
            List<String> tags = new ArrayList<>();
            metadata.path("tags").forEach(node -> tags.add(node.asText()));
            String category = metadata.path("category").asText("");
            return java.util.Optional.of(new VideoSummary(
                    id,
                    title,
                    description,
                    duration,
                    "/media/" + id + "." + extension(thumbnailPath),
                    "/media/" + id + ".mp4",
                    ownerId,
                    List.copyOf(tags),
                    category
            ));
        } catch (IOException | IllegalArgumentException exception) {
            LOG.warn("Skipping invalid video metadata {}", metadataPath, exception);
            return java.util.Optional.empty();
        }
    }

    @Override
    public Optional<VideoSummary> updateMetadata(
                String id,
                UUID ownerId,
                String title,
                String description,
                List<String> tags,
                String category
        ) {
            Optional<VideoSummary> existing = findById(id);
            if (existing.isEmpty() || ownerId == null || !ownerId.equals(existing.get().ownerId())) {
                return Optional.empty();
            }
            Path metadataPath = storeDirectory.resolve(id + ".json");
            Path temporaryPath = storeDirectory.resolve("." + id + ".json.editing");
            try {
                JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
                var updated = objectMapper.createObjectNode();
                updated.put("title", title);
                var meta = updated.putObject("meta");
                meta.put("description", description);
                updated.put("ownerId", ownerId.toString());
                updated.put("category", category);
                updated.set("tags", objectMapper.valueToTree(tags));
                if (metadata.has("analytics")) {
                    updated.set("analytics", metadata.get("analytics"));
                }
                if (metadata.hasNonNull("duration")) {
                    updated.set("duration", metadata.get("duration"));
                }
                objectMapper.writeValue(temporaryPath.toFile(), updated);
                try {
                    Files.move(temporaryPath, metadataPath, StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                    Files.move(temporaryPath, metadataPath, StandardCopyOption.REPLACE_EXISTING);
                }
                return findById(id);
            } catch (IOException | RuntimeException exception) {
                try {
                    Files.deleteIfExists(temporaryPath);
                } catch (IOException cleanupException) {
                    LOG.warn("Unable to remove temporary metadata {}", temporaryPath, cleanupException);
                }
                throw new IllegalStateException("Unable to update video metadata: " + id, exception);
            }
    }

    private String extension(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String requiredText(JsonNode metadata, String field) {
        String value = metadata.path(field).asText("").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing required metadata field: " + field);
        }
        return value;
    }
}
