# Grader probe (SCRUM-237)

Two small services for the hosting test in [grader.md, section 5](../../docs/design/grader.md#5-hosting-decision-and-test). Neither is the production image.

**Grader probe** (this folder). It uses:

- the same Deno 2.9.6 and Pyodide 314.0.8 as the grader;
- the same memory snapshot;
- the same child flags and memory cap.

At every start (so at every Render wake-up) it runs a self-test, one child after the other, and publishes the times and memory figures at `GET /stats`:

1. three trivial tests;
2. a memory bomb;
3. an endless loop, killed 2 s after it starts (how fast a kill works when the child uses all the CPU);
4. escape attempts through the JavaScript bridge: read the parent's environment, its own environment and `/etc/passwd`, read an environment variable, write a file, run a command, open a network connection. Only the outcome is published, never what was read.

It runs nothing on request, so it needs **no secret**. `POST /probe` exists only if `PROBE_SECRET` is set; the test does not use it.

**Backend stand-in** (`backend-standin/`). It builds the backend of this branch and starts it **without a database**: Flyway off, Hibernate without JDBC metadata, no database health check. It measures Render's wake-up plus the Spring Boot start. A real start with Neon adds the connection, Flyway and the schema validation on top.

## Files

| File | What it does |
| --- | --- |
| `Dockerfile` | Grader probe: downloads Pyodide 314.0.8, builds the snapshot at build time, runs `server.js` as the `deno` user |
| `server.js` | The self-test at start-up, `GET /health` and `GET /stats` |
| `child.js` | The child: restores the snapshot, then runs one of the four fixed scenarios |
| `snap_make.js` | The build step that writes the snapshot |
| `measure.sh` | Wakes the backend, then the grader, times both, follows `/health` during the self-test, appends a line to `results.tsv` |
| `backend-standin/Dockerfile` | Backend stand-in, built from the repository root |

## Deploy on Render (free, no card)

In Render: **New → Web Service → Public Git Repository**, URL `https://github.com/Kerolly/software_engineering_II`. No environment variables are needed.

| Setting | Grader probe | Backend stand-in |
| --- | --- | --- |
| Name | `kodeling-grader-probe` | `kodeling-backend-standin` |
| Branch | `docs/SCRUM-237-grader-design` | `docs/SCRUM-237-grader-design` |
| Region | Frankfurt | Frankfurt |
| Language | Docker | Docker |
| Root Directory | `tools/grader-probe` | *(empty: the repository root)* |
| Dockerfile Path | `./Dockerfile` | `tools/grader-probe/backend-standin/Dockerfile` |
| Docker Build Context Directory | `.` | `.` |
| Instance Type | Free | Free |
| Health Check Path | `/health` | `/actuator/health/liveness` |

A service deployed from a public URL does not redeploy on push. After a change, use **Manual Deploy → Deploy latest commit**.

Check: `https://<grader-probe>/stats` shows `selfTest.state: "done"` about a minute after the start, and `https://<backend-standin>/actuator/health/liveness` answers `{"status":"UP"}`.

## Measure

From any shell with `curl`:

```bash
export PROBE_URL=https://<grader-probe>.onrender.com
export BACKEND_URL=https://<backend-standin>.onrender.com
# at least 20 minutes after the last request to either service, so both are asleep:
bash measure.sh cold
```

Repeat on three different days and keep the median.

**What to read in each line:**

| Value | Meaning | Goes to grader.md, section 5 |
| --- | --- | --- |
| 3rd column | Seconds (and HTTP code) until the backend answered: Render's wake-up plus the Spring Boot start | Backend part of "Both asleep" |
| 4th column | Seconds until the grader's `/health` answered, asked only after the backend answered | Part of "Grader asleep" |
| `selfTest.trivial.childTotalMs` | Child start from the snapshot plus three trivial tests, at Render's CPU | "Child start from the snapshot" |
| 4th column + `childTotalMs` | Grader asleep, a trivial submission | "Grader asleep" |
| 3rd + 4th column + `childTotalMs` | Both asleep, the Submit is the first request after the pause (the early wake-up does not help) | "Both asleep" |
| 5th column | The slowest `/health` while children were starting | Health check under load (should stay under 1 s) |
| `selfTest.trivial.child[1].testsMs`, plus a `warm` `/health` time | What a warm Submit costs once a child is ready | "Grader awake" |
| `selfTest.containerPeakMB` (`containerLimitMB` is about 512) | Peak memory of the whole service, memory bomb included | "Peak memory" |
| `selfTest.bomb.child[1]` | Should be `MemoryError after ... MB`, with `nextRun` `45` | Check only |
| `selfTest.spin.killToExitMs` | Time from the kill to the end of the process | Kill latency |
| `selfTest.escape.child[1].escape` | Every attempt should be `denied` | Check only |
| `bootAt` | On a `cold` line it must be a few seconds before the measurement | Check only |

## Clean up

Delete both Render services when the measurements are in, so they use no free hours.
