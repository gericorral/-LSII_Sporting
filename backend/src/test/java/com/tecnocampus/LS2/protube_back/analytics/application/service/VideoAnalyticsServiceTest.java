package com.tecnocampus.LS2.protube_back.analytics.application.service;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.out.VideoAnalyticsPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoAnalyticsServiceTest {
    private final VideoAnalyticsPort port = mock(VideoAnalyticsPort.class);
    private final VideoAnalyticsService service = new VideoAnalyticsService(port);

    @Test
    void returnsAnalyticsForAuthenticatedOwner() {
        UUID ownerId = UUID.randomUUID();
        List<VideoAnalytics> expected = List.of(new VideoAnalytics("1", "Video", 4, 2, 1));
        when(port.findByOwnerId(ownerId)).thenReturn(expected);

        assertEquals(expected, service.getForUser(ownerId));
    }

    @Test
    void rejectsMissingOwner() {
        assertThrows(IllegalArgumentException.class, () -> service.getForUser(null));
    }

    @Test
    void rejectsInvalidAnalyticsCounters() {
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics(null, "Video", 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics("", "Video", 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics("1", null, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics("1", "", 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics("1", "Video", -1, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics("1", "Video", 0, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoAnalytics("1", "Video", 0, 0, -1));
    }
}
