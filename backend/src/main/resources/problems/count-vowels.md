---
slug: count-vowels
title: Count the vowels
difficulty: EASY
topic: strings
position: 2
---

## Statement

Write a function `solve(text)` that returns how many vowels (`a`, `e`, `i`, `o`, `u`) `text` contains. Capital vowels count too; `y` is not a vowel.

Example: `solve("Hello World")` returns `3`.

Constraints: `text` has at most 1000 characters.

## Starter code

```python
def solve(text):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `["Hello World"]` | `3` | the example from the statement |
| `[""]` | `0` | an empty string |
| `["rhythm"]` | `0` | no vowels at all |
| `["AEIOU"]` | `5` | capital vowels count too |
| `["Programming in Python"]` | `5` | a longer sentence |
| `["aaa"]` | `3` | the same vowel repeated |

## Hints

1. Go through the string with a `for` loop, one character at a time.
2. `ch in "aeiou"` checks whether a character is a vowel.
3. Turn each character into lower case with `ch.lower()` first, so capital vowels count too.

## Reference solution

```python
def solve(text):
    count = 0
    for ch in text:
        if ch.lower() in "aeiou":
            count += 1
    return count
```
