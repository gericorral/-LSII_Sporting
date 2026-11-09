package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoFavorite;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ManageVideoFavoriteUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VideoFavoriteControllerTest {
    private final ManageVideoFavoriteUseCase useCase = mock(ManageVideoFavoriteUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final VideoFavoriteController controller = new VideoFavoriteController(useCase, session);
    private final UUID userId = UUID.randomUUID();

    @Test
    void managesFavoriteUsingSessionIdentity() {
        when(session.currentUserId()).thenReturn(userId);
        when(useCase.getFavorite("video", userId)).thenReturn(new VideoFavorite("video", false));
        when(useCase.addFavorite("video", userId)).thenReturn(new VideoFavorite("video", true));
        when(useCase.removeFavorite("video", userId)).thenReturn(new VideoFavorite("video", false));

        assertEquals(200, controller.getFavorite("video").getStatusCode().value());
        assertEquals(200, controller.addFavorite("video").getStatusCode().value());
        assertEquals(200, controller.removeFavorite("video").getStatusCode().value());
        verify(useCase).addFavorite("video", userId);
    }

    @Test
    void protectsAnonymousAndMissingVideos() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.getFavorite("video").getStatusCode().value());
        assertEquals(401, controller.addFavorite("video").getStatusCode().value());
        assertEquals(401, controller.removeFavorite("video").getStatusCode().value());
        assertEquals(401, controller.listFavorites().getStatusCode().value());

        when(session.currentUserId()).thenReturn(userId);
        when(useCase.addFavorite("missing", userId))
                .thenThrow(new ManageVideoFavoriteUseCase.VideoNotFoundException("missing"));
        when(useCase.getFavorite("missing", userId))
                .thenThrow(new ManageVideoFavoriteUseCase.VideoNotFoundException("missing"));
        assertEquals(404, controller.addFavorite("missing").getStatusCode().value());
        assertEquals(404, controller.getFavorite("missing").getStatusCode().value());
        when(useCase.listFavorites(userId)).thenReturn(List.of());
        assertEquals(200, controller.listFavorites().getStatusCode().value());
    }
}
