# Document the Swagger/OpenAPI API

**Approved on:** 2026-11-23
**Approved by:** ProTube delivery task
**Used for:** Adding traceable OpenAPI documentation to the Spring Boot backend and preserving the change in project documentation

## Prompt

Document the ProTube REST API with SpringDoc/OpenAPI while preserving the existing
routes, authentication behavior and profiles. Add a global API title, version,
description, contact, local and Docker server URLs, and a cookie-based session security
scheme for `JSESSIONID`. Annotate authentication, profile, video, comment and analytics
operations with concise summaries and tags so Swagger UI describes the implemented
feature areas. Keep Swagger UI and `/v3/api-docs` enabled in development and disabled
in the production profile, including when Docker Compose uses `prod`. Add or update
focused tests when behavior changes.

Update `AGENTS.md` and `docs/GUIA_ESTUDIO_EXAMEN.md`, then regenerate or update
`docs/GUIA_ESTUDIO_EXAMEN.docx` with the same Swagger/OpenAPI explanation. Do not
include generated files, credentials or private configuration in the prompt or output.

## Notes

Use SpringDoc annotations without changing the HTTP contracts or exposing session
tokens in URLs. Preserve the existing `/api/**` and `/media/**` behavior.
