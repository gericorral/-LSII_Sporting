package com.tecnocampus.LS2.protube_back.video.domain.port.out;

import java.util.List;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

public interface VideoCatalogPort {

    List<VideoSummary> findAll();
}
