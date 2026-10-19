# Implement user login

**Approved on:** 2026-10-19
**Approved by:** Project team review required
**Used for:** Implementing the User login story (US-02)

## Prompt

Implement US-02 from `REQUIREMENT.md` on top of the existing registration flow. Reuse
the HTTP session boundary and PBKDF2 password format already used by registration.
Add secure credential verification, a login endpoint, logout/session invalidation,
clear generic credential errors, a React login form, and focused backend/frontend
tests. Do not expose password hashes, reveal whether an email exists, add Google
OAuth, or implement unrelated protected features. Preserve existing public video
catalog behavior and update the exam study guide and its Word export.

## Notes

The implementation must create the session only after credentials are verified.
Invalid credentials must have the same response message and must not create an
authenticated session.
