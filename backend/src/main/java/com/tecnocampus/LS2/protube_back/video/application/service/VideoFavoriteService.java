package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoFavorite;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ManageVideoFavoriteUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoFavoritePort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VideoFavoriteService implements ManageVideoFavoriteUseCase {
    private final VideoFavoritePort videoFavoritePort;
    private final VideoCatalogPort videoCatalogPort;

    public VideoFavoriteService(VideoFavoritePort videoFavoritePort, VideoCatalogPort videoCatalogPort) {
        this.videoFavoritePort = videoFavoritePort;
        this.videoCatalogPort = videoCatalogPort;
    }

    @Override
    public VideoFavorite getFavorite(String videoId, UUID userId) {
        requireUser(userId);
        ensureVideoExists(videoId);
        return new VideoFavorite(videoId, videoFavoritePort.exists(videoId, userId));
    }

    @Override
    public VideoFavorite addFavorite(String videoId, UUID userId) {
        requireUser(userId);
        ensureVideoExists(videoId);
        videoFavoritePort.add(videoId, userId);
        return new VideoFavorite(videoId, true);
    }

    @Override
    public VideoFavorite removeFavorite(String videoId, UUID userId) {
        requireUser(userId);
        ensureVideoExists(videoId);
        videoFavoritePort.remove(videoId, userId);
        return new VideoFavorite(videoId, false);
    }

    @Override
    public List<VideoSummary> listFavorites(UUID userId) {
        requireUser(userId);
        return videoFavoritePort.findVideoIds(userId).stream()
                .map(videoCatalogPort::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new AuthenticationRequiredException();
        }
    }

    private void ensureVideoExists(String videoId) {
        if (videoId == null || videoId.isBlank() || videoCatalogPort.findById(videoId).isEmpty()) {
            throw new VideoNotFoundException(videoId);
        }
    }
}
