---
slug: most-frequent-word
title: Most frequent word
difficulty: MEDIUM
topic: dicts
position: 3
---

## Statement

Write a function `solve(text)` that returns the word that appears **most often** in `text`, in lower case.

- Words are separated by one or more spaces. Capital letters do not matter: `"Go"` and `"go"` are the same word.
- If several words appear the same number of times, return the one that appears **first** in the text.

Examples: `solve("the cat and the hat")` returns `"the"`, `solve("Apple banana apple Banana")` returns `"apple"`.

Constraints: `text` contains only letters and spaces, and at least one word.

## Starter code

```python
def solve(text):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `["the cat and the hat"]` | `"the"` | the first example from the statement |
| `["Apple banana apple Banana"]` | `"apple"` | a tie: the word that appears first wins |
| `["hello"]` | `"hello"` | a single word |
| `["Go go GO stop"]` | `"go"` | capital letters do not matter |
| `["a b c"]` | `"a"` | every word appears once |
| `["  spaces   between   words spaces "]` | `"spaces"` | extra spaces are ignored |
| `["one two two three three three"]` | `"three"` | the most frequent word comes last |

## Hints

1. `text.lower().split()` gives the words in lower case, without the extra spaces.
2. Count the words in a dictionary, as in "Letter count".
3. To break ties, go through the words in their original order and keep a word only if its count is strictly bigger than the best one so far.

## Reference solution

```python
def solve(text):
    words = text.lower().split()
    counts = {}
    for word in words:
        counts[word] = counts.get(word, 0) + 1
    best = words[0]
    for word in words:
        if counts[word] > counts[best]:
            best = word
    return best
```
