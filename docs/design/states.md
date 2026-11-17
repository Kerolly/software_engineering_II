# States

The lifecycles that matter for the backend: a submission, a problem, a user account and the daily streak.

## 1. Submission

**What it shows.** A submission is created as `PENDING`, becomes `RUNNING` when the backend sends it to the grader, and ends in exactly one final status. A final status is never changed again, which is what makes "each verdict counts once" possible. `INTERNAL_ERROR` means the grader failed (unreachable after one retry, or the submission was stuck in `RUNNING` for too long); it never gives XP.

How the submission status is chosen from the per-test statuses (a proposal): if every test passed it is `ACCEPTED`; otherwise it takes the status of the first test that did not pass.

```mermaid
stateDiagram-v2
    [*] --> PENDING : POST /api/v1/submissions
    PENDING --> RUNNING : sent to the grader
    PENDING --> INTERNAL_ERROR : grader unreachable after one retry

    RUNNING --> ACCEPTED : all tests passed
    RUNNING --> WRONG_ANSWER : a test gave a wrong value
    RUNNING --> RUNTIME_ERROR : the code raised an error
    RUNNING --> TIME_LIMIT_EXCEEDED : time ran out
    RUNNING --> COMPILE_ERROR : syntax error
    RUNNING --> INTERNAL_ERROR : grader failed, or stuck too long

    ACCEPTED --> [*]
    WRONG_ANSWER --> [*]
    RUNTIME_ERROR --> [*]
    TIME_LIMIT_EXCEEDED --> [*]
    COMPILE_ERROR --> [*]
    INTERNAL_ERROR --> [*]
```

## 2. Problem

**What it shows.** An admin creates a problem as a draft and edits it freely. Publishing is allowed only when the reference solution passes every test (checked through the grader). Users see only published problems. An archived problem disappears from the list but keeps its history, and can be published again.

```mermaid
stateDiagram-v2
    [*] --> DRAFT : admin creates the problem
    DRAFT --> PUBLISHED : reference solution passes all tests
    PUBLISHED --> ARCHIVED : admin archives it
    ARCHIVED --> PUBLISHED : admin restores it
```

## 3. User account

**What it shows.** A normal user is `ACTIVE` from the first login. The first admins are created at startup and must change their password before doing anything else (`PASSWORD_CHANGE_REQUIRED`, stored as the flag `must_change_password`). A blocked user is stopped at once, because the status is checked on every request. Blocking and unblocking are admin actions and go to the audit log.

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : first login of a normal user
    [*] --> PASSWORD_CHANGE_REQUIRED : first admin created at startup
    PASSWORD_CHANGE_REQUIRED --> ACTIVE : password changed
    ACTIVE --> BLOCKED : admin blocks
    BLOCKED --> ACTIVE : admin unblocks
```

## 4. Daily streak

**What it shows.** How the streak moves from day to day, in the user's own time zone. A day with at least one accepted submission keeps it alive. After a missed day the streak is in danger: a streak freeze (if the user has one) is used automatically when the next accepted submission arrives, and the streak continues. Without a freeze, or after a second missed day, it is broken and the next accepted submission starts again from 1. The state is evaluated when needed (on the next accepted submission or when the profile is read), not by a midnight job. The step-by-step calculation is in [flows.md](flows.md).

```mermaid
stateDiagram-v2
    [*] --> NO_STREAK
    NO_STREAK --> SOLVED_TODAY : accepted submission, streak = 1
    SOLVED_TODAY --> WAITING : the day ends
    WAITING --> SOLVED_TODAY : accepted submission the next day, streak + 1
    WAITING --> MISSED : the next day ends with no accepted submission
    MISSED --> SOLVED_TODAY : accepted submission and a freeze is available, freeze used, streak + 1
    MISSED --> NO_STREAK : no freeze, or a second day is missed
```
