package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoRating;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.RateVideoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos/{videoId}/rating")
public class VideoRatingController {
    private final RateVideoUseCase rateVideoUseCase;
    private final SessionPort sessionPort;

    public VideoRatingController(RateVideoUseCase rateVideoUseCase, SessionPort sessionPort) {
        this.rateVideoUseCase = rateVideoUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping
    public ResponseEntity<?> getRating(@PathVariable String videoId) {
        try {
            return ResponseEntity.ok(rateVideoUseCase.getRating(videoId, sessionPort.currentUserId()));
        } catch (RateVideoUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<?> rate(
            @PathVariable String videoId,
            @RequestBody RatingRequest request
    ) {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to rate videos."));
        }
        try {
            VideoRating result = rateVideoUseCase.rate(videoId, userId, request == null ? 0 : request.rating());
            return ResponseEntity.ok(result);
        } catch (RateVideoUseCase.InvalidRatingException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (RateVideoUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    public record RatingRequest(int rating) {
    }
}
