package com.tecnocampus.LS2.protube_back.video.domain.port.out;

import java.util.List;
import java.util.UUID;

public interface VideoFavoritePort {
    boolean exists(String videoId, UUID userId);

    void add(String videoId, UUID userId);

    void remove(String videoId, UUID userId);

    List<String> findVideoIds(UUID userId);
}
