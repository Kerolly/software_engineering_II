# Admin panel design

> **Status:** proposal by the tech lead, to be reviewed with the team (it feeds the database and architecture design).

The admin panel lets an admin run the app **without changing code or redeploying**: problems, hints, users and settings all live in the database.

## What an admin can do

1. **Problems:** create, edit, publish, archive. Fields: title, slug, difficulty, topic, position, statement (Markdown), starter code, tests, hints, reference solution.
2. **Hints:** an ordered list per problem, shown one at a time on request.
3. **Users:** list and search, see profile data (level, XP, streak, solved problems), **block** and **unblock**.
4. **Settings:** edit the values that used to be constants (below).
5. **Audit log:** a read-only list of what admins did (who, what, when).

## Problems and tests

- The database is the source of truth. The seed files in [problem-template.md](../problem-template.md) are only a way to import the first problems (and, if wanted, to export them).
- Lifecycle: `DRAFT` → `PUBLISHED` → `ARCHIVED`. Users only see published problems.
- **Publishing requires proof:** every problem has a **reference solution**. Before it can be published, the server runs the reference solution against all tests through the `Grader`; all tests must pass. This catches wrong expected values and broken tests. The reference solution is never sent to normal users.
- The statement is Markdown written by an admin. It must still be **sanitized** when rendered (no raw HTML or scripts): a stolen admin account must not become stored XSS for every user.
- Tests are data (arguments and expected value, plus a description), never code that runs with extra rights.
- Editing a published problem keeps old submissions as they were; results are stored per test with what was checked.

## Preview

Before publishing, the admin can open a **preview**: the problem exactly as users see it (rendered statement, starter code, tests table, hints revealed one by one with the XP warning). The preview works on `DRAFT` problems and has two parts:

- "Run" in the browser, like for users.
- **"Dry run"**: sends the code to the grader and shows the real per-test results. It is **not** stored as a submission and gives no XP, streak or badge. The same call is used to check the reference solution before publishing.

The dry run is an admin-only endpoint, so it is rate-limited like normal submissions.

## Hints

- Table `hints`: `problemId`, `position`, `text` (Markdown, sanitized like statements).
- At most **3 hints** per problem (the admin API rejects more; the limit is a setting).
- The user reveals them in order, and the interface warns before each reveal that it **lowers the XP** of the problem. The server records every reveal (`hint_reveals`: user, hint, time).
- **XP penalty:** each hint revealed *before the first accepted solve* reduces the XP of that solve by a percentage (setting `hintXpPenaltyPercent`, start value 25, so 3 hints leave 25% of the XP). XP = base XP x (100 - penalty x hints) / 100, never below 0. Hints revealed after the problem is solved cost nothing.
- A solve with any hint revealed does not count for the Sharpshooter badge (see [gamification.md](gamification.md)).

## Users: block and unblock

- `users.status`: `ACTIVE` or `BLOCKED`, plus who blocked, when and an optional reason.
- A Firebase token stays valid for up to an hour, so **the backend checks the status on every request** (a small cache is fine). A blocked user gets `403` with a clear `ProblemDetail`, and cannot submit or earn XP.
- The backend already holds a Firebase service account (needed for the first-admin bootstrap), so blocking can also disable the account in Firebase and revoke its refresh tokens. The status check on every request stays the rule that counts. The service-account JSON is a secret: environment variable only (`FIREBASE_SERVICE_ACCOUNT_JSON`).
- An admin cannot block themselves or the last admin.
- Admins are users with the role `ADMIN` in our database.
- **First admins:** set in environment variables. On startup the backend creates each account in Firebase (Admin SDK) if it does not exist and gives it the role `ADMIN`:
  - `ADMIN_BOOTSTRAP_EMAILS`: comma-separated emails;
  - `ADMIN_BOOTSTRAP_PASSWORD`: the initial password for those accounts.
- A default password is dangerous if it leaks, because it is a ready-made admin login. So:
  - the real value is set only in the environment of each deployment (set in the hosting platform), **never committed**; `.env.example` has a placeholder;
  - the account is marked `mustChangePassword`: after the first login the app forces a new password before anything else works;
  - the backend **refuses to start** in production if the password is still the placeholder or is shorter than 12 characters;
  - the startup creation is idempotent: an existing account keeps its password.
- After the first admins exist, more admins are managed in the panel (promote or demote a user). The last admin cannot be demoted.

## Settings instead of constants

Table `app_settings` (key, typed value, description, updated by and at). The backend reads it through one component, validates every change (type and range) and caches it. Candidate settings:

| Group | Settings |
| --- | --- |
| XP | XP per difficulty, daily XP cap, XP penalty per revealed hint (percent), maximum hints per problem |
| Levels | XP threshold and rank name per level |
| Unlocks | number of Easy solved to unlock Medium, level that grants a streak freeze |
| Limits | submissions per user per minute, maximum code size |
| Features | registration open or closed, maintenance message |

Badges are already rows (rule type and threshold), so the panel edits them too.

What stays **out** of the panel, on purpose:

- Secrets and credentials (Firebase, the grader shared secret): environment variables only.
- The grader's hard limits (timeout, heap, output size): they protect the infrastructure, so they change only through deployment and review.

Rules for changing settings:

- Levels are computed from total XP when read, so changing a threshold does not rewrite history.
- An XP event stores the amount that was awarded, so changing the XP value affects only future solves.
- Every change goes to the audit log.

## Security

- All `/api/v1/admin/**` routes require `ADMIN`, checked **on the server**; hiding the menu in the frontend is only a convenience.
- Admin actions are logged and cannot be edited.
- Admin endpoints follow the same [API conventions](../api-conventions.md) (`ProblemDetail`, `operationId`, tags).
- Because settings include rate limits, only admins may write them, and a value outside the allowed range is rejected.

## Scope for the MVP

Must have: problem create/edit/publish with tests and hints, the preview with dry run, users list with block and unblock, the settings used by gamification, audit log (even a simple one). Can wait: import and export of problems, search filters beyond the basics.

## Open points

- Who are the first admins (the emails to put in `ADMIN_BOOTSTRAP_EMAILS`)?
- Exact penalty and limit values (start: 25% per hint, 3 hints).
- How the app lets a user who forgot the initial password recover (Firebase password reset by email covers it).
