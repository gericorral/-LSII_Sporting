# Synchronize profile avatar with the header

**Approved on:** 2026-11-02
**Approved by:** Project team review required
**Used for:** Keeping the authenticated profile avatar synchronized between the profile page and the application header

## Prompt

In the existing ProTube React and TypeScript frontend, ensure that the avatar
configured in the authenticated user's profile is also displayed as a small
avatar beside the header greeting on the video catalog page.

When the own profile is loaded or saved, synchronize the returned
`displayName` and `avatarPath` with the authenticated frontend session state.
Use the avatar URL as an image source, keep the configured display name in the
greeting, and use the username and its initial as safe fallbacks when no
display name or avatar URL exists. Preserve the username as the account
identifier and do not alter public profile behavior.

Keep the change within the existing component structure, add focused frontend
tests for loading and saving the profile avatar, and update the study
documentation and Word export. Do not introduce avatar file-upload storage or
change unrelated authentication behavior.

## Notes

The profile currently accepts a public avatar URL/reference. The header avatar
is intentionally a small presentation element and uses the existing profile
data rather than introducing a second persistence mechanism.
