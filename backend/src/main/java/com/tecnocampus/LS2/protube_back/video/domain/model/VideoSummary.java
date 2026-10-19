package com.tecnocampus.LS2.protube_back.video.domain.model;

import java.util.List;
import java.util.UUID;

public record VideoSummary(
        String id,
        String title,
        String description,
        Integer duration,
        String thumbnailUrl,
        String videoUrl,
        UUID ownerId,
        List<String> tags,
        String category
) {
    public VideoSummary(
            String id,
            String title,
            String description,
            Integer duration,
            String thumbnailUrl,
            String videoUrl
    ) {
        this(id, title, description, duration, thumbnailUrl, videoUrl, null, List.of(), "");
    }
}
