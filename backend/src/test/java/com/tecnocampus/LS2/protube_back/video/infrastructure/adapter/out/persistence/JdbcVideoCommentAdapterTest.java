package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JdbcVideoCommentAdapterTest {
    @Test
    void reportsWhetherVideoExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcVideoCommentAdapter adapter = new JdbcVideoCommentAdapter(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(UUID.class))).thenReturn(1L);

        assertTrue(adapter.incrementComments(UUID.randomUUID().toString()));
        assertFalse(adapter.incrementComments("invalid"));
    }

    @Test
    void returnsFalseWhenVideoDoesNotExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcVideoCommentAdapter adapter = new JdbcVideoCommentAdapter(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(UUID.class))).thenReturn(0L);

        assertFalse(adapter.incrementComments(UUID.randomUUID().toString()));
    }
}
