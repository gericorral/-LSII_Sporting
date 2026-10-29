package com.tecnocampus.LS2.protube_back.analytics.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.in.ViewOwnVideoAnalyticsUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final ViewOwnVideoAnalyticsUseCase analyticsUseCase;
    private final SessionPort sessionPort;

    public AnalyticsController(ViewOwnVideoAnalyticsUseCase analyticsUseCase, SessionPort sessionPort) {
        this.analyticsUseCase = analyticsUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getOwnAnalytics() {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
        return ResponseEntity.ok(analyticsUseCase.getForUser(userId));
    }
}
