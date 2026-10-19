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
    public VideoPage listVideos(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        List<VideoSummary> videos = videoCatalogPort.findAll();
        long requestedFromIndex = (long) page * size;
        int fromIndex = (int) Math.min(requestedFromIndex, videos.size());
        int toIndex = Math.min(fromIndex + size, videos.size());
        int totalPages = videos.isEmpty() ? 0 : (videos.size() + size - 1) / size;
        return new VideoPage(
                videos.subList(fromIndex, toIndex),
                page,
                size,
                videos.size(),
                totalPages,
                toIndex < videos.size()
        );
    }

    public Optional<VideoSummary> getVideo(String id) {
        return videoCatalogPort.findById(id);
    }
}
