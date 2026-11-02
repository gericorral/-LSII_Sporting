# Implement US-10 comment replies and likes

**Approved on:** 2026-10-30
**Approved by:** User request
**Used for:** Completing US-10 on the existing hexagonal comments slice

## Prompt

Continue the user stories in `REQUIREMENT.md` after US-11 and implement US-10 in
the current hexagonal architecture without breaking US-09 comments or US-11
analytics. Add authenticated replies with stable parent relationships, persistent
per-user comment likes, server-derived authorship and like state, focused backend
and frontend tests, and update the Catalan exam guide and Word export.

Keep controllers focused on HTTP concerns, keep business rules in the application
service, and hide JDBC/schema details behind output ports and adapters. Preserve
the existing root-comment API and support legacy comment schemas idempotently.
