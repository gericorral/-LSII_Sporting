# Implement US-08 video search

**Approved on:** 2026-10-29
**Approved by:** Project team review required
**Used for:** Implementing US-08 video search on the current hexagonal, file-backed catalog

## Prompt

Implement US-08 from `REQUIREMENT.md` in the current ProTube architecture. Inspect the
active hexagonal video ports, file catalog adapter, pagination contract, frontend catalog
hook and US-11 analytics before changing code. Extend the existing catalog endpoint with
an optional trimmed, case-insensitive query. Rank exact title matches before partial title,
description and tag matches, use deterministic tie-breaking, filter before pagination,
and preserve the no-query catalog behavior. Add an accessible responsive search control,
clear empty state, loading/error handling, pagination and stale-request protection without
changing analytics or other video features. Add focused backend and frontend tests and
update the Catalan exam guide and Word export.

## Notes

The active catalog is backed by the shared video-store files rather than the relational
schema. Do not bypass ports or expose filesystem paths.
