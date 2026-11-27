// Benchmark child: load Pyodide from a local folder, run three trivial function tests,
// print the timings as one JSON line. Run with the grader child's Deno flags.
const dir = Deno.args[0];
const t0 = performance.now();
const { loadPyodide } = await import("file://" + dir + "/pyodide.mjs");
const t1 = performance.now();
const py = await loadPyodide({ indexURL: dir + "/" });
const t2 = performance.now();
py.runPython("def solve(a, b):\n    return a + b\n");
const t3 = performance.now();
const solve = py.globals.get("solve");
const values: number[] = [];
for (const [a, b] of [[1, 2], [3, 4], [10, -5]]) {
  values.push(solve(a, b));
}
const t4 = performance.now();
const mem = Deno.memoryUsage();
console.log(JSON.stringify({
  pyodide: py.version,
  python: py.runPython("import sys; sys.version.split()[0]"),
  importMs: Math.round(t1 - t0),
  loadPyodideMs: Math.round(t2 - t1),
  defineMs: Math.round(t3 - t2),
  threeTestsMs: Math.round((t4 - t3) * 10) / 10,
  values,
  rssMB: Math.round(mem.rss / 1048576),
}));
