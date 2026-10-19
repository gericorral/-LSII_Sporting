# Implement video metadata editing

**Approved on:** 2026-10-19
**Approved by:** Project team
**Used for:** Implementing US-07 video metadata editing

## Prompt

Implement US-07 in the existing ProTube architecture. Authenticated users must be able
to edit the title, description, tags and category of videos they own. Persist metadata
through the existing file-backed video catalog using atomic replacement, and enforce
ownership on the backend using the authenticated session user id and stored owner id.
Unauthenticated users and non-owners must not be able to update a video. Add the
frontend editor on the video detail view, preserve existing playback and routing, add
automated tests for validation, persistence, authorization and UI errors, update both
study guide formats, and keep coverage at or above 90%.

## Notes

Existing video metadata without an owner remains readable but is not editable.
