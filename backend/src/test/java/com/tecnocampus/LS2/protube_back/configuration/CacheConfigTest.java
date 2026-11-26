package com.tecnocampus.LS2.protube_back.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheConfigTest {

    @Test
    void createsCatalogAndDetailCaches() {
        CacheManager cacheManager = new CacheConfig().cacheManager();

        assertNotNull(cacheManager.getCache("videoCatalog"));
        assertNotNull(cacheManager.getCache("videoDetail"));
    }
}
