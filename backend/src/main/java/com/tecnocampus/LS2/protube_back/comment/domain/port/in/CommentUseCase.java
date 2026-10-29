package com.tecnocampus.LS2.protube_back.comment.domain.port.in;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentUseCase {
    CommentPage list(String videoId, int page, int size);

    Comment create(String videoId, UUID authorId, String content);

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
