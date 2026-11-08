---
slug: letter-grade
title: Letter grade
difficulty: EASY
topic: basics
position: 4
---

## Statement

Write a function `solve(score)` that turns a score from 0 to 100 into a letter grade:

| Score | Grade |
| --- | --- |
| 90 to 100 | `"A"` |
| 80 to 89 | `"B"` |
| 70 to 79 | `"C"` |
| 60 to 69 | `"D"` |
| below 60 | `"F"` |

Example: `solve(85)` returns `"B"`.

Constraints: `0 <= score <= 100`, and `score` is an integer.

## Starter code

```python
def solve(score):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[85]` | `"B"` | the example from the statement |
| `[100]` | `"A"` | the highest score |
| `[90]` | `"A"` | exactly on the border of A |
| `[89]` | `"B"` | just below A |
| `[75]` | `"C"` | in the middle of C |
| `[60]` | `"D"` | the lowest passing score |
| `[59]` | `"F"` | just below D |
| `[0]` | `"F"` | the lowest score |

## Hints

1. Check the thresholds from the highest one down.
2. Use `if`, `elif` and `else`: once one branch matches, the others are skipped.
3. `if score >= 90: return "A"`, then `elif score >= 80:` and so on, with `else` for `"F"`.

## Reference solution

```python
def solve(score):
    if score >= 90:
        return "A"
    elif score >= 80:
        return "B"
    elif score >= 70:
        return "C"
    elif score >= 60:
        return "D"
    else:
        return "F"
```
