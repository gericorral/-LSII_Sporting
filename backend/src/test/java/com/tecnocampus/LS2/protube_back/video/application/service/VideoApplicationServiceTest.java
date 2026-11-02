package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase.VideoPage;
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

        assertEquals(new VideoPage(videos, 0, 12, 1, 1, false), videoApplicationService.listVideos(0, 12));
    }

    @Test
    void returnsRequestedPage() {
        List<VideoSummary> videos = List.of(
                new VideoSummary("1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4"),
                new VideoSummary("2", "Video 2", "Description", 31, "/media/2.webp", "/media/2.mp4")
        );
        when(videoCatalogPort.findAll()).thenReturn(videos);

        assertEquals(new VideoPage(List.of(videos.get(1)), 1, 1, 2, 2, false),
                videoApplicationService.listVideos(1, 1));
        assertEquals(new VideoPage(List.of(videos.get(0)), 0, 1, 2, 2, true),
                videoApplicationService.listVideos(0, 1));
    }

    @Test
    void returnsEmptyPageForEmptyCatalog() {
        when(videoCatalogPort.findAll()).thenReturn(List.of());

        assertEquals(new VideoPage(List.of(), 0, 12, 0, 0, false),
                videoApplicationService.listVideos(0, 12));
    }

    @Test
    void rejectsInvalidPageArguments() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> videoApplicationService.listVideos(-1, 12)
        );
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> videoApplicationService.listVideos(0, 101)
        );
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> videoApplicationService.listVideos(0, 0)
        );
    }

    @Test
    void getsVideoThroughTheOutputPort() {
        VideoSummary video = new VideoSummary(
                "1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4"
        );
        when(videoCatalogPort.findById("1")).thenReturn(Optional.of(video));

        assertEquals(Optional.of(video), videoApplicationService.getVideo("1"));
    }

    @Test
    void searchesThroughTheOutputPortAndPaginatesResults() {
        List<VideoSummary> videos = List.of(
                new VideoSummary("1", "Travel", "", 30, "/media/1.webp", "/media/1.mp4"),
                new VideoSummary("2", "Travel guide", "", 31, "/media/2.webp", "/media/2.mp4")
        );
        when(videoCatalogPort.search("travel")).thenReturn(videos);

        assertEquals(new VideoPage(List.of(videos.get(1)), 1, 1, 2, 2, false),
                videoApplicationService.listVideos(1, 1, " travel "));
    }
}
