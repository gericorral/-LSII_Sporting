package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoControllerTest {

    private final ListVideosUseCase listVideosUseCase = mock(ListVideosUseCase.class);
    private final VideoController videoController = new VideoController(listVideosUseCase);

    @Test
    void returnsVideosFromTheInputPort() {
        when(listVideosUseCase.listVideos()).thenReturn(List.of(
                new VideoSummary("1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4")
        ));

        assertEquals(List.of(
                new VideoSummary("1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4")
        ), videoController.getVideos().getBody());
    }

    @Test
    void returnsVideoFromTheInputPort() {
        VideoSummary video = new VideoSummary(
                "1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4"
        );
        when(listVideosUseCase.getVideo("1")).thenReturn(Optional.of(video));

        assertEquals(video, videoController.getVideo("1").getBody());
    }

    @Test
    void returnsNotFoundWhenVideoDoesNotExist() {
        when(listVideosUseCase.getVideo("missing")).thenReturn(Optional.empty());

        assertEquals(404, videoController.getVideo("missing").getStatusCode().value());
    }
}
