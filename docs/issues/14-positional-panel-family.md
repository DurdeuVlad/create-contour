# Issue 14: Positional panel family spec (edges, middle, diagonal)

| Field | Value |
| --- | --- |
| Status | in-progress (v1 landed: contour_panel with 39 positions - 6 face, 3 mid, 24 edge, 6 diagonal; GameTest green; pack verification owed) |
| Milestone | M3 (family spec may resolve against issue 10 panel shapes) |
| Type | spec / feature |
| Suggested labels | `shape`, `needs-spec`, `panel` |
| Priority | proposed-medium |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Provide thin paintable panels that sit at non-full positions inside a
block space: mounted on a face, clipped to an edge, centered in the
middle, or diagonal through the cube. The owner's stated use cases are
glass panels at the edge of a block, panels in the middle, and diagonal
panels for train glazing and structure detailing.

## Expectation

- ONE panel per block space; position is a blockstate property, not a
  shared-position multipart system. ADR-013 records this scope bound;
  true multipart (several panels sharing one block space) is a separate
  later-family decision and is NOT part of this issue.
- `position` enum bounded to: `face` (6 orientations), `edge` (12 cube
  edges), `middle` (3 axis-aligned center planes), `diagonal` (6 planes
  through opposite edge pairs). Exact per-position crop geometry is
  pinned in implementation notes before the shape claims done.
- Material, paid ledger, facing, and versioned BE payload reuse the
  existing ContourBlockEntity model unchanged.
- Ledger: flat 1 paid unit per placed panel (ADR-013; integer
  consumed->paid->drop invariant, same as slab).
- Wrench behavior cycles `position` within the current position family
  (face -> edge -> middle -> diagonal order pinned in spec); orientation
  axis cycles inside each family.
- Coplanar adjacent panels visually connect where the shape implies it
  (middle/middle and face/face neighbors), matching pane behavior;
  connection rules pinned in spec and verified incl. neighbour removal.
- Drops carry shape + material + paid + position/facing, identical
  contract to first-three shapes.
- Overlap resolution: this spec MUST state its relationship to issue
  10's `carriage_panel` and `pane` (superset family vs sibling shape)
  before any implementation; the existing issue-10 order (ADR-010) is
  not silently reordered.

## Acceptance criteria

1. Geometry spec exists: per-position crop bounds, collision = render,
   waterlogging, and the full position enum table; no implementation
   claim without the pinned table.
2. The `carriage_panel`/`pane` overlap decision is recorded in this
   issue and reflected in issue 10 (or explicitly scoped around).
3. Every position state: correct placement rules (which clicked face/
   edge yields which position), collision, occlusion, lighting.
4. Full six-material matrix + safe-failure + transform coverage at the
   same proof standard as issue 10 per-shape blocks.
5. Wrench cycle map extended; sneak-wrench pickup preserves position +
   ledger.
6. Ledger: 1 unit consumed -> paid 1 -> break drops the ledger item
   (paid, unpaid, config-off cases), verified by GameTest + pack trace
   reuse of the issue-13 harness.
7. Pack re-verification where position panels touch declared risk
   surfaces (Create Connected adjacent geometry, schematic capture).

## Context the code cannot infer

- Position selection on placement must derive from the click context
  (hit face + hit position within the face), not a mode item; the exact
  mapping table is part of the geometry spec.
- Diagonal panels raise texture-rotation and lighting questions; the
  spec must state whether material textures rotate with the plane or
  stay axis-locked.
- Multipart (2b) is deliberately out: shared-position panels change
  block-entity, collision, drop, and schematic architecture and are a
  different subsystem.

## Scope

### In scope

- Family spec + eventual implementation of the single-position panel
  block with the bounded position enum.

### Non-goals

- Multipart panels sharing a block space.
- Any reordering of issue 10's accepted shape order.
- Implementation before issue 09 PASS.

## Dependencies and open decisions

- Depends on: 09 PASS for implementation; spec/drafting unblocked now.
- Interacts with: 10 (`carriage_panel`, `pane` overlap), 12 (family
  tracking), 07 (wrench/ledger workflows), 13 (pack re-verification).
- Open: exact position-count in the enum (27 listed vs reduced set);
  texture-rotation policy on diagonals; coplanar-connect rules;
  placement hit-context mapping table.

## Verification

- Spec completeness review; then per-shape proof standard of issue 10
  plus GameTests for position-aware ledger/drop and wrench cycles;
  pack harness re-verification per issue 13 method.

## PR contract

Full restatement per `.github/pull_request_template.md`.
