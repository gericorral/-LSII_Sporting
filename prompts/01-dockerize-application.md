# Dockerize backend and frontend

**Approved on:** 2026-10-08
**Approved by:** Project team
**Used for:** Dockerizing the ProTube backend and frontend and adding them to `compose.yaml`

## Prompt

I need to add Docker support for the backend and frontend of this repository. First inspect the monorepo structure,
`backend/pom.xml`, `backend/src/main/resources/application.properties`, `frontend/package.json`,
`frontend/.env.production`, `frontend/src/utils/Env.ts`, `DEVELOPMENT.md`, `REQUIREMENT.md`, and the current
`compose.yaml`, and explain where each implementation decision comes from before modifying files.

Create `backend/Dockerfile` to build and run Spring Boot with Java 21 and Maven using the `prod` profile. Since the
production profile also builds the frontend and copies it into the jar, check how this strategy interacts with the
separate frontend container and avoid breaking the existing configuration.

Create `frontend/Dockerfile` to build the React application with Node 22 and serve it with Nginx. Add
`frontend/nginx.conf` to serve the SPA, fall back to `index.html` for client-side routes, and proxy `/api/`,
`/media/`, `/auth/`, `/oauth2/`, and `/login/` requests to the `backend` service on port 8080.

Extend `compose.yaml` without removing PostgreSQL. It must start `db`, `backend`, and `frontend`, persist PostgreSQL
data in a named volume, mount the video directory at `/app/store`, pass the required `ENV_PROTUBE_*` variables to
the backend, wait for PostgreSQL to become healthy with a `healthcheck`, and publish the frontend on port 80 and the
backend on port 8080. Fix indentation, service names, or dependencies that would prevent `docker compose config` or
`docker compose up --build` from working.

Do not put real secrets in tracked files. Preserve the existing local development commands and document how to start
the complete application from a clean clone. Validate the Compose syntax, inspect the Git status, and prepare a
conventional commit describing the backend and frontend containerization. Include this prompt in `prompts/` to preserve
the project-required traceability.

## Notes

The frontend is available at `http://localhost`; the backend remains directly available on port 8080 for diagnostics.
PostgreSQL uses the named volume `postgres_data`. Do not revert unrelated local changes.
