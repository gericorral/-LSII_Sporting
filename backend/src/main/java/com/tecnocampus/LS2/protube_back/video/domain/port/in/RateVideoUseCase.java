package com.tecnocampus.LS2.protube_back.video.domain.port.in;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;

import java.util.UUID;

public interface RateVideoUseCase {
    VideoRating getRating(String videoId, UUID userId);

    VideoRating rate(String videoId, UUID userId, int rating);

    class InvalidRatingException extends RuntimeException {
        public InvalidRatingException(String message) {
            super(message);
        }
    }

    class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String videoId) {
            super("Video not found: " + videoId);
        }
    }
}
