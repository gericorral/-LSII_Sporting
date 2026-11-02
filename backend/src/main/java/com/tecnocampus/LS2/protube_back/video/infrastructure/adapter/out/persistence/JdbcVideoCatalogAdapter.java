package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.video.domain.port.out.VideoCatalogPort;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
@Profile("prod")
public class JdbcVideoCatalogAdapter implements VideoCatalogPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcVideoCatalogAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<VideoSummary> findAll() {
        return queryCatalog(null);
    }

    @Override
    public List<VideoSummary> search(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return findAll();
        }
        return queryCatalog(null).stream()
                .map(video -> new ScoredVideo(video, relevance(video, normalized)))
                .filter(video -> video.score() > 0)
                .sorted(Comparator.comparingInt(ScoredVideo::score).reversed()
                        .thenComparing(video -> video.video().title(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(video -> video.video().id()))
                .map(ScoredVideo::video)
                .toList();
    }

    @Override
    public Optional<VideoSummary> findById(String id) {
        try {
            UUID videoId = UUID.fromString(id);
            return queryCatalog(videoId).stream().findFirst();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<VideoSummary> updateMetadata(
            String id, UUID ownerId, String title, String description, List<String> tags, String category) {
        Optional<VideoSummary> existing = findById(id);
        if (existing.isEmpty() || ownerId == null || !ownerId.equals(existing.get().ownerId())) {
            return Optional.empty();
        }
        UUID videoId = UUID.fromString(id);
        jdbcTemplate.update(
                "UPDATE videos SET title = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND owner_id = ?",
                title, description, videoId, ownerId);
        replaceTags(videoId, tags);
        replaceCategory(videoId, category);
        return findById(id);
    }

    private List<VideoSummary> queryCatalog(UUID id) {
        String condition = id == null ? " " : " AND v.id = ? ";
        Object[] args = id == null ? new Object[0] : new Object[]{id};
        return jdbcTemplate.query("""
                SELECT v.id, v.owner_id, v.title, v.description, v.duration_seconds,
                       MAX(CASE WHEN a.asset_type = 'THUMBNAIL' THEN a.storage_key END) AS thumbnail_key,
                       MAX(CASE WHEN a.asset_type = 'VIDEO' THEN a.storage_key END) AS video_key,
                       COALESCE(string_agg(DISTINCT t.name, ',' ORDER BY t.name), '') AS tag_names,
                       COALESCE(MAX(c.name), '') AS category_name
                FROM videos v
                LEFT JOIN video_assets a ON a.video_id = v.id
                LEFT JOIN video_tags vt ON vt.video_id = v.id
                LEFT JOIN tags t ON t.id = vt.tag_id
                LEFT JOIN video_categories vc ON vc.video_id = v.id
                LEFT JOIN categories c ON c.id = vc.category_id
                WHERE v.status = 'PUBLISHED' AND v.visibility = 'PUBLIC'""" + condition + """
                GROUP BY v.id, v.owner_id, v.title, v.description, v.duration_seconds
                ORDER BY v.published_at DESC, v.id DESC
                """, (rs, rowNum) -> {
            UUID videoId = rs.getObject("id", UUID.class);
            return new VideoSummary(
                    videoId.toString(),
                    rs.getString("title"),
                    rs.getString("description"),
                    (Integer) rs.getObject("duration_seconds"),
                    mediaUrl(rs.getString("thumbnail_key")),
                    mediaUrl(rs.getString("video_key")),
                    rs.getObject("owner_id", UUID.class),
                    splitTags(rs.getString("tag_names")),
                    rs.getString("category_name")
            );
        }, args);
    }

    private void replaceTags(UUID videoId, List<String> tags) {
        jdbcTemplate.update("DELETE FROM video_tags WHERE video_id = ?", videoId);
        for (String tag : tags) {
            String normalized = tag.trim();
            UUID tagId = jdbcTemplate.queryForObject("""
                    INSERT INTO tags (id, name, slug) VALUES (?, ?, ?)
                    ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                    RETURNING id
                    """, UUID.class, UUID.randomUUID(), normalized, slug(normalized));
            jdbcTemplate.update("INSERT INTO video_tags (video_id, tag_id) VALUES (?, ?)", videoId, tagId);
        }
    }

    private void replaceCategory(UUID videoId, String category) {
        jdbcTemplate.update("DELETE FROM video_categories WHERE video_id = ?", videoId);
        if (category == null || category.isBlank()) {
            return;
        }
        String normalized = category.trim();
        UUID categoryId = jdbcTemplate.queryForObject("""
                INSERT INTO categories (id, name, slug) VALUES (?, ?, ?)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id
                """, UUID.class, UUID.randomUUID(), normalized, slug(normalized));
        jdbcTemplate.update("INSERT INTO video_categories (video_id, category_id) VALUES (?, ?)",
                videoId, categoryId);
    }

    private String mediaUrl(String key) {
        return key == null ? "" : "/media/" + key;
    }

    private List<String> splitTags(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> tags = new ArrayList<>();
        for (String tag : value.split(",")) {
            if (!tag.isBlank()) {
                tags.add(tag);
            }
        }
        return List.copyOf(tags);
    }

    private String slug(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    private int relevance(VideoSummary video, String query) {
        int score = 0;
        String title = video.title().toLowerCase(Locale.ROOT);
        if (title.equals(query)) score += 1000;
        else if (title.contains(query)) score += 500;
        if (video.description().toLowerCase(Locale.ROOT).contains(query)) score += 100;
        if (video.tags().stream().map(tag -> tag.toLowerCase(Locale.ROOT)).anyMatch(tag -> tag.contains(query))) {
            score += 50;
        }
        return score;
    }

    private record ScoredVideo(VideoSummary video, int score) {
    }
}
