---
slug: leap-year
title: Leap year
difficulty: EASY
topic: basics
position: 3
---

## Statement

A year is a **leap year** if it is divisible by 4, except the years divisible by 100, which are leap years only if they are also divisible by 400.

Write a function `solve(year)` that returns `True` if `year` is a leap year and `False` otherwise.

Examples: `solve(2024)` returns `True`, `solve(1900)` returns `False`, `solve(2000)` returns `True`.

Constraints: `1 <= year <= 9999`.

## Starter code

```python
def solve(year):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[2024]` | `true` | divisible by 4 |
| `[2023]` | `false` | not divisible by 4 |
| `[1900]` | `false` | divisible by 100 but not by 400 |
| `[2000]` | `true` | divisible by 400 |
| `[2100]` | `false` | another year divisible by 100 but not by 400 |
| `[4]` | `true` | a very small leap year |

## Hints

1. There are three rules. Check the most specific one first: divisible by 400.
2. If `year % 400 == 0` it is a leap year; otherwise, if `year % 100 == 0` it is not; otherwise check divisibility by 4.
3. It also fits in one expression: `year % 4 == 0 and (year % 100 != 0 or year % 400 == 0)`.

## Reference solution

```python
def solve(year):
    if year % 400 == 0:
        return True
    if year % 100 == 0:
        return False
    return year % 4 == 0
```
