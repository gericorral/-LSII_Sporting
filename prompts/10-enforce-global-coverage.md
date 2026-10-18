# Enforce global test coverage

**Approved on:** 2026-10-18
**Approved by:** Project team review required
**Used for:** Configuring and maintaining the project's global automated test coverage gate

## Prompt

You are a senior full-stack software engineer working on the ProTube monorepo. Configure
and verify a global minimum test coverage of 90% for the backend and frontend.
Before modifying files, inspect `AGENTS.md`, the backend `pom.xml`, the frontend
`package.json` and Jest configuration, all application source directories, existing
tests, CI workflows, and both exam study guide exports.

Run the complete backend and frontend test suites, not only tests related to the latest
feature. Configure JaCoCo to measure the complete backend application bundle and fail
the Maven build when global instruction, line, branch, or method coverage is below
90%. Configure Jest to collect coverage from all frontend application source files
while excluding test files, generated files, entry points that cannot be tested in the
Jest environment, and build-only configuration files. Make Jest fail when global
statements, branches, functions, or lines are below 90%.

Add focused automated tests for every uncovered behavior until both global gates pass.
Do not lower thresholds, exclude application logic merely to hide uncovered code, or
replace real tests with coverage-only assertions. Preserve existing behavior and
report the exact coverage percentages for every metric after validation.

Update `AGENTS.md` with the rule that automated tests must run after every code change
and coverage must remain at or above 90%. Update both
`docs/GUIA_ESTUDIO_EXAMEN.md` and `docs/GUIA_ESTUDIO_EXAMEN.docx` with the coverage
architecture, commands, thresholds, and exam implications. Reference this prompt in
the AI change log. Do not commit generated coverage reports, build output, secrets, or
local environment files.

## Notes

The backend report is generated at `backend/target/site/jacoco/jacoco.xml` and the
frontend report at `frontend/coverage`. Coverage thresholds must be enforced by the
test/build tools so a future regression fails automatically.
