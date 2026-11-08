---
slug: count-digits
title: Count the digits
difficulty: EASY
topic: loops
position: 3
---

## Statement

Write a function `solve(n)` that returns how many digits the non-negative integer `n` has. Use a loop rather than converting the number to a string.

Example: `solve(2026)` returns `4`. Note that `0` has one digit.

Constraints: `0 <= n <= 10**15`.

## Starter code

```python
def solve(n):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[2026]` | `4` | the example from the statement |
| `[7]` | `1` | a single digit |
| `[0]` | `1` | zero has one digit |
| `[10]` | `2` | a round number |
| `[999999]` | `6` | all nines |
| `[1000000000000000]` | `16` | the largest allowed value |

## Hints

1. Each time you divide an integer by 10 with `//`, it loses its last digit.
2. Count how many times you can do `n = n // 10` before `n` becomes `0`.
3. Be careful with `0`: it has one digit, but a `while n > 0` loop runs zero times.

## Reference solution

```python
def solve(n):
    if n == 0:
        return 1
    count = 0
    while n > 0:
        n = n // 10
        count += 1
    return count
```
