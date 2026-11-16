---
slug: count-positives
title: Count the positive numbers
difficulty: EASY
topic: lists
position: 1
---

## Statement

Write a function `solve(numbers)` that returns how many numbers in the list are **positive** (greater than zero).

Example: `solve([3, -1, 0, 5])` returns `2`.

Constraints: the list has at most 100 integers.

## Starter code

```python
def solve(numbers):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[[3, -1, 0, 5]]` | `2` | the example from the statement |
| `[[]]` | `0` | an empty list |
| `[[0, 0, 0]]` | `0` | zero is not positive |
| `[[-5, -2]]` | `0` | only negative numbers |
| `[[1, 2, 3, 4]]` | `4` | only positive numbers |
| `[[7]]` | `1` | a single number |

## Hints

1. Start a counter at `0`.
2. Loop over the list and add 1 to the counter when `number > 0`.
3. Zero is neither positive nor negative, so `>` is the right comparison, not `>=`.

## Reference solution

```python
def solve(numbers):
    count = 0
    for number in numbers:
        if number > 0:
            count += 1
    return count
```
