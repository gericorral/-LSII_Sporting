package com.tecnocampus.LS2.protube_back.comment.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CommentControllerTest {
    private final CommentUseCase useCase = mock(CommentUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final CommentController controller = new CommentController(useCase, session);
    private final UUID userId = UUID.randomUUID();

    @Test
    void listsCommentsPublicly() {
        when(useCase.list("1", 0, 20)).thenReturn(new CommentUseCase.CommentPage(
                List.of(), 0, 20, 0, 0, false));

        assertEquals(200, controller.list("1", 0, 20).getStatusCode().value());
    }

    @Test
    void createsCommentUsingSessionIdentity() {
        when(session.currentUserId()).thenReturn(userId);
        Comment comment = new Comment(UUID.randomUUID(), "1", userId, "player", "hello", null);
        when(useCase.create("1", userId, "hello")).thenReturn(comment);

        assertEquals(201, controller.create("1", new CommentController.CreateCommentRequest("hello"))
                .getStatusCode().value());
        verify(useCase).create("1", userId, "hello");
    }

    @Test
    void rejectsAnonymousAndInvalidRequests() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.create("1", new CommentController.CreateCommentRequest("hello"))
                .getStatusCode().value());
        when(session.currentUserId()).thenReturn(userId);
        when(useCase.create("1", userId, " ")).thenThrow(
                new CommentUseCase.InvalidCommentException("Comment cannot be empty."));
        assertEquals(400, controller.create("1", new CommentController.CreateCommentRequest(" "))
                .getStatusCode().value());
        when(useCase.list("missing", 0, 20)).thenThrow(new CommentUseCase.VideoNotFoundException("missing"));
        assertEquals(404, controller.list("missing", 0, 20).getStatusCode().value());
    }
}
