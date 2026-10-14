package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoApplicationServiceTest {

    private final VideoCatalogPort videoCatalogPort = mock(VideoCatalogPort.class);
    private final VideoApplicationService videoApplicationService =
            new VideoApplicationService(videoCatalogPort);

    @Test
    void listsVideosThroughTheOutputPort() {
        when(videoCatalogPort.findAll()).thenReturn(List.of("video 1", "video 2"));

        assertEquals(List.of("video 1", "video 2"), videoApplicationService.listVideos());
    }
}
