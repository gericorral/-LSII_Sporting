package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.VideoFile;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.storage.FileVideoPublicationAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcVideoPublicationAdapterTest {
    @Test
    void publishesFileAndMetadata() {
        FileVideoPublicationAdapter storage = mock(FileVideoPublicationAdapter.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcVideoPublicationAdapter adapter = new JdbcVideoPublicationAdapter(storage, jdbcTemplate);
        UUID ownerId = UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        VideoFile video = new VideoFile(new ByteArrayInputStream(new byte[]{1, 2, 3}), 3L, "video/mp4");
        var thumbnail = new UploadVideoUseCase.ThumbnailFile(
                new ByteArrayInputStream(new byte[]{4}), 1L, "image/jpeg");

        var result = adapter.publish(id, "Title", "Description", ownerId, video, thumbnail);

        assertEquals(id, result.id());
        assertEquals("/media/" + id + ".mp4", result.videoUrl());
        verify(storage).publish(id, "Title", "Description", ownerId, video, thumbnail);
        verify(jdbcTemplate, times(2)).update(anyString(), any(Object[].class));
    }

    @Test
    void wrapsPersistenceFailures() {
        FileVideoPublicationAdapter storage = mock(FileVideoPublicationAdapter.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doThrow(new RuntimeException("database down")).when(jdbcTemplate)
                .update(anyString(), any(Object[].class));
        JdbcVideoPublicationAdapter adapter = new JdbcVideoPublicationAdapter(storage, jdbcTemplate);
        String id = UUID.randomUUID().toString();
        VideoFile video = new VideoFile(new ByteArrayInputStream(new byte[0]), 0L, "video/mp4");
        var thumbnail = new UploadVideoUseCase.ThumbnailFile(
                new ByteArrayInputStream(new byte[]{4}), 1L, "image/png");

        assertThrows(IllegalStateException.class,
                () -> adapter.publish(id, "Title", "Description", UUID.randomUUID(), video, thumbnail));
    }
}
