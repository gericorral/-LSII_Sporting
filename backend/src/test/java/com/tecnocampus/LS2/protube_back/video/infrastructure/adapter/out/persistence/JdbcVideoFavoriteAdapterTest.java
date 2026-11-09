package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcVideoFavoriteAdapterTest {
    @Test
    void checksAndChangesFavorite() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UUID userId = UUID.randomUUID();
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq("video"), eq(userId)))
                .thenReturn(true);
        var adapter = new JdbcVideoFavoriteAdapter(jdbcTemplate);

        assertTrue(adapter.exists("video", userId));
        adapter.add("video", userId);
        adapter.remove("video", userId);

        verify(jdbcTemplate).update(contains("INSERT"), eq("video"), eq(userId));
        verify(jdbcTemplate).update(contains("DELETE"), eq("video"), eq(userId));
    }

    @Test
    void listsMostRecentFavoriteIds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UUID userId = UUID.randomUUID();
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(userId)))
                .thenReturn(List.of("two", "one"));

        assertEquals(List.of("two", "one"), new JdbcVideoFavoriteAdapter(jdbcTemplate).findVideoIds(userId));
    }
}
