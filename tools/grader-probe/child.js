// Probe child: restore Pyodide from the snapshot, then run one fixed scenario: three trivial
// tests, a memory bomb, an endless loop, escape attempts, or the time limit calibration (problems
// from the image, sent on stdin). It never runs code from a request.
const [dir, snapshotPath, mode] = Deno.args;
const t0 = performance.now();
const { loadPyodide } = await import("file://" + dir + "/pyodide.mjs");
const py = await loadPyodide({ indexURL: dir + "/", _loadSnapshot: await Deno.readFile(snapshotPath) });
const t1 = performance.now();
console.log(JSON.stringify({
  ready: true,
  restoreMs: Math.round(t1 - t0),
  sinceProcessStartMs: Math.round(t1),
  pyodide: py.version,
}));

if (mode === "bomb") {
  const s = performance.now();
  let bomb;
  try {
    bomb = py.runPython(`
x = []
try:
    while True:
        x.append("a" * 10**6)
    r = "no limit hit"
except MemoryError:
    n = len(x)
    x = None
    r = "MemoryError after %d MB" % n
r`);
  } catch (e) {
    bomb = "JS error: " + String(e && e.message).split("\n").slice(-2).join(" ").slice(0, 160);
  }
  let next;
  try {
    next = String(py.runPython("sum(range(10))"));
  } catch (e) {
    next = "interpreter unusable";
  }
  console.log(JSON.stringify({ bomb, bombMs: Math.round(performance.now() - s), nextRun: next }));
} else if (mode === "calibrate") {
  // Time limit calibration: the parent sends the problems' tests and solutions on stdin, as the
  // grader will send a submission. Each solution runs every test three times; the slowest run counts.
  const input = await new Response(Deno.stdin.readable).text();
  py.globals.set("calibration_input", input);
  const output = py.runPython(`
import json, time

def time_solution(code, tests, repeat=3):
    scope = {}
    exec(code, scope)
    solve = scope["solve"]
    slowest, total, passed = 0.0, 0.0, True
    for test in tests:
        expected = json.dumps(test["expected"], sort_keys=True)
        worst = 0.0
        for _ in range(repeat):
            args = json.loads(json.dumps(test["args"]))
            start = time.perf_counter()
            got = solve(*args)
            elapsed = (time.perf_counter() - start) * 1000
            worst = max(worst, elapsed)
            passed = passed and json.dumps(got, sort_keys=True) == expected
        slowest = max(slowest, worst)
        total += worst
    return {"slowestTestMs": round(slowest, 2), "allTestsMs": round(total, 2), "passed": passed}

data = json.loads(calibration_input)
results = []
for problem in data["problems"]:
    entry = {"slug": problem["slug"], "tests": len(problem["tests"])}
    for kind in ("reference", "naive"):
        entry[kind] = time_solution(problem[kind], problem["tests"])
    results.append(entry)

too_slow = []
for case in data["tooSlow"]:
    scope = {}
    exec(case["code"], scope)
    for n in case["args"]:
        start = time.perf_counter()
        scope["solve"](n)
        too_slow.append({"slug": case["slug"], "n": n, "ms": round((time.perf_counter() - start) * 1000, 1)})
json.dumps({"calibration": results, "tooSlow": too_slow})
`);
  console.log(output);
} else if (mode === "spin") {
  // Uses all the CPU it gets until the parent kills it (kill latency test).
  console.log(JSON.stringify({ spinning: true }));
  py.runPython("while True:\n    pass\n");
} else if (mode === "escape") {
  // Tries what a malicious submission would try through the JavaScript bridge. Only the outcome
  // is reported (the error name, or the size of what was read), never any content.
  py.runPython("import js");
  const attempt = async (name, code) => {
    try {
      const value = await py.runPythonAsync(code);
      return [name, "ALLOWED", typeof value === "string" ? value.length : String(value).slice(0, 20)];
    } catch (e) {
      const text = String(e && e.message);
      const match = text.match(/(NotCapable|PermissionDenied|NotFound|TypeError|ReferenceError)/);
      return [name, "denied", match ? match[1] : text.split("\n").slice(-2).join(" ").slice(0, 80)];
    }
  };
  const results = [];
  for (const [name, code] of [
    ["readParentEnviron", "js.Deno.readTextFileSync('/proc/' + str(js.Deno.ppid) + '/environ')"],
    ["readOwnEnviron", "js.Deno.readTextFileSync('/proc/self/environ')"],
    ["readEtcPasswd", "js.Deno.readTextFileSync('/etc/passwd')"],
    ["envGet", "js.Deno.env.get('PATH')"],
    ["writeFile", "js.Deno.writeTextFileSync('/tmp/x', 'x')"],
    ["runCommand", "js.Deno.Command.new('sh').outputSync()"],
    ["networkFetch", "(await js.fetch('https://example.com')).status"],
  ]) {
    results.push(await attempt(name, code));
  }
  console.log(JSON.stringify({ escape: results }));
} else {
  const s = performance.now();
  py.runPython("def solve(a, b):\n    return a + b\n");
  const solve = py.globals.get("solve");
  const values = [[1, 2], [3, 4], [10, -5]].map(([a, b]) => solve(a, b));
  console.log(JSON.stringify({ values, testsMs: Math.round((performance.now() - s) * 10) / 10 }));
}
