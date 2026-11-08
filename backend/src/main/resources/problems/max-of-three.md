---
slug: max-of-three
title: The largest of three
difficulty: EASY
topic: basics
position: 2
---

## Statement

Write a function `solve(a, b, c)` that returns the **largest** of three integers.

Try to solve it with `if` statements, without the built-in `max` function.

Example: `solve(3, 7, 5)` returns `7`.

Constraints: each number is between `-1000000` and `1000000`.

## Starter code

```python
def solve(a, b, c):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[3, 7, 5]` | `7` | the example from the statement |
| `[9, 2, 4]` | `9` | the largest number comes first |
| `[1, 2, 8]` | `8` | the largest number comes last |
| `[5, 5, 2]` | `5` | two numbers share the largest value |
| `[-4, -1, -9]` | `-1` | all numbers are negative |
| `[6, 6, 6]` | `6` | all three numbers are equal |

## Hints

1. Start by assuming that `a` is the largest and keep it in a variable.
2. Compare that variable with `b`, then with `c`, and replace it when you find a bigger number.
3. `largest = a`, then `if b > largest: largest = b`, and the same for `c`.

## Reference solution

```python
def solve(a, b, c):
    largest = a
    if b > largest:
        largest = b
    if c > largest:
        largest = c
    return largest
```
