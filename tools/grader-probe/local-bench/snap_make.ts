// Build step (trusted, no user code): start Pyodide once and save a memory snapshot,
// so each grader child can restore it instead of initializing Python from scratch.
const [dir, out] = Deno.args;
const { loadPyodide } = await import("file://" + dir + "/pyodide.mjs");
const py = await loadPyodide({ indexURL: dir + "/", _makeSnapshot: true });
py.runPython("import json, sys, traceback");
const snap: Uint8Array = py.makeMemorySnapshot();
await Deno.writeFile(out, snap);
console.log(JSON.stringify({ snapshotMB: Math.round(snap.length / 1048576 * 10) / 10 }));
