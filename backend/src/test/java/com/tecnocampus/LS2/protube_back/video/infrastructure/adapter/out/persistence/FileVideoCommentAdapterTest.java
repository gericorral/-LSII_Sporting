package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileVideoCommentAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void incrementsCommentsAndPreservesOtherAnalytics(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("1.json"),
                "{\"title\":\"Video\",\"analytics\":{\"views\":4,\"comments\":2,\"likes\":3}}");

        FileVideoCommentAdapter adapter = new FileVideoCommentAdapter(store.toString(), objectMapper);

        assertTrue(adapter.incrementComments("1"));
        var metadata = objectMapper.readTree(store.resolve("1.json").toFile());
        assertEquals(3, metadata.path("analytics").path("comments").asInt());
        assertEquals(4, metadata.path("analytics").path("views").asInt());
        assertEquals(3, metadata.path("analytics").path("likes").asInt());
    }

    @Test
    void createsMissingCounterAndRejectsMissingOrOverflowingMetadata(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("1.json"), "{\"title\":\"Video\"}");
        Files.writeString(store.resolve("max.json"),
                "{\"analytics\":{\"comments\":9223372036854775807}}");
        FileVideoCommentAdapter adapter = new FileVideoCommentAdapter(store.toString(), objectMapper);

        assertTrue(adapter.incrementComments("1"));
        assertEquals(1, objectMapper.readTree(store.resolve("1.json").toFile())
                .path("analytics").path("comments").asInt());
        assertFalse(adapter.incrementComments("missing"));
        assertThrows(IllegalStateException.class, () -> adapter.incrementComments("max"));
    }

    @Test
    void normalizesInvalidCountersAndRejectsMalformedMetadata(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("invalid.json"), "{\"analytics\":{\"comments\":-1}}");
        Files.writeString(store.resolve("array.json"), "[]");
        Files.writeString(store.resolve("malformed.json"), "{");
        Files.writeString(store.resolve("text.json"), "{\"analytics\":{\"comments\":\"unknown\"}}");
        Files.writeString(store.resolve("null.json"), "null");
        FileVideoCommentAdapter adapter = new FileVideoCommentAdapter(store.toString(), objectMapper);

        assertTrue(adapter.incrementComments("invalid"));
        assertEquals(1, objectMapper.readTree(store.resolve("invalid.json").toFile())
                .path("analytics").path("comments").asInt());
        assertFalse(adapter.incrementComments("array"));
        assertTrue(adapter.incrementComments("text"));
        assertFalse(adapter.incrementComments("null"));
        assertFalse(adapter.incrementComments("../invalid"));
        assertThrows(IllegalStateException.class, () -> adapter.incrementComments("malformed"));
    }
}
