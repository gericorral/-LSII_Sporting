package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UserLogoutServiceTest {
    private final SessionPort sessionPort = mock(SessionPort.class);
    private final UserLogoutService service = new UserLogoutService(sessionPort);

    @Test
    void invalidatesTheCurrentSession() {
        service.logout();
        verify(sessionPort).invalidate();
    }
}
