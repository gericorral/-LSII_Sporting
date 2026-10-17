package com.tecnocampus.LS2.protube_back.video.domain.model;

public record VideoSummary(
        String id,
        String title,
        String description,
        Integer duration,
        String thumbnailUrl,
        String videoUrl
) {
}
