# Structure the ProTube backend with hexagonal architecture

**Approved on:** 2026-10-14
**Approved by:** Project team review required
**Used for:** Migrating the Spring Boot backend to DDD-oriented hexagonal architecture with vertical business slices

## Prompt

Act as a software architect expert in Java 21, Spring Boot 3.3, PostgreSQL and Domain-Driven Design. Inspect the
existing backend source and tests before changing them. Organize the backend by business modules (`user`, `video`,
`comment`, `interaction`, `analytics`, `shared`) and apply strict Ports and Adapters layers inside each module:
framework-free domain models and ports, application services with immutable records, and infrastructure adapters for
web, persistence, storage and configuration.

The domain must not import Spring, JPA/Hibernate, Jackson or infrastructure. Web adapters may depend only on input
ports. Persistence entities and Spring Data repositories must remain in output adapters. Use Strategy for local and
future external storage and Adapter for persistence. Preserve existing routes and behavior while migrating a slice at
a time. Add architecture documentation or an ArchUnit/reflection test. Do not invent out-of-scope product features,
and preserve approved prompt traceability.

## Notes

This task establishes the structure and migrates the existing video listing as a safe first slice. Future user,
comment, interaction and analytics slices should follow the same boundaries.
