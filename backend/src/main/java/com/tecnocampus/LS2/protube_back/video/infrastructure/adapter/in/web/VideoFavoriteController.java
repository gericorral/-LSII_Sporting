package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoFavorite;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ManageVideoFavoriteUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class VideoFavoriteController {
    private final ManageVideoFavoriteUseCase favoriteUseCase;
    private final SessionPort sessionPort;

    public VideoFavoriteController(ManageVideoFavoriteUseCase favoriteUseCase, SessionPort sessionPort) {
        this.favoriteUseCase = favoriteUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping("/videos/{videoId}/favorite")
    public ResponseEntity<?> getFavorite(@PathVariable String videoId) {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to manage favorite videos."));
        }
        try {
            return ResponseEntity.ok(favoriteUseCase.getFavorite(videoId, userId));
        } catch (ManageVideoFavoriteUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/videos/{videoId}/favorite")
    public ResponseEntity<?> addFavorite(@PathVariable String videoId) {
        return changeFavorite(videoId, true);
    }

    @DeleteMapping("/videos/{videoId}/favorite")
    public ResponseEntity<?> removeFavorite(@PathVariable String videoId) {
        return changeFavorite(videoId, false);
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> listFavorites() {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to view favorite videos."));
        }
        return ResponseEntity.ok(favoriteUseCase.listFavorites(userId));
    }

    private ResponseEntity<?> changeFavorite(String videoId, boolean add) {
        UUID userId = sessionPort.currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to manage favorite videos."));
        }
        try {
            VideoFavorite result = add
                    ? favoriteUseCase.addFavorite(videoId, userId)
                    : favoriteUseCase.removeFavorite(videoId, userId);
            return ResponseEntity.ok(result);
        } catch (ManageVideoFavoriteUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
