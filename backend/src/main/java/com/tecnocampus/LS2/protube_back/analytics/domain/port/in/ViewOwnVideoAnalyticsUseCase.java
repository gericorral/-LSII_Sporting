package com.tecnocampus.LS2.protube_back.analytics.domain.port.in;

import com.tecnocampus.LS2.protube_back.analytics.domain.model.VideoAnalytics;

import java.util.List;
import java.util.UUID;

public interface ViewOwnVideoAnalyticsUseCase {
    List<VideoAnalytics> getForUser(UUID ownerId);
}
