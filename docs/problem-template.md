# Problem template

How to write a problem. Copy the example at the end, change it and send it as a pull request. Later, problems are created in the admin panel ([admin-panel.md](design/admin-panel.md)); the database is then the source of truth and these files are just a way to import the first problems. Each problem is one file in `backend/src/main/resources/problems/<slug>.md` (location to be confirmed by the backend teammate, who also decides how files are loaded as seed data).

## Rules for a good problem

- Audience: **beginners in Python**. One idea per problem; no tricks.
- Style (provisional, see [design/code-execution.md](design/code-execution.md)): a function with a given name, e.g. `def solve(n)`. The statement says exactly what it takes and returns.
- Every test is visible to the user, so each one needs a **short description** that explains why it exists ("an empty list", "negative numbers"). The user sees it when the test fails.
- Inputs must be small and fast. Each test has a hard time limit on the server; a correct simple solution must pass well within it.
- The expected output is compared outside Python as a value (not printed text), so it must be a plain value: number, string, list, dict, boolean.
- Write the reference solution and run it against all tests before submitting the problem.

## Fields

| Field | Meaning |
| --- | --- |
| `slug` | Unique, kebab-case, used in the URL (`/problems/two-sum`) |
| `title` | Short title |
| `difficulty` | `EASY`, `MEDIUM` or `HARD` |
| `topic` | One of: `basics`, `loops`, `strings`, `lists`, `dicts`, `functions` (extend as needed) |
| `position` | Order inside the topic (1, 2, 3, ...) |
| `statement` | Markdown: what to do, with 1–2 examples and constraints |
| `starterCode` | Code the editor starts with |
| `tests` | List of tests: `args`, `expected`, `description` |
| `hints` | Up to 3 hints, from a gentle nudge to almost the solution (each one revealed before solving lowers the XP) |
| `referenceSolution` | A correct solution that passes every test. Never shown to users. |

`args` and `expected` are written as **JSON**:

- `args` is the list of arguments passed to `solve`, so a single list argument is written `[[1, 2]]`;
- booleans are `true` and `false`, and dictionary keys are strings;
- dictionaries are compared without regard to the order of their keys;
- prefer integers, and keep them below 2^53 so that JavaScript can show them exactly.

## Example

````markdown
---
slug: sum-of-evens
title: Sum of even numbers
difficulty: EASY
topic: loops
position: 2
---

## Statement

Write a function `solve(numbers)` that receives a list of integers and returns the **sum of the even ones**.

Example: `solve([1, 2, 3, 4])` returns `6`.

Constraints: the list has at most 100 elements.

## Starter code

```python
def solve(numbers):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[[1, 2, 3, 4]]` | `6` | the example from the statement |
| `[[]]` | `0` | an empty list |
| `[[1, 3, 5]]` | `0` | no even numbers |
| `[[-2, -4, 3]]` | `-6` | negative even numbers |

## Hints

1. Go through the list with a `for` loop and keep a running total.
2. A number is even when `number % 2 == 0`.
3. Add the number to the total only inside `if number % 2 == 0:`.

## Reference solution

```python
def solve(numbers):
    total = 0
    for number in numbers:
        if number % 2 == 0:
            total += number
    return total
```
````

> The format above is a starting point. If the backend teammate prefers JSON or YAML for the seed data, change this document in the same pull request that adds the seed import.
