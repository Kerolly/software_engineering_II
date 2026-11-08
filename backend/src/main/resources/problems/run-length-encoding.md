---
slug: run-length-encoding
title: Run-length encoding
difficulty: MEDIUM
topic: strings
position: 4
---

## Statement

Run-length encoding replaces each run of the same character with the character followed by the length of the run.

Write a function `solve(text)` that returns the encoded string.

Example: `solve("aaabccdddd")` returns `"a3b1c2d4"`. An empty string stays empty.

Constraints: `text` has at most 1000 lowercase letters.

## Starter code

```python
def solve(text):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `["aaabccdddd"]` | `"a3b1c2d4"` | the example from the statement |
| `[""]` | `""` | an empty string |
| `["a"]` | `"a1"` | a single character |
| `["abc"]` | `"a1b1c1"` | no repeated characters |
| `["zzzzzzzzzzzz"]` | `"z12"` | a run longer than 9 |
| `["aabbaa"]` | `"a2b2a2"` | the same letter in two separate runs |

## Hints

1. Walk through the string and remember the current character and how many times you have seen it in a row.
2. When the character changes, write the previous character and its count, then start counting the new one.
3. Do not forget to write the last run after the loop ends.

## Reference solution

```python
def solve(text):
    if text == "":
        return ""
    result = ""
    current = text[0]
    count = 0
    for ch in text:
        if ch == current:
            count += 1
        else:
            result += current + str(count)
            current = ch
            count = 1
    result += current + str(count)
    return result
```
