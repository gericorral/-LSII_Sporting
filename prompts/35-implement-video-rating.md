# Implement video rating

**Approved on:** 2026-11-09
**Approved by:** Product requirement for the ProTube video-rating improvement
**Used for:** Implementing the video-rating use case through the existing hexagonal video module

## Prompt

Implement only the video-rating improvement in the existing ProTube application. Preserve
the current hexagonal architecture: expose an input port and application service for
rating, an output port and JDBC adapter for persistence, and a web adapter for HTTP.
Allow authenticated users to submit one integer rating from 1 to 5 per video and update
their existing rating. Expose the aggregate average, count and current user's rating,
and add the control to the video detail view. Use a new Flyway migration rather than
modifying an applied migration. Add focused backend and frontend tests and keep all
unrelated product improvements out of scope.

## Notes

The existing PostgreSQL schema already models the intended uniqueness constraint in
`video_ratings`; the migration must remain idempotent for installations created from
older versions. PostgreSQL may expose the `SMALLINT` user rating as a `Short` through
JDBC, so adapters must normalize numeric values without assuming `Integer`. The
frontend rating control uses accessible star buttons, visible selected-state feedback,
and disables submission while the request is in progress.
