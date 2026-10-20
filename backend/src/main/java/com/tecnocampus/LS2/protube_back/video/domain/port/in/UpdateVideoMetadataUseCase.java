package com.tecnocampus.LS2.protube_back.video.domain.port.in;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

import java.util.List;
import java.util.UUID;

public interface UpdateVideoMetadataUseCase {

    VideoSummary update(String id, UUID ownerId, String title, String description,
                        List<String> tags, String category);

    class InvalidMetadataException extends RuntimeException {
        public InvalidMetadataException(String message) {
            super(message);
        }
    }

    class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String message) {
            super(message);
        }
    }
}
