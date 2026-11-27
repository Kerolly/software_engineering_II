"""Writes calibration.json for the grader probe: the tests and the reference solution of every
problem in backend/src/main/resources/problems, plus a naive but correct solution of each.

Run from the repository root:
    python3 tools/grader-probe/calibration/make_calibration.py
It checks every solution with CPython first and stops if one fails a test.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[3]
sys.dont_write_bytecode = True
sys.path.insert(0, str(pathlib.Path(__file__).parent))
from naive_solutions import NAIVE, TOO_SLOW  # noqa: E402

problems = []
for path in sorted((ROOT / "backend/src/main/resources/problems").glob("*.md")):
    text = path.read_text(encoding="utf-8")
    slug = re.search(r"^slug: (.+)$", text, re.M).group(1).strip()
    table = text.split("## Tests", 1)[1].split("\n## ", 1)[0]
    tests = []
    for line in table.splitlines():
        match = re.match(r"\|\s*`(.+?)`\s*\|\s*`(.+?)`\s*\|", line)
        if match:
            tests.append({"args": json.loads(match.group(1)), "expected": json.loads(match.group(2))})
    reference = re.search(r"## Reference solution\s*```python\n(.*?)```", text, re.S).group(1)
    problems.append({"slug": slug, "tests": tests, "reference": reference, "naive": NAIVE[slug]})

for problem in problems:
    for kind in ("reference", "naive"):
        scope = {}
        exec(problem[kind], scope)
        for test in problem["tests"]:
            got = scope["solve"](*json.loads(json.dumps(test["args"])))
            if json.dumps(got, sort_keys=True) != json.dumps(test["expected"], sort_keys=True):
                sys.exit(f"{problem['slug']} ({kind}) fails {test['args']}: {got!r}")

too_slow = [{"slug": slug, "code": code, "args": [20, 23, 25]} for slug, code in TOO_SLOW.items()]
out = ROOT / "tools/grader-probe/calibration.json"
out.write_text(json.dumps({"problems": problems, "tooSlow": too_slow}, indent=1) + "\n", encoding="utf-8")
print(f"{out.relative_to(ROOT)}: {len(problems)} problems")
