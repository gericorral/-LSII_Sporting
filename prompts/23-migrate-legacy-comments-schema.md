# Migrate the legacy comments schema

**Approved on:** 2026-10-29
**Approved by:** Project team review required
**Used for:** Repairing a persistent PostgreSQL schema mismatch for US-09 comments

## Prompt

Investigate a production HTTP 500 in US-09 comments using Docker Compose logs and the
live PostgreSQL schema. When a persistent legacy `comments.text` column conflicts with
the current `content` model, implement the smallest safe migration in the existing
schema initializer. Rename the legacy column in place, preserve all rows and
identifiers, reject the unsafe state where both columns exist, and make constraints
and indexes idempotent. Add regression tests for legacy migration, repeated startup,
the current schema, conflict detection and JDBC operations. Verify the live endpoints
after rebuilding the backend without deleting the PostgreSQL volume.

## Notes

The migration must remain compatible with PostgreSQL 16 and the H2 development test
database, while preserving the hexagonal persistence boundary.
