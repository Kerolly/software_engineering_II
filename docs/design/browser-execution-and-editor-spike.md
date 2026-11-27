# Spike: Python in the Browser and Code Editor Choice

> **Status:** Completed (SCRUM-13). Unblocks the problem editor and test results screen.

## Summary

1. **In-browser execution ("Run"):** Validated **Pyodide** running inside a dedicated **Web Worker**. Cold start takes **3.88 s** (~12.4 MB network transfer, cached on subsequent visits), warm restart after worker termination takes **0.89 s**, function-based tests execute cleanly with expected values kept outside the Python runtime, and infinite loops are terminated instantaneously via `worker.terminate()` without freezing the UI.
2. **Pinned version & self-hosting:** Standardized on **Pyodide `v0.26.4` (Python 3.12)**, **self-hosted** as static assets alongside the frontend bundle so the browser runtime matches the server grader's vendored Pyodide build (`code-execution.md`, `submission-verification.md`) and avoids third-party CDN script execution.
3. **Code editor choice:** Selected **CodeMirror 6** (`@uiw/react-codemirror` + `@codemirror/lang-python`) over Monaco Editor due to its ~10x smaller bundle size, native mobile touch support, and seamless theming with Tailwind CSS / DaisyUI variables.

---

## 1. Pyodide in a Web Worker (Measurements & Validation)

To keep the React UI responsive and prevent user code from blocking the main thread, Python execution runs inside an isolated Web Worker.

### Measurement Environment

- **Device & CPU:** Lenovo Windows 11 laptop, 24-thread x86_64 processor (multi-core desktop-class CPU)
- **Browser:** Chromium-based desktop browser (Google Chrome / Microsoft Edge)
- **Network:** Local broadband Wi-Fi connection (initial cold-start measurement taken with browser cache disabled; warm-start measurement taken from browser disk cache)
- **Measured Prototype Build:** Pyodide `v0.26.4` (Python 3.12 runtime)

### Measurements (Desktop Browser)

| Metric | Measured Result | Notes |
| --- | --- | --- |
| **Pyodide version** | `v0.26.4` (Python 3.12) | Core runtime + `pyodide.asm.wasm` + `python_stdlib.zip` |
| **Download size (network transfer)** | **~12.4 MB** (~35 MB unpacked) | Downloaded once; served from browser cache on subsequent runs |
| **Cold start time (`loadPyodide()`)** | **3883 ms (3.88 s)** | Measured from initial worker load to ready state |
| **Warm start / worker restart** | **892 ms (0.89 s)** | Measured after terminating and re-instantiating the worker (cached assets) |
| **Per-test execution latency** | **< 15 ms** | Running 3 function-style assertions (`solve(...)`) sequentially |
| **Infinite loop termination** | **< 1 ms (0.00 ms)** | `worker.terminate()` kills `while True: pass` immediately |

### Pinned Pyodide Version & Self-Hosting Policy

- **Chosen Pinned Version:** **Pyodide `v0.26.4` (CPython `3.12`)**, locked identically across both the frontend **"Run"** worker and the backend grading service (`code-execution.md`, `submission-verification.md`).
- **Why this version:**
  - Keeps Python language features, standard library behavior, and error tracebacks 100% consistent between local **Run** feedback and authoritative **Submit** grading, and aligns with the `Python 3.12` environment referenced in `style-guide.md`.
  - `v0.26.4` is a stable, mature release whose core archive (`pyodide.js`, `pyodide.asm.wasm`, `pyodide.asm.js`, `python_stdlib.zip`) is verified and lightweight (~12.4 MB) without unnecessary data-science packages.
- **Self-Hosting (No Public CDN):**
  - Instead of loading Pyodide from `cdn.jsdelivr.net` in production, the pinned Pyodide runtime files are vendored/copied into the frontend's static assets (e.g., `/pyodide/v0.26.4/`) and served from our own origin—matching the grader's vendored Pyodide distribution.
  - Self-hosting eliminates third-party CDN supply-chain risk, prevents external availability outages, and keeps the Content Security Policy (`CSP`) strict (`script-src 'self' 'wasm-unsafe-eval'`).

### Security Analysis & Mitigations for In-Browser "Run"

#### Threat Model
Inside a Web Worker, Pyodide's `js` bridge exposes the worker's global scope (`self`), meaning untrusted student code can invoke `fetch()` and open `IndexedDB` for the worker's origin. By default, the Firebase Web SDK stores the authenticated user's session and refresh token in `IndexedDB` (`indexedDBLocalPersistence`). If the worker runs on the main application origin with default Firebase settings, malicious or copy-pasted Python code in **Run** could read the user's Firebase refresh token from `IndexedDB` and exfiltrate it via `fetch()`.

#### Architecture Comparison

| Approach | How It Works | Pros | Cons |
| --- | --- | --- | --- |
| **Option A: Same-Origin Worker + Auth Storage Isolation (MVP Recommended)** | Initialize Firebase Auth with `browserLocalPersistence` (`localStorage`, which Web Workers cannot access) instead of `IndexedDB`; never pass ID tokens to the worker; treat all `postMessage` payloads from the worker as untrusted strings and render outputs strictly as plain text (`textContent` / React text nodes). | Simple Vite/React integration; zero cross-origin iframe messaging boilerplate; fast worker restart (`0.89 s`); eliminates session token theft from `IndexedDB`. | Worker still shares the app's network origin (`fetch` to same-origin public endpoints or external URLs is still possible unless restricted by CSP). |
| **Option B: Sandboxed Cross-Origin `<iframe>` + Worker (`CSP connect-src 'none'`)** | Host the Pyodide worker inside an `<iframe sandbox="allow-scripts">` served from a separate subdomain/null origin with a strict response header `Content-Security-Policy: default-src 'none'; script-src 'self' 'wasm-unsafe-eval'; worker-src 'self'; connect-src 'none'`. | Strongest defense-in-depth: completely isolates `IndexedDB`, `localStorage`, cookies, and DOM from the main app origin, and `connect-src 'none'` blocks all outbound `fetch`/`XHR`/WebSocket exfiltration. | Requires an extra static origin/subdomain in deployment (`deployment.md`), double-hop `postMessage` bridging (`App <-> iframe <-> Worker`), and slightly more complex caching/CORS setup. |

#### Recommendation
- **Adopt Option A immediately for MVP** so the editor task is unblocked cleanly without extra infrastructure:
  1. Configure Firebase Auth explicitly with `setPersistence(auth, browserLocalPersistence)` so session tokens live exclusively in `localStorage` (inaccessible to Web Workers) and clear/block any default Firebase `IndexedDB` entries.
  2. Never send Firebase ID tokens, user emails, or private session data into the worker (`postMessage` carries only source code, function signature, and public sample inputs).
  3. Treat every message received from the worker as untrusted data and render all stdout/stderr/return values strictly as escaped plain text (never `dangerouslySetInnerHTML`).
- **Upgrade path to Option B (Post-MVP hardening):** Once deployment routing supports a separate static origin (or sandboxed iframe endpoint), wrap the worker in a cross-origin `<iframe sandbox="allow-scripts">` with `CSP connect-src 'none'` to block arbitrary outbound network requests as well.

### Additional Execution & UX Notes

- **Expected outputs & comparison parity:** The worker executes user code and evaluates only the function call (e.g., `solve(2, 3)`), returning raw actual values to TypeScript. Comparison in TypeScript must follow the exact grader rules in [problem-template.md](problem-template.md) and [code-execution.md](code-execution.md) (strict boolean vs. integer distinction where `True != 1`, order-independent dict comparison, integer-only numeric rules), validated against a shared set of edge-case test vectors.
- **Non-blocking UI, timeouts & OOM recovery:** Main-thread wall-clock timeouts (configured via admin settings, e.g., 3–5 s) terminate infinite loops via `worker.terminate()` and spawn a fresh worker (`~0.89 s`). Similarly, if user code triggers an out-of-memory crash (e.g., unbounded list allocation), the frontend catches the worker's `error` event, displays a clear *"Memory limit exceeded in browser runner"* message, and re-initializes the worker.
- **Run vs. Submit timing notice:** Because **Run** executes on the user's local device while **Submit** runs on the server grader under constrained CPU limits (`deployment.md`), the UI should note that passing **Run** does not guarantee passing **Submit** time limits.
- **UX pre-loading:** Pre-load the self-hosted Pyodide worker in the background when the user opens a mission page so **Run** responds immediately.

---

## 2. Code Editor Comparison: CodeMirror 6 vs. Monaco Editor

Evaluated against our frontend stack (**React, TypeScript, Vite, Tailwind CSS, DaisyUI**) and the **space** theme (`se2-dark` / `se2-light`).

| Criterion | CodeMirror 6 (`@uiw/react-codemirror`) | Monaco Editor (`@monaco-editor/react`) |
| --- | --- | --- |
| **Bundle size (gzipped)** | **~150–250 KB** (modular packages) | **~2.5–4.0 MB** + extra web workers |
| **Mobile & touch support** | **Native & reliable** (mobile selection, virtual keyboards) | **Poor / unsupported** (known cursor & keyboard issues on mobile) |
| **Theming (Tailwind / DaisyUI)** | **Direct CSS variable mapping** via `EditorView.theme()` — adapts automatically to light/dark space themes | Requires custom JS token/color theme definitions; harder to sync dynamically with DaisyUI themes |
| **Python support** | Official `@codemirror/lang-python` (syntax highlighting, auto-indent, bracket matching) | Built-in Python syntax highlighting (no LSP/autocomplete out of the box without a backend) |
| **Initial page weight (with Pyodide)** | Keeps total initial payload lightweight | Adds ~3 MB on top of Pyodide's ~12.4 MB self-hosted assets |

### Decision

**Use CodeMirror 6** (`@uiw/react-codemirror` + `@codemirror/lang-python`):
- Beginners solving short Python missions do not need heavy IDE features (multi-file refactoring, minimap), which makes Monaco's multi-megabyte footprint unnecessary.
- CodeMirror 6 works smoothly across desktop and mobile viewports and integrates cleanly with DaisyUI theme tokens for our space theme.