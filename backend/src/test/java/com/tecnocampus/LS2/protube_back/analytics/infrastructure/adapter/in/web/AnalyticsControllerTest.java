package com.tecnocampus.LS2.protube_back.analytics.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.in.ViewOwnVideoAnalyticsUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsControllerTest {
    private final ViewOwnVideoAnalyticsUseCase useCase = mock(ViewOwnVideoAnalyticsUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final AnalyticsController controller = new AnalyticsController(useCase, session);

    @Test
    void returnsOwnAnalytics() {
        UUID ownerId = UUID.randomUUID();
        List<VideoAnalytics> analytics = List.of(new VideoAnalytics("1", "Video", 5, 1, 2));
        when(session.currentUserId()).thenReturn(ownerId);
        when(useCase.getForUser(ownerId)).thenReturn(analytics);

        assertEquals(analytics, controller.getOwnAnalytics().getBody());
    }

    @Test
    void returnsEmptyAnalyticsForOwnerWithoutVideos() {
        UUID ownerId = UUID.randomUUID();
        when(session.currentUserId()).thenReturn(ownerId);
        when(useCase.getForUser(ownerId)).thenReturn(List.of());

        assertEquals(List.of(), controller.getOwnAnalytics().getBody());
    }

    @Test
    void rejectsUnauthenticatedRequest() {
        when(session.currentUserId()).thenReturn(null);

        assertEquals(401, controller.getOwnAnalytics().getStatusCode().value());
    }
}
