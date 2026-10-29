# CLAUDE.md

Context for Claude Code. Read this before working on the repository.

## Project

**ByteBee** (working name) is a LeetCode-style learning platform: users solve programming problems in the browser, their code is executed and checked against test cases, and they earn progress through gamification (XP, levels, badges) and a **daily streak**.

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

- **Backend:** Java 21, Spring Boot, Maven, Spring Data JPA, PostgreSQL, Actuator, springdoc-openapi (OpenAPI spec for client generation)
- **Frontend:** React, TypeScript, Vite, TanStack Query, Axios, Tailwind CSS, DaisyUI
- **API client:** generated automatically with **Orval** from the backend OpenAPI spec (plus Zod schemas)
- **Auth:** **Logto**, self-hosted in Docker
- **Database:** PostgreSQL 17
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
├── docker-compose.yml
├── justfile
├── .env.example          # committed; copy to .env locally (never commit .env)
├── .gitignore
├── .gitattributes        # forces LF line endings
├── .editorconfig         # 4 spaces Java, 2 spaces elsewhere
├── CONTRIBUTING.md
└── README.md
```

## Local development

Everything runs in Docker. Local JDK and Node are optional.

```bash
cp .env.example .env
just up
```

| Service | URL | Notes |
| --- | --- | --- |
| Frontend | http://localhost:5173 | Vite dev server, hot reload (polling enabled) |
| Backend | http://localhost:8080 | Health: `/actuator/health` |
| Logto | http://localhost:3001 | Auth endpoint |
| Logto Admin | http://localhost:3002 | Admin console |
| Postgres | localhost:5432 | DB `bytebee`; Logto uses a separate DB `logto` on the same server |

### Docker Compose notes

- Compose project name is fixed to `bytebee`, so the data volume is `bytebee_postgres-data`.
- Backend waits for Postgres to be healthy. Configuration comes from `SPRING_DATASOURCE_*` env vars, not from `application.properties`.
- Backend code changes require `just restart backend`. Frontend reloads automatically.
- Frontend uses an anonymous volume for `/app/node_modules`.
- Logto runs `npm run cli db seed -- --swe && npm start` (seeds only if needed). Pin `LOGTO_VERSION` in `.env` to a concrete tag.

### just commands

| Command | What it does |
| --- | --- |
| `just` | List all commands |
| `just up` / `just down` | Start (with rebuild) / stop the environment |
| `just restart <service>` | Restart one service |
| `just logs [service]` | Follow logs |
| `just ps` | Container status |
| `just test` | Run backend and frontend tests |
| `just generate-api` | Regenerate the Orval API client (backend must be running) |
| `just db-shell` | psql into the app database |
| `just db-reset` | **Deletes all local data**, including Logto accounts (asks for confirmation) |

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
- Do **not** add Spring Security / OAuth2 Resource Server until the auth task: without configuration it locks every endpoint, including the healthcheck.
- Inside Docker, the Orval spec URL is `http://backend:8080/v3/api-docs` (service name, not `localhost`).
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

- Final product name and GitHub organization name (ByteBee is the working name)
- Supported languages at launch (Python only, or also Java / C++ / JavaScript)
- Judge0 vs. a custom sandbox for server-side execution
- Code editor: Monaco vs. CodeMirror
