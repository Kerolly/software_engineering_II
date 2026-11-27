"""Naive but correct solutions: what a beginner could write. Each one passes every test, but does
more work than the reference solution. Used only to calibrate the grader's time limits."""

NAIVE = {}

NAIVE["balanced-brackets"] = '''
def solve(text):
    only = ""
    for ch in text:
        if ch in "()[]{}":
            only += ch
    changed = True
    while changed:
        changed = False
        for pair in ["()", "[]", "{}"]:
            if pair in only:
                only = only.replace(pair, "", 1)
                changed = True
    return only == ""
'''

NAIVE["collatz-steps"] = '''
def solve(n):
    if n == 1:
        return 0
    if n % 2 == 0:
        return 1 + solve(n // 2)
    return 1 + solve(3 * n + 1)
'''

NAIVE["count-digits"] = '''
def solve(n):
    count = 1
    while 10 ** count <= n:
        count = count + 1
    return count
'''

NAIVE["count-positives"] = '''
def solve(numbers):
    count = 0
    for i in range(len(numbers)):
        if numbers[i] > 0:
            count = count + 1
    return count
'''

NAIVE["count-vowels"] = '''
def solve(text):
    count = 0
    for ch in text:
        for vowel in "aeiouAEIOU":
            if ch == vowel:
                count = count + 1
    return count
'''

NAIVE["even-or-odd"] = '''
def solve(n):
    if n < 0:
        n = -n
    while n >= 2:
        n = n - 2
    if n == 0:
        return "even"
    return "odd"
'''

NAIVE["fibonacci"] = '''
def solve(n):
    numbers = [0, 1]
    while len(numbers) <= n:
        numbers.append(numbers[-1] + numbers[-2])
    return numbers[n]
'''

NAIVE["flatten-list"] = '''
def solve(items):
    result = list(items)
    found = True
    while found:
        found = False
        flat = []
        for element in result:
            if isinstance(element, list):
                found = True
                for inner in element:
                    flat.append(inner)
            else:
                flat.append(element)
        result = flat
    return result
'''

NAIVE["group-anagrams"] = '''
def solve(words):
    groups = []
    for word in words:
        placed = False
        for group in groups:
            if sorted(group[0]) == sorted(word):
                group.append(word)
                placed = True
                break
        if not placed:
            groups.append([word])
    return groups
'''

NAIVE["invert-dictionary"] = '''
def solve(capitals):
    result = {}
    for country in list(capitals.keys()):
        for other in capitals:
            if other == country:
                result[capitals[other]] = other
    return result
'''

NAIVE["is-palindrome"] = '''
def solve(text):
    clean = []
    for ch in text:
        if ch.isalnum():
            clean.append(ch.lower())
    backwards = []
    for i in range(len(clean) - 1, -1, -1):
        backwards.append(clean[i])
    return clean == backwards
'''

NAIVE["is-prime"] = '''
def solve(n):
    if n < 2:
        return False
    for d in range(2, n):
        if n % d == 0:
            return False
    return True
'''

NAIVE["leap-year"] = '''
def solve(year):
    return (year % 4 == 0 and year % 100 != 0) or year % 400 == 0
'''

NAIVE["letter-count"] = '''
def solve(word):
    counts = {}
    for ch in word:
        counts[ch] = word.count(ch)
    return counts
'''

NAIVE["letter-grade"] = '''
def solve(score):
    for grade, low in [("A", 90), ("B", 80), ("C", 70), ("D", 60)]:
        if score >= low:
            return grade
    return "F"
'''

NAIVE["max-of-three"] = '''
def solve(a, b, c):
    return sorted([a, b, c])[-1]
'''

NAIVE["most-frequent-word"] = '''
def solve(text):
    words = text.lower().split()
    best = words[0]
    for word in words:
        if words.count(word) > words.count(best):
            best = word
    return best
'''

NAIVE["remove-duplicates"] = '''
def solve(numbers):
    result = []
    for i in range(len(numbers)):
        seen = False
        for j in range(i):
            if numbers[j] == numbers[i]:
                seen = True
        if not seen:
            result.append(numbers[i])
    return result
'''

NAIVE["reverse-string"] = '''
def solve(text):
    letters = list(text)
    result = ""
    while letters:
        result = result + letters.pop()
    return result
'''

NAIVE["run-length-encoding"] = '''
def solve(text):
    result = ""
    i = 0
    while i < len(text):
        j = i
        while j < len(text) and text[j] == text[i]:
            j = j + 1
        result = result + text[i] + str(j - i)
        i = j
    return result
'''

NAIVE["second-largest"] = '''
def solve(numbers):
    values = []
    for number in numbers:
        if number not in values:
            values.append(number)
    values.sort()
    if len(values) < 2:
        return None
    return values[-2]
'''

NAIVE["spiral-order"] = '''
def solve(matrix):
    rows = [list(row) for row in matrix]
    result = []
    while rows and rows[0]:
        result = result + rows.pop(0)
        rows = [list(column) for column in zip(*rows)][::-1]
    return result
'''

NAIVE["sum-of-evens"] = '''
def solve(numbers):
    evens = []
    for number in numbers:
        if number % 2 == 0:
            evens.append(number)
    total = 0
    for number in evens:
        total = total + number
    return total
'''

NAIVE["sum-to-n"] = '''
def solve(n):
    total = 0
    i = 1
    while i <= n:
        total = total + i
        i = i + 1
    return total
'''

NAIVE["triangle-type"] = '''
def solve(a, b, c):
    sides = sorted([a, b, c])
    if sides[0] + sides[1] <= sides[2]:
        return "invalid"
    if sides[0] == sides[2]:
        return "equilateral"
    if sides[0] == sides[1] or sides[1] == sides[2]:
        return "isosceles"
    return "scalene"
'''

# Solutions that are expected to fail on time: they show that the limit catches them.
TOO_SLOW = {
    "fibonacci": '''
def solve(n):
    if n < 2:
        return n
    return solve(n - 1) + solve(n - 2)
''',
}
