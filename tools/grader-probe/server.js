// Grader probe for the SCRUM-237 hosting test.
// At every start (so at every Render wake-up) it runs a self-test: it starts children exactly like
// the grader will (same Deno flags, snapshot, memory cap), one after the other: three trivial tests,
// a memory bomb, an endless loop killed after 2 s (kill latency) and escape attempts. It keeps the
// times and memory figures. Nobody can make it run anything: GET /health and GET /stats only read.
// POST /probe exists only if PROBE_SECRET is set.
const bootAt = new Date();
const env = (name, fallback) => Deno.env.get(name) ?? fallback;
const PORT = Number(env("PORT", "10000"));
const SECRET = env("PROBE_SECRET", "");
const PYODIDE = env("PROBE_PYODIDE_DIR", "/opt/pyodide");
const CHILD = env("PROBE_CHILD", "/opt/grader/child.js");
const SNAPSHOT = env("PROBE_SNAPSHOT", "/opt/grader/pyodide.snapshot");
const WORKDIR = env("PROBE_WORKDIR", "/opt/grader/empty");
const DENO = env("PROBE_DENO", "deno");

const encoder = new TextEncoder();
function secretMatches(header) {
  const given = encoder.encode(header ?? "");
  const wanted = encoder.encode("Bearer " + SECRET);
  let diff = given.length ^ wanted.length;
  for (let i = 0; i < wanted.length; i++) diff |= (given[i] ?? 0) ^ wanted[i];
  return diff === 0;
}

async function readNumber(files) {
  for (const file of files) {
    try {
      const text = (await Deno.readTextFile(file)).trim();
      if (text === "max") return null;
      return Math.round(Number(text) / 1048576);
    } catch {
      // try the next one
    }
  }
  return null;
}
const containerPeakMB = () =>
  readNumber(["/sys/fs/cgroup/memory.peak", "/sys/fs/cgroup/memory/memory.max_usage_in_bytes"]);
const containerLimitMB = () =>
  readNumber(["/sys/fs/cgroup/memory.max", "/sys/fs/cgroup/memory/memory.limit_in_bytes"]);

async function peakResidentMB(pid) {
  try {
    const status = await Deno.readTextFile(`/proc/${pid}/status`);
    const match = status.match(/VmHWM:\s+(\d+) kB/); // peak resident size so far
    return match ? Number(match[1]) / 1024 : 0;
  } catch {
    return 0;
  }
}

// Starts one child. The child is killed after 180 s in any case. With loopKillMs, it is killed that
// many milliseconds after it reports that its endless loop started (the per-test time limit).
async function runChild(mode, loopKillMs = null) {
  const start = performance.now();
  const child = new Deno.Command(DENO, {
    args: [
      "run", "--no-prompt",
      `--allow-read=${PYODIDE},${CHILD},${SNAPSHOT}`,
      "--deny-net", "--deny-write", "--deny-env", "--deny-run", "--deny-sys", "--deny-ffi", "--deny-import",
      "--v8-flags=--max-old-space-size=128,--wasm-max-mem-pages=2048",
      CHILD, PYODIDE, SNAPSHOT, mode,
    ],
    clearEnv: true,
    env: { PATH: "/usr/local/bin:/usr/bin:/bin" },
    cwd: WORKDIR,
    stdin: "null",
    stdout: "piped",
    stderr: "piped",
  }).spawn();

  let peak = 0;
  let watching = true;
  const watcher = (async () => {
    while (watching) {
      peak = Math.max(peak, await peakResidentMB(child.pid));
      await new Promise((resolve) => setTimeout(resolve, 50));
    }
  })();
  let killedAt = null;
  const kill = () => {
    if (killedAt !== null) return;
    killedAt = performance.now();
    try {
      child.kill("SIGKILL");
    } catch {
      // already gone
    }
  };
  const hardKiller = setTimeout(kill, 180000);
  let loopKiller = null;

  // Read stdout as it comes, up to 64 KB, so the loop timer can start when the loop starts.
  const stderrText = new Response(child.stderr).text();
  let stdoutText = "";
  const decoder = new TextDecoder();
  for await (const chunk of child.stdout) {
    if (stdoutText.length < 65536) stdoutText += decoder.decode(chunk, { stream: true });
    if (loopKillMs !== null && loopKiller === null && stdoutText.includes('"spinning":true')) {
      loopKiller = setTimeout(kill, loopKillMs);
    }
  }
  const status = await child.status;
  const exitedAt = performance.now();
  clearTimeout(hardKiller);
  if (loopKiller !== null) clearTimeout(loopKiller);
  watching = false;
  await watcher;

  const lines = stdoutText.slice(0, 65536).trim().split("\n").filter(Boolean)
    .map((line) => {
      try {
        return JSON.parse(line);
      } catch {
        return { raw: line.slice(0, 200) };
      }
    });
  return {
    mode,
    childTotalMs: Math.round(exitedAt - start),
    childExitCode: status.code,
    childPeakRssMB: Math.round(peak),
    killToExitMs: killedAt === null ? null : Math.round(exitedAt - killedAt),
    child: lines,
    childStderrTail: (await stderrText).slice(-300),
  };
}

// Self-test at start-up, one child after the other (as in the grader).
const selfTest = { state: "running" };
(async () => {
  try {
    selfTest.serverReadyMs = Math.round(performance.now());
    selfTest.trivial = await runChild("trivial");
    selfTest.bomb = await runChild("bomb");
    selfTest.spin = await runChild("spin", 2000);
    selfTest.escape = await runChild("escape");
    selfTest.parentRssMB = Math.round(Deno.memoryUsage().rss / 1048576);
    selfTest.containerPeakMB = await containerPeakMB();
    selfTest.containerLimitMB = await containerLimitMB();
    selfTest.finishedAfterBootMs = Math.round(performance.now());
    selfTest.state = "done";
  } catch (e) {
    selfTest.state = "failed";
    selfTest.error = String(e && e.message).slice(0, 300);
  }
})();

const publicHeaders = { "Access-Control-Allow-Origin": "*", "Cache-Control": "no-store" };
let busy = false;
Deno.serve({ hostname: "0.0.0.0", port: PORT }, async (req) => {
  const url = new URL(req.url);
  if (req.method === "GET" && url.pathname === "/health") {
    return new Response("ok", { headers: publicHeaders });
  }
  if (req.method === "GET" && url.pathname === "/stats") {
    return Response.json({
      bootAt: bootAt.toISOString(),
      uptimeMs: Math.round(performance.now()),
      deno: Deno.version.deno,
      selfTest,
    }, { headers: publicHeaders });
  }
  if (req.method === "POST" && url.pathname === "/probe" && SECRET.length >= 16) {
    if (!secretMatches(req.headers.get("authorization"))) {
      return new Response("unauthorized", { status: 401 });
    }
    if (busy || selfTest.state === "running") {
      return new Response("busy", { status: 503, headers: { "Retry-After": "30" } });
    }
    busy = true;
    try {
      const result = await runChild(url.searchParams.get("mode") === "bomb" ? "bomb" : "trivial");
      return Response.json({
        bootAt: bootAt.toISOString(),
        uptimeMs: Math.round(performance.now()),
        parentRssMB: Math.round(Deno.memoryUsage().rss / 1048576),
        containerPeakMB: await containerPeakMB(),
        ...result,
      });
    } finally {
      busy = false;
    }
  }
  return new Response("not found", { status: 404 });
});
