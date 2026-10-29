# Fix comment timestamp rendering

**Approved on:** 2026-10-29
**Approved by:** Project team review required
**Used for:** Fixing frontend rendering of US-09 comment timestamps

## Prompt

Inspect the actual US-09 comments REST payload and the frontend date formatter. If
Spring serializes `LocalDateTime` as a numeric array while the React component expects
an ISO string, make the smallest compatible frontend fix. Parse both the current
backend array format and ISO strings, validate malformed or missing values, avoid
rendering `Invalid Date`, add regression tests for persisted and newly-created
comments, and preserve US-08 search and US-11 analytics.

## Notes

The current backend response uses arrays such as
`[2026, 10, 29, 19, 2, 45, 66046000]`; the frontend should remain tolerant of ISO
strings for compatibility with alternate serializers and test fixtures.
