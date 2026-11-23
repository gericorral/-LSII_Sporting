package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPreferencesRepositoryTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final UserPreferencesRepository repository = new UserPreferencesRepository(jdbcTemplate);

    @Test
    void findsStoredDarkModePreference() throws Exception {
        UUID userId = UUID.randomUUID();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("user_id", UUID.class)).thenReturn(userId);
        when(resultSet.getBoolean("dark_mode")).thenReturn(true);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), eq(userId)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Optional<UserPreferences>> extractor = invocation.getArgument(1);
                    return extractor.extractData(resultSet);
                });

        Optional<UserPreferences> result = repository.findByUserId(userId);

        assertTrue(result.isPresent());
        assertTrue(result.orElseThrow().darkMode());
        assertEquals(userId, result.orElseThrow().userId());
    }

    @Test
    void returnsEmptyWhenUserHasNoPreference() throws Exception {
        UUID userId = UUID.randomUUID();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), eq(userId)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Optional<UserPreferences>> extractor = invocation.getArgument(1);
                    return extractor.extractData(resultSet);
                });

        assertTrue(repository.findByUserId(userId).isEmpty());
    }

    @Test
    void savesPreferenceWithUpsert() {
        UUID userId = UUID.randomUUID();

        repository.save(userId, true);

        verify(jdbcTemplate).update(contains("INSERT INTO user_preferences"), eq(userId), eq(true));
    }
}

