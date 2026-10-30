package com.tecnocampus.LS2.protube_back.comment.domain.port.in;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentUseCase {
    CommentPage list(String videoId, int page, int size);

    CommentPage list(String videoId, int page, int size, UUID viewerId);

    Comment create(String videoId, UUID authorId, String content);

    Comment reply(String videoId, UUID authorId, UUID parentCommentId, String content);

    LikeResult toggleLike(String videoId, UUID commentId, UUID userId);

    record LikeResult(UUID commentId, long likeCount, boolean likedByCurrentUser) {
    }

    record CommentPage(
            List<Comment> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
    }

    class InvalidCommentException extends RuntimeException {
        public InvalidCommentException(String message) {
            super(message);
        }
    }

    class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String videoId) {
            super("Video not found: " + videoId);
        }
    }
}
