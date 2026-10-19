package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase.VideoPage;
import com.tecnocampus.LS2.protube_back.video.application.service.VideoUploadService;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final ListVideosUseCase listVideosUseCase;
    private final VideoUploadService videoUploadService;

    public VideoController(ListVideosUseCase listVideosUseCase, VideoUploadService videoUploadService) {
        this.listVideosUseCase = listVideosUseCase;
        this.videoUploadService = videoUploadService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> uploadVideo(
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestPart("video") MultipartFile video,
            HttpSession session
    ) {
        Object userId = session.getAttribute("authenticatedUserId");
        if (!(userId instanceof UUID ownerId)) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to upload videos."));
        }
        try {
            return ResponseEntity.status(201)
                    .body(videoUploadService.upload(title, description, video, ownerId));
        } catch (VideoUploadService.InvalidUploadException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (VideoUploadService.UploadFailedException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "The video could not be published."));
        }
    }

    @GetMapping
    public ResponseEntity<VideoPage> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(listVideosUseCase.listVideos(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoSummary> getVideo(@PathVariable String id) {
        return listVideosUseCase.getVideo(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
