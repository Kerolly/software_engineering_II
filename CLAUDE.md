# CLAUDE.md

Context for Claude Code. Read this before working on the repository.

## Project

**SE2** (the final product name is not decided yet) is a LeetCode-style learning platform: users solve programming problems in the browser, their code is executed and checked against test cases, and they earn progress through gamification (XP, levels, badges) and a **daily streak**.

- University project for **Software Engineering II**, built by team **TTVZ** (4 people).
- Theme: space 🚀 (a working choice; the team decides the final look and names) — stardust as XP, ranks from Cadet to Commander, streak as days in orbit, a star-map activity grid. Use this theme for gamification-related naming and UI where it fits. Audience: Python beginners.

### Team roles

| Role | Responsibilities |
| --- | --- |
| Tech lead / PM | Project setup, Jira & GitHub, assigns tasks to the team and unblocks teammates, helps everywhere |
| Backend | Spring Boot API, database, business logic |
| Frontend | React app, UI, API client |
| Cybersecurity / DevSecOps | Code-execution security (grader review), CI/CD, hardening, deployment |

### Communication

The developer prefers **Romanian** in chat. Everything in the repository — code, comments, commit messages, PR titles, docs — is in **English**.

## Tech stack

- **Backend:** Java 25, Spring Boot, Maven, Spring Data JPA, PostgreSQL, Actuator, springdoc-openapi (OpenAPI spec for client generation)
- **Frontend:** React, TypeScript, Vite, TanStack Query, Axios, Tailwind CSS, DaisyUI
- **API client:** generated automatically with **Orval** from the backend OpenAPI spec (plus Zod schemas)
- **Auth:** **Firebase Authentication** (hosted, free Spark plan); the backend validates standard JWTs and checks issuer and audience
- **Database:** PostgreSQL 18 (Docker locally; the production host is not decided)
- **Tooling:** Docker / Docker Compose for local infrastructure, **just** as the task runner
- **Hosting:** **not decided yet**, free tiers only, no Caddy and no server of our own. Planned model: a static frontend host (with a `/api/*` rewrite to the backend), a Docker host for the backend and the grader, and a hosted PostgreSQL. The hosting research compares the options and a deployment test checks the limits.

## Repository layout (target)

```text
/
├── backend/              # Spring Boot (Maven)
├── frontend/             # React + Vite
├── docs/                 # documentation; docs/overview.md is the start-here document, docs/design/ the design documents
├── .github/              # PR template, CI workflows
├── scripts/              # setup scripts used by `just setup`
├── docker-compose.yml    # dev infrastructure (Postgres)
├── justfile
├── .env.example          # committed; copy to .env locally (never commit .env)
├── .gitignore
├── .gitattributes        # forces LF line endings
├── .editorconfig         # 4 spaces Java, 2 spaces elsewhere
├── CONTRIBUTING.md       # team workflow and conventions
└── README.md
```

## Local development

Hybrid setup: **PostgreSQL runs in Docker, the backend and the frontend run on the host** (fast hot reload, real debugger). This needs **JDK 25**, **Node 22+** and **Docker** installed.

```bash
just setup   # once: checks/installs JDK and Node, creates .env, downloads dependencies, warns if Docker is missing
just dev     # every day: PostgreSQL + backend + frontend
```

`just setup` is idempotent. If it installs a tool, open a new terminal and run it again. It never installs Docker (install it yourself); it only checks it at the end.

| Service | URL | Notes |
| --- | --- | --- |
| Frontend | http://localhost:5173 | Vite dev server with HMR; `/api` is proxied to the backend |
| Backend | http://localhost:8080 | Health: `/actuator/health`, OpenAPI: `/v3/api-docs` |
| Postgres | localhost:5432 | DB `se2` |

### Docker Compose notes

- `docker-compose.yml` is dev only (Postgres). Production is not Compose: the planned model is managed hosting (a static frontend host, a Docker host for the backend and the grader, a hosted PostgreSQL); the platforms are not decided yet.
- The Compose project name is fixed (`se2-dev`), so the dev data volume is `se2-dev_postgres-data`.
- The backend runs on the host, so `SPRING_DATASOURCE_URL` in `.env` points to `localhost`. `just` loads `.env` automatically (`set dotenv-load`). Configuration comes from `SPRING_DATASOURCE_*` env vars, not from `application.properties`.
- Firebase settings (`FIREBASE_PROJECT_ID`, `VITE_FIREBASE_*`) come from `.env` and are per Firebase project; the whole team uses the same project.

### just commands

| Command | What it does |
| --- | --- |
| `just` | List all commands |
| `just setup` | Install and check everything needed for development |
| `just dev` | Start infrastructure, backend and frontend (Ctrl+C stops the apps) |
| `just infra` / `just down` | Start / stop only PostgreSQL |
| `just dev-backend` / `just dev-frontend` | Run one app on the host |
| `just test` | Run all tests (`test-backend` also starts PostgreSQL; `test-frontend` runs Vitest) |
| `just up` / `just restart [service]` | `up` is an alias of `infra`; restart the Docker services |
| `just logs [service]` | Follow logs of the Docker services |
| `just ps` | Container status |
| `just db-shell` | psql into the app database |
| `just db-reset` | **Deletes all local data** in the local database (asks for confirmation) |

Planned: `just gen-api` (regenerates `backend/openapi.json` and the Orval client, once the first API endpoint exists).

## Code execution and security

This is the most sensitive part of the system. User-submitted code must never be able to affect our infrastructure.

- **"Run" (instant feedback):** Python runs client-side in the browser with Pyodide (WebAssembly) in a Web Worker. Never trusted for scoring; the client can always forge a result.
- **"Submit" (graded):** the browser sends the **source code**, never a verdict. The server re-executes it and decides. Proposed implementation (**pending review by the security teammate**, see `docs/design/code-execution.md`): a separate `grader` service running the same Pyodide build in Deno with permissions denied (`--deny-net --deny-env --deny-run --deny-write`), one process per submission, time / heap / output limits, no secrets and no database access, called by the backend through the `Grader` interface. Judge0 is dropped (it needs privileged containers).
- Expected test outputs must never enter the interpreter that runs user code; compare outside Python.
- Rate-limit submissions per user. Award XP only on the first solve of a problem and cap XP per day.
- XP and streaks are awarded **only** from server-side verified results.
- There are no hidden tests for now: for every test the user sees where it failed and why. Decided: no hidden tests. The known limitation is that a user can hard-code the expected outputs; it is accepted and limited by awarding XP only on the first solve and capping XP per day.

## Conventions

### Jira

- One shared Jira project for the whole year; project key is **`SCRUM`** (codes like `SCRUM-58`; numbers are shared across all teams).
- No epics available. Grouping is done with a title prefix and labels.
- **Task title:** `[TTVZ][Area] Description` — e.g. `[TTVZ][Auth] Implement login with Firebase`
- **Labels:** always `ttvz` + one type label:
  `backend`, `frontend`, `devops` or `docs`.
- **Task** = internal technical work. **Story** = user-visible functionality ("As a user, I want… so that…").

### Git

Full rules are in `CONTRIBUTING.md`. The Jira code must appear in the branch name and in the PR title. `[TTVZ]` and `[Area]` are only used in Jira titles.

- **Branch:** `type/SCRUM-<nr>-short-description` — lowercase, hyphens, e.g. `feat/SCRUM-58-login-with-firebase`
- **Commit:** `type(scope): description` — lowercase, imperative, no trailing period; the scope is one lowercase word for the part of the project touched (`auth`, `problems`, `execution`, `gamification`, `setup`, `docs`, `ci`, `deps`), e.g. `feat(auth): add login button to navbar`
- **PR title:** `SCRUM-<nr> Description`, e.g. `SCRUM-58 Implement login with Firebase`.
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
- **Breaking change:** `!` after the type — e.g. `feat(api)!: change submission response format`
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
- Anything touching code execution or the grader must be reviewed with security in mind; flag risks explicitly.
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
2. **Auth** — Firebase integration, JWT validation (issuer and audience) in Spring Boot, login/logout in React, user profile synced on first login
3. **Problems** — DB schema (problems, test cases, difficulty, tags), CRUD API, list page with filters, detail page, seed data
4. **Execution** — code editor (Monaco or CodeMirror), in-browser "Run" (Pyodide), server-verified "Submit" (grader service), results display
5. **Security** — threat model, grader review and limits, rate limiting, pentesting with malicious payloads
6. **Gamification** — XP and levels, daily streak (timezone-aware), star-map activity grid, leaderboard (post-MVP)
7. **Deploy** — choose the free hosting platforms, Dockerfiles for the backend and the grader, secrets per platform, CD on merge to `main`

**Gamification (proposal, details in `docs/design/gamification.md`):** XP by difficulty on first solve only, 6 levels with space ranks, daily streak with a streak freeze from level 3, 6 badges, Medium problems unlock after 10 Easy solved, cosmetics, problems grouped by topic. Audience: Python beginners. Theme: space (working choice, the team decides).

**Admin panel (proposal, `docs/design/admin-panel.md`):** admins create and publish problems (text, tests, hints, reference solution that must pass before publishing), view, block and unblock users (status checked on every request), preview drafts with an admin-only dry run, and edit settings stored in the database (XP, levels, unlock thresholds, limits). Up to 3 hints per problem; each hint revealed before the first solve lowers that solve's XP. The first admins are created from `ADMIN_BOOTSTRAP_EMAILS` / `ADMIN_BOOTSTRAP_PASSWORD` (forced password change; the backend refuses to start in production with a weak password). Secrets and the grader's hard limits stay in environment variables.

**MVP flow:** log in → pick a problem → write code → submit → see results → streak/XP update.

## Open decisions

- Final product name and GitHub organization name (SE2 is used as the placeholder name everywhere: DB, Compose project, env vars)
- Supported languages at launch (Python only, or also Java / C++ / JavaScript)
- Approval of the Deno + Pyodide grader for server-side execution (proposed in `docs/design/code-execution.md`, needs the security review), or the child-process fallback; whether two free services on the backend host fit is **not verified yet** (to be measured in a deployment test)
- Function-based (`def solve(...)`) or stdin/stdout problems
- Code editor: Monaco vs. CodeMirror
- Hosting platforms for the frontend, the backend (and grader) and PostgreSQL, all free (decided after the hosting research and a deployment test)
- Rate limiting: where and how much (waiting for the infrastructure teammate)
- Exact request and response shape of a submission
