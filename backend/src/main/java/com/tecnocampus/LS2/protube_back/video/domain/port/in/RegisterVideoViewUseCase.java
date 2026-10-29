package com.tecnocampus.LS2.protube_back.video.domain.port.in;

public interface RegisterVideoViewUseCase {

    void register(String videoId);

    class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String message) {
            super(message);
        }
    }
}
