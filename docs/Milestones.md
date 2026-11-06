# Milestones - Create: Contour

Outcome-based milestones, not date buckets. All owners unassigned, all dates
unscheduled, all priorities proposed (not owner-approved). Issue drafts live
in [issues/](issues/README.md).

| Milestone | Outcome | Issues | Status |
| --- | --- | --- | --- |
| M0 | Reproducible, legal, minimal foundation | 01, 02 | in-progress (01 bootstrap builds on both profiles; 02 not started) |
| M1 | First three shapes usable, safe vertical slice | 03, 04, 05, 06, 07 | planned |
| M2 | First three proven production-like AND pack-compatible (trains, schematics, RC2 isolated runtime, two clients) - STRICT expansion gate | 08, 09, 13 | planned |
| M3 | Expansion from 3 to 10 shapes + discoverability | 10, 11 | planned |
| M4 | Additional shape families + release readiness | 12 | backlog |

## M0 - Foundation

- Outcome: a clean ModDevGradle workspace that compiles against pinned Create
  6.0.8-168 and a resolved 6.0.10 build, loads on a dedicated server, has
  license/credits/provenance plus the original neutral fallback assets, and
  ships an explicit Rustic Craft 2 pack profile record
  ([Compatibility.md](Compatibility.md), settled 2026-11-02) plus
  hash-verified isolated copies of the pack client/server runtime ready
  for issue 13.
- Dependencies: none.
- Risks: OD-1 resolved (6.0.10-280, source-commit match); OD-2 resolved
  (+62 unsatisfiable; legacy uses required +67, deviation recorded);
  non-required dev-POM integrations polluting the resolved classpath;
  pack integrations (renderer, Connector, KubeJS, Create addons) that only
  full-pack executions expose - deferred to issue 13.
- Proof: build logs + jar SHA-256 for both planned target profiles (primary
  pack 6.0.10/NF21.1.248; legacy 6.0.8-168/NF21.1.213); dedicated server
  boot log with no client-class errors; provenance register and asset
  inventory; isolated pack copies verified by hash (issues 01, 02).

## M1 - Vertical slice

- Outcome: slab, vertical_slab, and stairs place, paint, waterlog, cycle, and
  fail safely end-to-end with full resources (models, recipes, tags, lang,
  tooltips) and the workflow/economy rules implemented.
- Dependencies: M0.
- Risks: the IBE generic seam for our own BE type; material acceptance of
  non-full shapes needs documented overrides; orientation mapping across the
  cycle family.
- Proof: per-shape real-client screenshots + logs for the six-material
  matrix; safe-failure cases from issue 03; workflow ACs from issue 07.

## M2 - Production-like proof (STRICT GATE)

- Outcome: the first three shapes survive contraption/train assembly,
  rotate/mirror transforms, schematicannon save/load/count, and - the
  mandatory pack criterion - a hash-verified isolated copy of the Rustic
  Craft 2 client AND server runtime (stable 2.2.2, NF 21.1.248, Create
  6.0.10, full declared mod list) with two real clients, including join,
  shader/renderer, KubeJS recipe, pack-addon interaction, and
  save/reload regressions.
- Dependencies: M1; settled pack profile (Compatibility.md) and isolated
  pack copies (issue 01).
- Risks: contraption transform edge cases on asymmetric materials;
  schematic SafeNBT stripping; multiplayer-only defects invisible to
  single player; pack interactions (Sodium/Iris/Solas, Connector, JEI
  19.57, KubeJS recipe rewrites, Create Connected, Aeronautics, Threaded
  Trains, unverified Schematic Checker) that a minimal profile cannot
  expose.
- Proof: issues 13 and 09 fully executed - every case pass/fail/
  unsupported with command, versions, world fixture, screenshot, log,
  hash. Any required case marked unsupported blocks this milestone and
  all shape 4+ work.
- Gate rule: the gate blocks SHAPE work only - no shape-4+ implementation
  (issues 10, 12) until 09 (which requires 13) passes. Issue 11
  (discoverability for the first three) is NOT gated and may proceed
  after issue 07. The clean minimal profile remains a separate
  reproduction environment; it never substitutes for pack proof. Testing
  uses only isolated copies - never mutates the original modpack repo or
  live worlds - and any live/external server action requires explicit
  permission (ADR-008).

## M3 - Expansion to ten

- Outcome: seven more train-first shapes (carriage_panel, platform_edge,
  rail_bed_trim, beam, pillar, wall, pane) land individually with full
  resources and regression proof, plus JEI/Ponder discoverability.
- Dependencies: M2 gate passed.
- Risks: per-shape connectivity (wall/pane) geometry; tooltip/Ponder scope
  creep.
- Proof: per-shape evidence rows; JEI optional in both profiles; two
  original Ponder scenes.

## M4 - Later families + readiness

- Outcome: backlog families (fence, fence_gate, trapdoor, door, layers 1..8,
  slopes, quarter-round) specified and, when authorized, implemented per
  family sub-issues; release-readiness checklist exists.
- Dependencies: M3; each family needs its own geometry spec first; any
  publication step additionally requires explicit owner authorization
  (ADR-007) and the ADR-001 re-checks.
- Risks: interactive blocks (door halves, gate open state) add redstone,
  pathing, and collision surface; scope must stay bounded per family.
- Proof: per-family acceptance lists inside issue 12's sub-checklists.
