# SE2

SE2 (the final product name is not decided yet) is a LeetCode-style learning platform. Users solve programming problems in the browser, their code is run against test cases, and they earn XP, levels, badges and a daily streak. The theme is a bee: honey is XP, activity is shown on a honeycomb grid, and ranks go from larva to queen.

University project for **Software Engineering II**, built by team **TTVZ**.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 25, Spring Boot, Maven, Spring Data JPA, springdoc-openapi |
| Frontend | React, TypeScript, Vite, TanStack Query, Axios, Tailwind CSS, DaisyUI |
| API client | Generated with Orval from the backend OpenAPI spec |
| Auth | Logto (self-hosted) |
| Database | PostgreSQL 18 |
| Tooling | Docker Compose, just |

## Repository layout

```text
backend/              Spring Boot API (Maven)
frontend/             React + Vite app
infra/postgres/init/  SQL run on first database start
docs/                 Documentation and decisions
.github/              PR template and CI workflows
scripts/              Setup scripts used by `just setup`
```

## Running locally

Dev runs PostgreSQL and Logto in Docker and the backend and frontend directly on your machine, for fast hot reload.

Install [Git](https://git-scm.com) and [just](https://github.com/casey/just) first (`winget install Casey.Just` on Windows, `brew install just` on macOS, `sudo apt install just` on Debian/Ubuntu or `sudo dnf install just` on Fedora; other options in the [just docs](https://github.com/casey/just#installation)). Then:

```bash
just setup   # once: checks/installs JDK 25 and Node 22+, creates .env, downloads dependencies, and warns if Docker is missing
just dev     # every day: starts Postgres, Logto, the backend and the frontend
```

`just setup` can be run again at any time. If it installs a tool, open a new terminal and run it again.

| Service | URL |
| --- | --- |
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8080 (health: `/actuator/health`) |
| Logto | http://localhost:3001 |
| Logto Admin | http://localhost:3002 |
| PostgreSQL | localhost:5432 |

Run `just` to list all available commands.

## Configuration

All configuration goes through environment variables. `.env.example` is committed with placeholder values; `.env` is local and ignored by git.
