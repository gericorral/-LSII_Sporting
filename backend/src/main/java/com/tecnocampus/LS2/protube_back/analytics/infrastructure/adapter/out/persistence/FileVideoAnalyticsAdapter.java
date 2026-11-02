package com.tecnocampus.LS2.protube_back.analytics.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.out.VideoAnalyticsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class FileVideoAnalyticsAdapter implements VideoAnalyticsPort {
    private static final Logger LOG = LoggerFactory.getLogger(FileVideoAnalyticsAdapter.class);

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public FileVideoAnalyticsAdapter(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
    }

    @Override
    public List<VideoAnalytics> findByOwnerId(UUID ownerId) {
        if (ownerId == null || !Files.isDirectory(storeDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(storeDirectory)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> readAnalytics(path, ownerId))
                    .flatMap(java.util.Optional::stream)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read analytics from video store: " + storeDirectory, exception);
        }
    }

    private java.util.Optional<VideoAnalytics> readAnalytics(Path metadataPath, UUID ownerId) {
        String fileName = metadataPath.getFileName().toString();
        String videoId = fileName.substring(0, fileName.length() - ".json".length());
        try {
            JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
            if (!ownerId.toString().equals(metadata.path("ownerId").asText(null))) {
                return java.util.Optional.empty();
            }
            String title = metadata.path("title").asText("").trim();
            JsonNode analytics = metadata.path("analytics");
            return java.util.Optional.of(new VideoAnalytics(
                    videoId,
                    title,
                    counter(analytics, "views"),
                    counter(analytics, "comments"),
                    counter(analytics, "likes")
            ));
        } catch (IOException | RuntimeException exception) {
            LOG.warn("Skipping invalid analytics metadata {}", metadataPath, exception);
            return java.util.Optional.empty();
        }
    }

    private long counter(JsonNode analytics, String field) {
        JsonNode value = analytics.path(field);
        return value.canConvertToLong() && value.asLong() >= 0 ? value.asLong() : 0;
    }
}
