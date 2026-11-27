// Build step (trusted, no user code): start Pyodide once and save a memory snapshot.
const [dir, out] = Deno.args;
const { loadPyodide } = await import("file://" + dir + "/pyodide.mjs");
const py = await loadPyodide({ indexURL: dir + "/", _makeSnapshot: true });
py.runPython("import json, sys, traceback");
const snapshot = py.makeMemorySnapshot();
await Deno.writeFile(out, snapshot);
console.log(`snapshot written: ${out}, ${Math.round(snapshot.length / 1048576)} MB`);
