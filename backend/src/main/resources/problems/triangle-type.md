---
slug: triangle-type
title: Kind of triangle
difficulty: MEDIUM
topic: basics
position: 5
---

## Statement

Write a function `solve(a, b, c)` that receives the lengths of three sides and returns what kind of triangle they make:

- `"equilateral"`: all three sides are equal;
- `"isosceles"`: exactly two sides are equal;
- `"scalene"`: all sides are different;
- `"invalid"`: the sides cannot form a triangle. Each side must be **strictly smaller** than the sum of the other two.

Examples: `solve(3, 3, 3)` returns `"equilateral"`, `solve(3, 4, 5)` returns `"scalene"`, `solve(1, 2, 3)` returns `"invalid"` (because `1 + 2` is not greater than `3`).

Constraints: `1 <= a, b, c <= 1000`.

## Starter code

```python
def solve(a, b, c):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[3, 3, 3]` | `"equilateral"` | all sides equal |
| `[3, 4, 5]` | `"scalene"` | all sides different |
| `[5, 5, 8]` | `"isosceles"` | two equal sides |
| `[5, 8, 5]` | `"isosceles"` | the equal sides are not next to each other |
| `[1, 2, 3]` | `"invalid"` | a flat triangle: 1 + 2 equals 3 |
| `[1, 1, 5]` | `"invalid"` | two equal sides, but too short to close the triangle |
| `[10, 1, 1]` | `"invalid"` | the longest side comes first |
| `[2, 3, 4]` | `"scalene"` | a valid triangle with all sides different |

## Hints

1. Check whether the triangle is valid before anything else. Otherwise `(1, 1, 5)` looks isosceles.
2. Valid means `a < b + c and b < a + c and c < a + b`.
3. Then compare the sides: all equal is equilateral, any two equal is isosceles, otherwise scalene.

## Reference solution

```python
def solve(a, b, c):
    if a >= b + c or b >= a + c or c >= a + b:
        return "invalid"
    if a == b and b == c:
        return "equilateral"
    if a == b or b == c or a == c:
        return "isosceles"
    return "scalene"
```
