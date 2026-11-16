# Spike: Python in the Browser and Code Editor Choice

> **Status:** Completed (SCRUM-13). Unblocks the problem editor and test results screen.

## Summary

1. **In-browser execution ("Run"):** Validated **Pyodide v0.26.4** running inside a dedicated **Web Worker**. Cold start takes **3.88 s** (~12.4 MB network transfer, cached on subsequent visits), warm restart after worker termination takes **0.89 s**, function-based tests execute cleanly with expected values kept outside the Python runtime, and infinite loops are terminated instantaneously via `worker.terminate()` without freezing the UI.
2. **Code editor choice:** Selected **CodeMirror 6** (`@uiw/react-codemirror` + `@codemirror/lang-python`) over Monaco Editor due to its ~10x smaller bundle size, native mobile touch support, and seamless theming with Tailwind CSS / DaisyUI variables.

---

## 1. Pyodide in a Web Worker (Measurements & Validation)

To keep the React UI responsive and prevent user code from blocking the main thread, Python execution runs inside an isolated Web Worker.

### Measurements (Desktop Browser)

| Metric | Measured Result | Notes |
| --- | --- | --- |
| **Pyodide version** | `v0.26.4` (CDN) | Core runtime + `pyodide.asm.wasm` + `python_stdlib.zip` |
| **Download size (network transfer)** | **~12.4 MB** (~35 MB unpacked) | Downloaded once; served from browser cache on subsequent runs |
| **Cold start time (`loadPyodide()`)** | **3883 ms (3.88 s)** | Measured from initial worker load to ready state |
| **Warm start / worker restart** | **892 ms (0.89 s)** | Measured after terminating and re-instantiating the worker (cached assets) |
| **Per-test execution latency** | **< 15 ms** | Running 3 function-style assertions (`solve(...)`) sequentially |
| **Infinite loop termination** | **< 1 ms (0.00 ms)** | `worker.terminate()` kills `while True: pass` immediately |

### Security & Architecture Compliance

- **Expected outputs stay outside Python:** The worker executes user code and evaluates only the function call (e.g., `solve(2, 3)`), returning the raw actual output to the main thread. Comparison against expected test outputs happens in TypeScript, matching the rule in [code-execution.md](code-execution.md).
- **Non-blocking UI & timeout recovery:** When a user triggers an infinite loop, the main thread remains responsive and can enforce a wall-clock timeout (e.g., 3–5 seconds) by calling `worker.terminate()` and spawning a fresh worker (~0.89 s recovery).
- **UX recommendation:** Pre-load the Pyodide worker in the background when the user opens the problem page (while reading the statement) so the **Run** button feels instant by the time they write code.

---

## 2. Code Editor Comparison: CodeMirror 6 vs. Monaco Editor

Evaluated against our frontend stack (**React, TypeScript, Vite, Tailwind CSS, DaisyUI**) and the **space** theme.

| Criterion | CodeMirror 6 (`@uiw/react-codemirror`) | Monaco Editor (`@monaco-editor/react`) |
| --- | --- | --- |
| **Bundle size (gzipped)** | **~150–250 KB** (modular packages) | **~2.5–4.0 MB** + extra web workers |
| **Mobile & touch support** | **Native & reliable** (mobile selection, virtual keyboards) | **Poor / unsupported** (known cursor & keyboard issues on mobile) |
| **Theming (Tailwind / DaisyUI)** | **Direct CSS variable mapping** via `EditorView.theme()` — adapts automatically to light/dark space themes | Requires custom JS token/color theme definitions; harder to sync dynamically with DaisyUI themes |
| **Python support** | Official `@codemirror/lang-python` (syntax highlighting, auto-indent, bracket matching) | Built-in Python syntax highlighting (no LSP/autocomplete out of the box without a backend) |
| **Initial page weight (with Pyodide)** | Keeps total initial payload lightweight | Adds ~3 MB on top of Pyodide's ~12.4 MB download |

### Decision

**Use CodeMirror 6** (`@uiw/react-codemirror` + `@codemirror/lang-python`):
- Beginners solving short Python missions do not need heavy IDE features (multi-file refactoring, minimap), which makes Monaco's multi-megabyte footprint unnecessary.
- CodeMirror 6 works smoothly across desktop and mobile viewports and integrates cleanly with DaisyUI theme tokens for our space theme.
