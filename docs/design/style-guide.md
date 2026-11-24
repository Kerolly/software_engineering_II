# SE2 (OrbitPy Proposal) Design System & Style Guide

> **Status:** Proposed (SCRUM-10). Single source of truth for the DaisyUI theme and UI screens.  
> **Product Name:** `SE2` placeholder in code (proposed brand name: **OrbitPy**, pending SCRUM-14 decision; team TTVZ)  
> **UI Language:** English  
> **Layout Priority:** Desktop-first (optimized for split-pane code editing, responsive down to tablet/mobile)  
> **Default Theme:** `se2-dark` (with `se2-light` toggle supported)

---

## 1. Brand Identity & Space Vocabulary

OrbitPy (proposed brand identity for SE2, SCRUM-14) turns beginner Python practice into space missions. Every domain concept maps consistently across the UI:

| Domain Concept | Space Term | Icon / Visual Cue |
| --- | --- | --- |
| **Platform** | **OrbitPy** (SE2 proposal) | Stylized orbital ring around a Python-inspired dual-node planet |
| **Problem** | **Mission** | Rocket / waypoint marker (`Easy`, `Medium`, `Hard`) |
| **Topic** | **Sector** | Constellation / sector badge (e.g., *Sector 01: Variables & Conditions*) |
| **XP** | **Stardust** | Four-point sparkling star (`#FBBF24`) |
| **Daily Streak** | **Days in Orbit** | Orbital flame / satellite loop (`#FB923C`) |
| **Streak Freeze** | **Cryo Shield** | Cyan ice-crystal shield (`#38BDF8`), unlocked at the configured shield unlock level |

---

## 2. Color Palette & DaisyUI Theme Tokens

Designed around a deep-space navy canvas with high-contrast text (**WCAG AA compliant**, ≥ 4.5:1 for normal text across both themes) and crisp semantic status colors for all submission and per-test verdicts.

### DaisyUI Configuration (`se2-dark` & `se2-light`)

| Token | `se2-dark` (Default) | `se2-light` | Usage |
| --- | --- | --- | --- |
| `base-100` | `#0B0F19` (Deep Space) | `#F8FAFC` (Starlight White) | Main page background |
| `base-200` | `#131A2B` (Orbital Hull) | `#F1F5F9` (Lunar Mist) | Cards, problem statement panel, sidebar |
| `base-300` | `#1E293B` (Slate Panel) | `#E2E8F0` (Border Slate) | Borders, dividers, table headers, input borders |
| `base-content` | `#F1F5F9` (Crisp Starlight) | `#0F172A` (Midnight Ink) | Primary body text and headings |
| `neutral` | `#1E293B` | `#334155` | Secondary buttons, badges, `INTERNAL_ERROR` / `NOT_EXECUTED` containers |
| `neutral-content` | `#94A3B8` | `#F8FAFC` | Muted labels, timestamps, metadata, unexecuted test rows |
| `primary` | `#38BDF8` (Nebula Cyan) | `#0369A1` (Deep Ocean Blue) | Primary CTAs (**Submit**, **Start Mission**, active nav) |
| `primary-content` | `#082F49` | `#FFFFFF` | Text on primary buttons |
| `secondary` | `#818CF8` (Cosmic Indigo) | `#4F46E5` (Royal Indigo) | Ranks, level progress bars, sector highlights |
| `secondary-content` | `#0F172A` | `#FFFFFF` | Text on secondary elements |
| `accent` | `#FBBF24` (Stardust Gold) | `#B45309` (Deep Amber) | XP / Stardust counters, unlocked badges, streak highlights |
| `accent-content` | `#1C1917` | `#FFFFFF` | Text on accent badges |
| `info` | `#38BDF8` | `#0369A1` | Informational banners, Cryo Shield (Streak Freeze) |
| `success` | `#34D399` (Telemetry Green) | `#047857` (Deep Emerald) | `ACCEPTED` verdict, `PASS` test rows, solved checkmarks |
| `warning` | `#FBBF24` (Solar Flare) | `#B45309` | Hint XP penalty warnings, `PENDING` / `RUNNING` states |
| `error` | `#F87171` (Supernova Coral) | `#DC2626` (Crimson) | `WRONG_ANSWER`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `COMPILE_ERROR`, `FAIL` rows |

### Verdict & Per-Test Status Mapping (`api-conventions.md`)

| Status | Theme Token / Color | Visual Treatment |
| --- | --- | --- |
| `ACCEPTED` / `PASS` | `success` (`#34D399` dark / `#047857` light) | Solid/tinted badge + check icon |
| `PENDING` / `RUNNING` | `warning` (`#FBBF24` dark / `#B45309` light) | Subtle pulse indicator while grading |
| `WRONG_ANSWER` / `FAIL` | `error` (`#F87171` dark / `#DC2626` light) | Diff view (expected vs. actual output) |
| `TIME_LIMIT_EXCEEDED` | `error` (`#F87171` dark / `#DC2626` light) | Clock badge + timeout message |
| `RUNTIME_ERROR` | `error` (`#F87171` dark / `#DC2626` light) | Traceback panel with `error` border |
| `COMPILE_ERROR` | `error` (`#F87171` dark / `#DC2626` light) | Syntax/compiler diagnostic banner before test execution |
| `INTERNAL_ERROR` | `neutral` / `info` (`#94A3B8` dark / `#334155` light) | Neutral *"System error — please try again"* banner with retry action (non-penalizing) |
| `NOT_EXECUTED` (per-test) | `base-300` + `neutral-content` (`#94A3B8` dark / `#475569` light) | Greyed-out test row when execution halts early (e.g., on `COMPILE_ERROR`) |

---

## 3. Typography & Spacing Scale

### Font Families (Self-Hosted)
- **Self-Hosting Policy:** Fonts are bundled locally via npm (`@fontsource/inter` and `@fontsource/jetbrains-mono`) rather than loaded from external CDNs (such as Google Fonts) to avoid third-party IP leaks and keep the Content Security Policy (`CSP`) strict.
- **UI & Headings (`font-sans`):** `Inter`, system-ui, -apple-system, sans-serif
- **Code Editor, Test Inputs/Outputs & Badges (`font-mono`):** `JetBrains Mono`, `Fira Code`, monospace

### Type Scale
- **Page Title (`h1`):** `1.75rem` (28px), `font-bold`, `tracking-tight`
- **Section / Sector Title (`h2`):** `1.25rem` (20px), `font-semibold`
- **Card / Problem Title (`h3`):** `1.0625rem` (17px), `font-semibold`
- **Body / Problem Statement:** `0.9375rem` (15px), `leading-relaxed` (`1.65`)
- **Code Editor & Test Tables:** `0.875rem` (14px), `font-mono`, `leading-normal` (`1.5`)
- **Micro Labels / Metadata:** `0.75rem` (12px), `font-medium`, `uppercase tracking-wider`

### Spacing & Radii
- **Base Grid Unit:** `4px` (`0.25rem`). Standard card padding: `20px` (`p-5`), section gap: `24px` (`gap-6`), split-pane gap: `12px` (`gap-3`).
- **Border Radius:** Cards `12px` (`rounded-xl`), Buttons & Inputs `8px` (`rounded-lg`), Status Pills/Badges `9999px` (`rounded-full`).

---

## 4. Core UI Components

### Header & Footer
- **Top Navbar (`h-16`, `bg-base-200/80 backdrop-blur border-b border-base-300`):**
  - **Left:** Platform logo + primary nav links (`Missions`, `Profile`, and `Admin` visible only for `ADMIN` role).
  - **Right:** Compact telemetry pills: **Days in Orbit** (`🔥 5d` + Cryo Shield icon when active), **Stardust** (`✦ 120 XP`), **Rank Badge** (`Navigator · Lvl 3`), theme toggle, and user avatar dropdown (`Profile`, `Sign out`).
- **Footer (`border-t border-base-300 bg-base-200/40 text-xs text-neutral-content`):**
  - Left: `SE2 · Python Mission Control` · Right: `Team TTVZ · Software Engineering II` + quick status indicator.

### Buttons
- **Primary (`btn btn-primary`):** Solid Nebula Cyan (`#38BDF8` in `se2-dark`, `#0369A1` in `se2-light`) — used for **Submit** and main CTAs.
- **Secondary / Run (`btn btn-outline border-base-300 hover:border-primary`):** Used for **Run** (local Pyodide check) so it is clearly distinct from **Submit** (scored server grading).
- **Ghost (`btn btn-ghost btn-sm`):** Reset starter code, reveal hint, table actions.
- **Danger (`btn btn-error btn-outline`):** Block user, archive mission.

### Cards & Forms
- **Mission Card (`bg-base-200 border border-base-300 rounded-xl p-4 hover:border-primary/50 transition`):**
  - Displays mission order number (`01`), title, difficulty pill (`Easy` green, `Medium` amber, `Hard` red), Stardust reward (`+XP` from mission config), and status icon (`Solved`, `In Progress`, `Not Started`, or `Locked`).
- **Locked Mission State:** Card stays visible at `opacity-65` with a lock icon and explicit unlock requirement badge (derived from sector unlock rules).
- **Form Controls (`input input-bordered bg-base-100 border-base-300 focus:border-primary`):** Consistent 40px height, clear inline validation error messages in `text-error text-xs`.

### Code Editor Panel (CodeMirror 6)
- **Container:** Full-height split-pane card (`bg-base-100 border border-base-300 rounded-xl overflow-hidden`).
- **Editor Top Toolbar (`h-11 bg-base-200 px-4 border-b border-base-300`):** Language pill (`Python 3.12 · Pyodide Ready`), font size controls, and `Reset Code` button.
- **Editor Surface:** CodeMirror 6 mapped to `base-100` (`#0B0F19`), subtle active line highlight (`#131A2B`), high-contrast line numbers (`#94A3B8` in `se2-dark` for ≥ 7:1 contrast; `#475569` in `se2-light`), matching bracket highlight in `#38BDF8`.
- **Bottom Action Bar:** Left shows runtime status (`Browser ready` / `Grading on server...`); Right holds **Run** (local test) and **Submit** (scored).

### Star-Map Activity Grid (Profile)
- **Layout:** 26-week (6-month) contribution grid (`7 rows × 26 columns`, `12px × 12px` rounded-sm cells with `3px` gap), styled like a deep-space star chart.
- **Intensity Scale (Accepted Submissions per Day):**
  - `0 missions`: `#131A2B` (`border border-base-300/60`) — empty space
  - `1 mission`: `#0C4A6E` — dim star
  - `2 missions`: `#0369A1` — bright star
  - `3+ missions`: `#38BDF8` with subtle outer glow (`0 0 6px rgba(56,189,248,0.5)`) — pulsar
- **Tooltip on Hover:** `Nov 16, 2026 · 3 missions completed (+40 Stardust)`.

---

## 5. Gamification Visual System

### Ranks & Dynamic Settings
- **Configuration Source:** All numeric gamification parameters—including level XP thresholds, hint XP penalty percentages, and streak freeze unlock levels—are administrator-configurable settings (see `gamification.md` and `admin-panel.md`). The frontend reads these values dynamically from the settings/profile API rather than hardcoding them in UI components.
- **Visual Rank Progression (Emblem Accents):**

| Level | Rank Title | Emblem Accent | Visual Cue |
| --- | --- | --- | --- |
| **1** | Cadet | Slate (`#94A3B8`) | Single-chevron cadet insignia |
| **2** | Pilot | Sky (`#38BDF8`) | Dual-wing orbital chevron |
| **3** | Navigator | Teal (`#2DD4BF`) | Compass star + **Cryo Shield** status indicator (when unlocked per admin config) |
| **4** | Engineer | Indigo (`#818CF8`) | Hexagonal reactor emblem |
| **5** | Captain | Violet (`#C084FC`) | Command ring crest |
| **6** | Commander | Gold (`#FBBF24`) | Full supernova laurel emblem |

### Badges (MVP Set of 6)
All badge criteria and labels follow `gamification.md`:
1. **First Launch** — *Complete your first accepted mission* (`SOLVED_COUNT >= 1`)
2. **Cadet Training** — *Solve 10 Easy missions* (`SOLVED_EASY >= 10`)
3. **In Orbit** — *Maintain a 7-day streak* (`STREAK >= 7`)
4. **Sharpshooter** — *Solve 5 missions on the first attempt without hints* (`FIRST_TRY_SOLVED >= 5`)
5. **Beyond the Belt** — *Solve your first Medium mission* (`SOLVED_MEDIUM >= 1`)
6. **Mission Complete** — *Complete every mission in a sector* (`TOPIC_COMPLETED >= 1`)

*Visual states:* **Unlocked** badges render in full color with a `border-accent/60` glow and unlock date; **Locked** badges render in monochrome silhouette (`opacity-45`) with progress bar (e.g., `7 / 10 Easy`).

---

## 6. UI Screens Reference

All screens follow the exact DaisyUI tokens (`se2-dark` / `se2-light`) and top navigation bar above:
- **Interactive UI Prototype (Claude Artifact):** https://claude.ai/artifact/9gGLVx27aVKmYdx4gXXScB
- **Covered MVP Views:**
  1. **Landing & Auth:** Hero overview, Login/Register, and Forced Password Change state
  2. **Missions List:** Missions grouped by Sector (Topics) with Solved & Locked states
  3. **Problem Workspace:** Split-pane statement, CodeMirror 6 editor, Run/Submit, per-test results (`PASS`, `FAIL`, `COMPILE_ERROR`, `INTERNAL_ERROR`, `NOT_EXECUTED`) & Hints
  4. **Profile & Star-Map:** Dynamic rank progress, Days in Orbit streak, Cryo Shield, Star-Map grid & 6 Badges
  5. **Admin Panel:** Mission Editor (Preview/Dry Run), Users (Block/Unblock), Dynamic Gamification Settings & Audit Log