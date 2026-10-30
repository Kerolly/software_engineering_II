# Contributing

Shared working rules for team TTVZ. Every task, branch, commit and pull request follows the same structure, so the Jira board and the Git history always match.

To set up your machine, see [Running locally](README.md#running-locally) (`just setup`).

## Workflow

1. Pick a task in Jira and move it to *In Progress*. Note its code, e.g. `SCRUM-58`.
2. Create a branch from an up-to-date `main`: `git switch main && git pull && git switch -c feat/SCRUM-58-login-with-logto`.
3. Work in small steps and commit often.
4. Push the branch and open a pull request. The template is filled in for you.
5. Get at least one approval from a teammate. Fix review comments with new commits.
6. Merge the pull request. The branch is deleted after the merge. Move the Jira task to *Done*.

Never push directly to `main`. One pull request maps to one Jira task; keep it small and focused.

## Jira

- One shared Jira project for the whole year; the key is `SCRUM` and numbers are shared across all teams.
- **Task title:** `[TTVZ][Area] Description`, e.g. `[TTVZ][Auth] Implement login with Logto`.
- **Labels:** always `ttvz` + one type label:
  `backend`, `frontend`, `devops` or `docs`.
- **Task** is internal technical work. **Story** is user-visible functionality ("As a user, I want… so that…").
- `[TTVZ]` and `[Area]` are used **only** in Jira titles, never in branches, commits or pull requests.

## Branches

Format: `type/SCRUM-<nr>-short-description`. Lowercase, words separated by hyphens.

```text
feat/SCRUM-58-login-with-logto
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

Title format: `SCRUM-<nr> Description`, e.g. `SCRUM-58 Implement login with Logto`.

The title must contain the Jira code. Write it clearly; it is what the team sees in the list of pull requests.

- Fill in the template: what changed, why, how to test.
- At least one approval is required before merging.
- Anything that touches code execution or the sandbox needs a security-minded review. Flag the risks in the description.

## Code rules

- Never commit `.env`, passwords, API keys or tokens. Configuration goes through environment variables, each with an entry in `.env.example`.
- Files use LF line endings (enforced by `.gitattributes`).
- Run `just test` before opening a pull request.
- API changes follow `docs/api-conventions.md`.
- Code, comments, commits, pull requests and docs are written in English.
