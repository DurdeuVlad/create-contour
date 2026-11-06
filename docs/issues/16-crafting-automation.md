# Issue 16: Manual crafting and Create-automated crafting

| Field | Value |
| --- | --- |
| Status | in-progress (manual+mechanical landed 2026-11-03; deployer/JEI/pack checks open) |
| Milestone | M3 |
| Type | feature |
| Suggested labels | `recipe`, `automation`, `jei` |
| Priority | proposed-medium |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Make every shipped shape producible by hand and by Create machinery so
the addon plays in survival without JEI archaeology: manual recipes for
neutral shapes and the codex, plus Create-native automation paths for
shaping and painting.

## Expectation

- Manual: a crafting path for every shipped neutral shape (crafting
  table and/or stonecutter-style single-input), plus a `contour_codex`
  recipe. Neutral-shape recipes already exist per-shape from issues
  04-06; this issue audits/completes them and owns all new shapes'
  recipe contract.
- Automated (Create-native, any subset that proves out):
  - Mechanical crafting matrix recipes for neutral shapes.
  - Deployer in-world painting: a deployer holding material applied to
    a placed shape flows through the same MaterialPolicy consumption
    path as a player click - consumed once, ledger written.
  - Mechanical mixer/sequenced-assembly recipe producing a painted
    shape item directly IS permitted only where the recipe provably
    consumes the material input - recipe output may carry paid 1 only
    when a real material item was an input. A recipe emitting PREPAID
    from non-material inputs is a credit-mint defect.
- JEI visibility for every recipe under `create_contour` namespace
  (issue 11 verification path reused).

## Acceptance criteria

1. Recipe audit table: every shipped shape has >= 1 manual path; gaps
   listed explicitly, not silently absent.
2. `contour_codex` recipe added and visible.
3. Deployer painting verified on a mechanical arm/deployer in dev
   world + pack runtime: exactly one material consumed, paid 1 ledger,
   no dupes; evidence screenshots/logs.
4. Any machine-produced painted item carries paid units ONLY equal to
   provably consumed material inputs (GameTest on the recipe output
   path).
5. Mechanical crafting recipe(s) for at least one shape family
   verified end-to-end in a running Create contraption/crafter.
6. All recipes visible in JEI inside the pack (issue 13 harness),
   including KubeJS reload survival - no recipe vanishes or collides
   after `kubejs reload`.
7. Recipe datagen covers every shipped shape; adding a shape without
   its recipes fails a build-time check.

## Context the code cannot infer

- Recipe-time PREPAID is ledger-legit (consumption proven by the
  recipe itself) and is the natural way to mass-produce prepaid shapes;
  the invariant is consumption, not where consumption happens.
- Deployer painting reuses MaterialPolicy unchanged - no special-case
  path for machines; if deployer use-block events bypass the normal
  use-item path, that deviation must be documented and tested.

## Scope

### In scope

- Manual recipes audit/completion, codex recipe, mechanical crafting
  recipes, deployer painting verification, JEI + KubeJS reload
  evidence.

### Non-goals

- Non-Create automation mods, recipe datapack config formats, schematic
  cannon material costing (issue 08 owns schematic ledger semantics).

## Dependencies and open decisions

- Depends on: 07 (consumption/ledger rules); 15 (codex recipe target).
- Feeds: 11 (JEI verification), 13 (pack JEI/KubeJS checks).
- Open: stonecutter vs crafting-table policy per shape; whether
  sequenced assembly is justified for painted shapes or deployer path
  suffices.

## Implementation notes (v1)

- Codex recipe: shapeless book + andesite_alloy.
- Mechanical crafting for all 3 shapes (`create:mechanical_crafting`,
  8 per craft, mirror-accepted): slab `NNN/AAA/NNN`, vertical
  `NAN/NAN/NAN`, stairs `N  /NA /AAA` motif.
- All 4 recipes parse clean on the GameTest server (2824 recipes
  loaded, no errors). JEI visibility inside the pack NOT RUN.
- Deployer-paint and machine-PREPAID paths NOT RUN; recipe-emitted
  PREPAID remains spec'd but unimplemented.

## Verification

- Datagen output review + GameTests for recipe accounting + dev-world
  deployer/crafter runs + pack JEI/KubeJS screenshots via issue-13
  harness.

## PR contract

Full restatement per `.github/pull_request_template.md`.
