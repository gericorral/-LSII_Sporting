package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.RegisterVideoViewUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoViewPort;
import org.springframework.stereotype.Service;

@Service
public class VideoViewService implements RegisterVideoViewUseCase {
    private final VideoViewPort videoViewPort;

    public VideoViewService(VideoViewPort videoViewPort) {
        this.videoViewPort = videoViewPort;
    }

    @Override
    public void register(String videoId) {
        if (videoId == null || videoId.isBlank() || videoId.contains("/") || videoId.contains("\\")
                || videoId.contains("..")) {
            throw new VideoNotFoundException("Video not found.");
        }
        if (!videoViewPort.incrementViews(videoId)) {
            throw new VideoNotFoundException("Video not found.");
        }
    }
}
