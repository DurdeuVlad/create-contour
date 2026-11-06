# Issue 11: Discoverability - JEI, Ponder scenes, tooltips

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M3 (not blocked by the 09 gate; may run after 07, before/parallel to 10) |
| Type | feature |
| Suggested labels | `jei`, `ponder`, `ux` |
| Priority | proposed-medium |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Make the addon's recipes and workflows discoverable in-game without adding
a hard dependency: JEI stays optional, Ponder scenes teach the two core
gestures, and tooltips stay concise.

## Expectation

- All addon recipes visible and correct in JEI (pack version 19.57.0.449
  and the optional profile); the game fully works without JEI.
- Two original Ponder scenes: (a) placing a shape and painting material,
  (b) cycling shapes with the wrench.
- Concise per-shape wrench tooltips on all shipped shapes.

## Acceptance criteria

1. With JEI: every `create_contour` recipe appears under its own
   namespace/category correctly; screenshot evidence.
2. Without JEI: game boots and all workflows function; no hard
   dependency sneaks into `neoforge.mods.toml` or code paths.
3. Two original Ponder scenes exist (placing+material, wrench cycling),
   registered under our addon; they run without errors and cover the
   first-three family. Scene scripts are original content. Coverage of
   shapes 4-10 waits for those shapes' own gate - this issue ships for
   the first three.
4. Per-shape tooltip lines are concise (one or two lines) and present on
   every shipped shape; the initial three shapes' tooltips were already
   required in issues 04-06 - this issue verifies and extends, never
   postpones them.
5. JEI visibility verified inside the pack (issue 13 harness) so KubeJS
   recipe rewrites do not hide or break our entries.
6. No Create assets reused in scenes; original assets only.

## Context the code cannot infer

- JEI is a soft dependency everywhere: compile-time optional, runtime
  optional, pack version 19.57.0.449.
- Ponder arrives as a required dependency THROUGH Create (not an optional
  mod dependency of ours); our scenes are an optional feature built on it.
  Scene content is ours.

## Scope

### In scope

- JEI plugin, two Ponder scenes, tooltip pass across shipped shapes.

### Non-goals

- Documentation website, guides, or non-JEI recipe viewers.
- New shapes (issue 10 covers them; this issue extends to them where
  timing allows - explicitly note coverage at handoff).

## Dependencies and open decisions

- Depends on: 07 (workflows to demonstrate).
- Feeds: none blocking.
- Open: none blocking - scenes cover the first three; extending to
  shapes 4-10 is deferred until those shapes pass their own gate.

## Verification

- L3 client evidence (JEI on/off screenshots, scene playback recordings
  or screenshots, tooltip screenshots); pack JEI check inside issue 13
  harness.

## PR contract

Full restatement per `.github/pull_request_template.md`.
