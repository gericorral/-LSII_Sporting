# Keep the ProTube schema within the approved scope

**Approved on:** 2026-10-14
**Approved by:** Project team review required
**Used for:** Reviewing the ProTube schema against the official MVP and Product Improvements scope

## Prompt

Review `README.md`, `REQUIREMENT.md`, and the existing ProTube Flyway schema. Enforce a strict scope boundary:
the database may contain only tables and fields that directly support the MVP requirements or the official Product
Improvements listed in `README.md`.

The MVP schema must contain only users, user profiles, videos, video assets, categories, video categories, tags,
video tags, comments, comment likes, video views, video likes, and the real-time `video_analytics_summary` view.
Official Product Improvements may add auth accounts and sessions for authentication/OAuth, video ratings, favorites,
subscriptions, and user preferences for dark mode, language/i18n, and accessibility.

Do not add any feature not explicitly listed in the official MVP or Product Improvements. The README requires
professor approval before implementing features outside that list. Preserve the required foreign keys, deletion
rules, uniqueness constraints, and indexes for the retained model. Keep video binaries outside PostgreSQL and store
only their file references.

## Notes

This prompt is a scope review, not permission to implement additional product features. Any future feature outside
the official list requires explicit professor approval before schema changes.
