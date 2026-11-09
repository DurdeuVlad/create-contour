# Business - Create: Contour

Status: draft (local planning). Owner: project owner (unassigned for issues).

## Classification

- Ownership: single-developer.
- Visibility: public GitHub repository (public alpha since 2026-11-06).
- License intent: MIT (`LICENSE`). This is an intent statement, not an
  authorization to publish or host publicly.

## Problem

Create's copycat blocks let builders paint one block with the texture and
properties of another, but Create ships only a fixed set of shapes (block,
step, panel, and a few more). Builders decorating trains, stations, and
contraptions routinely need more geometry - slabs, vertical slabs, stairs,
beams, panels - with the same paint-any-material behavior. Today they either
give up the material illusion or use other mods that do not integrate with
Create contraptions.

At the same time, painting copycats is click-heavy: pick material, apply,
wrench to strip, repeat. Repetitive train decoration amplifies that cost.

## Users

- Create builders, especially train/contraption decorators (primary).
- The project owner, as sole maintainer (secondary).

## Value proposition

- More shapes carrying the copycat material illusion, selected by train
  usefulness first.
- Fewer clicks per decorated block: offhand material, look-at sampling,
  one-click wrench shape cycling, and the Contour Placer shape gun.
- Safe failure everywhere: unknown or unsupported materials are rejected
  without consuming items or corrupting saved data, and rendering always has
  an original neutral fallback - never a crash or a missing-texture hole.
- No material loss or duplication: economic rules are explicit and survive
  save/reload, shape cycling, and schematics.

## Solution shape

A NeoForge-only addon (`create_contour`) for Minecraft 1.21.1 that extends
Create 6's copycat machinery (own block entity type, own shape blocks and
models) rather than reimplementing it. ModDevGradle build. No new hard
dependencies; JEI is a soft/optional integration only.

## Actors and flows

- Player places a shape item; material is resolved by precedence
  (legacy stored material > offhand > look-target sample > neutral).
- Player right-clicks a placed shape with a material BlockItem to paint
  it (charged once); breaking or swooping returns the blank shape and
  the material separately.
- Player uses a wrench to cycle compatible shapes, preserving material and
  orientation; sneak-wrench picks up the block preserving its data.
- Player configures the Contour Placer's shape on its back-mounted
  selector, left-clicks to place it consuming `contour_blank` feedstock,
  and right-clicks a placed shape to swoop it back to feedstock plus the
  paid material.
- Contraption/train assemblies and the schematicannon move, rotate, and
  consume shape blocks without losing or duplicating material data.

## Business rules (binding)

1. Material source precedence: legacy stored item material > offhand >
   targeted block sample > neutral. Sampling is captured at placement
   time, before world mutation, and validated server-side. Stored item
   payloads are legacy-only (ADR-014); new items never carry them.
2. Credit model (ADR-014): shape items are ALWAYS blank - paint lives on
   the world block only. The block entity carries a paid ledger of
   verified server origin counting units actually charged; client
   payloads are never trusted.
3. Consumption on (default): first paint at placement or by applying a
   material BlockItem to a placed shape consumes exactly one matching
   material item. If no matching item is available the application is
   rejected without consuming shape or material - never a silent free
   grant.
4. Consumption off: newly applied material records zero paid units - no
   refund on break, no schematic material requirement. Pre-existing paid
   credit survives config toggles; nothing is retroactively charged or
   destroyed.
5. Creative: application is free and never refunds; pick-block yields a
   BLANK shape item. Survival pick-block only selects an existing
   inventory item; it never spawns a stack.
6. Break/pickup: ordinary break, sneak-wrench pickup and placer swoop all
   yield `hostUnits` blank shape items plus `paidUnits` material items -
   the shape and its paint come back separately (ADR-014). A double slab
   yields two shape items and up to two material units; wrench-cycling a
   double is blocked.
7. Schematics: capture never imports paid ledger as reusable currency or
   mints credit. Printing with consumption on requires shape units plus
   one material unit per shape unit (double = 2); consumption off
   requests zero material; on print the ledger reflects items actually
   consumed.
8. Wrench cycling stays inside a compatible shape family
   (initially slab / vertical_slab / stairs), preserves material, and maps
   facing/half through canonical orientation metadata when the destination
   shape lacks the source property. Conversions that would lose volume
   are rejected rather than lossy.
9. Mixed-material slab merge is REJECTED: a different-material click on a
   slab consumes and places nothing on that target; ordinary adjacent
   placement proceeds only where the click context allows. There is no
   double vertical slab. Stairs connect to our stairs AND vanilla
   StairBlock when half and orientation match vanilla corner rules,
   independent of paint material; connections recompute on removal.
10. Waterlogging belongs to the host shape's geometry and the world's
    fluid; the addon never simulates a source material's fluid behavior.
    Sound, light, tint, and particles still delegate to the material.
11. Unknown or dynamically unsupported (other mod's block entity)
    materials are rejected safely - no consumption, explicit neutral
    fallback - and the mod never claims that literally all modded block
    visuals are supported.
12. Texture rotation config (default on) rotates material axes with shape
    transforms; when off, texture orientation is preserved while the
    shape still transforms correctly.
13. Expansion gate: no shape beyond slab/vertical_slab/stairs is
    implemented until those three pass the full real-game proof matrix
    (milestone M2, issue 09). The gate blocks shape work only; issue 11
    discoverability is unaffected.
14. Rustic Craft 2 compatibility is mandatory: the first three shapes are
    only "done" when proven on an isolated copy of the RC2 client AND
    server pack runtime with the addon installed. The settled pack profile
    (RusticCraft2 2.2.2 stable, NF 21.1.248, Create 6.0.10, full declared
    mod list and risk surfaces) is recorded in docs/Compatibility.md. A
    clean minimal profile is a separate reproduction environment, never a
    substitute for pack proof, and no pack mod becomes a hard dependency
    of the addon.

## Success measures

- Each of the first three shapes demonstrably works with the six-material
  matrix on an isolated copy of the Rustic Craft 2 pack (client and server
  runtime) with zero join, train, or schematic regressions, evidenced by
  screenshots and logs (issue 09). This is the strict gate for all
  expansion.
- Zero crashes, missing models, or chunk-mesh failures under the
  no-material, null-data, and resource-reload cases.
- Schematicannon produces accurate shape and material item counts; no
  duplication under rotate/mirror/disassemble.

## Non-goals

- Multi-loader support (NeoForge only; Architectury rejected, ADR-002).
- Guaranteeing support for every modded block's dynamic visuals.
- Publishing any release or platform upload without explicit owner
  authorization (each release is an owner decision).
- Gameplay content unrelated to copycat shapes (no new machines, no worldgen).
