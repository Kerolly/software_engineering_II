---
slug: is-palindrome
title: Palindrome sentence
difficulty: MEDIUM
topic: strings
position: 3
---

## Statement

A palindrome reads the same forwards and backwards. Write a function `solve(text)` that returns `True` if `text` is a palindrome when you **ignore capital letters** and **every character that is not a letter or a digit**, and `False` otherwise.

Examples: `solve("racecar")` returns `True`, `solve("A man, a plan, a canal: Panama")` returns `True`, `solve("hello")` returns `False`.

An empty string counts as a palindrome.

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
| `["racecar"]` | `true` | a simple palindrome |
| `["A man, a plan, a canal: Panama"]` | `true` | capitals, spaces and punctuation are ignored |
| `["hello"]` | `false` | not a palindrome |
| `[""]` | `true` | an empty string reads the same both ways |
| `["No 'x' in Nixon"]` | `true` | quotes are ignored too |
| `["12321"]` | `true` | digits count |
| `["ab"]` | `false` | two different letters |

## Hints

1. First build a cleaned string with only the letters and digits, in lower case.
2. `ch.isalnum()` tells you whether a character is a letter or a digit.
3. A string is a palindrome when it equals its reverse: `clean == clean[::-1]`.

## Reference solution

```python
def solve(text):
    clean = ""
    for ch in text:
        if ch.isalnum():
            clean += ch.lower()
    return clean == clean[::-1]
```
