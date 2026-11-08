---
slug: collatz-steps
title: Collatz steps
difficulty: MEDIUM
topic: loops
position: 4
---

## Statement

Start from a positive integer `n` and repeat:

- if `n` is even, divide it by 2;
- if `n` is odd, replace it with `3 * n + 1`.

Write a function `solve(n)` that returns **how many steps** it takes to reach `1`.

Example: `solve(6)` returns `8`, because `6 → 3 → 10 → 5 → 16 → 8 → 4 → 2 → 1` takes 8 steps. `solve(1)` returns `0`.

Constraints: `1 <= n <= 10000`.

## Starter code

```python
def solve(n):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[6]` | `8` | the example from the statement |
| `[1]` | `0` | already at 1 |
| `[2]` | `1` | one halving |
| `[16]` | `4` | a power of two only halves |
| `[7]` | `16` | goes up before it comes down |
| `[27]` | `111` | a long trip |
| `[97]` | `118` | the longest trip below 100 |

## Hints

1. You do not know in advance how many steps there are, so use `while n != 1:`.
2. Inside the loop, change `n` with an `if`/`else` and add 1 to a counter.
3. Use integer division, `n // 2`, so that `n` stays an integer.

## Reference solution

```python
def solve(n):
    steps = 0
    while n != 1:
        if n % 2 == 0:
            n = n // 2
        else:
            n = 3 * n + 1
        steps += 1
    return steps
```
