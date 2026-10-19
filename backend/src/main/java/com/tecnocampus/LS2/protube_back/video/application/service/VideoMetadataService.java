package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VideoMetadataService {
    private final VideoCatalogPort videoCatalogPort;

    public VideoMetadataService(VideoCatalogPort videoCatalogPort) {
        this.videoCatalogPort = videoCatalogPort;
    }

    public VideoSummary update(
            String id,
            UUID ownerId,
            String title,
            String description,
            List<String> tags,
            String category
    ) {
        if (id == null || id.isBlank()) {
            throw new InvalidMetadataException("A video id is required.");
        }
        if (title == null || title.isBlank() || title.trim().length() > 255) {
            throw new InvalidMetadataException("Title is required and must contain at most 255 characters.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidMetadataException("Description is required.");
        }
        List<String> normalizedTags = tags == null ? List.of() : tags.stream()
                .map(tag -> tag == null ? "" : tag.trim())
                .filter(tag -> !tag.isBlank())
                .distinct()
                .limit(20)
                .toList();
        String normalizedCategory = category == null ? "" : category.trim();
        if (normalizedCategory.length() > 80) {
            throw new InvalidMetadataException("Category must contain at most 80 characters.");
        }
        return videoCatalogPort.updateMetadata(
                        id, ownerId, title.trim(), description.trim(), normalizedTags, normalizedCategory
                )
                .orElseThrow(() -> new VideoNotFoundException("Video not found or not owned by the user."));
    }

    public static class InvalidMetadataException extends RuntimeException {
        public InvalidMetadataException(String message) {
            super(message);
        }
    }

    public static class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String message) {
            super(message);
        }
    }
}
