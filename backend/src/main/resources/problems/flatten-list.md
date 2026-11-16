---
slug: flatten-list
title: Flatten a nested list
difficulty: HARD
topic: functions
position: 3
---

## Statement

A nested list can contain integers and other lists, at any depth. Write a function `solve(items)` that returns a **flat list** with all the integers, in the order in which they appear.

Example: `solve([1, [2, [3, 4]], 5])` returns `[1, 2, 3, 4, 5]`.

Constraints: at most 100 integers in total, nested at most 10 levels deep.

## Starter code

```python
def solve(items):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[[1, [2, [3, 4]], 5]]` | `[1, 2, 3, 4, 5]` | the example from the statement |
| `[[]]` | `[]` | an empty list |
| `[[1, 2, 3]]` | `[1, 2, 3]` | already flat |
| `[[[[[[7]]]]]]` | `[7]` | one number, deeply nested |
| `[[[], [[]], 1]]` | `[1]` | empty inner lists disappear |
| `[[[1, 2], [3, [4, [5, 6]]], 7]]` | `[1, 2, 3, 4, 5, 6, 7]` | several levels of nesting |

## Hints

1. A function may call itself. To flatten an inner list, flatten it with the same function.
2. For each element: if `isinstance(element, list)`, flatten it and `extend` the result with it; otherwise `append` the element.
3. The empty list is the simplest case: the loop does nothing and the function returns `[]`.

## Reference solution

```python
def solve(items):
    result = []
    for element in items:
        if isinstance(element, list):
            result.extend(solve(element))
        else:
            result.append(element)
    return result
```
