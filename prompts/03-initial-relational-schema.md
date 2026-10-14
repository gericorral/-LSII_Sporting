# Create the ProTube initial relational schema

**Approved on:** 2026-10-14
**Approved by:** Project team review required
**Used for:** Creating the PostgreSQL/Flyway initial schema for the ProTube data model

## Prompt

Act as a software engineer expert in Spring Boot 3.3, Java 21, PostgreSQL, and Flyway. Read all repository Markdown
requirements and apply exactly these changes to the ProTube relational data model:

1. Add `video_likes` with composite primary key `(video_id, user_id)`, `created_at`, and foreign keys with
   `ON DELETE CASCADE` to `videos` and `users`.
2. Replace the physical `video_analytics_daily` table with a SQL view named `video_analytics_summary`. It must
   calculate in real time `view_count` from `video_views`, visible `comment_count` from `comments`, and
   `like_count` from `video_likes`, grouped by `video_id` and `owner_id`. Explain how the view maps to JPA/Spring
   Data using `@Immutable`/`@Subselect` or a repository interface projection.
3. Define referential-integrity and deletion rules. Use `ON DELETE CASCADE` for `video_tags`, `video_categories`,
   `video_assets`, `comment_likes`, `video_likes`, `favorites`, and `video_ratings`. In `comments`, make
   `author_id` nullable with `ON DELETE SET NULL`. Add required unique constraints for email, username, and N:M
   combinations.

Generate the complete PostgreSQL/Flyway-compatible `V1__initial_schema.sql` migration with tables, recommended
indexes, foreign keys, constraints, and the `video_analytics_summary` view. Do not put binary video data in the
database; store file references and keep binaries in the configured file store or object storage.

## Notes

The generated migration must be reviewed by the team before implementation is considered complete. The application
still needs Flyway dependency/configuration and JPA entities/repositories in a later task.
