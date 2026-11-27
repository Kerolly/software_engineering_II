# Flows over time

The main sequences of the backend, in the order a user (and then an admin) meets them. Endpoint names that are not yet fixed are marked *proposed*; they are also listed in [api-conventions.md](../api-conventions.md).

## 1. Login and first-login user sync

**What it shows.** The browser logs in with Firebase and gets an ID token; the backend never sees the password. On the first request with a token whose `sub` is unknown, the backend creates the user (role `USER`, status `ACTIVE`, level 1, an empty streak row, time zone `UTC`). The browser then sets the real time zone with `PATCH /api/v1/me`. That first change does **not** start the 7-day limit: `time_zone_changed_at` stays empty until the user changes the time zone themselves. If two first requests arrive at the same moment, the unique `auth_subject` makes one insert fail and that request simply reads the existing user.

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
    API->>API: Validate the token (all checks in section 3)
    API->>DB: Find user by auth_subject
    alt user not found (first login)
        API->>DB: INSERT user and empty streak row
    end
    API-->>B: 200 current user
    B->>API: PATCH /api/v1/me with the time zone
    Note over API,DB: The first change from UTC does not set time_zone_changed_at
    API-->>B: 200
```

## 2. Creating the first admins at startup

**What it shows.** There is no admin UI before the first admin exists, so the backend creates them at startup from `ADMIN_BOOTSTRAP_EMAILS` and `ADMIN_BOOTSTRAP_PASSWORD`. In production it refuses to start if the password is weak or still the placeholder.

Sign-up with email and password is open and Firebase does not verify the email, so someone could register an admin's email before the first startup. That is why, when the account already exists, the backend does not trust it: it sets the bootstrap password and revokes the refresh tokens, so only the person who knows the bootstrap password gets in. The new admins are marked `must_change_password`, and can do nothing else until they change it (section 10).

Bootstrap runs at **every** startup, so it skips an admin who already chose a password (`ADMIN` with `must_change_password = false`); otherwise it would reset a legitimate password. Every step is written to `audit_log`; the `admin_id` is empty because the actor is the system.

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
            BE->>FB: Find the account by email
            alt the account exists
                BE->>DB: Find the user by auth_subject
                alt already ADMIN and must_change_password is false
                    Note over BE,DB: Skip, this admin already chose a password
                else
                    BE->>FB: Set the bootstrap password and revoke refresh tokens
                    BE->>DB: Upsert user (role ADMIN, must_change_password true)
                    BE->>DB: INSERT audit_log
                end
            else
                BE->>FB: Create the account with the bootstrap password
                FB-->>BE: Firebase uid
                BE->>DB: INSERT user (role ADMIN, must_change_password true)
                BE->>DB: INSERT audit_log
            end
        end
    end
```

## 3. Checking a request (every call)

**What it shows.** The decisions made, in order, for every authenticated request. The token is checked locally with Firebase's public keys. The user's status is read from our database on every request, so blocking takes effect immediately even though the token itself stays valid for up to an hour. Roles are stored in our database and checked on the server; hiding a menu is not security. A user who must change the password can call only `POST /api/v1/me/password` (section 10).

```mermaid
flowchart TD
    R["Request"] --> T{"Valid token?<br/>all checks in the list below"}
    T -- no --> E401["401 Unauthorized"]
    T -- yes --> L["Load user by auth_subject<br/>(create it on first login)"]
    L --> BL{"Status BLOCKED?"}
    BL -- yes --> E403A["403 Forbidden"]
    BL -- no --> PW{"must_change_password and<br/>not POST /api/v1/me/password?"}
    PW -- yes --> E403B["403 password change required"]
    PW -- no --> AD{"Path under /api/v1/admin<br/>and role is not ADMIN?"}
    AD -- yes --> E403C["403 Forbidden"]
    AD -- no --> OK["Run the controller"]
```

The token checks, all of them (so the auth task does not miss one):

| Check | Expected |
| --- | --- |
| Algorithm | `alg` is `RS256` |
| Key | `kid` is one of Google's public keys for Firebase tokens (cached) |
| Signature | valid for that key |
| Expiry | `exp` is in the future |
| Issue time | `iat` and `auth_time` are in the past |
| Audience | `aud` is the Firebase project id (Spring does not check this by default: add a validator) |
| Issuer | `iss` is `https://securetoken.google.com/<projectId>` |
| Subject | `sub` is not empty |

## 4. Submitting code

**What it shows.** The full Submit chain. The browser sends only the source code. Before anything is stored, the backend rejects what it can: a problem that is not published or is locked for this user (the unlock rule is checked on the server), code that is too large, and rate limits. Otherwise it stores a `PENDING` submission and answers `202` immediately. The grader call runs in the background, with a 150 s timeout, long enough for a sleeping grader to wake up. It is retried once only when the grader did not accept it; after the timeout the backend gives up and the grader drops the request ([grader.md](grader.md#2-contract-between-the-backend-and-the-grader)). Meanwhile the browser polls until the status leaves `PENDING` and `RUNNING`.

To shorten the wait after a long pause, the backend wakes the grader early: it calls the grader's `GET /health`, without waiting for the answer, when the backend starts and when a user opens a problem page (at most once every 10 minutes).

The free host can restart the backend at any time. A cleanup job therefore moves submissions that stay `PENDING` or `RUNNING` for more than 5 minutes to `INTERNAL_ERROR`. "Grader unreachable" always happens in `RUNNING`, because the status is set before the call.

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
        Note over API,DB: A cleanup job moves old PENDING and RUNNING submissions to INTERNAL_ERROR
        par background task
            API->>DB: UPDATE status RUNNING
            API->>G: POST /grade (Bearer secret, code, tests with expected values)
            alt grader answers
                G-->>API: Result per test
                API->>DB: One transaction: results, final status, XP, streak, badges
            else not accepted after one retry, or no answer within 150 s
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

**What it shows.** Everything that happens in the single transaction that stores a verdict, so a verdict is never counted twice. The streak and the activity day are updated for **every** accepted submission (see section 6).

XP follows these rules:

- It is awarded only if the user has no `xp_event` for this problem yet. The unique key in the database is the final guard if two submissions race.
- If the daily cap is already reached, **no** `xp_event` is created. The user keeps the chance to earn this problem's XP with a later accepted submission (for example the next day). Below the cap the user gets the full XP, with no partial amounts.
- The hint penalty counts the hints revealed **before the `submitted_at` of the user's first accepted submission** for this problem, even when XP is awarded on a later one. A hint opened while a submission was running, or after the problem was solved, costs nothing.
- Because of the cap, "problem solved" is **not** derived from `xp_events`. It means that an `ACCEPTED` submission exists for the user and the problem. Unlocks and badges use that.

Badge rules and unlocks are evaluated from stored data after XP and streak are updated. All numbers (XP per difficulty, hint penalty, daily cap, level thresholds) are read from `app_settings`.

```mermaid
flowchart TD
    A["Grader result arrives"] --> B["Start one transaction"]
    B --> C["Save test_results<br/>Set the final submission status"]
    C --> D{"Status is ACCEPTED?"}
    D -- no --> Z["Commit"]
    D -- yes --> E["Update streak and activity day<br/>(see section 6)"]
    E --> F{"xp_event already exists<br/>for this user and problem?"}
    F -- yes --> H
    F -- no --> K{"Daily XP cap<br/>already reached?"}
    K -- yes --> H
    K -- no --> G["Base XP from the difficulty<br/>minus the hint penalty<br/>INSERT xp_event, update xp, recompute level"]
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

**What it shows.** Hints are revealed one by one, and the text is never available any other way: the problem detail returns only the number of hints. The backend records the reveal **before** returning the text, so a user cannot read a hint for free. Revealing the same hint twice is harmless. The XP penalty is computed later, in the verdict transaction (section 5), from the reveals made before the first accepted submission of the problem.

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

**What it shows.** An admin blocks a user and must give a reason (not blank, at most 500 characters), which is stored in `users.blocked_reason` and in the audit entry. Unblocking clears `blocked_reason`; the earlier reasons stay in `audit_log`. An admin cannot block themselves or the last active admin; the check runs in the same transaction as the block, with the active admin rows locked, so two admins cannot block each other at the same moment. The status change and the audit entry are written in that transaction. The Firebase account is also disabled and its refresh tokens revoked, so the user cannot get new tokens. If the Firebase call fails, the block still works, because the status is checked on every request (section 3).

```mermaid
sequenceDiagram
    actor Admin
    participant B as Browser
    participant API as Backend API
    participant DB as PostgreSQL
    participant FB as Firebase Admin SDK

    Admin->>B: Click Block on a user
    B->>API: POST /api/v1/admin/users/id/block with the reason (proposed)
    API->>API: Check the role is ADMIN
    alt reason missing, blank or too long
        API-->>B: 400 ProblemDetail
    else
        API->>DB: Lock the active admin rows and read the target user
        alt target is the caller or the last active admin
            API-->>B: 409 ProblemDetail
        else
            API->>DB: UPDATE user status BLOCKED and blocked_reason, INSERT audit_log with the reason (one transaction)
            API->>FB: Disable the account and revoke refresh tokens
            API-->>B: 204 No Content
            Note over API,DB: The blocked user's next request gets 403, even with a valid token
        end
    end
```

## 9. An admin creates and publishes a problem

**What it shows.** The content workflow. A problem starts as a draft; the admin adds tests, up to 3 hints and a reference solution. A dry run sends code to the grader without storing a submission or giving XP. Because nothing is stored in `submissions`, its rate limit is counted from `audit_log` (a `DRY_RUN` entry per call, index on `(admin_id, created_at)`). Publishing is accepted only if the reference solution passes every test, checked through the grader at that moment. Every step goes to the audit log.

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
        API->>DB: Count the recent DRY_RUN entries of this admin in audit_log
        alt limit exceeded
            API-->>B: 429 with Retry-After
        else
            API->>DB: INSERT audit_log (DRY_RUN)
            API->>G: POST /grade
            G-->>API: Result per test
            API-->>B: 200 results, nothing stored, no XP
        end
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

## 10. Changing the password of a first admin

**What it shows.** How `must_change_password` is cleared. The password lives in Firebase, so the backend cannot know that it changed unless it makes the change itself. A client call such as "I changed it" would let anyone who knows the bootstrap password clear the flag. So the backend does everything in one step: it checks the strength of the new password, changes it through the Admin SDK, revokes the refresh tokens, and clears the flag. The client then signs in again.

A token issued before the bootstrap reset stays valid for up to an hour, so on this endpoint only the token is verified with the revoked check (one extra call to Firebase). The endpoint works only while the flag is true; this is also the only endpoint the check in section 3 lets through for such a user.

```mermaid
sequenceDiagram
    actor Admin
    participant B as Browser
    participant API as Backend API
    participant FB as Firebase Admin SDK
    participant DB as PostgreSQL

    Admin->>B: Enter the new password
    B->>API: POST /api/v1/me/password (proposed)
    API->>FB: Verify the token with the revoked check
    alt token revoked or invalid
        API-->>B: 401
    else must_change_password is false
        API-->>B: 409 ProblemDetail
    else the new password is too weak
        API-->>B: 400 ProblemDetail
    else
        API->>FB: Set the new password and revoke refresh tokens
        API->>DB: UPDATE must_change_password false and INSERT audit_log
        API-->>B: 204 No Content
        B->>FB: Sign in again with the new password
    end
```
