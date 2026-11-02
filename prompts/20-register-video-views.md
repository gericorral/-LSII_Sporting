# Register video views

**Approved on:** 2026-10-29
**Approved by:** Project team
**Used for:** Implementing persistent video playback analytics with hexagonal architecture

## Prompt

Implement the missing video view tracking for US-11 using the existing hexagonal
architecture. Add an input port and application service for registering a view,
an output port and file persistence adapter that atomically increments the
JSON analytics counter while preserving existing metadata, and a web endpoint.
Trigger the endpoint once when the frontend video player starts playback. Add
backend and frontend tests for successful registration, missing videos, metadata
preservation, and duplicate play events. Update the architecture and study-guide
documentation.

## Notes

The client-side guard prevents repeated play events during one detail-page
instance; the backend remains the source of truth for persisted counters.
