package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UpdateVideoMetadataUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase.VideoPage;
import jakarta.servlet.http.HttpSession;
import org.springframework.mock.web.MockMultipartFile;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoControllerTest {

    private final ListVideosUseCase listVideosUseCase = mock(ListVideosUseCase.class);
    private final UploadVideoUseCase uploadVideoUseCase = mock(UploadVideoUseCase.class);
    private final UpdateVideoMetadataUseCase updateVideoMetadataUseCase = mock(UpdateVideoMetadataUseCase.class);
    private final VideoController videoController = new VideoController(
            listVideosUseCase, uploadVideoUseCase, updateVideoMetadataUseCase
    );

    @Test
    void returnsVideosFromTheInputPort() {
        when(listVideosUseCase.listVideos(0, 12)).thenReturn(new VideoPage(List.of(
                new VideoSummary("1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4")
        ), 0, 12, 1, 1, false));

        assertEquals(List.of(
                new VideoSummary("1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4")
        ), videoController.getVideos(0, 12).getBody().content());
    }

    @Test
    void returnsVideoFromTheInputPort() {
        VideoSummary video = new VideoSummary(
                "1", "Video 1", "Description", 30, "/media/1.webp", "/media/1.mp4"
        );
        when(listVideosUseCase.getVideo("1")).thenReturn(Optional.of(video));

        assertEquals(video, videoController.getVideo("1").getBody());
    }

    @Test
    void returnsNotFoundWhenVideoDoesNotExist() {
        when(listVideosUseCase.getVideo("missing")).thenReturn(Optional.empty());

        assertEquals(404, videoController.getVideo("missing").getStatusCode().value());
    }

    @Test
    void rejectsInvalidPaginationArguments() {
        assertEquals(400, videoController.getVideos(-1, 12).getStatusCode().value());
        assertEquals(400, videoController.getVideos(0, 101).getStatusCode().value());
        assertEquals(400, videoController.getVideos(0, 0).getStatusCode().value());
    }

    @Test
    void rejectsUnauthenticatedUpload() {
        HttpSession session = mock(HttpSession.class);

        assertEquals(401, videoController.uploadVideo(
                "Title", "Description",
                new MockMultipartFile("video", "video.mp4", "video/mp4", new byte[] {1}),
                session
        ).getStatusCode().value());
    }

    @Test
    void uploadsForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        VideoSummary uploaded = new VideoSummary(
                "id", "Title", "Description", null, "/media/id.png", "/media/id.mp4"
        );
        when(uploadVideoUseCase.upload(
                org.mockito.ArgumentMatchers.eq("Title"),
                org.mockito.ArgumentMatchers.eq("Description"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(userId)
        )).thenReturn(uploaded);

        assertEquals(201, videoController.uploadVideo(
                "Title", "Description",
                new MockMultipartFile("video", "video.mp4", "video/mp4", new byte[] {1}),
                session
        ).getStatusCode().value());
    }

    @Test
    void updatesVideoForAuthenticatedOwner() {
        UUID userId = UUID.randomUUID();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        VideoSummary updated = new VideoSummary(
                "1", "New", "Description", null, "/media/1.webp", "/media/1.mp4", userId, List.of(), ""
        );
        when(updateVideoMetadataUseCase.update("1", userId, "New", "Description", List.of(), ""))
                .thenReturn(updated);

        assertEquals(200, videoController.updateVideo(
                "1", new VideoController.UpdateVideoRequest("New", "Description", List.of(), ""), session
        ).getStatusCode().value());
    }

    @Test
    void deniesUnauthenticatedMetadataUpdate() {
        HttpSession session = mock(HttpSession.class);

        assertEquals(401, videoController.updateVideo(
                "1", new VideoController.UpdateVideoRequest("New", "Description", List.of(), ""), session
        ).getStatusCode().value());
    }

    @Test
    void deniesMetadataUpdateForAnotherOwner() {
        UUID userId = UUID.randomUUID();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        when(updateVideoMetadataUseCase.update("1", userId, "New", "Description", List.of(), ""))
                .thenThrow(new UpdateVideoMetadataUseCase.VideoNotFoundException("not owner"));

        assertEquals(403, videoController.updateVideo(
                "1", new VideoController.UpdateVideoRequest("New", "Description", List.of(), ""), session
        ).getStatusCode().value());
    }

    @Test
    void rejectsInvalidMetadataUpdate() {
        UUID userId = UUID.randomUUID();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        when(updateVideoMetadataUseCase.update("1", userId, "", "Description", List.of(), ""))
                .thenThrow(new UpdateVideoMetadataUseCase.InvalidMetadataException("Title is required."));

        assertEquals(400, videoController.updateVideo(
                "1", new VideoController.UpdateVideoRequest("", "Description", List.of(), ""), session
        ).getStatusCode().value());
    }
}
