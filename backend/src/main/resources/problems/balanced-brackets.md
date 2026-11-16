---
slug: balanced-brackets
title: Balanced brackets
difficulty: HARD
topic: strings
position: 5
---

## Statement

Write a function `solve(text)` that returns `True` if every bracket in `text` is closed by the matching bracket in the right order, and `False` otherwise. The brackets are `()`, `[]` and `{}`. All other characters are ignored.

Examples: `solve("{[()]}")` returns `True`, `solve("([)]")` returns `False`.

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
| `["{[()]}"]` | `true` | the first example from the statement |
| `["([)]"]` | `false` | closed in the wrong order |
| `[""]` | `true` | no brackets at all |
| `["((("]` | `false` | never closed |
| `["())"]` | `false` | one closing bracket too many |
| `["}{"]` | `false` | closed before it was opened |
| `["print(items[0])"]` | `true` | other characters are ignored |
| `["{a: [1, (2)]}"]` | `true` | brackets around other text |

## Hints

1. Keep a list of the brackets that are still open. The last one opened must be the first one closed.
2. On an opening bracket, `append` it. On a closing bracket, check that the list is not empty and that its last element is the matching opening bracket, then `pop` it.
3. At the end, the text is balanced only if no bracket is left open.

## Reference solution

```python
def solve(text):
    pairs = {")": "(", "]": "[", "}": "{"}
    stack = []
    for ch in text:
        if ch in "([{":
            stack.append(ch)
        elif ch in pairs:
            if not stack or stack[-1] != pairs[ch]:
                return False
            stack.pop()
    return len(stack) == 0
```
