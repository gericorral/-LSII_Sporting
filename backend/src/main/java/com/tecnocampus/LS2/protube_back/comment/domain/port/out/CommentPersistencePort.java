package com.tecnocampus.LS2.protube_back.comment.domain.port.out;

import com.tecnocampus.LS2.protube_back.comment.domain.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentPersistencePort {
    List<Comment> findByVideoId(String videoId);

    Comment save(String videoId, UUID authorId, String content);
}
