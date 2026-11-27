# Local benchmark (SCRUM-237)

The scripts behind the *local* numbers in [grader.md, section 4](../../../docs/design/grader.md#4-inside-the-grader). They run a child with the grader's Deno flags, on Linux, with the CPU limited to 10 ms per 100 ms by a cgroup v1 quota (what a Render free instance gives).

| File | What it measures |
| --- | --- |
| `child.ts` | A child that starts Python from scratch and runs three trivial tests |
| `snap_make.ts` | Writes the memory snapshot (the build step) |
| `child_snap.ts` | A child restored from the snapshot, then three trivial tests |
| `child_bomb.ts` | A memory bomb in a child restored from the snapshot, then a check that the interpreter still works |
| `run_bench.py` | Runs a child several times and prints wall time, CPU time and peak memory, one JSON line per run |

With Deno 2.9.6 and the `pyodide@314.0.8` npm package unpacked in `./pyodide`, as root (the cgroup needs it):

```bash
deno run --no-prompt --allow-read=./pyodide --allow-write=./snap.bin snap_make.ts "$PWD/pyodide" "$PWD/snap.bin"
python3 run_bench.py "$(command -v deno)" "$PWD/child.ts" 3 0.1 "$PWD/pyodide"
BENCH_CHILD_ARGS="$PWD/snap.bin" python3 run_bench.py "$(command -v deno)" "$PWD/child_snap.ts" 3 0.1 "$PWD/pyodide"
BENCH_CHILD_ARGS="$PWD/snap.bin" BENCH_V8_FLAGS=--wasm-max-mem-pages=2048 \
  python3 run_bench.py "$(command -v deno)" "$PWD/child_bomb.ts" 1 0.1 "$PWD/pyodide"
```

Use `0` instead of `0.1` for no CPU limit. On a machine with cgroup v2, `docker run --cpus=0.1 --memory=512m` on the probe image gives the same kind of limit; that is how the probe's self-test was checked before it went to Render.
