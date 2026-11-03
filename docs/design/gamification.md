# Gamification design

> **Status:** proposal by the tech lead. The numbers are starting values, kept in the database (`app_settings`, edited from the admin panel, see [admin-panel.md](admin-panel.md)), not in code, so they can be tuned without a deployment. The theme (space) is a working choice; the frontend teammate may rename and restyle everything.

Audience: **beginners in Python**. The design pushes users to go in order, solve a few problems every day and see their progress.

## Rules at a glance

- XP is awarded **only** from verdicts computed by the server, and **only the first time** a user solves a problem.
- Streak, badges and unlocks are derived from stored server results. They are never set by a client request.
- A "day" is the user's own calendar day (their time zone).

## XP and levels

| Difficulty | XP (first solve only) |
| --- | --- |
| Easy | 10 |
| Medium | 20 |
| Hard | 40 |

| Level | Rank | Total XP needed |
| --- | --- | --- |
| 1 | Cadet | 0 |
| 2 | Pilot | 30 |
| 3 | Navigator | 80 |
| 4 | Engineer | 150 |
| 5 | Captain | 250 |
| 6 | Commander | 400 |

Calibrated for about 30 problems (15 Easy, 10 Medium, 5 Hard = 550 XP). Recalibrate when the real number of problems is known. A daily XP cap (value to be decided) limits farming.

**Hints lower the XP.** Each hint revealed before the first accepted solve of a problem reduces that solve's XP (start value: 25% per hint, at most 3 hints, so with all three hints the user gets 25% of the XP). The values are settings. Hints revealed after the problem is solved cost nothing.

## Daily streak

- The streak grows by one for each day with at least one **accepted** submission, counted in the user's time zone.
- Missing a day resets it to 0, unless a streak freeze is used (below).
- The profile shows the current streak, the longest streak and the activity grid (one cell per day, colored by the number of accepted submissions).

## Unlocks

| Unlock | Condition | Notes |
| --- | --- | --- |
| **Medium problems** | 10 Easy problems solved | Locked problems stay **visible** with a message such as "4 more Easy problems to unlock". Hard problems: same idea, to be defined when we know how many Medium there are. |
| **Streak freeze** | Reach level 3 (Navigator) | The user holds at most 1 freeze. It is used **automatically** when exactly one day was missed, and is then gone. A new one is granted when the user reaches the next level that grants one (to be decided; start with level 3 only). |
| **Cosmetics** | Levels and badges | Avatar or ship, profile color, frame for the activity grid. Mostly frontend; the backend stores which cosmetic is selected and checks that it is unlocked. |

## Badges (first set of 6)

Each badge is a row in a `badges` table with a rule type and a threshold; the server evaluates the rules after every verdict. A badge is awarded once (`user_badges` has a unique pair of user and badge).

| Badge | Rule | Rule type |
| --- | --- | --- |
| First Launch | first accepted submission | `SOLVED_COUNT >= 1` |
| Cadet Training | 10 Easy problems solved | `SOLVED_EASY >= 10` |
| In Orbit | 7-day streak | `STREAK >= 7` |
| Sharpshooter | 5 problems solved on the first attempt **without hints** | `FIRST_TRY_SOLVED >= 5` |
| Beyond the Belt | first Medium problem solved | `SOLVED_MEDIUM >= 1` |
| Mission Complete | every problem of one topic solved | `TOPIC_COMPLETED >= 1` |

A 30-day streak badge is left out on purpose: nobody can earn it before a demo.

## Topics

Problems belong to a **topic** and have a **position** inside it, so the list reads like a course: variables and conditions, loops, strings, lists, dictionaries, functions. The list page groups problems by topic. "Mission Complete" and later learning paths build on this.

## What the backend needs

- `problems`: `difficulty`, `topic`, `position`; unlock rules read these.
- `submissions` and the result per test; "first attempt" is derived from the order of a user's submissions for a problem.
- `hints` and `hint_reveals` (user, hint, time): the XP penalty and the Sharpshooter rule use the reveals made before the first accepted solve.
- `xp_events` or a counter on the user, `streak` data with the freeze, `badges`, `user_badges`, selected cosmetics.
- All of it is computed after a verdict, in one transaction, so a verdict is never counted twice.

## Open points

- Daily XP cap value.
- Whether Hard problems unlock (and how).
- Which levels grant a streak freeze after level 3.
- Names and art: decided by the frontend teammate in the UI design.
