---
slug: invert-dictionary
title: Capitals and countries
difficulty: EASY
topic: dicts
position: 2
---

## Statement

Write a function `solve(capitals)` that receives a dictionary from countries to their capitals and returns a **new dictionary** from capitals to countries.

All capitals are different.

Example: `solve({"France": "Paris", "Italy": "Rome"})` returns `{"Paris": "France", "Rome": "Italy"}`.

Constraints: at most 100 entries.

## Starter code

```python
def solve(capitals):
    # your code here
    pass
```

## Tests

| args | expected | description |
| --- | --- | --- |
| `[{"France": "Paris", "Italy": "Rome"}]` | `{"Paris": "France", "Rome": "Italy"}` | the example from the statement |
| `[{}]` | `{}` | an empty dictionary |
| `[{"Romania": "Bucharest"}]` | `{"Bucharest": "Romania"}` | a single entry |
| `[{"Japan": "Tokyo", "Kenya": "Nairobi", "Peru": "Lima"}]` | `{"Tokyo": "Japan", "Nairobi": "Kenya", "Lima": "Peru"}` | several entries |
| `[{"a": "b", "b": "a"}]` | `{"b": "a", "a": "b"}` | the same strings appear as keys and as values |

## Hints

1. Create a new, empty dictionary for the result.
2. `for country, capital in capitals.items():` gives you each pair.
3. In the new dictionary the capital is the key: `result[capital] = country`.

## Reference solution

```python
def solve(capitals):
    result = {}
    for country, capital in capitals.items():
        result[capital] = country
    return result
```
