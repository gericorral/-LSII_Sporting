package com.tecnocampus.LS2.protube_back.comment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Comment(
        UUID id,
        String videoId,
        UUID authorId,
        String authorName,
        String content,
        LocalDateTime createdAt
) {
}
