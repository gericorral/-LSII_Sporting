package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
}
