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
contracts. `ListVideosUseCase`, `UploadVideoUseCase` and `UpdateVideoMetadataUseCase` are input ports. The file
catalog and publication adapters implement output ports, so JSON serialization, thumbnail generation, atomic file
moves and multipart handling remain outside the application services. This keeps EPIC 4 independent of Spring
multipart types, Jackson and filesystem APIs.
