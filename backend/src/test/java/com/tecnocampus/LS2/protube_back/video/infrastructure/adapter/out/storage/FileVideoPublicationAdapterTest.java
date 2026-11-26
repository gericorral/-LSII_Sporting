package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.ThumbnailFile;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.VideoFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class FileVideoPublicationAdapterTest {

    @Test
    void publishesMediaAndMetadataUsingTheMatchingThumbnailExtension(@TempDir Path store) throws IOException {
        FileVideoPublicationAdapter adapter = new FileVideoPublicationAdapter(store.toString(), new ObjectMapper());
        Map<String, String> contentTypes = Map.of(
                "image/jpeg", "jpg",
                "image/webp", "webp",
                "image/gif", "gif",
                "image/png", "png"
        );
        UUID ownerId = UUID.randomUUID();

        for (var entry : contentTypes.entrySet()) {
            String id = entry.getValue();
            var summary = adapter.publish(
                    id, "Title", "Description", ownerId,
                    video("video bytes"), thumbnail("thumbnail bytes", entry.getKey())
            );

            assertEquals("/media/" + id + "." + entry.getValue(), summary.thumbnailUrl());
            assertEquals("/media/" + id + ".mp4", summary.videoUrl());
            assertEquals("video bytes", Files.readString(store.resolve(id + ".mp4")));
            assertEquals("thumbnail bytes", Files.readString(store.resolve(id + "." + entry.getValue())));
            JsonNode metadata = new ObjectMapper().readTree(store.resolve(id + ".json").toFile());
            assertEquals("Title", metadata.path("title").asText());
            assertEquals("Description", metadata.path("meta").path("description").asText());
            assertEquals(ownerId.toString(), metadata.path("ownerId").asText());
            assertEquals(0, metadata.path("analytics").path("views").asInt());
        }
    }

    @Test
    void removesTemporaryFilesWhenAThumbnailCannotBeRead(@TempDir Path store) {
        FileVideoPublicationAdapter adapter = new FileVideoPublicationAdapter(store.toString(), new ObjectMapper());
        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("broken thumbnail");
            }
        };

        assertThrows(IllegalStateException.class, () -> adapter.publish(
                "broken", "Title", "Description", UUID.randomUUID(),
                video("video bytes"), new ThumbnailFile(brokenStream, 0, "image/png")
        ));

        assertFalse(Files.exists(store.resolve(".broken.mp4.uploading")));
        assertFalse(Files.exists(store.resolve("broken.mp4")));
    }

    @Test
    void removesTemporaryFilesWhenMetadataCannotBeWritten(@TempDir Path store) throws IOException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        doThrow(new IOException("metadata failure")).when(mapper).writeValue(any(File.class), any());
        FileVideoPublicationAdapter adapter = new FileVideoPublicationAdapter(store.toString(), mapper);

        assertThrows(IllegalStateException.class, () -> adapter.publish(
                "failed", "Title", "Description", UUID.randomUUID(),
                video("video bytes"), thumbnail("thumbnail bytes", "image/png")
        ));

        assertFalse(Files.exists(store.resolve(".failed.mp4.uploading")));
        assertFalse(Files.exists(store.resolve(".failed.png.uploading")));
        assertFalse(Files.exists(store.resolve("failed.mp4")));
        try (var files = Files.list(store)) {
            assertTrue(files.findAny().isEmpty());
        }
    }

    private VideoFile video(String content) {
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new VideoFile(new ByteArrayInputStream(bytes), bytes.length, "video/mp4");
    }

    private ThumbnailFile thumbnail(String content, String contentType) {
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new ThumbnailFile(new ByteArrayInputStream(bytes), bytes.length, contentType);
    }
}
