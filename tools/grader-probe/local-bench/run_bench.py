"""Run the Pyodide child benchmark several times and report wall time, CPU time and peak RSS.

Usage: python3 -I run_bench.py <deno binary> <child.ts> <runs> <cpu quota or 0> <pyodide dir> [<pyodide dir> ...]

A CPU quota of 0.1 puts each run in a cgroup v1 CPU group limited to 10% of one CPU,
which is roughly what a Render free instance gives.
"""
import json
import os
import resource
import subprocess
import sys
import time

deno, child, runs, quota = sys.argv[1], sys.argv[2], int(sys.argv[3]), float(sys.argv[4])
dirs = sys.argv[5:]
CG = "/sys/fs/cgroup/cpu/grader-bench"

if quota > 0:
    os.makedirs(CG, exist_ok=True)
    with open(f"{CG}/cpu.cfs_period_us", "w") as f:
        f.write("100000")
    with open(f"{CG}/cpu.cfs_quota_us", "w") as f:
        f.write(str(int(100000 * quota)))


def enter_cgroup():
    if quota > 0:
        with open(f"{CG}/cgroup.procs", "w") as f:
            f.write(str(os.getpid()))


for d in dirs:
    for i in range(runs):
        extra_v8 = os.environ.get("BENCH_V8_FLAGS", "")
        extra_deno = os.environ.get("BENCH_DENO_FLAGS", "").split()
        extra_args = os.environ.get("BENCH_CHILD_ARGS", "").split()
        extra_read = "".join("," + a for a in extra_args)
        cmd = [
            deno, "run", "--no-prompt", *extra_deno,
            f"--allow-read={d},{child}{extra_read}",
            "--deny-net", "--deny-write", "--deny-env", "--deny-run", "--deny-sys", "--deny-ffi", "--deny-import",
            "--v8-flags=--max-old-space-size=128" + ("," + extra_v8 if extra_v8 else ""),
            child, d, *extra_args,
        ]
        with open("/tmp/grader-bench-stderr.txt", "w+") as err:
            start = time.monotonic()
            proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=err, text=True, preexec_fn=enter_cgroup)
            stdout = proc.stdout.read()
            _, status, usage = os.wait4(proc.pid, 0)  # rusage of this run only
            wall = time.monotonic() - start
            err.seek(0)
            stderr = err.read()
        out = stdout.strip().splitlines()[-1] if stdout.strip() else ""
        try:
            data = json.loads(out)
        except ValueError:
            data = {"error": (stderr or stdout)[-400:]}
        data.update({
            "run": i + 1,
            "cpuQuota": quota or "none",
            "wallS": round(wall, 2),
            "cpuS": round(usage.ru_utime + usage.ru_stime, 2),
            "peakRssMB": round(usage.ru_maxrss / 1024),
            "exit": os.waitstatus_to_exitcode(status),
        })
        print(json.dumps(data), flush=True)
