package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoFavoritePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoFavoriteServiceTest {
    private final VideoFavoritePort favoritePort = mock(VideoFavoritePort.class);
    private final VideoCatalogPort catalogPort = mock(VideoCatalogPort.class);
    private final VideoFavoriteService service = new VideoFavoriteService(favoritePort, catalogPort);
    private final UUID userId = UUID.randomUUID();

    @Test
    void addsReadsAndRemovesFavoriteForExistingVideo() {
        when(catalogPort.findById("video")).thenReturn(Optional.of(summary("video")));
        when(favoritePort.exists("video", userId)).thenReturn(true);

        assertTrue(service.addFavorite("video", userId).favorited());
        assertTrue(service.getFavorite("video", userId).favorited());
        assertFalse(service.removeFavorite("video", userId).favorited());
        verify(favoritePort).add("video", userId);
        verify(favoritePort).remove("video", userId);
    }

    @Test
    void listsOnlyVideosStillPresentInCatalog() {
        when(favoritePort.findVideoIds(userId)).thenReturn(List.of("one", "missing"));
        when(catalogPort.findById("one")).thenReturn(Optional.of(summary("one")));
        when(catalogPort.findById("missing")).thenReturn(Optional.empty());

        assertEquals(List.of(summary("one")), service.listFavorites(userId));
    }

    @Test
    void requiresAuthenticationAndExistingVideo() {
        assertThrows(VideoFavoriteService.AuthenticationRequiredException.class,
                () -> service.listFavorites(null));
        when(catalogPort.findById("missing")).thenReturn(Optional.empty());
        assertThrows(VideoFavoriteService.VideoNotFoundException.class,
                () -> service.addFavorite("missing", userId));
        assertThrows(VideoFavoriteService.VideoNotFoundException.class,
                () -> service.getFavorite(null, userId));
    }

    private VideoSummary summary(String id) {
        return new VideoSummary(id, "Title", "Description", null, "", "");
    }
}
