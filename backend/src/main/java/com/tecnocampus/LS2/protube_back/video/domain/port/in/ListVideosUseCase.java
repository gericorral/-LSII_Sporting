package com.tecnocampus.LS2.protube_back.video.domain.port.in;

import java.util.Optional;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

public interface ListVideosUseCase {

    VideoPage listVideos(int page, int size);

    VideoPage listVideos(int page, int size, String query);

    Optional<VideoSummary> getVideo(String id);

    record VideoPage(
            java.util.List<VideoSummary> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
    }
}
