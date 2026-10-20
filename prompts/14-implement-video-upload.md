# Implement the ProTube video upload

**Approved on:** 2026-10-19
**Approved by:** Project team review required
**Used for:** Implementing the Upload a Video user story (US-06)

## Prompt

Read `AGENTS.md`, `README.md`, `REQUIREMENT.md`, `DEVELOPMENT.md`, the backend video
catalog, authentication session flow, shared video-store configuration, Docker Compose
configuration and the frontend routing/forms before changing code.

Implement US-06 following the existing controller-service-port-adapter structure. An
authenticated user must be able to submit a supported MP4 video, title and description.
Reject unauthenticated requests, missing or invalid metadata, unsupported formats and
files above the configured 100 MB limit with clear errors. Do not use the original
filename as a storage path.

Use the configured shared video store, write uploads to temporary files, and publish
the video, thumbnail and metadata only after every part succeeds. Remove temporary and
partially published files when an upload fails. Keep the existing video catalog
compatible and make a successfully uploaded video visible in it.

Add the authenticated frontend upload route and form, with client-side validation and
clear server/network errors. Add focused backend and frontend tests for success,
authorization, validation, unsupported files and cleanup. Update the Catalan study
guide in both Markdown and Word formats and add the change to the AI change log.
Do not implement US-07 metadata editing or change unrelated local configuration.
