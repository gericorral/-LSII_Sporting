package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileVideoCatalogAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void listsVideosWithCompleteMetadataAndMedia(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("1.json"), """
                {"title":"Test video","duration":42,"meta":{"description":"A description"}}
                """);
        Files.createFile(store.resolve("1.mp4"));
        Files.createFile(store.resolve("1.webp"));

        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(List.of(new VideoSummary(
                "1", "Test video", "A description", 42, "/media/1.webp", "/media/1.mp4"
        )), adapter.findAll());
    }

    @Test
    void skipsVideosWithInvalidMetadataOrMissingMedia(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("invalid.json"), "{\"meta\":{}}");
        Files.writeString(store.resolve("missing-media.json"), "{\"title\":\"Missing media\"}");
        Files.createFile(store.resolve("invalid.mp4"));
        Files.createFile(store.resolve("invalid.webp"));

        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(List.of(), adapter.findAll());
    }
}
