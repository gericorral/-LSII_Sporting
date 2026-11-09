# Backend architecture

The backend is organized by business module and uses a Ports and Adapters structure:

```text
<module>/
  domain/
    model/
    port/in/
    port/out/
    exception/
  application/
    service/
    dto/
  infrastructure/
    adapter/in/web/
    adapter/out/persistence/
    adapter/out/storage/
    config/
```

## Dependency rules

1. `domain` contains framework-independent business code. It must not import Spring, JPA, Hibernate, Jackson or
   infrastructure packages.
2. `application` depends on domain ports and models only. Spring annotations are allowed on application services.
3. Web adapters depend on input ports, never on repositories or persistence entities.
4. Persistence entities and Spring Data repositories remain inside infrastructure output adapters.
5. Storage implementations are adapters selected behind a domain storage port.
6. DTOs crossing application or web boundaries are Java records. Persistence entities never cross those boundaries.
7. Shared technical configuration belongs in `shared` or module infrastructure, not in the domain.

The video catalog, upload and metadata-management slices follow this structure while preserving their existing HTTP
contracts. `ListVideosUseCase`, `UploadVideoUseCase` and `UpdateVideoMetadataUseCase` are input ports. The JDBC
catalog and publication adapters implement output ports, while the local storage adapter owns thumbnail generation,
atomic file moves and multipart handling. PostgreSQL stores video metadata and asset keys; the physical media store
remains an infrastructure detail. This keeps EPIC 4 independent of Spring JDBC, multipart types, Jackson and
filesystem APIs.

The analytics slice follows the same boundary. `ViewOwnVideoAnalyticsUseCase` is the input port used by
`AnalyticsController`, while `VideoAnalyticsPort` hides the file-store implementation from
`VideoAnalyticsService`. `FileVideoAnalyticsAdapter` reads only metadata owned by the authenticated user and
defaults absent counters to zero, so users without uploads receive an empty collection rather than an error.

Video playback uses the same boundaries: `RegisterVideoViewUseCase` is invoked by
`VideoController` and `VideoViewService` delegates to `VideoViewPort`. The
`JdbcVideoViewAdapter` records one row in `video_views`, while
`JdbcVideoAnalyticsAdapter` reads the database view derived from videos, comments,
views and likes. Counter persistence therefore remains outside the application layer.

Favorite videos use the same separation. `ManageVideoFavoriteUseCase` is the input port
used by `VideoFavoriteController`, `VideoFavoriteService` validates authentication and
video existence, and `VideoFavoritePort` hides `JdbcVideoFavoriteAdapter`. The adapter
stores one favorite per user and video, while the service resolves the saved identifiers
through `VideoCatalogPort`, keeping the feature compatible with both production UUIDs and
development file-catalog identifiers.
