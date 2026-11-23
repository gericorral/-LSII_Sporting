package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ManageUserPreferencesUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.UserPreferences;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserPreferencesControllerTest {
    private final ManageUserPreferencesUseCase useCase = mock(ManageUserPreferencesUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final UserPreferencesController controller = new UserPreferencesController(useCase, session);
    private final UUID userId = UUID.randomUUID();

    @Test
    void readsAndUpdatesAuthenticatedPreference() {
        when(session.currentUserId()).thenReturn(userId);
        when(useCase.get(userId)).thenReturn(new UserPreferences(userId, false));
        when(useCase.update(userId, true)).thenReturn(new UserPreferences(userId, true));

        assertEquals(200, controller.get().getStatusCode().value());
        assertEquals(200, controller.update(new UserPreferencesController.PreferencesRequest(true))
                .getStatusCode().value());
        verify(useCase).update(userId, true);
    }

    @Test
    void protectsAnonymousRequestsAndHandlesNullBody() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.get().getStatusCode().value());
        assertEquals(401, controller.update(null).getStatusCode().value());

        when(session.currentUserId()).thenReturn(userId);
        when(useCase.update(userId, false)).thenReturn(new UserPreferences(userId, false));
        assertEquals(200, controller.update(null).getStatusCode().value());
        verify(useCase).update(userId, false);
    }
}
