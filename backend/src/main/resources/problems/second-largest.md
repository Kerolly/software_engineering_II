---
slug: second-largest
title: Second largest
difficulty: MEDIUM
topic: lists
position: 3
---

## Statement

Write a function `solve(numbers)` that returns the **second largest distinct value** in the list: the largest value that is strictly smaller than the maximum.

Examples: `solve([4, 1, 7, 3])` returns `4` and `solve([7, 7, 3])` returns `3`, because the two 7s count as one value.

Constraints: the list has at most 100 integers and contains at least two different values.

## Starter code

```python
def solve(numbers):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[[4, 1, 7, 3]]` | `4` | the first example from the statement |
| `[[7, 7, 3]]` | `3` | the largest value appears twice |
| `[[5, 5, 5, 1]]` | `1` | the largest value appears many times |
| `[[1, 2]]` | `1` | only two values |
| `[[3, 3, 8, 8, 5]]` | `5` | both values repeat |
| `[[-3, -1, -2]]` | `-2` | negative numbers |
| `[[10, 9]]` | `9` | the largest value comes first |

## Hints

1. Find the largest value first.
2. Then look for the largest value among the ones that are strictly smaller than it.
3. Sorting and taking the second-to-last element is not enough: for `[7, 7, 3]` it gives `7`.

## Reference solution

```python
def solve(numbers):
    largest = max(numbers)
    second = None
    for number in numbers:
        if number < largest and (second is None or number > second):
            second = number
    return second
```
