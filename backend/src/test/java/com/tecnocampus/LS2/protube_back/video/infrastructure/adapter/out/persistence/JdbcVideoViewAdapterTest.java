package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcVideoViewAdapterTest {
    @Test
    void insertsAViewWhenTheVideoExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcVideoViewAdapter adapter = new JdbcVideoViewAdapter(jdbcTemplate);
        when(jdbcTemplate.update(anyString(), any(UUID.class), any(UUID.class))).thenReturn(1);

        assertTrue(adapter.incrementViews(UUID.randomUUID().toString()));
    }

    @Test
    void returnsFalseForMissingOrInvalidVideo() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcVideoViewAdapter adapter = new JdbcVideoViewAdapter(jdbcTemplate);
        when(jdbcTemplate.update(anyString(), any(UUID.class), any(UUID.class))).thenReturn(0);

        assertFalse(adapter.incrementViews(UUID.randomUUID().toString()));
        assertFalse(adapter.incrementViews("invalid"));
    }
}
