# Flows over time

The main sequences of the backend, in the order a user (and then an admin) meets them. Endpoint names that are not yet in [api-conventions.md](../api-conventions.md) are marked *proposed*.

## 1. Login and first-login user sync

**What it shows.** The browser logs in with Firebase and gets an ID token; the backend never sees the password. On the first request with a token whose `sub` is unknown, the backend creates the user (role `USER`, status `ACTIVE`, level 1, an empty streak row, time zone `UTC`). The browser then sets the real time zone with `PATCH /api/v1/me`. If two first requests arrive at the same moment, the unique `auth_subject` makes one insert fail and that request simply reads the existing user.

```mermaid
sequenceDiagram
    actor User
    participant B as Browser
    participant FB as Firebase
    participant API as Backend API
    participant DB as PostgreSQL

    User->>B: Enter email and password
    B->>FB: Sign in
    FB-->>B: ID token (JWT)
    B->>API: GET /api/v1/me with Authorization Bearer token
    API->>API: Validate signature, issuer, audience and expiry
    API->>DB: Find user by auth_subject
    alt user not found (first login)
        API->>DB: INSERT user and empty streak row
    end
    API-->>B: 200 current user
    B->>API: PATCH /api/v1/me with the time zone
    API-->>B: 200
```

## 2. Creating the first admins at startup

**What it shows.** There is no admin UI before the first admin exists, so the backend creates them at startup from `ADMIN_BOOTSTRAP_EMAILS` and `ADMIN_BOOTSTRAP_PASSWORD`. In production it refuses to start if the password is weak or still the placeholder. The new admins are marked `must_change_password`, so they can do nothing else until they change it.

```mermaid
sequenceDiagram
    participant BE as Backend (startup)
    participant FB as Firebase Admin SDK
    participant DB as PostgreSQL

    BE->>BE: Read ADMIN_BOOTSTRAP_EMAILS and the password
    alt production and weak or placeholder password
        BE->>BE: Refuse to start
    else
        loop each email
            BE->>FB: Find the account, create it if missing
            FB-->>BE: Firebase uid
            BE->>DB: INSERT user (role ADMIN, must_change_password = true) if missing
        end
    end
```

## 3. Checking a request (every call)

**What it shows.** The decisions made, in order, for every authenticated request. The token is checked locally with Firebase's public keys. The user's status is read from our database on every request, so blocking takes effect immediately even though the token itself stays valid for up to an hour. Roles are stored in our database and checked on the server; hiding a menu is not security.

```mermaid
flowchart TD
    R["Request"] --> T{"Valid token:<br/>signature, issuer,<br/>audience, expiry?"}
    T -- no --> E401["401 Unauthorized"]
    T -- yes --> L["Load user by auth_subject<br/>(create it on first login)"]
    L --> BL{"Status BLOCKED?"}
    BL -- yes --> E403A["403 Forbidden"]
    BL -- no --> PW{"must_change_password<br/>and not a password endpoint?"}
    PW -- yes --> E403B["403 password change required"]
    PW -- no --> AD{"Path under /api/v1/admin<br/>and role is not ADMIN?"}
    AD -- yes --> E403C["403 Forbidden"]
    AD -- no --> OK["Run the controller"]
```

## 4. Submitting code

**What it shows.** The full Submit chain. The browser sends only the source code. Before anything is stored, the backend rejects what it can: a problem that is not published or is locked for this user (the unlock rule is checked on the server), code that is too large, and rate limits. Otherwise it stores a `PENDING` submission and answers `202` immediately. The grader call runs in the background, with a timeout long enough for a sleeping grader to wake up and one retry. Meanwhile the browser polls until the status leaves `PENDING` and `RUNNING`.

```mermaid
sequenceDiagram
    actor User
    participant B as Browser
    participant API as Backend API
    participant DB as PostgreSQL
    participant G as Grader

    User->>B: Click Submit
    B->>API: POST /api/v1/submissions (problemSlug, language, code)
    API->>DB: Load the problem, the user's solved count, recent submissions
    alt problem not published or locked for this user
        API-->>B: 403 or 404 ProblemDetail
    else code too large
        API-->>B: 400 ProblemDetail
    else a rate limit is exceeded
        API-->>B: 429 with Retry-After, or 503 if the grader is busy
    else accepted for grading
        API->>DB: INSERT submission (PENDING)
        API-->>B: 202 Accepted, Location /api/v1/submissions/id
        par background task
            API->>DB: UPDATE status RUNNING
            API->>G: POST /grade (Bearer secret, code, tests with expected values)
            alt grader answers
                G-->>API: Result per test
                API->>DB: One transaction: results, final status, XP, streak, badges
            else grader unreachable after one retry
                API->>DB: UPDATE status INTERNAL_ERROR
            end
        and browser polling
            loop until the status is final
                B->>API: GET /api/v1/submissions/id
                API-->>B: PENDING or RUNNING
            end
            B->>API: GET /api/v1/submissions/id
            API-->>B: Final status and results per test
        end
        B-->>User: Show passed tests, and where and why the others failed
    end
```

## 5. After a verdict: XP, streak, badges

**What it shows.** Everything that happens in the single transaction that stores a verdict, so a verdict is never counted twice. The streak and the activity day are updated for **every** accepted submission. XP is awarded **only** if the user has no `xp_event` for this problem yet; the unique key in the database is the final guard if two submissions race. Badge rules and unlocks are evaluated from stored data after XP and streak are updated. All numbers (XP per difficulty, hint penalty, daily cap, level thresholds) are read from `app_settings`.

```mermaid
flowchart TD
    A["Grader result arrives"] --> B["Start one transaction"]
    B --> C["Save test_results<br/>Set the final submission status"]
    C --> D{"Status is ACCEPTED?"}
    D -- no --> Z["Commit"]
    D -- yes --> E["Update streak and activity day<br/>(see the streak calculation below)"]
    E --> F{"xp_event already exists<br/>for this user and problem?"}
    F -- yes --> H
    F -- no --> G["Base XP from the difficulty<br/>minus the hint penalty<br/>apply the daily cap<br/>INSERT xp_event, update xp, recompute level"]
    G --> H["Evaluate the badge rules<br/>INSERT user_badges for new ones"]
    H --> I["Grant a streak freeze if the new level gives one"]
    I --> Z
```

## 6. Daily streak calculation

**What it shows.** How the streak is updated when an accepted submission arrives. The "day" is the date in the user's stored time zone at the moment of the verdict. Same day: nothing changes except the activity counter. Next day: the streak grows. Two days ago (exactly one day missed): a freeze, if the user has one, is consumed and the streak continues. Anything older: the streak restarts from 1.

```mermaid
flowchart TD
    A["Accepted submission at time t"] --> B["day = date of t in the user's time zone"]
    B --> C{"last_active_day compared to day"}
    C -- "same day" --> D["Streak unchanged"]
    C -- "one day before" --> E["current_streak + 1"]
    C -- "two days before" --> F{"freeze available?"}
    C -- "older, or no activity yet" --> H["current_streak = 1<br/>the streak was broken"]
    F -- yes --> G["Consume the freeze<br/>mark the missed day as freeze_used<br/>current_streak + 1"]
    F -- no --> H
    D --> I
    E --> I
    G --> I
    H --> I["last_active_day = day<br/>longest_streak = max of longest and current<br/>increment activity_days for day"]
```

## 7. Revealing a hint

**What it shows.** Hints are revealed one by one, and the text is never available any other way: the problem detail returns only the number of hints. The backend records the reveal **before** returning the text, so a user cannot read a hint for free. Revealing the same hint twice is harmless. The XP penalty applies only to hints revealed before the first accepted solve; it is computed later, in the verdict transaction (diagram 5).

```mermaid
sequenceDiagram
    actor User
    participant B as Browser
    participant API as Backend API
    participant DB as PostgreSQL

    User->>B: Click Reveal hint 2 (the UI warns about the XP cost)
    B->>API: POST /api/v1/problems/slug/hints/2/reveal (proposed)
    API->>DB: Load the problem and check it is published and unlocked
    alt hint 1 was not revealed yet
        API-->>B: 409 ProblemDetail
    else
        API->>DB: INSERT hint_reveal, ignore if it already exists
        API->>DB: SELECT the hint text
        API-->>B: 200 hint text
    end
    B-->>User: Show the hint
```

## 8. Blocking a user

**What it shows.** An admin blocks a user. The status change and the audit entry are written in the same transaction. The Firebase account is also disabled and its refresh tokens revoked, so the user cannot get new tokens. If the Firebase call fails, the block still works, because the status is checked on every request (diagram 3).

```mermaid
sequenceDiagram
    actor Admin
    participant B as Browser
    participant API as Backend API
    participant DB as PostgreSQL
    participant FB as Firebase Admin SDK

    Admin->>B: Click Block on a user
    B->>API: POST /api/v1/admin/users/id/block (proposed)
    API->>API: Check the role is ADMIN
    API->>DB: UPDATE user status BLOCKED and INSERT audit_log (one transaction)
    API->>FB: Disable the account and revoke refresh tokens
    API-->>B: 204 No Content
    Note over API,DB: The blocked user's next request gets 403, even with a valid token
```

## 9. An admin creates and publishes a problem

**What it shows.** The content workflow. A problem starts as a draft; the admin adds tests, up to 3 hints and a reference solution. A dry run sends code to the grader without storing a submission or giving XP (it is rate-limited like normal submissions). Publishing is accepted only if the reference solution passes every test, checked through the grader at that moment. Every step goes to the audit log.

```mermaid
sequenceDiagram
    actor Admin
    participant B as Browser
    participant API as Backend API
    participant DB as PostgreSQL
    participant G as Grader

    Admin->>B: Fill in the problem editor
    B->>API: POST /api/v1/admin/problems
    API->>DB: INSERT problem (DRAFT) and audit_log
    API-->>B: 201 Created
    B->>API: PATCH the problem: statement, tests, hints, reference solution
    API->>DB: UPDATE and audit_log
    API-->>B: 200

    opt try some code
        B->>API: POST /api/v1/admin/problems/id/dry-run (proposed)
        API->>G: POST /grade
        G-->>API: Result per test
        API-->>B: 200 results, nothing stored, no XP
    end

    Admin->>B: Click Publish
    B->>API: POST /api/v1/admin/problems/id/publish (proposed)
    API->>G: POST /grade with the reference solution and all tests
    G-->>API: Result per test
    alt every test passed
        API->>DB: UPDATE status PUBLISHED and audit_log
        API-->>B: 200
    else some test failed
        API-->>B: 409 ProblemDetail with the failing tests
    end
```
