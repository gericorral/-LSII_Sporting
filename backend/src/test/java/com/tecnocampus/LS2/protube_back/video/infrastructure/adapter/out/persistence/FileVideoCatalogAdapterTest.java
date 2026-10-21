package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    void listsNewestVideoFirst(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("old.json"), "{\"title\":\"Old\",\"meta\":{}}");
        Files.createFile(store.resolve("old.mp4"));
        Files.createFile(store.resolve("old.webp"));
        Files.writeString(store.resolve("new.json"), "{\"title\":\"New\",\"meta\":{}}");
        Files.createFile(store.resolve("new.mp4"));
        Files.createFile(store.resolve("new.webp"));
        Files.setLastModifiedTime(store.resolve("old.mp4"), FileTime.fromMillis(1_000));
        Files.setLastModifiedTime(store.resolve("new.mp4"), FileTime.fromMillis(2_000));

        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(List.of("New", "Old"), adapter.findAll().stream()
                .map(VideoSummary::title)
                .toList());
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

    @Test
    void findsACompleteVideoById(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("1.json"), """
                {"title":"Test video","duration":42,"meta":{"description":"A description"}}
                """);
        Files.createFile(store.resolve("1.mp4"));
        Files.createFile(store.resolve("1.webp"));

        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals("Test video", adapter.findById("1").orElseThrow().title());
    }

    @Test
    void doesNotResolveAnUnsafeOrMissingId(@TempDir Path store) {
        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(Optional.empty(), adapter.findById(null));
        assertEquals(Optional.empty(), adapter.findById(""));
        assertEquals(Optional.empty(), adapter.findById("../1"));
        assertEquals(Optional.empty(), adapter.findById("a/b"));
        assertEquals(Optional.empty(), adapter.findById("a\\b"));
        assertEquals(Optional.empty(), adapter.findById("missing"));
    }

    @Test
    void returnsEmptyForMissingStore(@TempDir Path store) {
        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(
                store.resolve("missing").toString(), objectMapper
        );

        assertEquals(List.of(), adapter.findAll());
        assertEquals(Optional.empty(), adapter.findById("1"));
    }

    @Test
    void skipsMalformedMetadataAndSupportsMissingOptionalFields(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("malformed.json"), "{");
        Files.createFile(store.resolve("malformed.mp4"));
        Files.createFile(store.resolve("malformed.webp"));
        Files.writeString(store.resolve("without-duration.json"), "{\"title\":\"No duration\"}");
        Files.createFile(store.resolve("without-duration.mp4"));
        Files.createFile(store.resolve("without-duration.webp"));

        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(Optional.empty(), adapter.findById("malformed"));
        assertEquals(null, adapter.findById("without-duration").orElseThrow().duration());
    }

    @Test
    void rejectsEachIncompleteMediaCombination(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("missing-thumbnail.json"), "{\"title\":\"Video\"}");
        Files.createFile(store.resolve("missing-thumbnail.mp4"));
        Files.writeString(store.resolve("missing-video.json"), "{\"title\":\"Video\"}");
        Files.createFile(store.resolve("missing-video.webp"));

        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(Optional.empty(), adapter.findById("missing-thumbnail"));
        assertEquals(Optional.empty(), adapter.findById("missing-video"));
    }

    @Test
    void updatesMetadataOnlyForTheOwner(@TempDir Path store) throws IOException {
        UUID ownerId = UUID.randomUUID();
        Files.writeString(store.resolve("1.json"), """
                {"title":"Old","meta":{"description":"Old description"},"ownerId":"%s","tags":["old"],"category":"Old"}
                """.formatted(ownerId));
        Files.createFile(store.resolve("1.mp4"));
        Files.createFile(store.resolve("1.webp"));
        FileVideoCatalogAdapter adapter = new FileVideoCatalogAdapter(store.toString(), objectMapper);

        assertEquals(Optional.empty(), adapter.updateMetadata(
                "1", UUID.randomUUID(), "New", "New description", List.of("new"), "New category"
        ));
        VideoSummary updated = adapter.updateMetadata(
                "1", ownerId, "New", "New description", List.of("new", "live"), "New category"
        ).orElseThrow();
        assertEquals("New", updated.title());
        assertEquals(List.of("new", "live"), updated.tags());
        assertEquals("New category", updated.category());
        assertEquals("New description", updated.description());
    }
}
