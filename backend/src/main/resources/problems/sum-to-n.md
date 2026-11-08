---
slug: sum-to-n
title: Sum from 1 to n
difficulty: EASY
topic: loops
position: 1
---

## Statement

Write a function `solve(n)` that returns the sum `1 + 2 + ... + n`, using a loop.

For `n = 0` there is nothing to add, so the result is `0`.

Example: `solve(5)` returns `15`.

Constraints: `0 <= n <= 10000`.

## Starter code

```python
def solve(n):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[5]` | `15` | the example from the statement |
| `[1]` | `1` | only one number |
| `[0]` | `0` | nothing to add |
| `[10]` | `55` | a small sum |
| `[100]` | `5050` | a bigger sum |
| `[10000]` | `50005000` | the largest allowed value |

## Hints

1. Keep a running total in a variable that starts at `0`.
2. `range(1, n + 1)` gives the numbers `1, 2, ..., n`.
3. `for i in range(1, n + 1): total += i`, then return `total`.

## Reference solution

```python
def solve(n):
    total = 0
    for i in range(1, n + 1):
        total += i
    return total
```
