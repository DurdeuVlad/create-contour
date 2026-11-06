# Issue 06: Stairs shape end-to-end

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M1 |
| Type | feature |
| Suggested labels | `shape`, `stairs` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Ship the third and final gated shape - stairs - including the full
connection state machine (straight/inner/outer corners) so the first
family covers real train-decoration geometry.

## Expectation

- Stairs support facing, top/bottom half, and vanilla-style connection
  states: straight, inner_left, inner_right, outer_left, outer_right.
- Connections recompute on BOTH neighbour placement and neighbour removal.
- Mirrored/rotated geometry keeps collision equal to render.
- Full resources and the six-material matrix.

## Acceptance criteria

1. Placement sets facing + half correctly from click context.
2. All five shape states form correctly against compatible neighbours AND
   revert correctly when a neighbour is removed (both directions tested).
3. Mirrored and rotated geometry produce collision/outline shapes equal
   to rendered geometry for every state (per-state evidence).
4. Six-material matrix (`stone`, `oak_slab`, `glass`, `oak_log`,
   `oak_leaves`, `andesite_casing`) across states; screenshots cover at
   minimum straight + one inner + one outer state per material.
5. Unknown/BE-bearing material rejected without consumption; neutral
   fallback renders.
6. Resources complete: blockstate + models, recipe, loot, tags, lang,
   tooltip.
7. No-material render uses the original neutral model; real client
   screenshot + clean log.
8. L2 state-transition tests (form + unform) accompany L3 evidence.

## Context the code cannot infer

- Upstream `getAcceptedBlockState` rejects `StairBlock` materials; stair
  MATERIAL acceptance is a documented override decision - record the
  implemented policy.
- Neighbour compatibility is settled (ADR-010): our stairs connect to
  other Contour stairs AND to vanilla `StairBlock` when half and
  orientation match vanilla corner rules, independent of paint material;
  connections recompute on neighbour removal.

## Scope

### In scope

- Stairs block + connection state machine, per-shape model, resources,
  six-material matrix, safe-failure cases.

### Non-goals

- Wrench cycling (07), contraption/schematic (08), pack proof (13/09).
- Stair material acceptance policy beyond documenting the implemented
  override.

## Dependencies and open decisions

- Depends on: 05.
- Feeds: 07 (completes the cycle family), 08.
- Open: none - neighbour compatibility settled (ADR-010).

## Verification

- L2 state machine tests + L3 screenshots/logs per AC; Testing.md rows
  updated with actual results.

## PR contract

Full restatement per `.github/pull_request_template.md`.
