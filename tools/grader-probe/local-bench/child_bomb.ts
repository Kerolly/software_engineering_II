// Memory-limit probe: restore the snapshot, run a memory bomb, then check that the
// interpreter still answers. Run it with and without --wasm-max-mem-pages.
const [dir, snapPath] = Deno.args;
const { loadPyodide } = await import("file://" + dir + "/pyodide.mjs");
const py = await loadPyodide({ indexURL: dir + "/", _loadSnapshot: await Deno.readFile(snapPath) });
const rssMB = () => Math.round(Deno.memoryUsage().rss / 1048576);
const before = rssMB();
const t0 = performance.now();
let bomb: string;
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
  bomb = "JS error: " + String((e as Error).message).split("\n").slice(-2).join(" ").slice(0, 160);
}
const t1 = performance.now();
let after: string;
try {
  after = String(py.runPython("sum(range(10))"));
} catch (e) {
  after = "interpreter unusable: " + String((e as Error).message).slice(0, 120);
}
console.log(JSON.stringify({ rssBeforeMB: before, bomb, bombMs: Math.round(t1 - t0), nextRun: after, rssAfterMB: rssMB() }));
