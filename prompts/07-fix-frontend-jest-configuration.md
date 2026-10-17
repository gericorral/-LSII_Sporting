# Fix the ProTube frontend Jest configuration

**Approved on:** 2026-10-17
**Approved by:** Project team review required
**Used for:** Fixing the frontend test runner configuration and preserving CI test execution

## Prompt

You are a senior frontend build and test engineer working on the ProTube monorepo.
Diagnose and fix the frontend Jest startup error:

```text
Jest: Failed to parse the TypeScript config file
TypeError: value.replace is not a function
```

First inspect `frontend/package.json`, `frontend/package-lock.json`,
`frontend/jest.config.ts`, `frontend/babel.config.cjs`, `frontend/tsconfig*.json`,
`frontend/jest.setup.ts`, and the existing test files. Determine whether the failure
comes from the Jest configuration format, the installed Jest/transformer versions, or
the TypeScript/ES module setup before editing anything.

Choose the smallest repository-consistent fix that allows Jest to load its configuration
from both the command line and IntelliJ IDEA. Reuse the existing Babel presets and
installed dependencies where possible. If changing the configuration file format is
necessary, preserve the existing test environment, setup file, TypeScript/JSX
transformation, coverage reporters, and 75% global coverage thresholds. Do not weaken
coverage thresholds, remove tests, hide errors, or add unrelated build changes.

Run the full frontend test command in CI mode and update only snapshots that are
expected to change because of already-intended UI output. Also run the frontend build
and report any pre-existing lint or environment issues separately from failures caused
by this fix.

After implementation, update the Catalan exam study guide in
`docs/GUIA_ESTUDIO_EXAMEN.md` and its Word export with the cause, the configuration
decision, and what the team should understand for the exam. Preserve this prompt in
`prompts/`, reference it in the AI change log, and include it in the eventual
conventional commit description.

## Notes

The project currently uses Jest 30, `ts-jest` 29, Babel, TypeScript, React, and the
`jest-environment-jsdom` environment. The solution must not depend on a developer's
absolute local path or an IntelliJ-only workaround.
