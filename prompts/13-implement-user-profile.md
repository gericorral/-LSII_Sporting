# Implement user profile

**Approved on:** 2026-10-19
**Approved by:** Project team review required
**Used for:** Implementing the View and edit profile story (US-03)

## Prompt

Implement US-03 from `REQUIREMENT.md` on top of the existing local registration and
HTTP-session authentication. Support viewing the authenticated user's profile,
editing profile fields, and viewing another user's public profile without edit
controls. Reuse the approved `user_profiles` relational shape, keep controllers
focused on HTTP concerns, validate input and return safe DTOs, persist changes in
the configured database, and add focused backend/frontend tests. Do not expose
private profiles, passwords, or infrastructure paths, and do not implement
unrelated features.

## Notes

The MVP profile fields are display name, description, avatar reference, website,
location, and public visibility. Avatar upload storage is intentionally deferred;
the first slice accepts a safe public avatar URL/reference.
