package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoMetadataServiceTest {
    private final VideoCatalogPort catalog = mock(VideoCatalogPort.class);
    private final VideoMetadataService service = new VideoMetadataService(catalog);
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void normalizesAndUpdatesMetadata() {
        VideoSummary updated = new VideoSummary(
                "1", "New title", "New description", null, "/media/1.webp", "/media/1.mp4",
                ownerId, List.of("music", "live"), "Music"
        );
        when(catalog.updateMetadata("1", ownerId, "New title", "New description",
                List.of("music", "live"), "Music")).thenReturn(Optional.of(updated));

        assertEquals(updated, service.update(
                "1", ownerId, " New title ", " New description ",
                List.of("music", "live", "music", " "), " Music "
        ));
    }

    @Test
    void rejectsInvalidMetadata() {
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update(null, ownerId, "Title", "Description", List.of(), ""));
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update("1", ownerId, "", "Description", List.of(), ""));
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update("1", ownerId, null, "Description", List.of(), ""));
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update("1", ownerId, "Title", "", List.of(), ""));
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update("1", ownerId, "Title", null, List.of(), ""));
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update("1", ownerId, "Title", "Description", List.of(), "x".repeat(81)));
        assertThrows(VideoMetadataService.InvalidMetadataException.class,
                () -> service.update("", ownerId, "Title", "Description", List.of(), ""));
    }

    @Test
    void rejectsVideoNotFoundOrNotOwned() {
        when(catalog.updateMetadata(any(), any(), any(), any(), any(), any())).thenReturn(Optional.empty());

        assertThrows(VideoMetadataService.VideoNotFoundException.class,
                () -> service.update("missing", ownerId, "Title", "Description", Arrays.asList((String) null), null));
    }
}
