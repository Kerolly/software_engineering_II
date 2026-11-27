// Probe child: restore Pyodide from the snapshot, then run one fixed scenario: three trivial
// tests, a memory bomb, an endless loop, or escape attempts. It never runs code from a request.
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
