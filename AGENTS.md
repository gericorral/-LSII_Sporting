# ProTube - AI Agent Configuration

## 1. Project Overview and Architecture

ProTube is a web application where registered users can watch, upload, and comment on
videos.

This repository is a monorepo:

- `backend/` is a Spring Boot 3 REST API written in Java 21 and built with Maven. It
  exposes `/api/**` and serves file-based media under `/media/**`.
- `frontend/` is a React 19 and TypeScript single-page application built with Vite.
- `tooling/videoGrabber/` contains the Python tool that downloads sample videos and
  creates the `.mp4`, `.webp`, and `.json` files consumed by the backend.

The backend follows this flow:

```text
HTTP requests flow through the Controller, Service, and Persistence layers.
```

Keep business logic in services, keep controllers focused on HTTP concerns, and use
the existing persistence abstractions when adding or changing data access. The video
store is shared by the video grabber and backend; do not hardcode a store path.

## 2. Commands

### Backend

Run these commands from `backend/`. The Maven wrapper is not committed, so use Maven
3.9+ unless a wrapper is generated locally.

```powershell
# Required before starting the application
$env:ENV_PROTUBE_STORE_DIR = "C:\absolute\path\to\video-store\"

mvn spring-boot:run
mvn clean verify
mvn test -Dtest=ClassName
mvn test -Dtest=ClassName#methodName
mvn clean verify -Pcoverage
```

The default development profile runs on `http://localhost:8080`, uses an in-memory
H2 database, and loads initial data. H2 is suitable for development only; the MVP
requires persistent database storage. The production profile uses PostgreSQL
configuration and also requires the database environment variables documented in
`DEVELOPMENT.md`.

The `ENV_PROTUBE_STORE_DIR` value must be an absolute path to the directory
containing the media files and must end with a path separator.

### Frontend

Run these commands from `frontend/`:

```powershell
npm install
npm run dev
npm run build
npm run test
npm run lint
npm run format
```

The Vite development server runs on `http://localhost:5173`. API and media domains
are configured through `VITE_API_DOMAIN` and `VITE_MEDIA_DOMAIN`; local development
defaults are in `frontend/.env.development`.

### Video grabber

Run the tool from `tooling/videoGrabber/` after installing Python dependencies,
`yt-dlp`, and `ffmpeg`:

```powershell
pip install -r dependencies.txt
python main.py --store=C:\absolute\path\to\video-store --id=10 --videos=..\..\resources\video_list.txt
```

## 3. Definition of Done and Workflow

A change is done when:

- It implements an agreed issue or user story and follows the existing architecture
  and naming conventions.
- New or changed behavior has appropriate automated tests.
- Backend and frontend checks relevant to the change pass, including build, tests,
  lint, and formatting where applicable.
- Overall application coverage remains above 50% for both backend and frontend.
- No secrets, credentials, generated media, or local environment files are committed.
- Relevant documentation is updated when commands, configuration, or behavior changes.
- The pull request has at least one human team approval and all Copilot review
  comments have been resolved.
- All source-code comments must be written in English and should explain only
  non-obvious design decisions or complex logic.

Work on a feature branch; do not commit directly to `main`. Open a pull request and
wait for the GitHub Actions CI pipeline to pass before merging. Use Conventional
Commits and reference the related task or Kanban issue. Squash-merge approved pull
requests.

## 4. AI Policy

- Store every prompt used to generate code, architecture, or other project artifacts
  in the repository's `/prompts` directory.
- Reuse an existing approved prompt for the same task type. Propose and obtain team
  approval before introducing or changing a recurring prompt.
- For every AI-assisted change, automatically generate and include a Conventional
  Commits message in the task handoff without asking the user whether a commit
  message is wanted. The message must reference the related Kanban task or user
  story, identify the approved prompt, and state that the work was generated with AI
  and logged in `AGENTS.md`.
- Do not create or push a git commit unless the user explicitly requests that action;
  generating the commit message and including it in the handoff is mandatory.
- Review AI-generated output as carefully as a teammate's pull request. Do not
  include secrets, tokens, passwords, or private configuration in prompts,
  instructions, source code, or MCP configuration.
- Keep this file as the single source of truth for repository-wide AI guidance.
  Provider-specific files should reference it rather than duplicate its rules.

## 5. References

- [Development guide](DEVELOPMENT.md)
- [MVP requirements](REQUIREMENT.md)
- [Project rules](README.md)
- [Approved prompts](prompts/README.md)

## Approved AI Prompts

Approved prompts are stored as individual files in `/prompts`, following the naming
and review rules in [prompts/README.md](prompts/README.md). Do not duplicate prompt
content in this file.
