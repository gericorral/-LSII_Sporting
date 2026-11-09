# Keep Google OAuth configured with local placeholders

**Approved on:** 2026-11-09
**Approved by:** Project team
**Used for:** Preventing local startup failures when Google OAuth credentials are absent

## Prompt

Keep Google OAuth configured in the default local development profile. When its
environment credentials are absent or empty, substitute harmless non-secret
placeholder values so the Spring application can start and local
username/password authentication remains available. Real Google OAuth must become
operational simply by providing the environment credentials, without requiring a
new Spring profile. Update the development documentation and validate startup and
tests.

## Notes

OAuth credentials must remain environment-provided and must never be committed.
Environment variables explicitly set to empty strings must be treated the same as
missing variables before Spring Security validates the client registration.
