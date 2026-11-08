---
slug: is-prime
title: Prime number
difficulty: EASY
topic: functions
position: 1
---

## Statement

A **prime** number is an integer greater than 1 whose only divisors are 1 and itself.

Write a function `solve(n)` that returns `True` if `n` is prime and `False` otherwise.

Examples: `solve(7)` returns `True`, `solve(9)` returns `False`.

Constraints: `0 <= n <= 1000000`.

## Starter code

```python
def solve(n):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[7]` | `true` | the first example from the statement |
| `[9]` | `false` | 3 times 3 |
| `[1]` | `false` | 1 is not prime |
| `[0]` | `false` | 0 is not prime |
| `[2]` | `true` | the smallest prime, and the only even one |
| `[97]` | `true` | a two-digit prime |
| `[100]` | `false` | a round number |
| `[7919]` | `true` | a larger prime |

## Hints

1. Numbers below 2 are never prime.
2. Try every divisor `d` from 2 upwards. If `n % d == 0`, `n` is not prime.
3. You only need to try divisors while `d * d <= n`. That keeps it fast for large numbers.

## Reference solution

```python
def solve(n):
    if n < 2:
        return False
    d = 2
    while d * d <= n:
        if n % d == 0:
            return False
        d += 1
    return True
```
