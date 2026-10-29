package com.tecnocampus.LS2.protube_back.analytics.application.service;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.in.ViewOwnVideoAnalyticsUseCase;
import com.tecnocampus.LS2.protube_back.analytics.domain.port.out.VideoAnalyticsPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VideoAnalyticsService implements ViewOwnVideoAnalyticsUseCase {
    private final VideoAnalyticsPort analyticsPort;

    public VideoAnalyticsService(VideoAnalyticsPort analyticsPort) {
        this.analyticsPort = analyticsPort;
    }

    @Override
    public List<VideoAnalytics> getForUser(UUID ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        return analyticsPort.findByOwnerId(ownerId);
    }
}
