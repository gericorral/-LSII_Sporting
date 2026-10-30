package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCommentPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class FileVideoCommentAdapter implements VideoCommentPort {
    private static final Logger LOG = LoggerFactory.getLogger(FileVideoCommentAdapter.class);

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public FileVideoCommentAdapter(
            @Value("${pro_tube.store.dir}") String storeDirectory,
            ObjectMapper objectMapper
    ) {
        this.storeDirectory = Path.of(storeDirectory);
        this.objectMapper = objectMapper;
    }

    @Override
    public synchronized boolean incrementComments(String videoId) {
        Path metadataPath = storeDirectory.resolve(videoId + ".json").normalize();
        if (!metadataPath.getParent().equals(storeDirectory.normalize())
                || !Files.isRegularFile(metadataPath)) {
            return false;
        }
        Path temporaryPath = storeDirectory.resolve("." + videoId + ".json.commenting");
        try {
            JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
            if (metadata == null || !metadata.isObject()) {
                return false;
            }
            ObjectNode analytics = ((ObjectNode) metadata).with("analytics");
            JsonNode comments = analytics.get("comments");
            long currentComments = comments != null && comments.canConvertToLong() && comments.asLong() >= 0
                    ? comments.asLong() : 0;
            if (currentComments == Long.MAX_VALUE) {
                throw new IllegalStateException("Video comment count cannot be increased.");
            }
            analytics.put("comments", currentComments + 1);
            objectMapper.writeValue(temporaryPath.toFile(), metadata);
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
                LOG.warn("Unable to remove temporary comment metadata {}", temporaryPath, cleanupException);
            }
            if (exception instanceof IllegalStateException illegalStateException
                    && "Video comment count cannot be increased.".equals(illegalStateException.getMessage())) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to record comment for video: " + videoId, exception);
        }
    }
}
