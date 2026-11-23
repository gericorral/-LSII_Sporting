# Implement User Subscriptions

**Approved on:** 2026-11-11
**Approved by:** Product task request
**Used for:** Implementing an authenticated user-to-user feature through the backend hexagonal layers and React frontend.

## Prompt

Implement a subscription system in ProTube. Authenticated users must be able to subscribe to and unsubscribe from other users, see the current subscription state on a public profile, and view the users they subscribe to from a dedicated authenticated page. Reuse the existing `subscriptions` database table, preserve the ports-and-adapters architecture, reject self-subscriptions, add automated tests, and update the Catalan exam study guide and Word export.

The video detail view must also load the video's public owner profile and show the
author avatar, name, profile link, and Subscribe/Unsubscribe action below the player.
