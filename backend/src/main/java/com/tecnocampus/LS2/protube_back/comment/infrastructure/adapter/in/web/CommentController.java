package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos/{videoId}/comments")
@Tag(name = "Comments")
public class CommentController {
    private final CommentUseCase commentUseCase;
    private final SessionPort sessionPort;

    public CommentController(CommentUseCase commentUseCase, SessionPort sessionPort) {
        this.commentUseCase = commentUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping
    @Operation(summary = "List comments")
    public ResponseEntity<?> list(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
                UUID viewerId = sessionPort.currentUserId();
                return ResponseEntity.ok(viewerId == null
                        ? commentUseCase.list(videoId, page, size)
                        : commentUseCase.list(videoId, page, size, viewerId));
        } catch (CommentUseCase.InvalidCommentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (CommentUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a comment")
    public ResponseEntity<?> create(
            @PathVariable String videoId,
            @RequestBody CreateCommentRequest request
    ) {
        if (sessionPort.currentUserId() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to comment."));
        }
        try {
            Comment comment = request.parentCommentId() == null
                    ? commentUseCase.create(videoId, sessionPort.currentUserId(), request.content())
                    : commentUseCase.reply(videoId, sessionPort.currentUserId(), request.parentCommentId(), request.content());
            return ResponseEntity.status(201).body(comment);
        } catch (CommentUseCase.InvalidCommentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (CommentUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "Like or unlike a comment")
    public ResponseEntity<?> toggleLike(
            @PathVariable String videoId,
            @PathVariable UUID commentId
    ) {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to like comments."));
        }
        try {
            return ResponseEntity.ok(commentUseCase.toggleLike(videoId, commentId, userId));
        } catch (CommentUseCase.InvalidCommentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (CommentUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    public record CreateCommentRequest(String content, UUID parentCommentId) {
        public CreateCommentRequest(String content) {
            this(content, null);
        }
    }
}