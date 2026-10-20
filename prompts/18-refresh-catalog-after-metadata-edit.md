# Refresh catalog after metadata edit

**Approved on:** 2026-10-19
**Approved by:** Project team
**Used for:** Keeping catalog state synchronized after US-07 metadata updates

## Prompt

Fix the frontend catalog synchronization after editing video metadata. After a
successful metadata update from the video detail page, refresh the first catalog
page before returning to the catalog so the updated title, description and other
metadata are visible immediately. Preserve pagination, routing, loading/error states,
accessibility and existing tests.

## Notes

The change is frontend state synchronization only; the backend update contract remains
unchanged.
