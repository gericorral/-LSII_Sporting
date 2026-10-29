package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UpdateVideoMetadataUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.RegisterVideoViewUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase.VideoPage;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final ListVideosUseCase listVideosUseCase;
    private final UploadVideoUseCase uploadVideoUseCase;
    private final UpdateVideoMetadataUseCase updateVideoMetadataUseCase;
    private final RegisterVideoViewUseCase registerVideoViewUseCase;

    public VideoController(
            ListVideosUseCase listVideosUseCase,
            UploadVideoUseCase uploadVideoUseCase,
            UpdateVideoMetadataUseCase updateVideoMetadataUseCase,
            RegisterVideoViewUseCase registerVideoViewUseCase
    ) {
        this.listVideosUseCase = listVideosUseCase;
        this.uploadVideoUseCase = uploadVideoUseCase;
        this.updateVideoMetadataUseCase = updateVideoMetadataUseCase;
        this.registerVideoViewUseCase = registerVideoViewUseCase;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVideo(
            @PathVariable String id,
            @RequestBody UpdateVideoRequest request,
            HttpSession session
    ) {
        Object userId = session.getAttribute("authenticatedUserId");
        if (!(userId instanceof UUID ownerId)) {
            return ResponseEntity.status(401).body(Map.of("message", "Log in to edit videos."));
        }
        try {
            return ResponseEntity.ok(updateVideoMetadataUseCase.update(
                    id, ownerId, request.title(), request.description(), request.tags(), request.category()
            ));
        } catch (UpdateVideoMetadataUseCase.InvalidMetadataException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (UpdateVideoMetadataUseCase.VideoNotFoundException exception) {
            return ResponseEntity.status(403).body(Map.of("message", "You cannot edit this video."));
        }
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
            UploadVideoUseCase.VideoFile videoFile = new UploadVideoUseCase.VideoFile(
                    video.getInputStream(), video.getSize(), video.getContentType()
            );
            return ResponseEntity.status(201)
                    .body(uploadVideoUseCase.upload(title, description, videoFile, ownerId));
        } catch (java.io.IOException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "The video could not be published."));
        } catch (UploadVideoUseCase.InvalidUploadException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (UploadVideoUseCase.UploadFailedException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "The video could not be published."));
        }
    }

    public ResponseEntity<VideoPage> getVideos(int page, int size) {
        return getVideos(page, size, "");
    }

    @GetMapping
    public ResponseEntity<VideoPage> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String query
    ) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(query == null || query.isBlank()
                ? listVideosUseCase.listVideos(page, size)
                : listVideosUseCase.listVideos(page, size, query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoSummary> getVideo(@PathVariable String id) {
        return listVideosUseCase.getVideo(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<Void> registerView(@PathVariable String id) {
        try {
            registerVideoViewUseCase.register(id);
            return ResponseEntity.noContent().build();
        } catch (RegisterVideoViewUseCase.VideoNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    public record UpdateVideoRequest(
            String title,
            String description,
            List<String> tags,
            String category
    ) {
    }
}
