---
slug: sum-of-evens
title: Sum of even numbers
difficulty: EASY
topic: loops
position: 2
---

## Statement

Write a function `solve(numbers)` that receives a list of integers and returns the **sum of the even ones**.

Example: `solve([1, 2, 3, 4])` returns `6`.

Constraints: the list has at most 100 elements.

## Starter code

```python
def solve(numbers):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[[1, 2, 3, 4]]` | `6` | the example from the statement |
| `[[]]` | `0` | an empty list |
| `[[1, 3, 5]]` | `0` | no even numbers |
| `[[-2, -4, 3]]` | `-6` | negative even numbers |

## Hints

1. Go through the list with a `for` loop and keep a running total.
2. A number is even when `number % 2 == 0`.
3. Add the number to the total only inside `if number % 2 == 0:`.

## Reference solution

```python
def solve(numbers):
    total = 0
    for number in numbers:
        if number % 2 == 0:
            total += number
    return total
```
