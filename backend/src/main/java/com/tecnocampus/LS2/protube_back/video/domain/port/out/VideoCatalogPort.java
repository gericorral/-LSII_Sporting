package com.tecnocampus.LS2.protube_back.video.domain.port.out;

import java.util.List;
import java.util.Optional;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

public interface VideoCatalogPort {

    List<VideoSummary> findAll();

    Optional<VideoSummary> findById(String id);
}
