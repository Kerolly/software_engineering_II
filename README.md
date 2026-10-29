# ByteBee

ByteBee (working name) is a LeetCode-style learning platform. Users solve programming problems in the browser, their code is run against test cases, and they earn XP, levels, badges and a daily streak. The theme is a bee: honey is XP, activity is shown on a honeycomb grid, and ranks go from larva to queen.

University project for **Software Engineering II**, built by team **TTVZ**.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot, Maven, Spring Data JPA, springdoc-openapi |
| Frontend | React, TypeScript, Vite, TanStack Query, Axios, Tailwind CSS, DaisyUI |
| API client | Generated with Orval from the backend OpenAPI spec |
| Auth | Logto (self-hosted) |
| Database | PostgreSQL 17 |
| Tooling | Docker Compose, just |

## Repository layout

```text
backend/              Spring Boot API (Maven)
frontend/             React + Vite app
infra/postgres/init/  SQL run on first database start
docs/                 Documentation and decisions
.github/              PR template and CI workflows
```

## Running locally

> The Docker Compose setup and the `justfile` are added in separate setup tasks. Until they are merged, the commands below do not work yet.

Requirements: Docker and [just](https://github.com/casey/just). A local JDK and Node are optional.

```bash
cp .env.example .env
just up
```

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
