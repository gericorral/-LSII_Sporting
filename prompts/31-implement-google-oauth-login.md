# Implement Google OAuth login and registration

**Approved on:** 2026-11-09
**Approved by:** ProTube team
**Used for:** Adding OAuth2/OIDC authentication to the Spring Boot and React applications

## Prompt

Implement the complete Google OAuth2/OIDC authorization-code login and registration flow
in ProTube. Preserve local email/password authentication and the existing HTTP session
contract. Provision new users and profiles using the Google subject as the stable
provider account identifier, never auto-link a local account by email, keep OAuth tokens
server-side, and update persistence migrations, frontend session restoration, tests and
documentation while following the repository hexagonal architecture.

## Notes

OAuth client secrets are supplied only through environment variables.
