package com.tecnocampus.LS2.protube_back.video.domain.port.in;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;

import java.io.InputStream;
import java.util.UUID;

public interface UploadVideoUseCase {

    VideoSummary upload(String title, String description, VideoFile video, UUID ownerId);

    record VideoFile(InputStream content, long size, String contentType) {
    }

    class InvalidUploadException extends RuntimeException {
        public InvalidUploadException(String message) {
            super(message);
        }
    }

    class UploadFailedException extends RuntimeException {
        public UploadFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
