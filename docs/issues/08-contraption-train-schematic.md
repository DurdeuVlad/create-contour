# Issue 08: Contraption, train, and schematic fidelity for the first three shapes

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M2 |
| Type | feature + verification |
| Suggested labels | `contraption`, `train`, `schematic` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Prove the first three shapes behave correctly inside Create's moving
machinery - contraptions, real trains, rotate/mirror transforms, and the
schematicannon - without losing or duplicating material or shape data.

## Expectation

- Assembled contraptions render and disassemble correctly.
- A real moving train carrying all three shapes assembles, rides,
  reloads, and disassembles with materials intact.
- Structure transforms (90/180/270 rotation, mirror) behave correctly on
  asymmetric materials.
- Schematic save/load produces correct shape+material item counts with
  consumption config on AND off, no duplication.

## Acceptance criteria

1. Moving contraption containing all 3 shapes x representative materials:
   assembles, renders while moving, disassembles to the same states
   (material, orientation, accounting). Screenshots + logs.
2. Actual moving train carriage with the shapes: assembly, ride,
   stationing/disassembly, and save/reload of the assembled train all
   preserve data. Evidence per step.
3. The distinction between placing a block ADJACENT to a moving
   contraption vs modifying an already-assembled contraption is
   implemented and the latter is explicitly UNSUPPORTED where vanilla
   does not support it - recorded, not silently failing.
4. Structure transforms: 90/180/270 rotation and mirror applied to
   asymmetric materials (`oak_log` axis, stairs shape states) produce
   correct material+shape results; canonical orientation metadata is
   exercised.
5. Schematicannon (ADR-009): capture NEVER imports paid ledger as
   reusable currency or grants free credits. With consumption ON,
   printing requires the shape units PLUS one material unit per shape
   unit (a merged double counts as 2); with consumption OFF the material
   request is zero. On printer apply, the ledger reflects items actually
   consumed. `getRequiredItems` reports accordingly; no duplication on
   print in either config.
6. SafeNBT (`writeSafe`) keeps version + material only: it strips paid
   credit AND unwanted item components (per upstream
   `DataComponentPatch.EMPTY` behavior); the printed ledger derives from
   items actually consumed, never from exported ledger-as-currency.
   Verified via schematic NBT inspection.
7. Pack-dimension regressions (Threaded Trains, Schematic Checker) are
   owned by issue 13 - this issue proves the mechanics on the dev/minimal
   environment and notes which cases re-run in pack.

## Context the code cannot infer

- Upstream `CopycatBlockEntity` implements `TransformableBlockEntity`,
  `PartialSafeNBT`, `SpecialBlockEntityItemRequirement` - the mechanics
  largely exist; this issue proves our subclass preserves them.
- `create-server` pack config `maxBlocksMoved=2048`: fixtures stay within.

## Scope

### In scope

- Contraption/train/schematic behaviors for slab, vertical_slab, stairs;
  transform correctness; requirement counts; SafeNBT verification.

### Non-goals

- Full-pack proof (issue 13), two-client sync proof (09), shapes 4+
  (10-12).

## Dependencies and open decisions

- Depends on: 07 (workflows/economy defined).
- Feeds: 13, 09.
- Open: none currently; unsupported modify-assembled interactions are
  recorded explicitly.

## Verification

- L5 fixtures with world saves, action logs, screenshots per case;
  Testing.md rows updated with actual results.

## PR contract

Full restatement per `.github/pull_request_template.md`.
