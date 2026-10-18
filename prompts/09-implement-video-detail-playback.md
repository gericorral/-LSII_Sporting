# Implement video detail and playback

**Approved on:** 2026-10-18
**Approved by:** Project team review required
**Used for:** Implementing the Play a Video and View Its Details user story (US-05)

## Prompt

You are a senior full-stack software engineer working on the ProTube monorepo. Implement
US-05 from `REQUIREMENT.md` on top of the existing US-04 file-backed video catalog.
Before modifying files, inspect `AGENTS.md`, `REQUIREMENT.md`, the existing video
controller, application service, input and output ports, file adapter, media resource
configuration, frontend API hook, video grid, tests, and study guide.

Add a detail endpoint for a video identifier that returns the same stable video summary
contract as the catalog and returns HTTP 404 when the video does not exist or is
incomplete. Keep the controller focused on HTTP concerns, put lookup behavior behind
the existing application and persistence ports, reuse the configured
`ENV_PROTUBE_STORE_DIR`, and reject unsafe identifiers without exposing filesystem
paths. Reuse the existing `/media/**` resource handler for the video file.

Update the frontend so a catalog card opens a detail view with an accessible HTML5
video player, thumbnail poster, title, description, duration when available, a
back-to-catalog link, loading state, and an explicit not-found/error state. Do not add
a router dependency for this small flow if the existing application does not use one.
Add focused backend and frontend tests for successful lookup and not-found behavior.
Update both `docs/GUIA_ESTUDIO_EXAMEN.md` and its Word export with the architecture,
behavior, error handling, tests, and exam points. Preserve this prompt in `prompts/`
and reference it in the AI change log.

## Notes

The video store is configured at runtime and generated media must not be committed.
The existing catalog response already contains the public thumbnail and video URLs.
