---
slug: fibonacci
title: Fibonacci number
difficulty: MEDIUM
topic: functions
position: 2
---

## Statement

The Fibonacci numbers start with `0` and `1`, and each next number is the sum of the two before it: `0, 1, 1, 2, 3, 5, 8, 13, ...`

Write a function `solve(n)` that returns the `n`-th Fibonacci number, counting from `0`: `solve(0)` is `0`, `solve(1)` is `1`.

Example: `solve(10)` returns `55`.

Constraints: `0 <= n <= 70`. A function that calls itself twice for every number is far too slow for `n = 70`.

## Starter code

```python
def solve(n):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[10]` | `55` | the example from the statement |
| `[0]` | `0` | the first number |
| `[1]` | `1` | the second number |
| `[2]` | `1` | the first sum |
| `[20]` | `6765` | a bigger index |
| `[70]` | `190392490709135` | the largest index: a slow recursive solution times out |

## Hints

1. Each number is the sum of the two before it, so you only ever need to remember the last two.
2. Keep them in two variables and update both at every step, instead of calling the function recursively.
3. `a, b = 0, 1`, then repeat `n` times `a, b = b, a + b`, and return `a`.

## Reference solution

```python
def solve(n):
    a, b = 0, 1
    for _ in range(n):
        a, b = b, a + b
    return a
```
