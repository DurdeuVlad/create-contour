# Issue 05: Vertical slab shape end-to-end

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M1 |
| Type | feature |
| Suggested labels | `shape`, `vertical-slab` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Ship the second shape - vertical slab - with placement that matches builder
muscle memory (edge/alignment driven facing) and full parity with the slab
pipeline.

## Expectation

- Vertical slab supports all four horizontal facings, chosen by which
  edge/alignment of the target face is clicked.
- Waterlogging, collision=render, and the full resource set, matching the
  conventions proven in issue 04.
- Same six-material matrix and safe-failure coverage.

## Acceptance criteria

1. All four facings place correctly from edge/alignment hit positions;
   facing is predictable and documented (placement rules written in code
   comments and tooltip).
2. Collision and outline shapes equal rendered geometry for every facing.
3. Waterlogging matches vanilla-style semantics for a vertical half
   block; no source-fluid simulation.
4. Six-material matrix (`stone`, `oak_slab`, `glass`, `oak_log`,
   `oak_leaves`, `andesite_casing`) renders correctly per facing;
   screenshots.
5. Unknown/BE-bearing material rejected without consumption; neutral
   fallback renders.
6. Resources complete: blockstate + models, recipe, loot, tags, lang,
   tooltip.
7. No-material render uses the original neutral model; real client
   screenshot + clean log.
8. L2 placement/orientation logic tests accompany the L3 evidence.

## Context the code cannot infer

- Facing choice on edge clicks is a UX decision: align with how the wrench
  cycle family (issue 07) maps vertical slab orientation to slab/stairs
  orientation via canonical metadata.
- Shares the acceptance-override seam documented in issue 04.

## Scope

### In scope

- Vertical slab block, model, placement/facing rules, resources,
  six-material matrix, safe-failure cases.

### Non-goals

- Wrench cycling (07), contraption/schematic (08), pack proof (13/09).
- No "double vertical slab" combined state - agent-selected default
  (ADR-010):
  adjacent vertical slabs stay independent blocks.

## Dependencies and open decisions

- Depends on: 04 (pipeline conventions proven).
- Feeds: 06, 07.
- Open: none - combined-state question settled (no double vertical slab,
  ADR-010).

## Verification

- L2 + L3 evidence per AC in docs/Testing.md format.

## PR contract

Full restatement per `.github/pull_request_template.md`.
