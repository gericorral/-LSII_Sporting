package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VideoStoreDatabaseImporterTest {
    @Test
    void skipsAStoreThatDoesNotExist(@TempDir Path temp) {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        VideoStoreDatabaseImporter importer = new VideoStoreDatabaseImporter(
                temp.resolve("missing").toString(), new ObjectMapper(), jdbcTemplate);

        assertDoesNotThrow(() -> importer.run(new DefaultApplicationArguments(new String[0])));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void skipsIncompleteInvalidAndUntitledVideos(@TempDir Path store) throws IOException {
        Files.writeString(store.resolve("missing.json"), "{\"title\":\"Missing\"}");
        Files.writeString(store.resolve("invalid.json"), "{");
        Files.createFile(store.resolve("invalid.mp4"));
        Files.createFile(store.resolve("invalid.webp"));
        Files.writeString(store.resolve("untitled.json"), "{\"meta\":{}}");
        Files.createFile(store.resolve("untitled.mp4"));
        Files.createFile(store.resolve("untitled.png"));
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        new VideoStoreDatabaseImporter(store.toString(), new ObjectMapper(), jdbcTemplate)
                .run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void importsMetadataWithWebpTagsAndCategory(@TempDir Path store) throws IOException {
        writeCompleteVideo(store, "42", "webp", """
                {"title":"Imported","duration":12,"meta":{"description":"Description"},
                 "tags":["travel"," ","sports"],"category":" Guides "}
                """);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(UUID.class), any(), any(), any()))
                .thenReturn(UUID.randomUUID());

        assertDoesNotThrow(() -> new VideoStoreDatabaseImporter(
                store.toString(), new ObjectMapper(), jdbcTemplate)
                .run(new DefaultApplicationArguments(new String[0])));

        verify(jdbcTemplate).update(contains("INSERT INTO videos"), any(), any(), any(), any(), any());
        verify(jdbcTemplate).update(contains("DELETE FROM video_tags"), any(UUID.class));
        verify(jdbcTemplate).update(contains("DELETE FROM video_categories"), any(UUID.class));
        verify(jdbcTemplate, atLeastOnce()).queryForObject(anyString(), eq(UUID.class), any(), any(), any());
    }

    @Test
    void importsPngWithoutOptionalCategory(@TempDir Path store) throws IOException {
        writeCompleteVideo(store, "43", "png", "{\"title\":\"Imported\",\"meta\":{}}");
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(UUID.class), any(), any(), any()))
                .thenReturn(UUID.randomUUID());

        new VideoStoreDatabaseImporter(store.toString(), new ObjectMapper(), jdbcTemplate)
                .run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate).update(contains("DELETE FROM video_categories"), any(UUID.class));
        verify(jdbcTemplate, never()).queryForObject(contains("INSERT INTO categories"), eq(UUID.class), any(), any(), any());
    }

    @Test
    void treatsARegularStorePathAsAnEmptyStore(@TempDir Path temp) throws IOException {
        Path store = temp.resolve("store");
        Files.createFile(store);
        assertDoesNotThrow(() -> new VideoStoreDatabaseImporter(
                store.toString(), new ObjectMapper(), mock(JdbcTemplate.class))
                .run(new DefaultApplicationArguments(new String[0])));
    }

    private void writeCompleteVideo(Path store, String id, String thumbnailExtension, String metadata)
            throws IOException {
        Files.writeString(store.resolve(id + ".json"), metadata);
        Files.createFile(store.resolve(id + ".mp4"));
        Files.createFile(store.resolve(id + "." + thumbnailExtension));
    }
}
