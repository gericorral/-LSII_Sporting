# Fix Flyway checksum mismatch without data loss

**Approved on:** 2026-11-09
**Approved by:** Project team
**Used for:** Repairing a Flyway migration that was modified after being applied

## Prompt

Inspect the Flyway migration history and identify any versioned migration that was
modified after it had already been applied. Restore that migration exactly to its
applied content and move the later schema change into a new, idempotent migration.
Preserve existing data, avoid `flyway repair` and database deletion, keep migrations
ordered and compatible with both fresh and persistent PostgreSQL databases, and
validate startup and the relevant automated tests.

## Notes

Flyway checksums are part of the migration contract. Never silently edit an applied
migration to match the current code or reset a persistent database when a new
migration can safely carry the change.
