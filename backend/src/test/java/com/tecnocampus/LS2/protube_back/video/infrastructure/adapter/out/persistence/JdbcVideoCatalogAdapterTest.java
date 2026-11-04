package com.tecnocampus.LS2.protube_back.video.infrastructure.adapter.out.persistence;

import com.tecnocampus.LS2.protube_back.video.domain.model.VideoSummary;
import org.mockito.ArgumentMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcVideoCatalogAdapterTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcVideoCatalogAdapter adapter;
    private UUID ownerId;
    private UUID videoId;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        adapter = new JdbcVideoCatalogAdapter(jdbcTemplate);
        ownerId = UUID.randomUUID();
        videoId = UUID.randomUUID();
    }

    @Test
    void findsAndSearchesPublishedVideos() {
        VideoSummary video = video("Travel guide", "A guide", List.of("travel"), "Guides");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoSummary>>any(),
                any(Object[].class))).thenReturn(List.of(video));

        assertEquals(video, adapter.findAll().get(0));
        assertEquals(List.of(video), adapter.search(" TRAVEL "));
        assertEquals(List.of(video), adapter.search(null));
        assertEquals(List.of(video), adapter.search(""));
        assertEquals(List.of(), adapter.search("missing"));
    }

    @Test
    void ranksExactTitleDescriptionAndTagMatches() {
        VideoSummary exact = video("Travel", "Other", List.of(), "");
        VideoSummary description = new VideoSummary("description", "Other", "Travel notes", 1,
                "", "", ownerId, List.of(), "");
        VideoSummary tag = new VideoSummary("tag", "Other", "Other", 1,
                "", "", ownerId, List.of("travel"), "");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoSummary>>any(),
                any(Object[].class))).thenReturn(List.of(description, tag, exact));

        assertEquals(List.of(videoId.toString(), "description", "tag"),
                adapter.search("travel").stream().map(VideoSummary::id).toList());
        assertEquals(exact, adapter.search("Travel").get(0));
    }

    @Test
    void mapsDatabaseRowsAndNormalizesOptionalMediaAndTags() throws Exception {
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoSummary>>any(),
                any(Object[].class))).thenAnswer(invocation -> {
            RowMapper<VideoSummary> mapper = invocation.getArgument(1);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getObject("id", UUID.class)).thenReturn(videoId);
            when(resultSet.getObject("owner_id", UUID.class)).thenReturn(ownerId);
            when(resultSet.getString("title")).thenReturn("Mapped");
            when(resultSet.getString("description")).thenReturn("Description");
            when(resultSet.getObject("duration_seconds")).thenReturn(10);
            when(resultSet.getString("thumbnail_key")).thenReturn(null);
            when(resultSet.getString("video_key")).thenReturn("mapped.mp4");
            when(resultSet.getString("tag_names")).thenReturn("one,, two");
            when(resultSet.getString("category_name")).thenReturn(null);
            return List.of(mapper.mapRow(resultSet, 1));
        });

        VideoSummary mapped = adapter.findAll().get(0);

        assertEquals("", mapped.thumbnailUrl());
        assertEquals("/media/mapped.mp4", mapped.videoUrl());
        assertEquals(List.of("one", " two"), mapped.tags());
        assertEquals(null, mapped.category());
    }

    @Test
    void resolvesIdsAndRejectsInvalidIds() {
        VideoSummary video = video("Title", "Description", List.of(), "");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoSummary>>any(),
                any(Object[].class))).thenReturn(List.of(video));

        assertEquals(Optional.of(video), adapter.findById(videoId.toString()));
        assertEquals(Optional.empty(), adapter.findById("not-a-uuid"));
    }

    @Test
    void updatesMetadataOnlyForTheOwnerAndReplacesTagsAndCategory() {
        VideoSummary existing = video("Old", "Old description", List.of("old"), "Old");
        VideoSummary updated = video("New", "New description", List.of("new", "live"), "Sports");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoSummary>>any(),
                any(Object[].class)))
                .thenReturn(List.of(existing))
                .thenReturn(List.of(updated));
        when(jdbcTemplate.queryForObject(anyString(), eq(UUID.class), any(), any(), any()))
                .thenReturn(UUID.randomUUID());

        assertEquals(Optional.empty(), adapter.updateMetadata(
                videoId.toString(), UUID.randomUUID(), "New", "New description", List.of("new"), "Sports"));
        assertEquals(Optional.of(updated), adapter.updateMetadata(
                videoId.toString(), ownerId, "New", "New description", List.of("new", "live"), "Sports"));
        verify(jdbcTemplate).update(contains("UPDATE videos"), eq("New"), eq("New description"),
                eq(videoId), eq(ownerId));
        verify(jdbcTemplate).update(contains("DELETE FROM video_tags"), eq(videoId));
        verify(jdbcTemplate).update(contains("DELETE FROM video_categories"), eq(videoId));
    }

    @Test
    void updatesWithoutCategoryAndHandlesEmptyCatalog() {
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<VideoSummary>>any(),
                any(Object[].class))).thenReturn(List.of());

        assertEquals(List.of(), adapter.findAll());
        assertEquals(Optional.empty(), adapter.updateMetadata(
                videoId.toString(), ownerId, "New", "Description", List.of(), " "));
        verify(jdbcTemplate, never()).update(contains("UPDATE videos"));
    }

    private VideoSummary video(String title, String description, List<String> tags, String category) {
        return new VideoSummary(videoId.toString(), title, description, 42,
                "/media/thumb.webp", "/media/video.mp4", ownerId, tags, category);
    }
}
