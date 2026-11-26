# Configure SonarCloud code quality analysis

**Approved on:** 2026-11-23
**Approved by:** ProTube delivery task
**Used for:** Adding SonarCloud static analysis and coverage reporting to GitHub Actions

## Prompt

Configure SonarCloud for the ProTube monorepo without committing credentials or
making local development depend on SonarCloud. Define the organization, project key,
source and test directories, Java 21 binaries, JaCoCo XML coverage and frontend LCOV
coverage. Add a SonarCloud step to the pull-request and main CI workflow after build,
tests and lint have completed.

The scan must only run when a `SONAR_TOKEN` GitHub Actions secret exists. Preserve
the existing CI and Docker delivery behavior. Add the LCOV frontend reporter while
keeping the existing JSON and HTML reports.

Update `AGENTS.md`, `README.md` and `docs/GUIA_ESTUDIO_EXAMEN.md`, then update
`docs/GUIA_ESTUDIO_EXAMEN.docx` with the SonarCloud implementation.

## Notes

The SonarCloud organization and project key must match the GitHub organization and
SonarCloud project settings. The token belongs only in GitHub Actions secrets.
