# Issue 15: Contour Codex - in-game shape and material selector book

| Field | Value |
| --- | --- |
| Status | in-progress (v1 landed 2026-11-03: item, screen, packet, recipe, codexTemplateIsUnpaid GameTest) |
| Milestone | M3 |
| Type | feature / ux |
| Suggested labels | `ux`, `item`, `gui` |
| Priority | proposed-medium |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Give players one item - the Contour Codex - that opens a selection GUI
listing every shipped shape and lets them pick a shape (optionally with
a material) and receive a ready-to-place item, instead of hunting
through JEI or the creative tab.

## Expectation

- A `contour_codex` item opens a client GUI with a shape grid plus an
  optional material picker.
- Confirming a selection emits a TEMPLATE shape item (paid 0) to the
  player: shape + optional material preset, never PREPAID. ADR-009
  forbids minting paid units outside verified server origins; the codex
  is not an economy path.
- Same rule in survival and creative: TEMPLATE always. Creative users
  paint for free already; survival users still consume real material at
  paint time. No mode branch, no credit path.
- Material picker sources: materials already present in the player's
  inventory (survival) or the full material list (creative). Server
  validates every emitted template; client selection is a request, not
  an authority.
- All GUI art is original (ADR-005); no Create or pack assets reused.

## Acceptance criteria

1. Codex item registered with own original texture, lang, tooltip, and
   recipe (recipe lands under issue 16).
2. GUI lists only shapes actually shipped at runtime; new shapes appear
   automatically as they register (data-driven list, no hardcoded set).
3. Selection emits the exact TEMPLATE payload (shape id, material tag,
   paid 0, default facing) verified server-side; tampered packets emit
   nothing.
4. Survival discipline proven by test: codex output never carries paid
   units; painting a codex template still consumes real material.
5. GUI opens/closes cleanly in dev client and pack runtime; keyboard
   escape and inventory keys work; screenshots in evidence.
6. No hard dependency added: codex functions with or without JEI;
   dedicated server loads without the screen class ever loading.
7. Ponder/tooltip discoverability for the codex is wired into issue 11
   scope (tooltip required here; Ponder scene optional there).

## Context the code cannot infer

- Template-with-material selection is legitimate: it mirrors the
  sneak-copy TEMPLATE already in the ledger model (material tag, paid 0)
  and does not bypass consumption.
- The codex deliberately does NOT place blocks directly; it hands an
  item so placement flows through the normal placement/prediction path
  the probe harness already verifies.
- Networking: selection request -> server validates -> server inserts
  the template item. Client never writes the payload itself.

## Scope

### In scope

- Codex item, selection GUI, server-validated template emission,
  original art, tooltip.

### Non-goals

- Direct world placement, wand mode, radial menus, paid/prepaid output,
  recipe cost configurability beyond the one recipe.

## Dependencies and open decisions

- Depends on: 07 (ledger/workflow rules to respect); benefits from 10
  (more shapes make it more useful) but is not blocked by the 09 gate
  since it emits only already-shipped shapes.
- Feeds: 11 (tooltip/discoverability), 16 (codex recipe).
- Open: item name (`contour_codex` vs `contour_catalog`), GUI layout
  (grid vs list), whether material picker shows counts.

## Implementation notes (v1)

- `CodexSelectPayload` (shape item id + material block id,
  `minecraft:air` = blank) -> `ContourNetwork.handleCodexSelect`
  server-validates both ids; tampered/invalid requests emit nothing.
- `ContourNetwork.buildCodexTemplate` is the single emission path,
  covered by `codexTemplateIsUnpaid` GameTest (25 tests pass).
- Screen is data-driven off `ContourRegistries.ITEMS` (new shapes appear
  automatically). Material strip: blank + inventory-sourced paintables;
  creative adds a curated set. DEVIATION from AC: creative picker is
  curated+inventory, not the full material list - a scrollable full
  registry list is deliberately deferred.
- GUI is code-drawn (original, no textures) per ADR-005.

## Verification

- L3 client evidence: GUI screenshots, emitted-item payload dumps
  (server inspect), survival-consumption GameTest on codex templates,
  pack-runtime open/close check via the issue-13 harness.

## PR contract

Full restatement per `.github/pull_request_template.md`.
