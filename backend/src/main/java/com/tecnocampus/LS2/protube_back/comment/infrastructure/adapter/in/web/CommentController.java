package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase.CommentPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/videos/{videoId}/comments")
public class CommentController {
    private final CommentUseCase commentUseCase;
    private final SessionPort sessionPort;

    public CommentController(CommentUseCase commentUseCase, SessionPort sessionPort) {
        this.commentUseCase = commentUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            return ResponseEntity.ok(commentUseCase.list(videoId, page, size));
        } catch (CommentUseCase.InvalidCommentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (CommentUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @PathVariable String videoId,
            @RequestBody CreateCommentRequest request
    ) {
        if (sessionPort.currentUserId() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to comment."));
        }
        try {
            Comment comment = commentUseCase.create(videoId, sessionPort.currentUserId(), request.content());
            return ResponseEntity.status(201).body(comment);
        } catch (CommentUseCase.InvalidCommentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (CommentUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    public record CreateCommentRequest(String content) {
    }
}
