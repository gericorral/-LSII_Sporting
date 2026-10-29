package com.tecnocampus.LS2.protube_back.comment.application.service;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase;
import com.tecnocampus.LS2.protube_back.comment.domain.port.out.CommentPersistencePort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService implements CommentUseCase {
    private static final int MAX_CONTENT_LENGTH = 2000;

    private final CommentPersistencePort commentPersistencePort;
    private final VideoCatalogPort videoCatalogPort;

    public CommentService(
            CommentPersistencePort commentPersistencePort,
            VideoCatalogPort videoCatalogPort
    ) {
        this.commentPersistencePort = commentPersistencePort;
        this.videoCatalogPort = videoCatalogPort;
    }

    @Override
    public CommentPage list(String videoId, int page, int size) {
        validatePagination(page, size);
        ensureVideoExists(videoId);
        List<Comment> comments = commentPersistencePort.findByVideoId(videoId);
        long from = (long) page * size;
        int fromIndex = (int) Math.min(from, comments.size());
        int toIndex = Math.min(fromIndex + size, comments.size());
        int totalPages = comments.isEmpty() ? 0 : (comments.size() + size - 1) / size;
        return new CommentPage(
                comments.subList(fromIndex, toIndex),
                page,
                size,
                comments.size(),
                totalPages,
                toIndex < comments.size()
        );
    }

    @Override
    public Comment create(String videoId, UUID authorId, String content) {
        if (authorId == null) {
            throw new InvalidCommentException("Authentication is required.");
        }
        ensureVideoExists(videoId);
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isEmpty()) {
            throw new InvalidCommentException("Comment cannot be empty.");
        }
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidCommentException("Comment must be at most 2000 characters.");
        }
        return commentPersistencePort.save(videoId, authorId, normalizedContent);
    }

    private void ensureVideoExists(String videoId) {
        if (videoId == null || videoId.isBlank() || videoCatalogPort.findById(videoId).isEmpty()) {
            throw new VideoNotFoundException(videoId);
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidCommentException("Page must be non-negative and size must be between 1 and 100.");
        }
    }
}
