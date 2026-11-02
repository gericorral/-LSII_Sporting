package com.tecnocampus.LS2.protube_back.analytics.domain.port.out;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;

import java.util.List;
import java.util.UUID;

public interface VideoAnalyticsPort {
    List<VideoAnalytics> findByOwnerId(UUID ownerId);
}
