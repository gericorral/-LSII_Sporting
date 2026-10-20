# Fix the ProTube Docker production build

**Approved on:** 2026-10-17
**Approved by:** Project team review required
**Used for:** Fixing the Docker image build when production packaging skips tests

## Prompt

You are a senior DevOps engineer working on the ProTube monorepo. Diagnose and fix a
Docker build failure in `backend/Dockerfile` when the production jar is packaged with
tests skipped:

```text
Failed to execute goal org.jacoco:jacoco-maven-plugin:check
Coverage checks have not been met
```

Inspect `backend/Dockerfile`, `backend/pom.xml`, `.github/workflows/main.yml`,
`compose.yaml`, and `AGENTS.md` before editing. Explain the difference between the
production Docker image build and the CI quality build. The Docker image build may
skip tests and coverage verification only when this is explicit and safe; the CI
pipeline must continue to execute the full test and coverage checks.

Apply the smallest change that allows the production Docker image to be packaged
without running tests or JaCoCo checks, while preserving the existing Maven profiles,
frontend production build, runtime image, environment variables, and Docker Compose
behavior. Do not remove JaCoCo configuration from Maven, lower coverage thresholds,
skip tests in CI, or hide compilation and packaging errors.

Rebuild the backend and frontend images without cache, recreate the Compose services,
and verify that PostgreSQL is healthy, the backend is running, the frontend is
reachable, and `/api/videos` is served successfully through the frontend proxy. If
the local video store is empty, verify the expected empty catalog response rather than
inventing test media.

After implementation, update the Catalan exam study guide in
`docs/GUIA_ESTUDIO_EXAMEN.md` and its Word export with the distinction between Docker
packaging and CI quality gates. Preserve this prompt in `prompts/`, reference it in
the AI change log, and include it in the eventual conventional commit description.

## Notes

The Docker image should contain a production jar built from the current source. The
video store is mounted at runtime through `compose.yaml` and must not be copied into
the image or committed to Git.
