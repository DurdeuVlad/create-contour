# Issue 10: Seven next train-first shapes (total ten)

| Field | Value |
| --- | --- |
| Status | in-progress (implementation gate lifted by owner, ADR-014a; shipped: contour_layers, contour_post, contour_byte, contour_wall, contour_pane, contour_fence, contour_slope, contour_trapdoor, contour_ladder + issue-14 contour_panel; remaining listed shapes and pack verification still owed) |
| Milestone | M3 |
| Type | feature |
| Suggested labels | `shape`, `expansion` |
| Priority | proposed-medium |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Expand from the proven three to ten shapes total, in train-usefulness
order, each landing individually with the same resource and proof standard
as the first three.

## Expectation

- Seven shapes, final order (agent-selected default ADR-010, train-use
  rationale):
  `carriage_panel` (1/8 panel - carriage skin, the single most-used train
  surface), `platform_edge` (thin edge - station/platform edge marking),
  `rail_bed_trim` (ballast edging around track), `beam` (frame supports
  and bogies), `pillar` (column posts, supports), `wall` (barriers and
  fences of stations), `pane` (glazing for windows).
- Each shape: geometry + blockstate properties, own cropped-quad model,
  blockstate/model JSON, recipe, loot, tags, lang, tooltip; waterlogging
  where sensible; connectivity where the shape implies it (wall, pane).
- Each shape integrates all issue-07 workflows (precedence, sampling,
  sneak-copy, wrench behaviour per its family) and state transforms.
- Shapes land incrementally - no "all working" claim without per-shape
  evidence.

## Acceptance criteria

1. The settled ranking and rationale above is persisted; per-shape exact
   dimensions may remain draft/needs-spec entering implementation and
   must be pinned in each shape's implementation notes before it claims
   done.
2. Per shape (repeat full block):
   - geometry + placement rules implemented and documented;
   - collision/outline equals rendered geometry;
   - six-material matrix (`stone`, `oak_slab`, `glass`, `oak_log`,
     `oak_leaves`, `andesite_casing`) verified with screenshots;
   - waterlogging behavior correct or documented N/A;
   - connectivity behavior (wall/pane) verified incl. neighbour
     removal;
   - full resources (blockstate, models, recipe, loot, tags, lang,
     tooltip);
   - workflow integration verified (wrench behaviour defined per shape,
     material ops, pick-block);
   - transform behavior under rotate/mirror verified;
   - safe-failure cases (unknown/BE material, no-material neutral);
   - real-client evidence per docs/Testing.md template.
3. Each shape's wrench cycle family membership is specified (extend the
   canonical orientation map), or explicitly standalone.
4. Recipe namespacing `create_contour` maintained; JEI visibility verified
   in pack (reuses issue 13 harness where required).
5. No shape claims done until its per-shape evidence block is complete.

## Context the code cannot infer

- Issue 09 gate must be PASS before starting; pack regressions observed in
  issue 13 are re-verified for each new shape where relevant.
- `pane`/`wall` connectivity must not collide with Create Connected's
  adjacent geometry behavior - pack test covers the interaction.

## Scope

### In scope

- The seven listed shapes and their full per-shape deliverables.

### Non-goals

- No later-family shapes (issue 12).
- No discoverability content beyond per-shape tooltips (issue 11 owns
  JEI/Ponder scenes, though recipes must remain JEI-visible).

## Dependencies and open decisions

- Depends on: 09 (must be PASS; 09 transitively requires 13, 08, 07).
- Feeds: 12.
- Open: per-shape wrench family assignments; per-shape exact dimensions
  (draft/needs-spec entering implementation, per AC 1).

## Verification

- Per-shape L3 evidence + re-run of relevant pack cases (issue 13
  harness) where the new shape interacts with declared risk surfaces.

## PR contract

Full restatement per `.github/pull_request_template.md`.
