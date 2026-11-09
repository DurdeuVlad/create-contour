# Issue drafts - Create: Contour

Local Markdown issue drafts (no external issue tracker is authorized or
used; ADR-007). Each issue carries Intent, Expectation, Acceptance
criteria, non-code Context, Scope/Non-goals, Dependencies, and
Verification; every PR must restate that contract per
`.github/pull_request_template.md` - a link is not a substitute.

Statuses: `draft`, `ready-for-handoff`, `dependency-blocked` - honest, not
aspirational. All owners unassigned, dates unscheduled, priorities
proposed pending owner approval.

| # | Title | Milestone | Status | Depends on |
| --- | --- | --- | --- | --- |
| [01](01-build-foundation.md) | Build foundation + pack isolation prep | M0 | in-progress | - |
| [02](02-legal-asset-provenance.md) | Legal/provenance + neutral assets | M0 | ready-for-handoff | - (AC6 needs 01 runtime) |
| [03](03-versioned-be-foundation.md) | Versioned BE + safe material/render foundation | M1 | in-progress | 01, 02 |
| [04](04-slab.md) | Slab end-to-end | M1 | in-progress | 03 |
| [05](05-vertical-slab.md) | Vertical slab end-to-end | M1 | in-progress | 04 |
| [06](06-stairs.md) | Stairs end-to-end | M1 | in-progress | 05 |
| [07](07-material-shape-workflows.md) | Low-click workflows + economic integrity | M1 | in-progress | 04, 05, 06 |
| [08](08-contraption-train-schematic.md) | Contraption/train/schematic fidelity | M2 | dependency-blocked | 07 |
| [13](13-pack-compatibility.md) | Rustic Craft 2 full-pack compatibility proof | M2 | dependency-blocked | 01, 07, 08 |
| [09](09-proof-gate.md) | Expansion proof gate (consumes 13) | M2 | dependency-blocked | 13 |
| [10](10-shapes-four-through-ten.md) | Seven next shapes (total ten) | M3 | in-progress (11 shapes shipped incl. slope/trapdoor/ladder; pack proof owed) | 09 PASS deferred by owner (ADR-014) |
| [11](11-discoverability.md) | JEI/Ponder/tooltips discoverability | M3 | dependency-blocked | 07 |
| [12](12-later-shape-families.md) | Later shape families (backlog: corner slopes, doors, gates, slices, specials) | M4 | draft | 10 |
| [14](14-positional-panel-family.md) | Positional panel family (edge/middle/diagonal) | M3 | in-progress (v1 landed: 39 positions; pack proof owed) | 09 deferred (ADR-014) |
| [15](15-contour-codex.md) | Contour Codex selector book | M3 | in-progress (v1 landed) | 07 |
| [16](16-crafting-automation.md) | Manual + Create-automated crafting | M3 | in-progress (recipes landed) | 07, 15 |

## Dependency graph

```text
01 ----+--> 03 --> 04 --> 05 --> 06 --> 07 --> 08 --> 13 --> 09 --> 10 --> 12
02 ----'                       |       ^                    |
                               +-------+                    |
                               (07 needs 04,05,06)          |
                                                            v
                                            11 (depends 07 only; M3,
                                            may run after 07 parallel
                                            to 10; NOT gate-blocked)

14/15/16 sit under M3. ADR-014a lifted the implementation gate per
owner directive; issue-09/issue-13 pack verification remains owed on
ALL shipped shapes before compatibility claims.
depending on 07 only, not on the 09 gate.
```

Gate rule: issue 09 is the strict expansion gate for SHAPE work and
itself requires the mandatory pack proof in issue 13 - dependency
direction is 07/08 -> 13 -> 09 -> 10 -> 12, never cyclic. Issue 11
depends only on 07 and is not blocked by the gate.

## Handoff notes

- Issues 01 and 02 are executable now (01's pack facts are settled in
  `../Compatibility.md`; 02's runtime-proof AC waits on 01's build).
- Everything downstream of 03 is `dependency-blocked` until its chain is
  green; issue 12 stays `draft` until per-family specs exist.
- Any issue reaching a pull request must use the mandatory PR contract.
