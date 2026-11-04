package com.tecnocampus.LS2.protube_back.analytics.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import org.mockito.ArgumentMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcVideoAnalyticsAdapterTest {
    @Test
    void returnsNoAnalyticsForAnonymousOwner() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        assertEquals(List.of(), new JdbcVideoAnalyticsAdapter(jdbcTemplate).findByOwnerId(null));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void readsAnalyticsRowsForAnOwner() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoAnalytics>>any(), eq(ownerId)))
                .thenAnswer(invocation -> {
            var mapper = (RowMapper<VideoAnalytics>) invocation.getArgument(1);
            var resultSet = mock(java.sql.ResultSet.class);
            when(resultSet.getString("video_id")).thenReturn(videoId.toString());
            when(resultSet.getString("title")).thenReturn("Title");
            when(resultSet.getLong("view_count")).thenReturn(4L);
            when(resultSet.getLong("comment_count")).thenReturn(2L);
            when(resultSet.getLong("like_count")).thenReturn(1L);
            return List.of(mapper.mapRow(resultSet, 1));
                });

        assertEquals(new VideoAnalytics(videoId.toString(), "Title", 4, 2, 1),
                new JdbcVideoAnalyticsAdapter(jdbcTemplate).findByOwnerId(ownerId).get(0));
    }
}
