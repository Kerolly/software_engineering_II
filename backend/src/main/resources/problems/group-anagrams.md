---
slug: group-anagrams
title: Group the anagrams
difficulty: HARD
topic: dicts
position: 4
---

## Statement

Two words are **anagrams** if they use exactly the same letters, maybe in a different order: `"tea"` and `"eat"` are anagrams.

Write a function `solve(words)` that groups the anagrams together and returns a list of groups:

- the groups are in the order in which their first word appears in `words`;
- inside each group, the words keep their order from `words`.

Example: `solve(["eat", "tea", "tan", "ate", "nat", "bat"])` returns `[["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]`.

Constraints: at most 100 words, each with at most 20 lowercase letters.

## Starter code

```python
def solve(words):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[["eat", "tea", "tan", "ate", "nat", "bat"]]` | `[["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]` | the example from the statement |
| `[[]]` | `[]` | no words |
| `[["abc"]]` | `[["abc"]]` | a single word |
| `[["a", "b", "c"]]` | `[["a"], ["b"], ["c"]]` | no anagrams at all |
| `[["ab", "ba", "ab"]]` | `[["ab", "ba", "ab"]]` | a repeated word stays in its group |
| `[["listen", "silent", "enlist", "google"]]` | `[["listen", "silent", "enlist"], ["google"]]` | a group of three and a word alone |

## Hints

1. Two words are anagrams when their sorted letters are equal: `"".join(sorted("tea"))` is `"aet"`.
2. Use a dictionary from the sorted letters to the list of words in that group.
3. Dictionaries keep the order in which keys were added, so `list(groups.values())` already has the groups in the right order.

## Reference solution

```python
def solve(words):
    groups = {}
    for word in words:
        key = "".join(sorted(word))
        if key not in groups:
            groups[key] = []
        groups[key].append(word)
    return list(groups.values())
```
