---
slug: remove-duplicates
title: Remove duplicates
difficulty: EASY
topic: lists
position: 2
---

## Statement

Write a function `solve(numbers)` that returns a **new list** with the duplicates removed. Keep each number in the place where it first appears.

Example: `solve([1, 2, 1, 3, 2])` returns `[1, 2, 3]`.

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
| `[[1, 2, 1, 3, 2]]` | `[1, 2, 3]` | the example from the statement |
| `[[]]` | `[]` | an empty list |
| `[[5, 5, 5]]` | `[5]` | the same number three times |
| `[[3, 1, 2]]` | `[3, 1, 2]` | nothing to remove |
| `[[2, 1, 2, 1]]` | `[2, 1]` | the order of first appearance is kept |
| `[[-1, 0, -1]]` | `[-1, 0]` | negative numbers and zero |

## Hints

1. Build a new list and add each number only if it is not already in it.
2. `number not in result` checks that.
3. `set(numbers)` also removes duplicates, but it does not keep the order, so the loop is the right tool here.

## Reference solution

```python
def solve(numbers):
    result = []
    for number in numbers:
        if number not in result:
            result.append(number)
    return result
```
