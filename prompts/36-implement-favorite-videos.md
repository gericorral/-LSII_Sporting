# Implement Favorite Videos

**Approved on:** 2026-11-09
**Approved by:** Product task request
**Used for:** Implementing an authenticated feature through the backend hexagonal layers and React frontend.

## Prompt

Implement Favorite Videos across the ProTube monorepo. Inspect the existing architecture first and preserve
the ports-and-adapters boundaries. Add authenticated add, remove, status, and list operations, persist one
favorite per user and video, support both UUID production identifiers and numeric development file-catalog
identifiers, expose the feature in the video detail view and an authenticated favorites page, add automated
tests, and update the Catalan exam study guide and Word export.

## Notes

The existing database model already contained a `favorites` table without application behavior. The migration
must preserve existing rows and remove the UUID-only video constraint so file-catalog identifiers remain valid.
