# Select the video catalog source by environment

**Approved on:** 2026-11-02
**Approved by:** Project team review required
**Used for:** Loading the video catalog from the correct persistence adapter in development and production

## Prompt

In the ProTube Spring Boot backend, configure the existing video catalog
adapters so that the development environment reads the initial videos and
thumbnails directly from the shared `video-store` directory, while the
production environment reads persisted video metadata and asset references
from PostgreSQL.

Use the existing hexagonal architecture: keep the application service
dependent on the `VideoCatalogPort`, keep file and JDBC access inside output
adapters, and select the adapters through Spring profiles. The development
file adapter must be active for the `dev` profile and the JDBC adapter for the
`prod` profile. Do not put filesystem or database-specific logic in the
application layer, do not modify video files, and do not rely on a fallback
from an empty production database to local files.

Add or update focused tests and documentation so the catalog source and media
URLs remain verifiable. Preserve search, pagination, metadata editing, and
the existing `/media/{id}` resource behavior.

## Notes

The shared development store contains the initial `.json`, `.mp4`, and `.webp`
files. Production video persistence is managed by PostgreSQL and Flyway.
