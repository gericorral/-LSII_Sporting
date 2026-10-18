package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoApplicationServiceTest {

    private final VideoCatalogPort videoCatalogPort = mock(VideoCatalogPort.class);
    private final VideoApplicationService videoApplicationService =
            new VideoApplicationService(videoCatalogPort);

    @Test
    void listsVideosThroughTheOutputPort() {
        List<VideoSummary> videos = List.of(
                new VideoSummary("1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4")
        );
        when(videoCatalogPort.findAll()).thenReturn(videos);

        assertEquals(videos, videoApplicationService.listVideos());
    }

    @Test
    void getsVideoThroughTheOutputPort() {
        VideoSummary video = new VideoSummary(
                "1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4"
        );
        when(videoCatalogPort.findById("1")).thenReturn(Optional.of(video));

        assertEquals(Optional.of(video), videoApplicationService.getVideo("1"));
    }
}
