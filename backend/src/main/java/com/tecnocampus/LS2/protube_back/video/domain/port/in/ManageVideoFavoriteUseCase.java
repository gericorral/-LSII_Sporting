package com.tecnocampus.LS2.protube_back.video.domain.port.in;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoFavorite;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

import java.util.List;
import java.util.UUID;

public interface ManageVideoFavoriteUseCase {
    VideoFavorite getFavorite(String videoId, UUID userId);

    VideoFavorite addFavorite(String videoId, UUID userId);

    VideoFavorite removeFavorite(String videoId, UUID userId);

    List<VideoSummary> listFavorites(UUID userId);

    class AuthenticationRequiredException extends RuntimeException {
        public AuthenticationRequiredException() {
            super("Authentication is required.");
        }
    }

    class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String videoId) {
            super("Video not found: " + videoId);
        }
    }
}
