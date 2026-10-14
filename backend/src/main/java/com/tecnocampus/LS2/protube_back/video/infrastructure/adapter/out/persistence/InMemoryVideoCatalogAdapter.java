package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemoryVideoCatalogAdapter implements VideoCatalogPort {

    @Override
    public List<String> findAll() {
        return List.of("video1", "video2");
    }
}
