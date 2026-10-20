package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class HttpSessionAdapter implements SessionPort {
    private final HttpServletRequest request;

    public HttpSessionAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public UUID currentUserId() {
        var session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUserId");
        return value instanceof UUID ? (UUID) value : null;
    }

    @Override
    public void authenticate(UUID userId) {
        request.getSession(true).setAttribute("authenticatedUserId", userId);
    }

    @Override
    public void invalidate() {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
