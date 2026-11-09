# Implement Dark Mode

**Approved on:** 2026-11-09
**Approved by:** Product task request
**Used for:** Implementing a persisted user preference through the backend hexagonal architecture and React shell.

## Prompt

Implement a persisted dark-mode preference in the ProTube monorepo. Reuse the existing
`user_preferences.dark_mode` schema, add domain/application input and output ports,
service, JDBC adapter, authenticated REST endpoints, frontend loading and saving state,
an accessible on/off control, theme styling, automated tests, and the required
architecture and exam-guide documentation. Preserve anonymous light-mode behavior and
roll back the UI when saving fails.
