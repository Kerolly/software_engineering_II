# CLAUDE.md

Context for Claude Code. Read this before working on the repository.

## Project

**SE2** (the final product name is not decided yet) is a LeetCode-style learning platform: users solve programming problems in the browser, their code is executed and checked against test cases, and they earn progress through gamification (XP, levels, badges) and a **daily streak**.

- University project for **Software Engineering II**, built by team **TTVZ** (4 people).
- Mascot / theme: a bee 🐝 — honey as XP, honeycomb (hexagon) activity grid, ranks from larva to queen. Use this theme for gamification-related naming and UI where it fits.

### Team roles

| Role | Responsibilities |
| --- | --- |
| Lead / generalist | Project setup, Jira & GitHub, helps everywhere |
| Backend | Spring Boot API, database, business logic |
| Frontend | React app, UI, API client |
| Cybersecurity / DevSecOps | Code-execution sandbox security, CI/CD, hardening, deployment |

### Communication

The developer prefers **Romanian** in chat. Everything in the repository — code, comments, commit messages, PR titles, docs — is in **English**.

## Tech stack

- **Backend:** Java 25, Spring Boot, Maven, Spring Data JPA, PostgreSQL, Actuator, springdoc-openapi (OpenAPI spec for client generation)
- **Frontend:** React, TypeScript, Vite, TanStack Query, Axios, Tailwind CSS, DaisyUI
- **API client:** generated automatically with **Orval** from the backend OpenAPI spec (plus Zod schemas)
- **Auth:** **Logto**, self-hosted in Docker
- **Database:** PostgreSQL 18
- **Tooling:** Docker / Docker Compose for everything, **just** as the task runner
- **Hosting (planned):** Oracle Cloud Free Tier ARM VM, with Cloudflare in front (DNS, proxy, SSL)

## Repository layout (target)

```text
/
├── backend/              # Spring Boot (Maven)
├── frontend/             # React + Vite
├── infra/
│   └── postgres/init/    # SQL run on first DB start (creates the `logto` database)
├── docs/                 # documentation, decisions
├── .github/              # PR template, CI workflows
├── scripts/              # setup scripts used by `just setup`
├── docker-compose.yml    # dev infrastructure (Postgres + Logto)
├── justfile
├── .env.example          # committed; copy to .env locally (never commit .env)
├── .gitignore
├── .gitattributes        # forces LF line endings
├── .editorconfig         # 4 spaces Java, 2 spaces elsewhere
├── CONTRIBUTING.md
└── README.md
```

## Local development

Hybrid setup: **PostgreSQL and Logto run in Docker, the backend and the frontend run on the host** (fast hot reload, real debugger). This needs **JDK 25**, **Node 22+** and **Docker** installed.

```bash
just setup   # once: checks/installs JDK and Node, creates .env, downloads dependencies, warns if Docker is missing
just dev     # every day: infrastructure + backend + frontend
```

`just setup` is idempotent. If it installs a tool, open a new terminal and run it again. It never installs Docker (install it yourself); it only checks it at the end.

| Service | URL | Notes |
| --- | --- | --- |
| Frontend | http://localhost:5173 | Vite dev server with HMR; `/api` is proxied to the backend |
| Backend | http://localhost:8080 | Health: `/actuator/health`, OpenAPI: `/v3/api-docs` |
| Logto | http://localhost:3001 | Auth endpoint |
| Logto Admin | http://localhost:3002 | Admin console |
| Postgres | localhost:5432 | DB `se2`; Logto uses a separate DB `logto` on the same server |

### Docker Compose notes

- `docker-compose.yml` is dev only (Postgres + Logto). The production-like stack is `docker-compose.prod.yml` (planned, with Caddy serving the frontend and proxying `/api`).
- Compose project names are fixed (`se2-dev` for dev, `se2-prod` for prod), so the dev data volume is `se2-dev_postgres-data`.
- The backend runs on the host, so `SPRING_DATASOURCE_URL` in `.env` points to `localhost`. `just` loads `.env` automatically (`set dotenv-load`). Configuration comes from `SPRING_DATASOURCE_*` env vars, not from `application.properties`.
- Logto runs `npm run cli db seed -- --swe && npm start` (seeds only if needed). `LOGTO_VERSION` is pinned to a concrete tag in `.env`.

### just commands

| Command | What it does |
| --- | --- |
| `just` | List all commands |
| `just setup` | Install and check everything needed for development |
| `just dev` | Start infrastructure, backend and frontend (Ctrl+C stops the apps) |
| `just infra` / `just down` | Start / stop only PostgreSQL and Logto |
| `just dev-backend` / `just dev-frontend` | Run one app on the host |
| `just logs [service]` | Follow logs of the Docker services |
| `just ps` | Container status |
| `just db-shell` | psql into the app database |
| `just db-reset` | **Deletes all local data**, including Logto accounts (asks for confirmation) |

Planned: `just test`, `just generate-api` (Orval, once the first API endpoint exists), `just prod`.

## Code execution and security

This is the most sensitive part of the system. User-submitted code must never be able to affect our infrastructure.

- **"Run" (instant feedback):** Python runs client-side via **Pyodide** (WebAssembly). Never trusted for scoring.
- **"Submit" (graded):** runs server-side in an isolated sandbox. **Judge0** (self-hosted) is the leading candidate; final decision pending a research task.
- Sandbox requirements: separate container, no network access, CPU / memory / time limits, read-only filesystem, unprivileged user, consider gVisor. Rate-limit submissions per user.
- XP and streaks are awarded **only** from server-side verified results.
- Hidden test cases never reach the client.

## Conventions

### Jira

- One shared Jira project for the whole year; project key is **`SCRUM`** (codes like `SCRUM-58`; numbers are shared across all teams).
- No epics available. Grouping is done with a title prefix and labels.
- **Task title:** `[TTVZ][Area] Description` — e.g. `[TTVZ][Auth] Implement login with Logto`
- **Labels:** always `ttvz` + an area label + a type label.
  - Areas: `setup`, `auth`, `problems`, `execution`, `security`, `gamification`, `deploy`
  - Types: `backend`, `frontend`, `devops`, `docs`
- **Task** = internal technical work. **Story** = user-visible functionality ("As a user, I want… so that…").

### Git (Conventional Commits + Jira key)

The Jira code must appear in branches, commits and PR titles so the GitHub ↔ Jira integration links them. `[TTVZ]` and `[Area]` are only used in Jira titles.

- **Branch:** `type/SCRUM-<nr>-short-description` — e.g. `feat/SCRUM-58-login-with-logto`
- **Commit:** `type(scope): SCRUM-<nr> description` — lowercase, imperative, no trailing period
  - e.g. `feat(auth): SCRUM-58 add login button to navbar`
- **PR title:** same format as a commit (it becomes the squash commit message)
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
- **Breaking change:** `!` after the type — e.g. `feat(api)!: SCRUM-60 change submission response format`
- `main` is protected: PR required, at least one approval, **Squash and merge**, delete branch after merge.
- Never push directly to `main`.

## Rules for Claude Code

- Never commit `.env`, passwords, API keys or tokens. All config goes through environment variables with a matching entry in `.env.example`.
- Keep files with LF line endings (Windows teammates; `mvnw` breaks with CRLF).
- Application endpoints live under `/api`; Actuator and OpenAPI stay at the root.
- Do **not** add Spring Security / OAuth2 Resource Server until the auth task: without configuration it locks every endpoint, including the healthcheck.
- The Orval spec URL is `http://localhost:8080/v3/api-docs` (the backend runs on the host in dev).
- Never edit the generated API client by hand; change the backend and run `just generate-api`.
- Prefer small, focused changes that map to a single Jira task.
- Anything touching code execution or the sandbox must be reviewed with security in mind; flag risks explicitly.
- Ask before making architectural decisions that are still open (see below).

## Current status

- [ ] Repository is on a personal GitHub account — to be **transferred to the team organization**
- [ ] GitHub ↔ Jira integration (GitHub for Atlassian app) — needs a Jira site admin to install it
- [ ] `[TTVZ][Setup] Create monorepo structure`
- [ ] `[TTVZ][Setup] Set up Docker Compose for local development`
- [ ] `[TTVZ][Setup] Create justfile with common commands`
- [ ] `[TTVZ][Setup] Define team conventions` (CONTRIBUTING.md, PR template, branch protection)

## Roadmap (areas)

1. **Setup** — repo, Docker Compose, justfile, conventions, CI (build, lint, test), Dependabot, CodeQL, secret scanning
2. **Auth** — Logto integration, JWT validation in Spring Boot, login/logout in React, user profile synced on first login
3. **Problems** — DB schema (problems, test cases, difficulty, tags), CRUD API, list page with filters, detail page, seed data
4. **Execution** — code editor (Monaco or CodeMirror), Pyodide "Run", sandboxed "Submit", results display
5. **Security** — threat model, sandbox limits, rate limiting, pentesting with malicious payloads
6. **Gamification** — XP and levels, daily streak (timezone-aware), honeycomb activity grid, leaderboard (post-MVP)
7. **Deploy** — Oracle Cloud VM, Cloudflare, production Compose with secrets, CD on merge to `main`

**MVP flow:** log in → pick a problem → write code → submit → see results → streak/XP update.

## Open decisions

- Final product name and GitHub organization name (SE2 is used as the placeholder name everywhere: DB, Compose project, env vars)
- Supported languages at launch (Python only, or also Java / C++ / JavaScript)
- Judge0 vs. a custom sandbox for server-side execution
- Code editor: Monaco vs. CodeMirror
