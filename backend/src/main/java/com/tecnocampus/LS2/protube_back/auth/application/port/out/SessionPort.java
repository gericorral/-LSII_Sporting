package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import java.util.UUID;

public interface SessionPort {
    UUID currentUserId();

    void authenticate(UUID userId);

    void invalidate();
}
