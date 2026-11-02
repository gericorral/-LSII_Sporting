# Add end-to-end tests for the MVP

**Approved on:** 2026-11-01
**Approved by:** Project owner request
**Used for:** Adding browser-level E2E coverage for the ProTube MVP

## Prompt

Inspect the existing ProTube frontend, backend, Docker Compose configuration and
requirements before changing them. Add a Playwright E2E module that exercises the
complete application through the browser, including registration, authentication,
video upload, catalog search, playback navigation and commenting. Keep the backend
hexagonal: E2E tests must use HTTP and the real frontend, application services,
input ports, output ports and infrastructure adapters; do not put test-only business
logic in controllers or domain code. Make the suite reproducible in CI, avoid
committing generated media or secrets, document local execution, and run it against
the containerized stack.
