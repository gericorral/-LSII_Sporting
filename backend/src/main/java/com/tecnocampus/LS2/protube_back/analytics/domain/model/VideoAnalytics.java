package com.tecnocampus.LS2.protube_back.analytics.domain.model;

public record VideoAnalytics(
        String videoId,
        String title,
        long views,
        long comments,
        long likes
) {
    public VideoAnalytics {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("Video id is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Video title is required.");
        }
        if (views < 0 || comments < 0 || likes < 0) {
            throw new IllegalArgumentException("Analytics counters cannot be negative.");
        }
    }
}
