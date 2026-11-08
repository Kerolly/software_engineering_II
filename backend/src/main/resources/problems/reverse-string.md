---
slug: reverse-string
title: Reverse a string
difficulty: EASY
topic: strings
position: 1
---

## Statement

Write a function `solve(text)` that returns `text` written backwards.

Example: `solve("hello")` returns `"olleh"`.

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
| `["hello"]` | `"olleh"` | the example from the statement |
| `[""]` | `""` | an empty string |
| `["a"]` | `"a"` | a single character |
| `["racecar"]` | `"racecar"` | a palindrome stays the same |
| `["Hello, World!"]` | `"!dlroW ,olleH"` | capitals, spaces and punctuation |
| `["ab cd"]` | `"dc ba"` | a space in the middle |

## Hints

1. Build a new string by adding the characters one by one.
2. If you add each character to the front, `result = ch + result`, the order flips.
3. Slicing works too: `text[::-1]` reads the string backwards.

## Reference solution

```python
def solve(text):
    result = ""
    for ch in text:
        result = ch + result
    return result
```
