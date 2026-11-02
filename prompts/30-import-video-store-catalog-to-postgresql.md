# Import the video-store catalog into PostgreSQL

**Approved on:** 2026-11-02
**Approved by:** Project owner request
**Used for:** Repairing an empty PostgreSQL-backed catalog when legacy media already exists in `video-store`

## Prompt

Inspect the existing ProTube video catalog, PostgreSQL schema, Docker Compose configuration and
video-store format before changing the implementation. Diagnose why the production catalog can
return no videos when `.json`, `.mp4` and thumbnail files already exist in the mounted
`video-store`. Keep the physical video and thumbnail binaries outside PostgreSQL.

Implement an idempotent startup importer for the `prod` profile. It must scan valid JSON metadata
files from the configured `pro_tube.store.dir`, verify that the matching video and thumbnail
files exist, and persist the video identity, title, description, duration, publication state,
visibility, categories, tags and local asset references in PostgreSQL. Use deterministic video
identifiers derived from the store file identifiers so repeated starts update existing rows
instead of creating duplicates. Store only asset keys such as `<id>.mp4` and `<id>.png` in
`video_assets`; never store the binary file contents in PostgreSQL.

Keep the catalog query backed by PostgreSQL through `JdbcVideoCatalogAdapter`. Preserve the
existing upload flow so newly uploaded videos write their files to the shared store and their
metadata and asset references to PostgreSQL immediately. Ensure Docker Compose uses the
production profile for this behavior, and document that `/media/**` still serves files from
the mounted shared store.

Handle missing media and malformed metadata explicitly with warnings, while allowing unrelated
valid catalog entries to be imported. Database failures must remain visible and must not be
silently treated as invalid metadata. Add or update focused tests for valid imports, repeated
imports, missing media and malformed metadata. Run the relevant backend tests, Docker build and
catalog endpoint checks, and update the project documentation and exam study guide.

## Notes

This repair addresses the mismatch between legacy sample files generated before PostgreSQL
catalog persistence and the current `prod` adapter. It is a synchronization bridge for existing
or manually copied store files, not a replacement for the normal upload transaction.
