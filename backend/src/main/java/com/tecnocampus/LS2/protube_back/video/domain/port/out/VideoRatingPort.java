package com.tecnocampus.LS2.protube_back.video.domain.port.out;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;

import java.util.UUID;

public interface VideoRatingPort {
    VideoRating find(String videoId, UUID userId);

    void save(String videoId, UUID userId, int rating);
}
