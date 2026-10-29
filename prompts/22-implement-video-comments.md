# Implement US-09 video comments

**Approved on:** 2026-10-29
**Approved by:** Project team review required
**Used for:** Implementing US-09 video comments on the current hexagonal architecture

## Prompt

Implement US-09 from `REQUIREMENT.md` in the current ProTube architecture. Inspect the
active file-backed video catalog, authentication session port, relational user schema,
frontend video detail view, US-08 search and US-11 analytics before changing code.
Create a comment domain model and input/output ports, validate video existence and
authenticated server-side authorship in the application service, and persist comments
through a JDBC adapter with stable newest-first pagination. Expose public retrieval and
authenticated creation endpoints without trusting an author ID from the client. Add a
responsive comments section with login prompting, validation, loading/error/empty
states, immediate insertion, and load-more pagination. Add focused backend and
frontend tests, preserve search and analytics behavior, and update the Catalan exam
guide and Word export.

## Notes

The active catalog stores video IDs as strings from shared video-store files, so the
comment persistence adapter uses a string `video_id` while retaining a UUID foreign
key to the active relational `users` table.
