package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.LogoutUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import org.springframework.stereotype.Service;

@Service
public class UserLogoutService implements LogoutUseCase {
    private final SessionPort sessionPort;

    public UserLogoutService(SessionPort sessionPort) {
        this.sessionPort = sessionPort;
    }

    @Override
    public void logout() {
        sessionPort.invalidate();
    }
}
