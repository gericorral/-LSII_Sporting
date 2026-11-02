# Development guide

How to set up, run, test and build ProTube locally. For the assignment rules see [README.md](./README.md), and for
the MVP spec see [REQUIREMENT.md](./REQUIREMENT.md).

## Repository layout

| Folder                   | What it is                                                                 |
|--------------------------|----------------------------------------------------------------------------|
| `backend/`               | Spring Boot 3.3 REST API (Java 21, Maven). Serves `/api/**` and `/media/**` |
| `frontend/`              | React 19 + TypeScript SPA (Vite)                                           |
| `tooling/videoGrabber/`  | Python script that downloads sample videos into the video store folder     |
| `resources/`             | Images for the docs and `video_list.txt` (input for the video grabber)     |
| `.run/`                  | Shared IntelliJ IDEA run configurations                                    |
| `prompts/`               | Approved AI prompts (see the "Usage of AI" section of the README)          |

## Prerequisites

* **Java 21** (JDK; CI uses Amazon Corretto 21).
* **Maven 3.9+**. The Maven wrapper (`mvnw`) is **not** committed; either use your local `mvn`, or generate the
  wrapper once inside `backend/` with `mvn -N wrapper:wrapper` (CI does the equivalent). The commands below use
  `mvn`; swap in `./mvnw` if you generated it.
* **Node.js 22** and npm (CI uses Node 22; the production Maven build downloads Node v22.12.0 on its own).
* **Python 3, `yt-dlp` and `ffmpeg`** — only if you need to generate the sample videos.

## 1. Generate the video store

The video grabber writes physical media to the shared *store* folder. The running backend uses PostgreSQL for video
identity and metadata, while `video_assets` stores the corresponding media keys.

```bash
cd tooling/videoGrabber
pip install -r dependencies.txt
python3 main.py --store=/absolute/path/to/videos --id=10 --videos=../../resources/video_list.txt
```

* `--id` is your group id (used to pick the set of videos).
* `--recreate` wipes and regenerates an existing store folder (without it, the script exits if the folder exists).
* Use `python` instead of `python3` on Windows if that's how Python is installed.

## 2. Environment variables

| Variable                           | Needed for              | Description                                                                                      |
|------------------------------------|-------------------------|--------------------------------------------------------------------------------------------------|
| `ENV_PROTUBE_STORE_DIR`            | optional locally, required for external/production stores | Store folder override. If omitted during local development, the backend uses `../video-store/` relative to `backend/`; Docker uses the repository `./video-store` mount. End an override with a path separator (`/` or `\`). |
| `ENV_PROTUBE_GOOGLE_CLIENT_ID`     | Google OAuth login      | Google OAuth client id                                                                           |
| `ENV_PROTUBE_GOOGLE_CLIENT_SECRET` | Google OAuth login      | Google OAuth client secret                                                                       |
| `ENV_PROTUBE_DB_HOST`              | `dev`/`prod` profiles   | PostgreSQL host (defaults to `localhost` locally and `db` in Docker)                             |
| `ENV_PROTUBE_DB_USER`              | `dev`/`prod` profiles   | PostgreSQL database user                                                                           |
| `ENV_PROTUBE_DB_PWD`               | `dev`/`prod` profiles   | PostgreSQL database password                                                                       |
| `ENV_PROTUBE_DB`                   | `dev`/`prod` profiles   | PostgreSQL database name (see [Using PostgreSQL](#using-postgresql))                              |

Never commit real secrets. Set them in your shell, your OS, or your (local, uncommitted) IDE run configuration.

```bash
# Linux / macOS
export ENV_PROTUBE_STORE_DIR=/home/me/protube/videos/
```

```powershell
# Windows PowerShell (current session, only needed to use another store)
$env:ENV_PROTUBE_STORE_DIR = "D:\code\videos\"
```

## 3. Development mode

In development the backend and frontend run as two separate processes:

```
Browser ──► Vite dev server (http://localhost:5173) ──fetch──► Spring Boot (http://localhost:8080/api, /media)
```

### Backend

```bash
cd backend
mvn spring-boot:run
```

* Starts on **http://localhost:8080** with the `dev` profile (active by default through the Maven `dev` profile).
* `dev` and `prod` both use PostgreSQL. Flyway applies the versioned schema migrations at startup; data is retained
  across backend restarts. The default development connection is `localhost:5432/protube`.
* From IntelliJ you can instead use the shared **ProtubeBackApplication** run configuration (`.run/`). It
  resolves the store to `$PROJECT_DIR$/video-store/`, so the same configuration works after every clone.
* The repository-level `video-store/` directory is ignored by Git because it contains generated media. Each
  developer must generate or copy the sample files there once, but no configuration path needs to be changed.
* CORS is open for `/api/**` and `/auth/**`, so the Vite dev server can call the API directly.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

* Starts on **http://localhost:5173** with hot reload (or use the **frontend [DEV]** IntelliJ run configuration).
* API and media URLs come from `frontend/.env.development`:
  ```
  VITE_API_DOMAIN=http://localhost:8080
  VITE_MEDIA_DOMAIN=http://localhost:8080
  ```
  and are read through `src/utils/Env.ts` (`API_BASE_URL = ${VITE_API_DOMAIN}/api`,
  `MEDIA_BASE_URL = ${VITE_MEDIA_DOMAIN}/media`). To override locally without touching the committed file, create
  `frontend/.env.development.local`.

## 4. Tests, lint and formatting

### Backend

```bash
cd backend
mvn clean verify                          # build + all tests
mvn test -Dtest=ClassName                 # one test class
mvn test -Dtest=ClassName#methodName      # one test method
mvn clean verify -Pcoverage               # tests + JaCoCo report → target/site/jacoco/index.html
```

### Frontend

```bash
cd frontend
npm run test                              # Jest + Testing Library, coverage → coverage/
npx jest path/to/File.test.tsx            # a single test file
npm run lint                              # ESLint
npm run lint-fix                          # ESLint with autofix
npm run format                            # Prettier
```

The MVP requires **more than 50% coverage** on both apps.

### End-to-end tests

The Playwright suite in `e2e/` runs against the complete Docker Compose stack through
the browser. It covers registration, authenticated upload, catalog search, playback
navigation and commenting without importing frontend components or backend classes.

```powershell
docker compose up --build -d
cd e2e
npm ci
npx playwright install chromium
npm test
docker compose down
```

The upload scenario uses an in-memory synthetic MP4 payload. It exercises the real
multipart contract and storage adapter without committing generated media.

`postgres-persistence.spec.ts` additionally uploads a video, restarts only the backend container, and verifies that
the catalog can still find the video. The media files remain in the local store while video identity, metadata,
asset references, views and analytics are persisted in PostgreSQL.

Compose persists PostgreSQL in the repository-level `postgres-data/` directory and media in `video-store/`. Both
directories are ignored by Git and survive `docker compose down` and subsequent `docker compose up --build` runs.
Do not delete either directory or use `docker compose down -v` when you need to preserve local data.

### CI

`.github/workflows/main.yml` runs on every PR and push to `main`:

1. Frontend: `npm ci` → `npm run build` → `npm run test` → `npm run lint`
2. Backend: `./mvnw clean verify -Pcoverage`
3. Uploads both coverage reports as build artifacts.
4. Starts Docker Compose and runs the Playwright E2E suite against the complete stack.

Before opening a PR, run the same steps locally so the pipeline stays green.

## 5. Production mode

In production the frontend is bundled **inside** the backend jar and everything is served by Spring Boot from a
single origin (http://localhost:8080). The Maven `prod` profile:

1. Downloads Node/npm into `backend/target/`, runs `npm install` and `npm run build:prod` in `frontend/`.
2. Copies `frontend/dist/` into the jar's `static/` folder.
3. Activates the Spring `prod` profile (`pro_tube.load_initial_data=false`, `ddl-auto=validate`) and applies Flyway
   migrations before starting the application.

`frontend/.env.production` leaves `VITE_API_DOMAIN`/`VITE_MEDIA_DOMAIN` empty, so the built SPA calls the API with
relative URLs (`/api`, `/media`) on the same host.

Docker Compose uses the `prod` Spring profile for the shared local stack. At startup, `VideoStoreDatabaseImporter`
imports the JSON metadata and local asset references from `video-store/` into PostgreSQL idempotently. The
`JdbcVideoCatalogAdapter` then queries PostgreSQL for the catalog, while the `.mp4` and thumbnail files continue to be
served from the mounted `video-store/` directory.

### Run directly with Maven

```bash
cd backend
mvn -Pprod spring-boot:run
```

(IntelliJ: the shared **Production** run configuration does the same.)

### Build and run the jar

```bash
cd backend
mvn -Pprod clean package
java -jar target/protube-back-0.0.1-SNAPSHOT.jar
```

(IntelliJ: the **Create jar** run configuration runs `package` with the `prod` profile.) Remember that
`ENV_PROTUBE_STORE_DIR`, `ENV_PROTUBE_DB_USER` and `ENV_PROTUBE_DB_PWD` must be set in the environment where the jar
runs.

To only preview the production frontend bundle, without the backend: `cd frontend && npm run build:prod && npm run preview`.

### Using PostgreSQL

Both `dev` and `prod` profiles use PostgreSQL and apply Flyway migrations at startup. PostgreSQL is required for
durable domain data (NFR-01 in [REQUIREMENT.md](./REQUIREMENT.md)). To run locally:

1. Start a PostgreSQL server (e.g. `docker run -d --name protube-db -p 5432:5432 -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=protube postgres:16`) or use `docker compose up -d`.
2. Set `ENV_PROTUBE_DB_HOST` (for example `localhost`), `ENV_PROTUBE_DB` (for example `protube`),
   `ENV_PROTUBE_DB_USER` (for example `postgres`) and `ENV_PROTUBE_DB_PWD`.
3. Start the backend. Flyway creates the schema on a clean database or baselines the existing schema before applying
   the video persistence migration.

## 6. Workflow reminders

* `main` is protected: work in a branch and open a Pull Request; it needs at least one approval and all Copilot
  review comments resolved before merging.
* Use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/#summary) and reference the task.
* If AI generated part of a change, mark it in the commit message and reference the prompt from `prompts/` that was
  used (see [prompts/README.md](./prompts/README.md)).
* Setting up the GitHub repository: [docs/GITHUB_SETUP.md](./docs/GITHUB_SETUP.md).

## Troubleshooting

* **`Could not resolve placeholder 'ENV_PROTUBE_STORE_DIR'`** (or `ENV_PROTUBE_DB_USER`, ...): the variable isn't
  set in the environment of the process running the app. IDEs usually need a restart to pick up new OS-level
  variables.
* **Videos list loads but thumbnails/videos 404**: check that `ENV_PROTUBE_STORE_DIR` ends with a path separator and
  points to the folder containing the `.mp4`/`.webp` files.
* **Frontend can't reach the API in dev**: make sure the backend is running on port 8080, or adjust
  `VITE_API_DOMAIN` in `.env.development.local`.
* **Docker upload returns a generic error for files larger than 1 MB**: rebuild the frontend image after
  confirming that `frontend/nginx.conf` contains `client_max_body_size 100M`. Nginx otherwise applies its
  default 1 MB request limit before the multipart request reaches Spring Boot.
* **Port already in use**: change `server.port` (backend) or run `npm run dev -- --port 5174` (frontend).
