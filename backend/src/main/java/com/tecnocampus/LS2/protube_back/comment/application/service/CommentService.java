package com.tecnocampus.LS2.protube_back.comment.application.service;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.model.CommentLike;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase;
import com.tecnocampus.LS2.protube_back.comment.domain.port.out.CommentPersistencePort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCommentPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService implements CommentUseCase {
    private static final int MAX_CONTENT_LENGTH = 2000;

    private final CommentPersistencePort commentPersistencePort;
    private final VideoCatalogPort videoCatalogPort;
    private final VideoCommentPort videoCommentPort;

    public CommentService(
            CommentPersistencePort commentPersistencePort,
            VideoCatalogPort videoCatalogPort,
            VideoCommentPort videoCommentPort
    ) {
        this.commentPersistencePort = commentPersistencePort;
        this.videoCatalogPort = videoCatalogPort;
        this.videoCommentPort = videoCommentPort;
    }

    @Override
    public CommentPage list(String videoId, int page, int size) {
        return list(videoId, page, size, null);
    }

    @Override
    public CommentPage list(String videoId, int page, int size, UUID viewerId) {
        validatePagination(page, size);
        ensureVideoExists(videoId);
        List<Comment> comments = viewerId == null
                ? commentPersistencePort.findByVideoId(videoId)
                : commentPersistencePort.findByVideoId(videoId, viewerId);
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
        return create(videoId, authorId, null, content);
    }

    @Override
    public Comment reply(String videoId, UUID authorId, UUID parentCommentId, String content) {
        if (parentCommentId == null) {
            throw new InvalidCommentException("Parent comment is required.");
        }
        return create(videoId, authorId, parentCommentId, content);
    }

    private Comment create(String videoId, UUID authorId, UUID parentCommentId, String content) {
        if (authorId == null) {
            throw new InvalidCommentException("Authentication is required.");
        }
        ensureVideoExists(videoId);
        if (parentCommentId != null && !commentPersistencePort.existsInVideo(parentCommentId, videoId)) {
            throw new InvalidCommentException("Parent comment was not found for this video.");
        }
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isEmpty()) {
            throw new InvalidCommentException("Comment cannot be empty.");
        }
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidCommentException("Comment must be at most 2000 characters.");
        }
        Comment saved = parentCommentId == null
                ? commentPersistencePort.save(videoId, authorId, normalizedContent)
                : commentPersistencePort.save(videoId, authorId, parentCommentId, normalizedContent);
        if (!videoCommentPort.incrementComments(videoId)) {
            throw new VideoNotFoundException(videoId);
        }
        return saved;
    }

    @Override
    public LikeResult toggleLike(String videoId, UUID commentId, UUID userId) {
        if (userId == null) {
            throw new InvalidCommentException("Authentication is required.");
        }
        ensureVideoExists(videoId);
        if (commentId == null || !commentPersistencePort.existsInVideo(commentId, videoId)) {
            throw new InvalidCommentException("Comment was not found for this video.");
        }
        CommentLike result = commentPersistencePort.toggleLike(commentId, userId);
        return new LikeResult(result.commentId(), result.likeCount(), result.likedByCurrentUser());
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
