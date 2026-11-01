# Migrate video persistence to PostgreSQL

**Approved on:** 2026-11-01
**Approved by:** Project team
**Used for:** Migrating file-backed video metadata and analytics to PostgreSQL while preserving hexagonal ports

## Prompt

Migrate the ProTube video catalog, publication metadata, views and analytics from JSON-backed adapters to PostgreSQL.
Keep the existing hexagonal architecture: do not make controllers or application services depend on JDBC, SQL,
filesystem or multipart details. Add a versioned migration strategy that supports both a clean PostgreSQL database
and the existing deployed schema, normalize the comments content column, persist local media references in the database,
and keep the physical media store separate. Implement JDBC output adapters behind the existing output ports, change
development to PostgreSQL, and add an integration test proving video metadata survives a backend restart. Run the
relevant backend, frontend and Playwright checks and document the resulting behavior.

## Notes

The local media files remain in `video-store`; PostgreSQL stores the domain metadata and asset keys. Existing
comments may use either the legacy `body` column or the current `content` column and the migration must preserve rows.
