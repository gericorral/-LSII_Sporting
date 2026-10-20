package com.tecnocampus.LS2.protube_back.video.application.service;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoPublicationPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VideoUploadService implements UploadVideoUseCase {
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;
    private static final String MP4_CONTENT_TYPE = "video/mp4";

    private final VideoPublicationPort publicationPort;

    public VideoUploadService(VideoPublicationPort publicationPort) {
        this.publicationPort = publicationPort;
    }

    @Override
    public VideoSummary upload(String title, String description, VideoFile video, UUID ownerId) {
        validate(title, description, video);
        try {
            return publicationPort.publish(
                    UUID.randomUUID().toString(), title.trim(), description.trim(), ownerId, video
            );
        } catch (RuntimeException exception) {
            if (exception instanceof InvalidUploadException) {
                throw exception;
            }
            throw new UploadFailedException("Unable to publish the video", exception);
        }
    }

    private void validate(String title, String description, VideoFile video) {
        if (title == null || title.isBlank() || title.trim().length() > 255) {
            throw new InvalidUploadException("Title is required and must contain at most 255 characters.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidUploadException("Description is required.");
        }
        if (video == null || video.content() == null || video.size() <= 0) {
            throw new InvalidUploadException("A video file is required.");
        }
        if (video.size() > MAX_FILE_SIZE) {
            throw new InvalidUploadException("Video files must not exceed 100 MB.");
        }
        if (!MP4_CONTENT_TYPE.equalsIgnoreCase(video.contentType())) {
            throw new InvalidUploadException("Only MP4 video files are supported.");
        }
    }
}
