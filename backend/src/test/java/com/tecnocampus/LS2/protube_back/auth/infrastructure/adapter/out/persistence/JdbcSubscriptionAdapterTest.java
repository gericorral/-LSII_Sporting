package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcSubscriptionAdapterTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcSubscriptionAdapter adapter = new JdbcSubscriptionAdapter(jdbcTemplate);
    private final UUID subscriber = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void managesSubscriptionRows() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), any(UUID.class))).thenReturn(true);
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(subscriber), eq(target)))
                .thenReturn(true);

        assertTrue(adapter.targetExists(target));
        assertTrue(adapter.exists(subscriber, target));
        adapter.add(subscriber, target);
        adapter.remove(subscriber, target);
        verify(jdbcTemplate).update(contains("INSERT INTO subscriptions"), eq(subscriber), eq(target));
        verify(jdbcTemplate).update(contains("DELETE FROM subscriptions"), eq(subscriber), eq(target));
    }

    @Test
    void listsSubscribedProfiles() {
        ProfileUseCase.Profile profile = new ProfileUseCase.Profile(
                target, "creator", "Creator", null, null, null, null, true);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(subscriber)))
                .thenReturn(List.of(profile));

        assertEquals(List.of(profile), adapter.findSubscriptions(subscriber));
    }

    @Test
    void treatsFalseAndNullExistenceResultsAsNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), any(UUID.class))).thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(subscriber), eq(target)))
                .thenReturn(null);

        assertFalse(adapter.targetExists(target));
        assertFalse(adapter.exists(subscriber, target));
    }

    @Test
    void mapsEveryPublicProfileFieldFromTheSubscriptionQuery() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id", UUID.class)).thenReturn(target);
        when(resultSet.getString("username")).thenReturn("creator");
        when(resultSet.getString("display_name")).thenReturn("Creator");
        when(resultSet.getString("description")).thenReturn("A creator");
        when(resultSet.getString("avatar_path")).thenReturn("/avatar.png");
        when(resultSet.getString("website")).thenReturn("https://example.test");
        when(resultSet.getString("location")).thenReturn("London");
        when(resultSet.getBoolean("is_public")).thenReturn(true);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(subscriber))).thenAnswer(invocation -> {
            RowMapper<ProfileUseCase.Profile> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });

        assertEquals(new ProfileUseCase.Profile(
                target, "creator", "Creator", "A creator", "/avatar.png",
                "https://example.test", "London", true
        ), adapter.findSubscriptions(subscriber).get(0));
    }
}
