package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.ListVideosUseCase.VideoPage;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final ListVideosUseCase listVideosUseCase;

    public VideoController(ListVideosUseCase listVideosUseCase) {
        this.listVideosUseCase = listVideosUseCase;
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
