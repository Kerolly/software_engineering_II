---
slug: spiral-order
title: Spiral order
difficulty: HARD
topic: lists
position: 4
---

## Statement

A matrix is a list of rows, and every row has the same length. Write a function `solve(matrix)` that returns all its elements in **clockwise spiral order**, starting from the top-left corner.

Example:

```text
1 2 3
4 5 6
7 8 9
```

`solve([[1, 2, 3], [4, 5, 6], [7, 8, 9]])` returns `[1, 2, 3, 6, 9, 8, 7, 4, 5]`.

An empty matrix (`[]`) gives an empty list.

Constraints: at most 10 rows and 10 columns.

## Starter code

```python
def solve(matrix):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[[[1, 2, 3], [4, 5, 6], [7, 8, 9]]]` | `[1, 2, 3, 6, 9, 8, 7, 4, 5]` | the example from the statement |
| `[[]]` | `[]` | an empty matrix |
| `[[[7]]]` | `[7]` | a single element |
| `[[[1, 2, 3, 4]]]` | `[1, 2, 3, 4]` | a single row |
| `[[[1], [2], [3]]]` | `[1, 2, 3]` | a single column |
| `[[[1, 2], [3, 4]]]` | `[1, 2, 4, 3]` | a 2 by 2 matrix |
| `[[[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]]` | `[1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7]` | wider than tall |
| `[[[1, 2], [3, 4], [5, 6]]]` | `[1, 2, 4, 6, 5, 3]` | taller than wide |

## Hints

1. Keep four borders: the top row, the bottom row, the left column and the right column.
2. Go right along the top, down the right side, left along the bottom and up the left side, then move each border one step inwards.
3. Stop when the borders cross. Check them again before the bottom and left passes, so that a single row or column is not read twice.

## Reference solution

```python
def solve(matrix):
    result = []
    if not matrix:
        return result
    top, bottom = 0, len(matrix) - 1
    left, right = 0, len(matrix[0]) - 1
    while top <= bottom and left <= right:
        for col in range(left, right + 1):
            result.append(matrix[top][col])
        top += 1
        for row in range(top, bottom + 1):
            result.append(matrix[row][right])
        right -= 1
        if top <= bottom:
            for col in range(right, left - 1, -1):
                result.append(matrix[bottom][col])
            bottom -= 1
        if left <= right:
            for row in range(bottom, top - 1, -1):
                result.append(matrix[row][left])
            left += 1
    return result
```
