package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class HttpSessionAdapterTest {
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpSession session = mock(HttpSession.class);
    private final HttpSessionAdapter adapter = new HttpSessionAdapter(request);

    @Test
    void storesAuthenticatedUserIdInSession() {
        when(request.getSession(true)).thenReturn(session);
        UUID userId = UUID.randomUUID();
        adapter.authenticate(userId);
        verify(session).setAttribute("authenticatedUserId", userId);
    }

    @Test
    void readsAuthenticatedUserIdFromExistingSession() {
        UUID userId = UUID.randomUUID();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authenticatedUserId")).thenReturn(userId);
        assertEquals(userId, adapter.currentUserId());
    }

    @Test
    void returnsNullWithoutSession() {
        when(request.getSession(false)).thenReturn(null);
        assertNull(adapter.currentUserId());
    }

    @Test
    void returnsNullWhenSessionAttributeIsNotUuid() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authenticatedUserId")).thenReturn("not-a-uuid");
        assertNull(adapter.currentUserId());
    }

    @Test
    void invalidatesExistingSession() {
        when(request.getSession(false)).thenReturn(session);
        adapter.invalidate();
        verify(session).invalidate();
    }
}
