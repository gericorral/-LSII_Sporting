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

The initial video listing has been migrated to this structure while preserving `GET /api/videos`. The current
adapter returns the existing seed values; it is an intentional transitional adapter until the PostgreSQL entities
and repositories are implemented.
