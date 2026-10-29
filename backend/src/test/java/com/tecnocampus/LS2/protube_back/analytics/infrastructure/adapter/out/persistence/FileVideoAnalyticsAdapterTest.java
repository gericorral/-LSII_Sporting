package com.tecnocampus.LS2.protube_back.analytics.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileVideoAnalyticsAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsOwnedVideoCountersAndDefaultsMissingCounters(@TempDir Path store) throws IOException {
        UUID ownerId = UUID.randomUUID();
        Files.writeString(store.resolve("1.json"), """
                {"title":"Owned","ownerId":"%s","analytics":{"views":12,"comments":3,"likes":4}}
                """.formatted(ownerId));
        Files.writeString(store.resolve("2.json"), """
                {"title":"Other","ownerId":"%s","analytics":{"views":99}}
                """.formatted(UUID.randomUUID()));
        Files.writeString(store.resolve("3.json"), """
                {"title":"Missing counters","ownerId":"%s"}
                """.formatted(ownerId));

        FileVideoAnalyticsAdapter adapter = new FileVideoAnalyticsAdapter(store.toString(), objectMapper);

        assertEquals(List.of(
                new VideoAnalytics("1", "Owned", 12, 3, 4),
                new VideoAnalytics("3", "Missing counters", 0, 0, 0)
        ), adapter.findByOwnerId(ownerId));
    }

    @Test
    void returnsEmptyWhenStoreDoesNotExist(@TempDir Path store) {
        FileVideoAnalyticsAdapter adapter = new FileVideoAnalyticsAdapter(
                store.resolve("missing").toString(), objectMapper
        );

        assertEquals(List.of(), adapter.findByOwnerId(UUID.randomUUID()));
    }

    @Test
    void defaultsInvalidCountersAndSkipsMalformedMetadata(@TempDir Path store) throws IOException {
        UUID ownerId = UUID.randomUUID();
        Files.writeString(store.resolve("invalid-counters.json"), """
                {"title":"Invalid","ownerId":"%s","analytics":{"views":-1,"comments":"unknown","likes":9223372036854775808}}
                """.formatted(ownerId));
        Files.writeString(store.resolve("blank-title.json"), """
                {"title":" ","ownerId":"%s"}
                """.formatted(ownerId));
        Files.writeString(store.resolve("malformed.json"), "{");

        FileVideoAnalyticsAdapter adapter = new FileVideoAnalyticsAdapter(store.toString(), objectMapper);

        assertEquals(List.of(new VideoAnalytics("invalid-counters", "Invalid", 0, 0, 0)),
                adapter.findByOwnerId(ownerId));
    }
}
