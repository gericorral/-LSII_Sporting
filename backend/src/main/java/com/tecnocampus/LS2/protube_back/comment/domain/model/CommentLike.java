package com.tecnocampus.LS2.protube_back.comment.domain.model;

import java.util.UUID;

public record CommentLike(UUID commentId, long likeCount, boolean likedByCurrentUser) {
}
