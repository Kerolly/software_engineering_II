# Contributing

Shared working rules for team TTVZ. Every task, branch, commit and pull request follows the same structure, so the Jira board and the Git history always match.

To set up your machine, see [Running locally](README.md#running-locally) (`just setup`).

## Workflow

1. Pick a task in Jira and move it to *In Progress*. Note its code, e.g. `SCRUM-58`.
2. Create a branch from an up-to-date `main`: `git switch main && git pull && git switch -c feat/SCRUM-58-login-with-firebase`.
3. Work in small steps and commit often.
4. Push the branch and open a pull request. The template is filled in for you.
5. Get at least one approval from a teammate. Fix review comments with new commits.
6. Merge the pull request. The branch is deleted after the merge. Move the Jira task to *Done*.

Never push directly to `main`. One pull request maps to one Jira task; keep it small and focused.

## Jira

- One shared Jira project for the whole year; the key is `SCRUM` and numbers are shared across all teams.
- **Task title:** `[TTVZ][Area] Description`, e.g. `[TTVZ][Auth] Implement login with Firebase`.
- **Labels:** always `ttvz` + one type label:
  `backend`, `frontend`, `devsecops` or `docs`.
- **Task** is internal technical work. **Story** is user-visible functionality ("As a user, I want… so that…").
- `[TTVZ]` and `[Area]` are used **only** in Jira titles, never in branches, commits or pull requests.

## Branches

Format: `type/SCRUM-<nr>-short-description`. Lowercase, words separated by hyphens.

```text
feat/SCRUM-58-login-with-firebase
fix/SCRUM-61-streak-timezone
docs/SCRUM-8-contributing-guide
```

## Commits

Format: `type(scope): description`. Lowercase, imperative mood, no trailing period. The scope is the part of the project the commit touches, in one lowercase word: for example `auth`, `problems`, `execution`, `gamification`, `setup`, `docs`, `ci`, `deps`.

```text
feat(auth): add login button to navbar
fix(gamification): reset streak at local midnight
docs(contributing): describe the branch naming rules
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.

Mark a breaking change with `!` after the type, e.g. `feat(api)!: change submission response format`.

Keep commits small and clear, so the reviewer can follow the change.

## Pull requests

Title format: `SCRUM-<nr> Description`, e.g. `SCRUM-58 Implement login with Firebase`.

The title must contain the Jira code. Write it clearly; it is what the team sees in the list of pull requests.

- Fill in the template: what changed, why, how to test.
- At least one approval is required before merging.
- Anything that touches code execution or the grader needs a security-minded review. Flag the risks in the description.

## CI checks

CI ([.github/workflows/ci.yml](.github/workflows/ci.yml)) runs on every pull request and on every push to `main`. `main` is protected: the **CI** check must pass and a teammate must approve before the merge.

### Run the same checks locally

Before you push, run:

```bash
just lint
just test
```

| CI job | What CI runs | Run it locally |
| --- | --- | --- |
| Backend | `./mvnw -B verify` against PostgreSQL 18 | `just test-backend` (starts PostgreSQL; `verify` in CI also packages the app) |
| Frontend | `npm ci`, `npm run lint`, `npm test`, `npm run build` | `just lint`, `just test-frontend`, then `npm run build` in `frontend/` |
| Conventions | The pull request title and the branch name | Compare them with [Branches](#branches) and [Pull requests](#pull-requests) |

When you change frontend code, also run `npm run build` in `frontend/`. It is the only check that runs the TypeScript compiler (`tsc -b`); the linter and the tests do not.

### When CI is red

1. Open the failed check from the pull request (**Details**) or from the **Checks** tab. The **CI** check only collects the results of the other jobs, and its log lists them. Read the job with the red ✗: **Backend**, **Frontend** or **Conventions**.
2. Open the step with the red ✗ and read its log from the bottom up:
   - **Backend:** `Tests run: …, Failures: …` with the name of the failed test, or `COMPILATION ERROR` with the file and the line.
   - **Frontend:** oxlint prints the file, the line and the rule; Vitest marks the failed test with `FAIL`; the build prints TypeScript errors as `error TS…`. If `npm ci` fails because `package-lock.json` does not match `package.json`, run `npm install` in `frontend/` and commit the updated `package-lock.json`.
   - **Conventions:** the error line says what was expected and what it got.
3. When the backend job fails, the run also has a **backend-test-reports** artifact at the bottom of its **Summary** page, kept for 7 days. It holds the full report of each test class; open the `.txt` file of the failed one.
4. Reproduce the failure locally with the command from the table, fix it and push. CI runs again on the new commit and cancels the older run.
5. **Cancelled** usually means that a newer push replaced the run. If a job *exceeded the maximum execution time* (`timeout-minutes` in `ci.yml`), look for a test or a step that hangs. If GitHub itself failed (a download, a runner), use **Re-run failed jobs** on the run page.
6. **Re-run does not pick up a new pull request title:** it reuses the title the run started with. After fixing the title, push a new commit, or close and reopen the pull request. A wrong branch name needs a correctly named branch and a new pull request.

## Code rules

- Never commit `.env`, passwords, API keys or tokens. Configuration goes through environment variables, each with an entry in `.env.example`.
- Files use LF line endings (enforced by `.gitattributes`).
- Run `just lint` and `just test` before opening a pull request (see [CI checks](#ci-checks)).
- API changes follow `docs/api-conventions.md`.
- Code, comments, commits, pull requests and docs are written in English.
