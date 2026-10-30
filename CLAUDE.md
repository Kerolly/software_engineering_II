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
- **Auth:** **Logto**, self-hosted in Docker (provider not final; the backend validates standard JWTs)
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
├── CONTRIBUTING.md       # team workflow and conventions
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
| `just test` | Run all tests (`test-backend` also starts PostgreSQL; `test-frontend` runs Vitest) |
| `just up` / `just restart [service]` | `up` is an alias of `infra`; restart the Docker services |
| `just logs [service]` | Follow logs of the Docker services |
| `just ps` | Container status |
| `just db-shell` | psql into the app database |
| `just db-reset` | **Deletes all local data**, including Logto accounts (asks for confirmation) |

Planned: `just gen-api` (regenerates `backend/openapi.json` and the Orval client, once the first API endpoint exists), `just prod`.

## Code execution and security

This is the most sensitive part of the system. User-submitted code must never be able to affect our infrastructure.

- **"Run" (instant feedback):** Python runs client-side in the browser (library not final; Pyodide/WebAssembly was the first candidate). Never trusted for scoring.
- **"Submit" (graded):** runs server-side in an isolated sandbox. **Judge0** (self-hosted) is the leading candidate; final decision pending a research task.
- Sandbox requirements: separate container, no network access, CPU / memory / time limits, read-only filesystem, unprivileged user, consider gVisor. Rate-limit submissions per user.
- XP and streaks are awarded **only** from server-side verified results.
- There are no hidden tests for now: for every test the user sees where it failed and why. Only the server's verdict awards XP and streaks.

## Conventions

### Jira

- One shared Jira project for the whole year; project key is **`SCRUM`** (codes like `SCRUM-58`; numbers are shared across all teams).
- No epics available. Grouping is done with a title prefix and labels.
- **Task title:** `[TTVZ][Area] Description` — e.g. `[TTVZ][Auth] Implement login with Logto`
- **Labels:** always `ttvz` + one type label:
  `backend`, `frontend`, `devops` or `docs`.
- **Task** = internal technical work. **Story** = user-visible functionality ("As a user, I want… so that…").

### Git

Full rules are in `CONTRIBUTING.md`. The Jira code must appear in the branch name and in the PR title. `[TTVZ]` and `[Area]` are only used in Jira titles.

- **Branch:** `type/SCRUM-<nr>-short-description` — lowercase, hyphens, e.g. `feat/SCRUM-58-login-with-logto`
- **Commit:** `type: description` — lowercase, imperative, no trailing period, e.g. `feat: add login button to navbar`
- **PR title:** `SCRUM-<nr> Description`, e.g. `SCRUM-58 Implement login with Logto`.
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
- **Breaking change:** `!` after the type — e.g. `feat!: change submission response format`
- A PR needs at least one approval before merging; the branch is deleted after the merge.
- Never push directly to `main`.

## Rules for Claude Code

- Never commit `.env`, passwords, API keys or tokens. All config goes through environment variables with a matching entry in `.env.example`.
- Keep files with LF line endings (Windows teammates; `mvnw` breaks with CRLF).
- API conventions are in `docs/api-conventions.md`; follow them for every endpoint. Application endpoints live under `/api/v1`; Actuator and OpenAPI stay at the root.
- Do **not** add Spring Security / OAuth2 Resource Server until the auth task: without configuration it locks every endpoint, including the healthcheck.
- Orval reads the committed snapshot `backend/openapi.json`. Run `just gen-api` after every API change.
- Never edit the generated API client by hand; change the backend and run `just gen-api`.
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
4. **Execution** — code editor (Monaco or CodeMirror), in-browser "Run", sandboxed "Submit", results display
5. **Security** — threat model, sandbox limits, rate limiting, pentesting with malicious payloads
6. **Gamification** — XP and levels, daily streak (timezone-aware), honeycomb activity grid, leaderboard (post-MVP)
7. **Deploy** — Oracle Cloud VM, Cloudflare, production Compose with secrets, CD on merge to `main`

**MVP flow:** log in → pick a problem → write code → submit → see results → streak/XP update.

## Open decisions

- Final product name and GitHub organization name (SE2 is used as the placeholder name everywhere: DB, Compose project, env vars)
- Supported languages at launch (Python only, or also Java / C++ / JavaScript)
- Judge0 vs. a custom sandbox for server-side execution
- Code editor: Monaco vs. CodeMirror
- Auth provider (Logto is the current choice, not final; wanted: easy to run locally, no online account, no SMTP server; username + password login is enough, password reset by an admin is acceptable. Candidates: stay on Logto if it works without email, otherwise Zitadel, Supabase Auth or own Spring auth). The backend must stay provider-agnostic (standard JWT, `authSubject` in `users`).
- Rate limiting: where and how much (waiting for the infrastructure teammate)
- Implementation behind the `Grader` interface (Judge0, a Python runner or something else) and the library used for instant in-browser feedback
- Exact request and response shape of a submission
