package com.tecnocampus.LS2.protube_back.comment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Comment(
        UUID id,
        String videoId,
        UUID authorId,
        String authorName,
        String content,
        LocalDateTime createdAt,
        UUID parentCommentId,
        long likeCount,
        boolean likedByCurrentUser
) {
    public Comment(UUID id, String videoId, UUID authorId, String authorName, String content, LocalDateTime createdAt) {
        this(id, videoId, authorId, authorName, content, createdAt, null, 0, false);
    }
}
