package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mockito.InOrder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcGoogleAccountAdapterTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final UserRepository users = mock(UserRepository.class);
    private final JdbcGoogleAccountAdapter adapter = new JdbcGoogleAccountAdapter(jdbc, users);

    @Test
    void returnsExistingGoogleIdentityWithoutCreatingUser() {
        User existing = new User(UUID.randomUUID(), "google@example.com", "google_user", null);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(existing);

        assertSame(existing, adapter.provisionOrFind("subject", "google@example.com", "Name"));
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void refusesToLinkExistingLocalEmail() {
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(null);
        when(users.findByEmailIgnoreCase("google@example.com"))
                .thenReturn(Optional.of(new User(UUID.randomUUID(), "google@example.com", "local", "hash")));

        assertThrows(JdbcGoogleAccountAdapter.GoogleAccountConflictException.class,
                () -> adapter.provisionOrFind("subject", "google@example.com", "Name"));
    }

    @Test
    void provisionsNewIdentityAndProfile() {
        User saved = new User(UUID.randomUUID(), "google@example.com", "google_name", null);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(null);
        when(users.findByEmailIgnoreCase("google@example.com")).thenReturn(Optional.empty());
        when(users.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenReturn(saved);

        assertSame(saved, adapter.provisionOrFind("subject", "google@example.com", "Google Name"));
        InOrder order = inOrder(users, jdbc);
        order.verify(users).saveAndFlush(any(User.class));
        order.verify(jdbc).update(contains("INSERT INTO auth_accounts"), any(), eq(saved.getId()),
                eq("subject"), eq("google@example.com"));
        verify(jdbc).update(contains("INSERT INTO auth_accounts"), any(), eq(saved.getId()), eq("subject"),
                eq("google@example.com"));
        verify(jdbc).update(contains("INSERT INTO user_profiles"), eq(saved.getId()), eq("Google Name"));
    }

    @Test
    void retriesExistingIdentityWhenConcurrentInsertWins() {
        User saved = new User(UUID.randomUUID(), "google@example.com", "google_name", null);
        User concurrent = new User(UUID.randomUUID(), "google@example.com", "google_name_2", null);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(null, concurrent);
        when(users.findByEmailIgnoreCase("google@example.com")).thenReturn(Optional.empty());
        when(users.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenReturn(saved);
        when(jdbc.update(contains("INSERT INTO auth_accounts"), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertSame(concurrent, adapter.provisionOrFind("subject", "google@example.com", "Google Name"));
        verify(jdbc, never()).update(contains("INSERT INTO user_profiles"), any(), any());
    }

    @Test
    void addsSuffixWhenGeneratedUsernameIsTaken() {
        User saved = new User(UUID.randomUUID(), "google@example.com", "google_name_1", null);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(null);
        when(users.findByEmailIgnoreCase("google@example.com")).thenReturn(Optional.empty());
        when(users.existsByUsernameIgnoreCase("google_name")).thenReturn(true);
        when(users.existsByUsernameIgnoreCase("google_name_1")).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenReturn(saved);

        adapter.provisionOrFind("subject", "google@example.com", "Google Name");

        verify(users).saveAndFlush(
                argThat(user -> "google_name_1".equals(user.getUsername())));
    }

    @Test
    void usesEmailPrefixAndNullProfileNameForBlankDisplayName() {
        User saved = new User(UUID.randomUUID(), "short@example.com", "short", null);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(null);
        when(users.findByEmailIgnoreCase("short@example.com")).thenReturn(Optional.empty());
        when(users.existsByUsernameIgnoreCase("short")).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenReturn(saved);

        assertSame(saved, adapter.provisionOrFind("subject", "short@example.com", " "));

        verify(jdbc).update(contains("INSERT INTO user_profiles"), eq(saved.getId()), isNull());
    }

    @Test
    void fallsBackToGoogleUsernameForShortEmailPrefix() {
        User saved = new User(UUID.randomUUID(), "x@example.com", "google_user", null);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("subject")))
                .thenReturn(null);
        when(users.findByEmailIgnoreCase("x@example.com")).thenReturn(Optional.empty());
        when(users.existsByUsernameIgnoreCase("google_user")).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenReturn(saved);

        adapter.provisionOrFind("subject", "x@example.com", null);

        verify(users).saveAndFlush(
                argThat(user -> "google_user".equals(user.getUsername())));
    }
}
