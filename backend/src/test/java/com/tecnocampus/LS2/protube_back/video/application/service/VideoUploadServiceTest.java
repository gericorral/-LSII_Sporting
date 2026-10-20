package com.tecnocampus.LS2.protube_back.video.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.storage.FileVideoPublicationAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoUploadServiceTest {

    @Test
    void publishesVideoMetadataAndThumbnail(@TempDir Path store) throws Exception {
        VideoUploadService service = service(store);

        var result = service.upload(
                " Uploaded title ", " Description ", file(new byte[] {1, 2, 3}), UUID.randomUUID()
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
    void rejectsMissingMetadataAndUnsupportedFiles(@TempDir Path store) {
        VideoUploadService service = service(store);

        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "", "Description", file(new byte[] {1}), UUID.randomUUID()));
        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "Title", "Description",
                new UploadVideoUseCase.VideoFile(new ByteArrayInputStream(new byte[] {1}), 1, "video/x-msvideo"),
                UUID.randomUUID()));
        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "Title", "", file(new byte[] {1}), UUID.randomUUID()));
        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "Title", "Description", null, UUID.randomUUID()));
        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "Title", "Description", file(new byte[0]), UUID.randomUUID()));
        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "x".repeat(256), "Description", file(new byte[] {1}), UUID.randomUUID()));
        assertThrows(UploadVideoUseCase.InvalidUploadException.class, () -> service.upload(
                "Title", "Description",
                new UploadVideoUseCase.VideoFile(new ByteArrayInputStream(new byte[] {1}),
                        100L * 1024 * 1024 + 1, "video/mp4"),
                UUID.randomUUID()));
    }

    @Test
    void wrapsPublicationFailure(@TempDir Path store) {
        VideoUploadService service = new VideoUploadService(
                (id, title, description, ownerId, video) -> {
                    throw new IllegalStateException("disk full");
                }
        );

        assertThrows(UploadVideoUseCase.UploadFailedException.class, () -> service.upload(
                "Title", "Description", file(new byte[] {1}), UUID.randomUUID()));
    }

    private VideoUploadService service(Path store) {
        return new VideoUploadService(new FileVideoPublicationAdapter(store.toString(), new ObjectMapper()));
    }

    private UploadVideoUseCase.VideoFile file(byte[] content) {
        return new UploadVideoUseCase.VideoFile(new ByteArrayInputStream(content), content.length, "video/mp4");
    }
}
