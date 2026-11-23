package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoRatingPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoRatingServiceTest {
    private final VideoRatingPort ratingPort = mock(VideoRatingPort.class);
    private final VideoCatalogPort catalogPort = mock(VideoCatalogPort.class);
    private final VideoRatingService service = new VideoRatingService(ratingPort, catalogPort);
    private final UUID userId = UUID.randomUUID();

    @Test
    void readsAndSavesRatingForExistingVideo() {
        VideoRating rating = new VideoRating("video", BigDecimal.valueOf(4.5), 2, 5);
        when(catalogPort.findById("video")).thenReturn(Optional.of(summary("video")));
        when(ratingPort.find("video", userId)).thenReturn(rating);

        assertEquals(rating, service.getRating("video", userId));
        assertEquals(rating, service.rate("video", userId, 5));
        verify(ratingPort).save("video", userId, 5);
    }

    @Test
    void rejectsAnonymousAndOutOfRangeRatings() {
        assertThrows(VideoRatingService.InvalidRatingException.class, () -> service.rate("video", null, 4));
        assertThrows(VideoRatingService.InvalidRatingException.class, () -> service.rate("video", userId, 0));
        assertThrows(VideoRatingService.InvalidRatingException.class, () -> service.rate("video", userId, 6));
    }

    @Test
    void rejectsMissingVideo() {
        when(catalogPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(VideoRatingService.VideoNotFoundException.class,
                () -> service.getRating("missing", null));
        assertThrows(VideoRatingService.VideoNotFoundException.class,
                () -> service.getRating(null, null));
        assertThrows(VideoRatingService.VideoNotFoundException.class,
                () -> service.getRating(" ", null));
    }

    private VideoSummary summary(String id) {
        return new VideoSummary(id, "Title", "Description", null, "", "");
    }
}
