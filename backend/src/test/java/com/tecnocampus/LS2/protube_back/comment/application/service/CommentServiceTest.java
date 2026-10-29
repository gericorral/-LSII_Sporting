package com.tecnocampus.LS2.protube_back.comment.application.service;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.port.in.CommentUseCase;
import com.tecnocampus.LS2.protube_back.comment.domain.port.out.CommentPersistencePort;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CommentServiceTest {
    private final CommentPersistencePort persistence = mock(CommentPersistencePort.class);
    private final VideoCatalogPort catalog = mock(VideoCatalogPort.class);
    private final CommentService service = new CommentService(persistence, catalog);
    private final UUID authorId = UUID.randomUUID();

    @Test
    void createsTrimmedCommentForAnExistingVideoAndAuthenticatedAuthor() {
        Comment saved = new Comment(UUID.randomUUID(), "1", authorId, "player", "Hello", null);
        when(catalog.findById("1")).thenReturn(Optional.of(video()));
        when(persistence.save("1", authorId, "Hello")).thenReturn(saved);

        assertEquals(saved, service.create("1", authorId, "  Hello  "));
        verify(persistence).save("1", authorId, "Hello");
    }

    @Test
    void rejectsUnauthenticatedEmptyAndOverlongComments() {
        assertThrows(RuntimeException.class, () -> service.create("1", null, "hello"));
        assertThrows(RuntimeException.class, () -> service.create("1", authorId, "  "));
        when(catalog.findById("1")).thenReturn(Optional.of(video()));
        assertThrows(RuntimeException.class, () -> service.create("1", authorId, "x".repeat(2001)));
    }

    @Test
    void rejectsMissingVideosAndInvalidPagination() {
        when(catalog.findById("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create("missing", authorId, "hello"));
        assertThrows(RuntimeException.class, () -> service.create(null, authorId, "hello"));
        assertThrows(RuntimeException.class, () -> service.list("1", -1, 20));
        assertThrows(RuntimeException.class, () -> service.list("1", 0, 101));
    }

    @Test
    void listsNewestCommentsWithPaginationMetadata() {
        when(catalog.findById("1")).thenReturn(Optional.of(video()));
        List<Comment> comments = List.of(
                new Comment(UUID.randomUUID(), "1", authorId, "player", "new", null),
                new Comment(UUID.randomUUID(), "1", authorId, "player", "old", null)
        );
        when(persistence.findByVideoId("1")).thenReturn(comments);

        assertEquals(List.of(comments.get(1)), service.list("1", 1, 1).content());
        assertEquals(2, service.list("1", 0, 1).totalElements());
    }

    @Test
    void returnsEmptyPageMetadataAndRejectsNullContent() {
        when(catalog.findById("1")).thenReturn(Optional.of(video()));
        when(persistence.findByVideoId("1")).thenReturn(List.of());

        CommentUseCase.CommentPage page = service.list("1", 0, 20);

        assertEquals(0, page.totalPages());
        assertEquals(false, page.hasNext());
        assertThrows(RuntimeException.class, () -> service.create("1", authorId, null));
    }

    private VideoSummary video() {
        return new VideoSummary("1", "Video", "", null, "/media/1.webp", "/media/1.mp4");
    }
}
