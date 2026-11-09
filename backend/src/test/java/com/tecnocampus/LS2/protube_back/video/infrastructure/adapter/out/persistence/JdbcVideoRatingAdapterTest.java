package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcVideoRatingAdapterTest {
    @Test
    void readsAggregatedRatingAndCurrentUserRating() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UUID userId = UUID.randomUUID();
        String videoId = "5";
        when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<VideoRating>>any(),
                any(), any())).thenAnswer(invocation -> {
            RowMapper<VideoRating> mapper = invocation.getArgument(1);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getBigDecimal("average_rating")).thenReturn(BigDecimal.valueOf(4.5));
            when(resultSet.getLong("rating_count")).thenReturn(2L);
            when(resultSet.getObject("user_rating")).thenReturn((short) 5);
            return mapper.mapRow(resultSet, 1);
        });

        assertEquals(new VideoRating(videoId, BigDecimal.valueOf(4.50).setScale(2), 2, 5),
                new JdbcVideoRatingAdapter(jdbcTemplate).find(videoId, userId));
    }

    @Test
    void readsMissingCurrentUserRatingAsNull() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        String videoId = "5";
        when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<VideoRating>>any(),
                any(), any())).thenAnswer(invocation -> {
            RowMapper<VideoRating> mapper = invocation.getArgument(1);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getBigDecimal("average_rating")).thenReturn(BigDecimal.ZERO);
            when(resultSet.getLong("rating_count")).thenReturn(0L);
            when(resultSet.getObject("user_rating")).thenReturn(null);
            return mapper.mapRow(resultSet, 1);
        });

        assertEquals(null, new JdbcVideoRatingAdapter(jdbcTemplate).find(videoId, UUID.randomUUID()).userRating());
    }

    @Test
    void upsertsRating() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        String videoId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();

        new JdbcVideoRatingAdapter(jdbcTemplate).save(videoId, userId, 4);

        verify(jdbcTemplate).update(anyString(), eq(videoId), eq(userId), eq(4));
    }
}
