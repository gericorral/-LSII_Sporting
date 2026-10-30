package com.tecnocampus.LS2.protube_back.comment.domain.port.out;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.comment.domain.model.CommentLike;

import java.util.List;
import java.util.UUID;

public interface CommentPersistencePort {
    List<Comment> findByVideoId(String videoId);

    List<Comment> findByVideoId(String videoId, UUID viewerId);

    Comment save(String videoId, UUID authorId, String content);

    Comment save(String videoId, UUID authorId, UUID parentCommentId, String content);

    boolean existsInVideo(UUID commentId, String videoId);

    CommentLike toggleLike(UUID commentId, UUID userId);
}
