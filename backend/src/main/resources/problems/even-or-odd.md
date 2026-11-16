---
slug: even-or-odd
title: Even or odd
difficulty: EASY
topic: basics
position: 1
---

## Statement

Write a function `solve(n)` that receives an integer and returns the string `"even"` if it is even, or `"odd"` if it is not.

Example: `solve(4)` returns `"even"` and `solve(7)` returns `"odd"`.

Constraints: `-1000 <= n <= 1000`.

## Starter code

```python
def solve(n):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[4]` | `"even"` | the first example from the statement |
| `[7]` | `"odd"` | the second example from the statement |
| `[0]` | `"even"` | zero is even |
| `[-3]` | `"odd"` | a negative odd number |
| `[-8]` | `"even"` | a negative even number |
| `[1000]` | `"even"` | the largest allowed value |

## Hints

1. A number is even when dividing it by 2 leaves no remainder.
2. The `%` operator gives the remainder of a division: `7 % 2` is `1`.
3. Use `if n % 2 == 0:` and return the right string in each branch.

## Reference solution

```python
def solve(n):
    if n % 2 == 0:
        return "even"
    return "odd"
```
