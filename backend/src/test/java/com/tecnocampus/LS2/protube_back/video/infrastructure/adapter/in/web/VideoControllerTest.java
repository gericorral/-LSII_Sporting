package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoControllerTest {

    private final ListVideosUseCase listVideosUseCase = mock(ListVideosUseCase.class);
    private final VideoController videoController = new VideoController(listVideosUseCase);

    @Test
    void returnsVideosFromTheInputPort() {
        when(listVideosUseCase.listVideos()).thenReturn(List.of("video 1", "video 2"));

        assertEquals(List.of("video 1", "video 2"), videoController.getVideos().getBody());
    }
}
