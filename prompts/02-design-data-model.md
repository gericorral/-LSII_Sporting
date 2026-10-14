# Design the ProTube data model

**Approved on:** 2026-10-14
**Approved by:** Project team review required
**Used for:** Designing the persistent relational data model for all ProTube MVP and improvement features

## Prompt

Read every Markdown document in the repository, especially `README.md`, `REQUIREMENT.md`, `CLAUDE.md`,
`DEVELOPMENT.md`, the prompt-preservation rules in `prompts/`, and the documentation for the video grabber.
Design a complete PostgreSQL data model for ProTube that supports every listed MVP story and all selected
improvements, not only the minimum mandatory features.

The design must cover authentication with local credentials and Google OAuth, user profiles, roles, videos
uploaded by users or imported by the video grabber, video metadata and file references, categories, tags, search,
comments with nested replies, comment likes, analytics, video ratings, favorites, subscriptions, and any other
improvement explicitly selected by the team. Distinguish durable database data from video and thumbnail binaries
stored in the configured file store or an external object-storage provider.

For each table or aggregate, define its purpose, primary key, important fields and types, required and optional
fields, uniqueness constraints, foreign keys, cardinality, delete behavior, indexes, and audit timestamps. Explain
which analytics values are derived from event data and which may be maintained as counters. Preserve historical
analytics and enforce rules such as one rating, favorite, subscription, or like per user and target.

Prefer a normalized relational design compatible with Spring Boot 3, Java 21, Spring Data JPA, and PostgreSQL.
Use migrations for the schema rather than relying on `ddl-auto=update` in production. Include a migration and
rollout strategy that does not break existing video-grabber files or local development. Identify unresolved
product decisions explicitly instead of silently inventing behavior.

Do not implement code until the team has reviewed and approved the model. Preserve the prompt in `prompts/` and
reference it in the eventual conventional commit.

## Notes

This is a design prompt only. The team must confirm the proposed scope, naming, authentication/session strategy,
retention policy for view events, and whether external object storage is implemented in the current delivery.
