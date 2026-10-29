package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoViewPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoViewServiceTest {
    private final VideoViewPort videoViewPort = mock(VideoViewPort.class);
    private final VideoViewService service = new VideoViewService(videoViewPort);

    @Test
    void registersExistingVideo() {
        when(videoViewPort.incrementViews("1")).thenReturn(true);

        assertDoesNotThrow(() -> service.register("1"));
    }

    @Test
    void rejectsInvalidOrMissingVideo() {
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register(null));
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register(""));
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register(" "));
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register("folder/1"));
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register("../1"));
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register("a..b"));
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register("folder\\1"));
        when(videoViewPort.incrementViews("missing")).thenReturn(false);
        assertThrows(VideoViewService.VideoNotFoundException.class, () -> service.register("missing"));
    }
}
