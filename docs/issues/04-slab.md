# Issue 04: Slab shape end-to-end (first shape vertical slice)

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M1 |
| Type | feature |
| Suggested labels | `shape`, `slab`, `vertical-slice` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Ship the first copycat shape - slab - completely: geometry, placement,
material painting, waterlogging, and every required resource, proving the
full pipeline on one shape before the family grows.

## Expectation

- A slab block supporting top/bottom placement by hit position and double
  merging only when the existing half carries the same material.
- Collision/outline shape equals the rendered geometry for every state.
- Waterlogging behaves like a vanilla slab (host geometry + world fluid;
  no material-fluid simulation).
- Full resources: blockstate JSON, block+item models (cropped-quad model),
  recipe, loot, tags, lang entry, concise wrench tooltip.
- Six-material matrix verified: `stone`, `oak_slab`, `glass`, `oak_log`,
  `oak_leaves`, Create `andesite_casing`.

## Acceptance criteria

1. Placement selects top/bottom half from hit; existing half merges into
   double ONLY when materials match. Mixed-material merge is REJECTED
   (ADR-010): the click consumes and places nothing on that target;
   ordinary adjacent placement proceeds only where the click context
   allows.
2. Collision and outline voxel shapes equal the rendered geometry for
   bottom, top, and double states (screenshot evidence per state).
3. Waterlogged placement, behavior, and render match vanilla slab
   semantics; no source-fluid simulation.
4. Six-material matrix: each material renders correctly including
   `oak_slab` as material (requires the documented
   `getAcceptedBlockState` acceptance override), `glass` transparency,
   `oak_log` axis, `oak_leaves` tint, `andesite_casing` CTM. Screenshot
   per material.
5. Unknown or block-entity-bearing material is rejected gracefully: no
   consumption, no crash, neutral fallback.
6. Resources complete: blockstate + block/item model JSON, recipe,
   loot table, mineable/tool tags, lang name, tooltip line.
7. No-material slab renders the original neutral model (reuses issue 03
   fixture); real client screenshot + clean log.
8. Double-slab accounting (ADR-009): each half is one shape item + one
   material application; a same-material merge preserves two shape items'
   worth and up to two paid units (mixed paid/unpaid halves valid);
   breaking the double yields two individual shape items with the ledger
   split and no material duplication; wrench-cycling a double is blocked.
   Verified in evidence, not just code review.
9. Automation acceptance checks (L2) for placement/merge/waterlog logic
   accompany the real-client evidence - neither replaces the other.

## Context the code cannot infer

- `getAcceptedBlockState` upstream rejects `EntityBlock`, `StairBlock`,
  and non-full-bounds blocks; slab-material acceptance needs a documented
  override (ADR-003).
- The credit model (TEMPLATE vs PREPAID ledger, ADR-009) is settled in
  Business rules 2-7; the shared economy implementation lands in issue
  07, but the merge/break accounting in AC 8 is shape-specific and is
  owned here.

## Scope

### In scope

- Slab block + BE usage, per-shape model impl, all resources listed,
  six-material verification, safe-failure cases for this shape.

### Non-goals

- Wrench shape cycling and workflow economy (issue 07).
- Contraption/schematic proof (issue 08), pack proof (issue 09).

## Dependencies and open decisions

- Depends on: 03.
- Feeds: 05, 07.
- Open: none on merge policy - settled REJECT (ADR-010).

## Verification

- L2 logic tests + L3 real-client screenshots/logs per AC; entries in the
  docs/Testing.md matrix updated from NOT RUN to actual results.

## PR contract

Full restatement per `.github/pull_request_template.md`.
