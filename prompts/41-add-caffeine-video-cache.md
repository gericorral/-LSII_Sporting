# Add Caffeine caching for the video catalog

**Approved on:** 2026-11-23
**Approved by:** ProTube delivery task
**Used for:** Adding a local Caffeine cache to repeated catalog and video-detail reads

## Prompt

Add Caffeine caching to the Spring Boot backend without changing the REST API or
hexagonal architecture. Cache catalog pages and search results under stable keys,
cache video-detail lookups, and limit entries and lifetime explicitly. Enable Spring
Caching with a Caffeine `CacheManager`, with separate caches for catalog data and
video details.

Invalidate all catalog and detail cache entries after a successful video upload or
video metadata update. Do not cache mutations or user-specific analytics/comment
state. Add focused automated tests for cache configuration and run the relevant
backend tests and coverage checks.

Update `AGENTS.md`, `README.md` and `docs/GUIA_ESTUDIO_EXAMEN.md`, then regenerate
or update `docs/GUIA_ESTUDIO_EXAMEN.docx` with the caching explanation.

## Notes

Caffeine is process-local; it does not replace PostgreSQL as the source of truth.
The cache is a read-performance optimization and must be invalidated on writes.
