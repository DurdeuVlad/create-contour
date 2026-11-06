# Issue 09: First-three expansion proof gate (consumes pack proof)

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M2 |
| Type | verification gate |
| Suggested labels | `gate`, `verification`, `multiplayer` |
| Priority | proposed-high (blocks ALL expansion) |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

The strict expansion gate: certify the first three shapes are
production-like by consuming the mandatory Rustic Craft 2 pack proof
(issue 13) plus the multiplayer/save/render regression matrix - before any
shape 4+ work begins.

## Expectation

- Issue 13's full-pack evidence matrix is complete; this issue re-checks
  its results rather than re-deriving them.
- The multiplayer matrix runs on the isolated pack runtime: one dedicated
  pack server + two pack clients.
- A clean minimal profile reproduces the same matrix as a separate
  reproduction environment - it never substitutes for pack proof.
- Every case is recorded pass/fail/unsupported; required-unsupported or
  failed cases block M2 and all expansion.

## Acceptance criteria

1. Issue 13 verdict reviewed: all required pack scenarios observed with
   evidence; any outstanding pack failure/blocker is listed here as
   blocking.
2. Two pack clients + dedicated pack server: all 3 shapes x 6 materials
   place, paint, wrench-cycle, break on both clients - screenshots +
   logs per case.
3. No-material case proven in real rendering on the pack runtime
   (screenshot + clean log; headless evidence not accepted).
4. Save/reload fixture incl. missing-material world: state survives,
   neutral fallback renders, no corruption.
5. Repeated resource reload + chunk remesh on pack runtime: no stale
   material quads, no meshing errors.
6. Minimal clean profile (loader + Create + deps + addon only; JEI only
   in a separate optional profile; Connector excluded from the minimal
   test): same matrix reproduced, recorded as reproduction environment
   only.
7. Every case recorded per docs/Testing.md template: result, command,
   versions, world fixture, action, screenshot path, log path, jar hash.
   Unit/GameTests and the two compile profiles are NOT substitutes.
8. Gate verdict written: PASS enables SHAPE expansion (issues 10, 12);
   FAIL/UNSUPPORTED required cases keep shape expansion blocked and list
   what must change. Issue 11 is not gated by this verdict.

## Context the code cannot infer

- Pack isolation rules and risk surfaces: docs/Compatibility.md.
- MC Pilot discipline: send input only to the focused target window
  (user32 fallback); never launch or steal focus from unrelated apps.
- "Connector secondary profile" from earlier drafts is superseded:
  Connector lives inside the pack profile now; minimal profile excludes it.

## Scope

### In scope

- Gate evaluation, multiplayer matrix execution on isolated pack runtime,
  minimal-profile reproduction, verdict document.

### Non-goals

- New features or fixes (failures spawn their own issues).
- Pack interaction cases themselves (issue 13 owns them; this gate
  verifies they were done).

## Dependencies and open decisions

- Depends on: 13.
- Transitive note: 13 depends on 07 and 08; 13 never depends on 09 - the
  direction is strictly 07/08 -> 13 -> 09, no cycle.
- Feeds: 10, 12 (shape work blocked until PASS). Issue 11 depends only
  on 07 and is not gated by this issue.
- Open: none.

## Verification

- The verdict document is itself the evidence: per-case records with
  artifact paths, plus the consumed issue-13 evidence index.

## PR contract

Full restatement per `.github/pull_request_template.md`.
