---
slug: letter-count
title: Letter count
difficulty: EASY
topic: dicts
position: 1
---

## Statement

Write a function `solve(word)` that returns a **dictionary** with each letter of `word` as a key and the number of times it appears as the value.

Example: `solve("banana")` returns `{"b": 1, "a": 3, "n": 2}`. The order of the keys does not matter.

Constraints: `word` has at most 100 lowercase letters.

## Starter code

```python
def solve(word):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `["banana"]` | `{"b": 1, "a": 3, "n": 2}` | the example from the statement |
| `[""]` | `{}` | an empty word gives an empty dictionary |
| `["abc"]` | `{"a": 1, "b": 1, "c": 1}` | every letter once |
| `["zzz"]` | `{"z": 3}` | one letter repeated |
| `["mississippi"]` | `{"m": 1, "i": 4, "s": 4, "p": 2}` | a longer word |

## Hints

1. Start with an empty dictionary: `counts = {}`.
2. For each letter, add 1 to its count. If the letter is not in the dictionary yet, its count starts at 0.
3. `counts[ch] = counts.get(ch, 0) + 1` does both in one line.

## Reference solution

```python
def solve(word):
    counts = {}
    for ch in word:
        counts[ch] = counts.get(ch, 0) + 1
    return counts
```
