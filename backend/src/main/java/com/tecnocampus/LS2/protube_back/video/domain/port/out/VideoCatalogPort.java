package com.tecnocampus.LS2.protube_back.video.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

public interface VideoCatalogPort {

    List<VideoSummary> findAll();

    List<VideoSummary> search(String query);

    Optional<VideoSummary> findById(String id);

    Optional<VideoSummary> updateMetadata(
            String id,
            UUID ownerId,
            String title,
            String description,
            List<String> tags,
            String category
    );
}
