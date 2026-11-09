package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.RateVideoUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoRatingPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VideoRatingService implements RateVideoUseCase {
    private final VideoRatingPort videoRatingPort;
    private final VideoCatalogPort videoCatalogPort;

    public VideoRatingService(VideoRatingPort videoRatingPort, VideoCatalogPort videoCatalogPort) {
        this.videoRatingPort = videoRatingPort;
        this.videoCatalogPort = videoCatalogPort;
    }

    @Override
    public VideoRating getRating(String videoId, UUID userId) {
        ensureVideoExists(videoId);
        return videoRatingPort.find(videoId, userId);
    }

    @Override
    public VideoRating rate(String videoId, UUID userId, int rating) {
        if (userId == null) {
            throw new InvalidRatingException("Authentication is required.");
        }
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5.");
        }
        ensureVideoExists(videoId);
        videoRatingPort.save(videoId, userId, rating);
        return videoRatingPort.find(videoId, userId);
    }

    private void ensureVideoExists(String videoId) {
        if (videoId == null || videoId.isBlank() || videoCatalogPort.findById(videoId).isEmpty()) {
            throw new VideoNotFoundException(videoId);
        }
    }
}
