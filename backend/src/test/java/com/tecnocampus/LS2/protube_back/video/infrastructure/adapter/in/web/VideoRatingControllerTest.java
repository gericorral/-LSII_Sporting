package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.RateVideoUseCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VideoRatingControllerTest {
    private final RateVideoUseCase useCase = mock(RateVideoUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final VideoRatingController controller = new VideoRatingController(useCase, session);
    private final UUID userId = UUID.randomUUID();
    private final VideoRating rating = new VideoRating("video", BigDecimal.valueOf(4), 1, 4);

    @Test
    void readsPublicRatingWithOptionalSessionIdentity() {
        when(useCase.getRating("video", null)).thenReturn(rating);

        assertEquals(200, controller.getRating("video").getStatusCode().value());
        verify(useCase).getRating("video", null);
    }

    @Test
    void savesRatingUsingSessionIdentity() {
        when(session.currentUserId()).thenReturn(userId);
        when(useCase.rate("video", userId, 4)).thenReturn(rating);

        assertEquals(200, controller.rate("video", new VideoRatingController.RatingRequest(4))
                .getStatusCode().value());
        verify(useCase).rate("video", userId, 4);
    }

    @Test
    void protectsAndValidatesRatingEndpoint() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.rate("video", new VideoRatingController.RatingRequest(4))
                .getStatusCode().value());
        assertEquals(401, controller.rate("video", null).getStatusCode().value());

        when(session.currentUserId()).thenReturn(userId);
        when(useCase.rate("video", userId, 0))
                .thenThrow(new RateVideoUseCase.InvalidRatingException("Rating must be between 1 and 5."));
        assertEquals(400, controller.rate("video", null).getStatusCode().value());
        assertEquals(400, controller.rate("video", new VideoRatingController.RatingRequest(0))
                .getStatusCode().value());

        when(session.currentUserId()).thenReturn(null);
        when(useCase.getRating("missing", null))
                .thenThrow(new RateVideoUseCase.VideoNotFoundException("missing"));
        assertEquals(404, controller.getRating("missing").getStatusCode().value());
    }
}
