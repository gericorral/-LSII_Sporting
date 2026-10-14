package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoApplicationService implements ListVideosUseCase {

    private final VideoCatalogPort videoCatalogPort;

    public VideoApplicationService(VideoCatalogPort videoCatalogPort) {
        this.videoCatalogPort = videoCatalogPort;
    }

    @Override
    public List<String> listVideos() {
        return videoCatalogPort.findAll();
    }
}
