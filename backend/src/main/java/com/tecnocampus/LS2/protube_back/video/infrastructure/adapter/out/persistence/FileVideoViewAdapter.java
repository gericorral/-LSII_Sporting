package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoViewPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class FileVideoViewAdapter implements VideoViewPort {
    private static final Logger LOG = LoggerFactory.getLogger(FileVideoViewAdapter.class);

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public FileVideoViewAdapter(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
    }

    @Override
    public synchronized boolean incrementViews(String videoId) {
        Path metadataPath = storeDirectory.resolve(videoId + ".json").normalize();
        if (!metadataPath.getParent().equals(storeDirectory.normalize())
                || !Files.isRegularFile(metadataPath)) {
            return false;
        }
        Path temporaryPath = storeDirectory.resolve("." + videoId + ".json.viewing");
        try {
            JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
            if (metadata == null || !metadata.isObject()) {
                return false;
            }
            ObjectNode updated = (ObjectNode) metadata;
            ObjectNode analytics = updated.with("analytics");
            JsonNode views = analytics.get("views");
            long currentViews = views != null && views.canConvertToLong() && views.asLong() >= 0
                    ? views.asLong() : 0;
            if (currentViews == Long.MAX_VALUE) {
                throw new IllegalStateException("Video view count cannot be increased.");
            }
            analytics.put("views", currentViews + 1);
            objectMapper.writeValue(temporaryPath.toFile(), updated);
            try {
                Files.move(temporaryPath, metadataPath, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporaryPath, metadataPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            try {
                Files.deleteIfExists(temporaryPath);
            } catch (IOException cleanupException) {
                LOG.warn("Unable to remove temporary view metadata {}", temporaryPath, cleanupException);
            }
            if (exception instanceof IllegalStateException illegalStateException
                    && "Video view count cannot be increased.".equals(illegalStateException.getMessage())) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to record view for video: " + videoId, exception);
        }
    }
}
