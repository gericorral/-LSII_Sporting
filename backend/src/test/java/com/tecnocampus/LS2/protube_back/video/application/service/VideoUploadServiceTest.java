package com.tecnocampus.LS2.protube_back.video.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoUploadServiceTest {

    @Test
    void publishesVideoMetadataAndThumbnail(@TempDir Path store) throws Exception {
        VideoUploadService service = new VideoUploadService(store.toString(), new ObjectMapper());

        var result = service.upload(
                " Uploaded title ", " Description ",
                new MockMultipartFile("video", "sample.mp4", "video/mp4", new byte[] {1, 2, 3}),
                UUID.randomUUID()
        );

        assertEquals("Uploaded title", result.title());
        assertEquals("Description", result.description());
        try (var files = Files.list(store)) {
            assertEquals(3, files.count());
        }
        assertEquals("Uploaded title", new ObjectMapper()
                .readTree(store.resolve(result.id() + ".json").toFile()).path("title").asText());
        assertEquals("Description", new ObjectMapper()
                .readTree(store.resolve(result.id() + ".json").toFile()).path("meta").path("description").asText());
        assertEquals(true, Files.isRegularFile(store.resolve(result.id() + ".png")));
    }

    @Test
    void rejectsMissingMetadataAndUnsupportedFiles(@TempDir Path store) throws Exception {
        VideoUploadService service = new VideoUploadService(store.toString(), new ObjectMapper());

        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "", "Description",
                new MockMultipartFile("video", "sample.mp4", "video/mp4", new byte[] {1}),
                UUID.randomUUID()
        ));
        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "Title", "Description",
                new MockMultipartFile("video", "sample.avi", "video/x-msvideo", new byte[] {1}),
                UUID.randomUUID()
        ));
        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "Title", "", new MockMultipartFile("video", "sample.mp4", "video/mp4", new byte[] {1}),
                UUID.randomUUID()
        ));
        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "Title", "Description", null, UUID.randomUUID()
        ));
        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "Title", "Description", new MockMultipartFile("video", "sample.mp4", "video/mp4", new byte[0]),
                UUID.randomUUID()
        ));
        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "x".repeat(256), "Description",
                new MockMultipartFile("video", "sample.mp4", "video/mp4", new byte[] {1}),
                UUID.randomUUID()
        ));
        var oversized = mock(org.springframework.web.multipart.MultipartFile.class);
        when(oversized.isEmpty()).thenReturn(false);
        when(oversized.getSize()).thenReturn(100L * 1024 * 1024 + 1);
        assertThrows(VideoUploadService.InvalidUploadException.class, () -> service.upload(
                "Title", "Description", oversized, UUID.randomUUID()
        ));
        if (Files.exists(store)) {
            try (var files = Files.list(store)) {
                assertEquals(0, files.count());
            }
        }
    }

    @Test
    void wrapsPublicationFailureAndCleansFiles(@TempDir Path store) throws Exception {
        VideoUploadService service = new VideoUploadService(store.toString(), new ObjectMapper());
        var file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1L);
        when(file.getContentType()).thenReturn("video/mp4");
        doThrow(new java.io.IOException("disk full")).when(file).transferTo(any(Path.class));

        assertThrows(VideoUploadService.UploadFailedException.class, () -> service.upload(
                "Title", "Description", file, UUID.randomUUID()
        ));
        try (var files = Files.list(store)) {
            assertEquals(0, files.count());
        }
    }
}
