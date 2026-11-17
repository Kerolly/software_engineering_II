# Architecture

How the pieces fit together: who uses the app, which systems talk to each other, and how the backend is organized inside. The diagrams are written in [Mermaid](https://mermaid.js.org/), so GitHub renders them and they can be reviewed in a pull request.

## 1. Use cases

**What it shows.** The three kinds of actors and what each one can do. A visitor can only see the landing page and log in. A user solves problems and follows their progress. An admin does everything a user does, plus manages content, users and settings.

```mermaid
flowchart LR
    V([Visitor])
    U([User])
    A([Admin])

    subgraph app["Application"]
        UC1["See the landing page"]
        UC2["Register and log in"]
        UC3["Browse problems by topic"]
        UC4["Run code in the browser"]
        UC5["Reveal hints"]
        UC6["Submit code and see results per test"]
        UC7["See profile: XP, level, streak, badges"]
        UC8["Select cosmetics"]
        UC9["Manage problems and hints"]
        UC10["Block and unblock users"]
        UC11["Edit settings"]
        UC12["Read the audit log"]
    end

    V --> UC1
    V --> UC2
    U --> UC3
    U --> UC4
    U --> UC5
    U --> UC6
    U --> UC7
    U --> UC8
    A -->|"everything a user can do"| U
    A --> UC9
    A --> UC10
    A --> UC11
    A --> UC12
```

## 2. System context

**What it shows.** The parts of the system and who calls whom. The browser talks to only two addresses: Firebase (login) and the frontend host (the app and the `/api` forward). The backend is the only part that reaches the database, and the grader is called only by the backend. "Run" never leaves the browser and is never scored; "Submit" is the only path that reaches the grader.

```mermaid
flowchart LR
    subgraph device["User's device"]
        APP["React app"]
        PY["Pyodide in a Web Worker<br/>Run only, never scored"]
        APP <-->|"Run"| PY
    end

    FB["Firebase Authentication"]

    subgraph hosting["Free hosting"]
        FH["Frontend host<br/>static files and /api forward"]
        BE["Backend<br/>Spring Boot"]
        GR["Grader<br/>Deno + Pyodide"]
        DB[("PostgreSQL")]
    end

    APP -->|"1. load the app"| FH
    APP -->|"2. log in, get an ID token"| FB
    APP -->|"3. /api calls with the token"| FH
    FH -->|"4. forwards /api/*"| BE
    BE -.->|"5. public keys, Admin SDK"| FB
    BE <-->|"6. reads and writes data"| DB
    BE -->|"7. Submit only: code and tests"| GR
    GR -.->|"8. result per test"| BE
```

## 3. Backend components

**What it shows.** The backend is split by feature (one package per feature, each with its own controller, service, repository and DTOs). An arrow means "uses". The important point is how a verdict flows: `submissions` records the result and publishes a `VerdictRecorded` event; `gamification` reacts to it (XP, streak, badges, unlocks). That way `submissions` does not depend on the gamification rules. All numbers come from `settings`, never from constants, and every admin action ends in `audit`.

```mermaid
flowchart TB
    subgraph ext["Outside the backend"]
        FB["Firebase<br/>public keys and Admin SDK"]
        GR["Grader service"]
        DB[("PostgreSQL")]
    end

    subgraph be["Spring Boot backend, packages by feature"]
        CFG["config<br/>security, error handler, OpenAPI,<br/>rate limiting, path prefix"]
        USR["users<br/>JWT, users, roles, /me, block status"]
        PRB["problems<br/>topics, problems, tests, tags"]
        HNT["hints<br/>hints and reveals"]
        SUB["submissions<br/>submit, polling, results,<br/>Grader interface"]
        GAM["gamification<br/>XP, levels, streak, freeze,<br/>badges, unlocks, cosmetics"]
        ADM["admin<br/>problems, users, preview, dry run"]
        SET["settings<br/>app_settings, cached and validated"]
        AUD["audit<br/>audit log"]
    end

    CFG --> USR
    USR -->|"validates tokens,<br/>Admin SDK"| FB
    SUB -->|"Grader interface"| GR
    SUB --> PRB
    SUB --> USR
    SUB --> SET
    SUB -.->|"VerdictRecorded event"| GAM
    GAM --> SET
    GAM --> HNT
    GAM --> PRB
    GAM --> USR
    HNT --> PRB
    HNT --> USR
    PRB -->|"unlock status"| GAM
    ADM --> PRB
    ADM --> HNT
    ADM --> USR
    ADM --> SET
    ADM --> SUB
    ADM --> AUD
    SET --> AUD
    USR --> DB
    PRB --> DB
    SUB --> DB
    GAM --> DB
    SET --> DB
    AUD --> DB
```
