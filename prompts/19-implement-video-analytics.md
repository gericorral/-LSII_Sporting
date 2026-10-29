# Implement US-11 video analytics

**Approved on:** 2026-10-29
**Approved by:** Project team
**Used for:** Implementing US-11 with a hexagonal analytics slice

## Prompt

Implement US-11 for ProTube: an authenticated user must be able to view analytics
for their own uploaded videos, including views, comments and likes; a user without
uploaded videos must receive an empty state instead of an error.

Inspect the existing backend and frontend architecture before changing code. Apply
the approved Ports and Adapters structure from
`prompts/05-hexagonal-vertical-slicing-backend.md`: keep the analytics domain
framework-independent, expose an input port for the use case, hide storage behind
an output port, keep session and HTTP handling in infrastructure adapters, and
preserve the existing video upload and metadata-editing behavior.

Use the configured shared video store rather than hardcoding a path. Scope results
to the authenticated user's `ownerId`, initialize analytics counters for new
uploads, tolerate legacy metadata without counters by using zero values, and
preserve analytics when video metadata is edited. Add the authenticated API
contract, the frontend analytics route and UI states, automated tests for success,
empty, unauthorized and error cases, and update the architecture and exam-study
documentation.

## Notes

This delivery reads counters from file metadata because the current application
does not yet persist view, comment or like events. The output port is intentionally
separate so a relational analytics adapter can replace the file adapter later
without changing the use case or frontend contract.
