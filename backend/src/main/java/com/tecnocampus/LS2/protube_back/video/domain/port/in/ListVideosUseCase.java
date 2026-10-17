package com.tecnocampus.LS2.protube_back.video.domain.port.in;

import java.util.List;
import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

public interface ListVideosUseCase {

    List<VideoSummary> listVideos();
}
