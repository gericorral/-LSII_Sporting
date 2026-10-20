package com.tecnocampus.LS2.protube_back.video.domain.port.out;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase.VideoFile;

import java.util.UUID;

public interface VideoPublicationPort {
    VideoSummary publish(String id, String title, String description, UUID ownerId, VideoFile video);
}
