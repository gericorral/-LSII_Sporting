package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

@Service
public class VideoApplicationService implements ListVideosUseCase {

    private final VideoCatalogPort videoCatalogPort;

    public VideoApplicationService(VideoCatalogPort videoCatalogPort) {
        this.videoCatalogPort = videoCatalogPort;
    }

    @Override
    public List<VideoSummary> listVideos() {
        return videoCatalogPort.findAll();
    }

    public Optional<VideoSummary> getVideo(String id) {
        return videoCatalogPort.findById(id);
    }
}
