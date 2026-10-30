# API conventions

Rules for the HTTP API, shared by backend and frontend. Every endpoint must follow them; the checks listed at the end enforce the ones that can be automated.

> **Status:** the items marked *provisional* still need the backend teammate's agreement. The exact shape of a submission is **not final** (see the example at the end).

## Base path

- All application endpoints live under `/api/v1/...`.
- The prefix is configured in **one place** (a path prefix applied to the REST controllers), never repeated in each `@RequestMapping`.
- `/actuator/**` and `/v3/api-docs` stay at the root.
- There is no CORS configuration: the browser always talks to a single origin (Vite proxy in dev, Caddy in production).

## Resources and URLs

- Plural nouns in kebab-case: `/api/v1/problems`, `/api/v1/submissions`.
- The current user is `/api/v1/me`. There is no public `/users/{id}`.
- Administration lives under `/api/v1/admin/...` (see *Authentication and roles*).
- Problems are addressed by a unique **`slug`** (`/api/v1/problems/two-sum`). Submissions and users are addressed by **UUID**.

## Identifiers

- Internal IDs are **UUID v7** (time-ordered), everywhere an ID is exposed except for problem slugs.

## JSON

- Property names are `camelCase`.
- Timestamps are ISO-8601 in UTC, e.g. `2026-10-30T17:19:40Z`.
- Enums are `UPPER_SNAKE_CASE` strings, e.g. `"difficulty": "EASY"`.
- No envelope: a single resource is the response body itself. Lists use the page object below; errors use `ProblemDetail`.

## Errors

All errors are `ProblemDetail` (RFC 9457), produced by one global `@RestControllerAdvice`:

```json
{
  "type": "https://se2/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "errors": [{ "field": "title", "message": "must not be blank" }]
}
```

| Status | Meaning |
| --- | --- |
| `400` | Invalid request (validation, malformed body). There is no `422`. |
| `401` | Not authenticated |
| `403` | Authenticated, but not allowed |
| `404` | Resource does not exist |
| `409` | Conflict with the current state |

## HTTP semantics

- `POST` that creates a resource returns `201` with a `Location` header.
- `PATCH` for partial updates; `PUT` only when replacing the whole resource.
- `DELETE` returns `204`.
- Work that runs in the background returns `202 Accepted` with the id of the new resource; the client polls it.

## Lists, filtering, sorting

- Pagination: `?page=0&size=20`. The maximum `size` is **100**.
- Page response:

  ```json
  { "items": [], "page": 0, "size": 20, "totalItems": 0, "totalPages": 0 }
  ```

- Filters are explicit query parameters (`?difficulty=EASY&tag=arrays&q=sum`).
- Sorting is one parameter, `sort=field,direction` (e.g. `sort=title,asc`). Only whitelisted fields can be sorted.

## Authentication and roles

- Requests carry `Authorization: Bearer <JWT>`. The backend validates the token as a standard OIDC/JWT, so it does not depend on one provider (the auth provider is still an open decision).
- The `users` table has its own `id` (UUID v7) and a unique **`authSubject`** column holding the token's `sub`.
- **RBAC** with two roles, `USER` and `ADMIN`, stored in our database, not in the auth provider.
- Routes under `/api/v1/admin/**` require `ADMIN`.
- XP and streaks are awarded only from verdicts computed by the server.

## DTOs

*Provisional.* Controllers never return JPA entities, only DTOs (Java records). Mapping is manual at first.

## Backend layout

*Provisional.* Packages are organized by feature under `com.ttvz.se2` (`problems/`, `submissions/`, `users/`), each with its own controller, service, repository and DTOs, not by technical layer.

## OpenAPI and the generated client

- Every operation has a unique **`operationId`** in `verbNoun` camelCase (`listProblems`, `getProblem`, `createSubmission`, `getMe`). Orval names the generated functions after it. Do not rely on the names generated from method names.
- Every operation has one tag, the feature name (`problems`, `submissions`), so Orval groups the client by feature.
- The OpenAPI snapshot is committed as **`backend/openapi.json`**. Orval reads it from there.
- `just gen-api` regenerates it: it starts PostgreSQL, runs the snapshot test in "write" mode (`-Dopenapi.write=true`), then runs Orval to regenerate the frontend client. Run it after **every** API change.
- The same snapshot test, run normally by `just test`, fails when the code and `backend/openapi.json` differ.
- A second automated test reads the spec and fails if an operation has no tag, a non-unique `operationId` or one that is not `verbNoun` camelCase.

> `just gen-api` and these tests arrive with the first endpoint.

## Code execution

- The server decides the verdict. The browser may run the tests for instant feedback, but only the server's verdict awards XP or streaks.
- The backend talks to the code runner through an internal interface (`Grader`), so the implementation can change without touching the public API. The choice (Judge0, a Python runner or something else) is still open.
- There are no hidden tests for now: for every test the user must see **where** it failed and **why**. A flag for hidden tests can be added later, because results are already per test.

## Example (illustrative, not binding)

A hypothetical problem, "Two Sum".

**List**

```http
GET /api/v1/problems?difficulty=EASY&tag=arrays&page=0&size=20&sort=title,asc
```
```json
{
  "items": [
    { "slug": "two-sum", "title": "Two Sum", "difficulty": "EASY", "tags": ["arrays", "hash-table"] }
  ],
  "page": 0, "size": 20, "totalItems": 1, "totalPages": 1
}
```

**One problem, with all its tests**

```http
GET /api/v1/problems/two-sum
```
```json
{
  "slug": "two-sum",
  "title": "Two Sum",
  "difficulty": "EASY",
  "tags": ["arrays", "hash-table"],
  "statementMarkdown": "Given an array of integers `nums` and an integer `target`, ...",
  "starterCode": { "PYTHON": "def two_sum(nums, target):\n    pass\n" },
  "testCases": [
    { "index": 1, "input": { "nums": [2, 7, 11, 15], "target": 9 }, "expectedOutput": [0, 1] },
    { "index": 2, "input": { "nums": [3, 2, 4], "target": 6 }, "expectedOutput": [1, 2] }
  ]
}
```

**Submit**

```http
POST /api/v1/submissions
{ "problemSlug": "two-sum", "language": "PYTHON", "code": "def two_sum(nums, target):\n    ..." }
```
```http
202 Accepted
Location: /api/v1/submissions/0192f3a4-7c1e-7b2a-9d11-3f5a8c0e2b44
{ "id": "0192f3a4-7c1e-7b2a-9d11-3f5a8c0e2b44", "status": "PENDING" }
```

**Result.** The client polls until the status leaves `PENDING`/`RUNNING`. All tests are run, and each result says where and why it failed:

```http
GET /api/v1/submissions/0192f3a4-7c1e-7b2a-9d11-3f5a8c0e2b44
```
```json
{
  "id": "0192f3a4-7c1e-7b2a-9d11-3f5a8c0e2b44",
  "problemSlug": "two-sum",
  "language": "PYTHON",
  "status": "WRONG_ANSWER",
  "submittedAt": "2026-10-30T17:19:40Z",
  "summary": { "total": 40, "passed": 39, "failed": 1 },
  "results": [
    { "index": 1, "status": "PASSED", "runtimeMs": 8 },
    {
      "index": 25,
      "status": "WRONG_ANSWER",
      "input": { "nums": [3, 3], "target": 6 },
      "expectedOutput": [0, 1],
      "actualOutput": [1, 0],
      "stdout": "checking pair (3, 3)\n",
      "error": null,
      "runtimeMs": 9
    }
  ]
}
```

A test that crashes has `"status": "RUNTIME_ERROR"`, `"actualOutput": null` and `"error": { "type": "IndexError", "message": "list index out of range", "line": 4 }`, where `line` refers to the user's code, never to server internals.

- Per-test statuses: `PASSED`, `WRONG_ANSWER`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`.
- Submission statuses: `PENDING`, `RUNNING`, `ACCEPTED`, `WRONG_ANSWER`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `COMPILE_ERROR`, `INTERNAL_ERROR`.
- All tests run, within an overall time limit; tests not reached when the limit hits are reported as not executed.

**Error**

```http
GET /api/v1/problems/nope  →  404
{ "type": "https://se2/errors/not-found", "title": "Problem not found", "status": 404, "detail": "No problem with slug 'nope'." }
```

**Current user and administration**

```http
GET /api/v1/me
{ "id": "0192f3a4-...", "displayName": "andrei", "role": "USER", "xp": 120, "level": 2, "streakDays": 5 }

POST /api/v1/admin/problems     (ADMIN only)  →  201, Location: /api/v1/problems/two-sum
```

## Open points

- Rate limiting: where and how much (waiting for the infrastructure teammate). The idea so far: only on `POST /api/v1/submissions`, per user, `429` with `Retry-After`.
- The exact request and response shape of a submission.
- The auth provider.
- Which implementation sits behind `Grader`, and the Python-in-the-browser library used for instant feedback.
