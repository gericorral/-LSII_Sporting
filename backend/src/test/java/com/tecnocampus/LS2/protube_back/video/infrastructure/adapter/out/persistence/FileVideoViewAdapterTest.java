package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileVideoViewAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void incrementsViewsAndPreservesOtherMetadata(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("1.json"), """
                {"title":"Video","analytics":{"views":4,"comments":2,"likes":3},"ownerId":"owner"}
                """);

        FileVideoViewAdapter adapter = new FileVideoViewAdapter(store.toString(), objectMapper);

        assertTrue(adapter.incrementViews("1"));
        var metadata = objectMapper.readTree(store.resolve("1.json").toFile());
        assertEquals(5, metadata.path("analytics").path("views").asInt());
        assertEquals(2, metadata.path("analytics").path("comments").asInt());
        assertEquals("owner", metadata.path("ownerId").asText());
    }

    @Test
    void createsMissingAnalyticsAndRejectsMissingFiles(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("1.json"), "{\"title\":\"Video\"}");
        FileVideoViewAdapter adapter = new FileVideoViewAdapter(store.toString(), objectMapper);

        assertTrue(adapter.incrementViews("1"));
        assertEquals(1, objectMapper.readTree(store.resolve("1.json").toFile())
                .path("analytics").path("views").asInt());
        assertFalse(adapter.incrementViews("missing"));
    }

    @Test
    void normalizesInvalidViewsAndRejectsMalformedMetadata(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("invalid.json"), "{\"analytics\":{\"views\":-1}}");
        Files.writeString(store.resolve("malformed.json"), "{");
        Files.writeString(store.resolve("array.json"), "[]");
        FileVideoViewAdapter adapter = new FileVideoViewAdapter(store.toString(), objectMapper);

        assertTrue(adapter.incrementViews("invalid"));
        assertEquals(1, objectMapper.readTree(store.resolve("invalid.json").toFile())
                .path("analytics").path("views").asInt());
        assertFalse(adapter.incrementViews("array"));
        assertThrows(IllegalStateException.class, () -> adapter.incrementViews("malformed"));
    }

    @Test
    void rejectsViewCounterOverflow(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("max.json"),
                "{\"analytics\":{\"views\":9223372036854775807}}");
        FileVideoViewAdapter adapter = new FileVideoViewAdapter(store.toString(), objectMapper);

        assertThrows(IllegalStateException.class, () -> adapter.incrementViews("max"));
    }
}
