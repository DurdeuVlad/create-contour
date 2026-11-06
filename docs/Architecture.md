# Architecture - Create: Contour

## Placement accounting authority

`ContourBlockItem.place` owns the transaction across vanilla placement.
Validate stored materials before billing; an explicit invalid stored
payload falls back to neutral without retargeting another material or
transferring stale credit. Blank items retain offhand/look sampling.

Creative placement does not consume the held prepaid item, so it must not
copy that item's credit into the free placed host. Existing target credit
in a slab merge is preserved from the pre-placement snapshot, not from
vanilla-imported `BLOCK_ENTITY_DATA`. Only a consumed, validated prepaid
frame or an actual material reservation adds incoming paid units.
Fresh neutral placements explicitly commit neutral material and zero paid
units. These rules do not change the frozen future-payload contract.

Status: the common and client core below is implemented as a private
three-shape prototype and compiles under both target profiles. Development
tests are indexed in [evidence/manifest.md](evidence/manifest.md). Full-pack,
train, contraption, schematicannon and multiplayer integration is NOT RUN.
Package root is decided: `net.createcontour` - a provisional namespace with
no domain-ownership claim (formerly OD-3; agent-selected implementation
default 2026-11-02, reversible, not separately owner-approved).

## Implemented component map

```text
net.createcontour
  CreateContour.java            mod entrypoint; registrations wiring
  registry/                     blocks, block entity types, items, creative tab
  block/
    ContourBlock.java           shared base: extends Create CopycatBlock
    ContourSlabBlock.java       issue 04
    ContourVerticalSlabBlock    issue 05
    ContourStairsBlock          issue 06
    ShapeGrid.java             shared occupancy grid and collision geometry
  blockentity/
    ContourBlockEntity.java     extends CopycatBlockEntity; own type; versioned NBT
                               owns schema, paid units and canonical orientation
  client/
    ContourModel.java           extends CopycatModel; neutral fallback boundary
    ShapeQuads.java             emits occupancy-grid faces using material UVs
    per-shape model wrappers    choose the host occupancy mask
  item/
    ContourBlockItem.java       placement precedence and legacy item payload
  material/
    MaterialSafety.java         material normalization, eligibility and neutral
    MaterialPolicy.java         server payment, escrow, refunds and merge ledger
  config/
    ContourConfig.java          material consumption (default true),
                                texture rotation (default true)
  gametest/                     dedicated-server regression cases
src/main/resources/
  assets/create_contour/        lang, models, textures (all original)
  data/create_contour/          recipes, tags, loot tables
```

Shapes 4-10, optional JEI integration and original Ponder scenes remain
planned. Schema and canonical orientation do not have separate service
classes: `ContourBlockEntity`, the block mappings and item payload already
own those concepts. Ponder is a Create dependency; the placer tutorial
scene is implemented through `ContourPonderPlugin`/`PonderIndex` and adds
no new hard dependency.

## Design rules

### Inherit vs override (per pinned 6.0.8-168 source findings)

| Create behavior (observed) | Our choice | Why |
| --- | --- | --- |
| `CopycatBlock` abstract, `IBE<CopycatBlockEntity>`, `IWrenchable` | Extend per shape | Reuse wrench plumbing, material delegation, contraption hooks |
| `getBlockEntityType()` returns `AllBlockEntityTypes.COPYCAT` | Override to own type | Our versioned NBT and accounting must not write into Create's BE contract |
| `CopycatBlockEntity.material`/`consumedItem` private; base `read` validates material via `getAcceptedBlockState(consumedItem)` and ERASES the material when `Item` is empty (empty stack -> null acceptance -> reset to `COPYCAT_BASE`) | Subclass + documented `read` override seam | Under consumption-off the paid ledger is legitimately empty; base read would wipe a valid material on load - override required |
| `getAcceptedBlockState` rejects CopycatBlock recursion, `EntityBlock`, `StairBlock`, non-full bounds unless `COPYCAT_ALLOW`/`isAcceptedRegardless` | Documented acceptance override per shape | Slabs/stairs as material are required by the six-material matrix |
| `onWrenched` strips material and refunds `consumedItem`; `onSneakWrenched` delegates | Override both | Our wrench cycles shape family; sneak-wrench picks up preserving data; config-off means no refund |
| `getCloneItemStack` returns the MATERIAL item unless sneaking | Override | Pick-block returns the blank shape item (ADR-014) |
| `useItemOn` applies material, cycles same-material properties, `setPlacedBy` auto-applies offhand | Reuse + extend | Offhand precedence exists upstream; look-target sampling and stored-item precedence are added |
| `CopycatModel.getMaterial` defaults to `COPYCAT_BASE`; `OCCLUSION`/`WRAPPED_DATA` private | Own model subclass + own neutral fallback assets | Neutral identity is ours; no Create asset reuse (ADR-005) |
| `getCroppedQuads` abstract; `CopycatStepModel` shows crop-and-move | Shared occupancy-grid emission with per-shape masks | Host geometry must not depend on the material model's geometry; rendering and collision share `ShapeGrid` |
| `transform` applies `StructureTransform` to material; `writeSafe` strips item components; `getRequiredItems` reports `consumedItem` | Reuse hooks where compatible; OWN overrides where version/policy dictate (texture-rotation config in transform, paid-unit ledger in `getRequiredItems`, version field in `write`/`writeSafe`, config-off drop behavior) | NOT "free" verified compatibility - each hook is exercised and proven by issues 08/09 |

### Visual eligibility (`material/MaterialSafety`)

Material eligibility is owned centrally by MaterialSafety, not by a shape,
client shim or pack-mod exception. Air, fluids, block entities and recursive
copycats remain rejected. The context-free non-empty SELECTION/OUTLINE shape
is the visual eligibility boundary, not the physical collision shape.
The pack diagnostic established that a required source (oak leaves) can have
empty physical collision while remaining visible/selectable. Collision-based
validation rejected it and let normal item placement run instead of painting.
An empty outline or throwing context-free shape lookup remains rejected.
No event cancellation/forced-use workaround or leaf-only whitelist is used.

### Material resolution and economy (`material/MaterialPolicy`)

Credit model (ADR-014, owner directive): shape items are ALWAYS blank -
paint lives on the world block only. The block entity's `paidUnits`
ledger counts material units actually charged, from verified server
origins (in-world paint, placement reserve, schematic apply).

- Item payloads: nothing writes `contour_material`/`contour_paid` onto
  inventory stacks. Legacy stacks carrying those keys remain parseable
  and their escrow placement paths are retained, but no game path mints
  them anymore (ADR-009 TEMPLATE/PREPAID states are dead).
- Precedence at application/placement: legacy stored material >
  offhand > targeted block sample > neutral.
- Look-target sampling is captured at the placement hit before any world
  mutation and re-validated server-side; a clicked Contour block
  contributes its BE paint, not its recursive host block.
- Consumption ON (default): painting a placed shape or placing with a
  resolved incoming material consumes exactly one matching material item
  from offhand/inventory and records one paid unit. If no matching item
  is available, the application is REJECTED without consuming the shape
  or material - never a silent free grant.
- Consumption OFF: newly applied material records zero paid units; no
  refund on break and no schematic material requirement. Pre-existing
  paid credit is preserved across config toggles (no retroactive charge,
  no destruction).
- Creative: application is free; pick-block yields a BLANK item; never
  refunds.
- Failed placement returns reserved material fragments, including their
  components, to their original inventory/offhand slots where possible.
  If another event changed those slots, compatible inventory space is
  used before dropping only the true remainder; unrelated contents are
  never overwritten. Refunds are one-shot.
- Survival pick-block only selects an existing item in the inventory - it
  never spawns a stack.
- Break/pickup: ordinary break, sneak-wrench pickup and placer swoop
  yield `hostUnits` blank shape items plus `paidUnits` material items
  (`ContourBlock.ledgerDrops`) - shape and paint come back separately.
- Double slab: each half is one shape item + one material application;
  a same-material merge preserves two shape items' worth and up to two
  paid units (mixed paid/unpaid halves are valid); breaking a double
  yields two blank shape items plus the paid material units - no
  material duplication. Wrench-cycling a double is blocked (would lose
  volume).
- Schematics: capture must not import paid ledger as reusable currency or
  grant free credits. Printing with consumption ON requires shape units
  plus one material unit per shape unit (double = 2); consumption OFF
  requests zero material. On printer apply, the ledger reflects items
  actually consumed.
- Rejected materials (unknown id, block-entity-bearing, recursive copycat,
  otherwise unsupported dynamic visuals) fail without consumption and
  fall back to neutral - never a claim that all modded visuals work.

### Rendering safe-failure boundary

- Guard all public render entrypoints and the cropped-quad hook.
- Explicitly handled inputs: null item/inventory state, empty or null
  `ModelData`, missing material, air/unregistered material ids, material
  model exceptions, and resource reload - each resolves to the original
  neutral shape model at the narrow material boundary.
- No blanket `catch (Throwable)`; VM errors must propagate.
- Own neutral baked model + texture assets (issue 02), never Create's.

### Canonical orientation

- Canonical orientation metadata describes facing/half/shape independently
  of any single block's properties, so wrench cycling can map between
  shapes whose property sets differ, and contraption transforms can be
  verified for correctness.
- The same representation round-trips through the held item payload for
  stored materials and pick-block.

### Waterlogging and delegation

- Waterlogging belongs to host geometry + world fluid state; never simulate
  the material's fluid behavior.
- Sound, light, tint, particles, friction, harvest checks continue to
  delegate to the material via inherited Copycat behavior.

## Data contract (ADR-006 + ADR-009)

- `ContourBlockEntity` owns `ContourVersion` (currently 1), `Material`
  (full BlockState compound), `PaidUnits`, `Facing` (uppercase Direction
  name) and `Top` (boolean).
- `PaidUnits` is bounded by host capacity: one for a single shape, two
  for a double slab. The inherited `Item` hint may be serialized empty by
  Create, but is never economic currency; the owned consumed-item getter
  always returns empty.
- Read path: missing version = 0 -> migrate to 1; malformed/partial tags
  -> neutral fallback, no crash; unknown future version -> PRESERVE the
  raw unknown fields and the version on save (no silent downconvert that
  would lose future data). Non-destructive rejection/quarantine is the
  accepted alternative where preservation is impossible - never a
  best-effort "load known fields" that silently drops data.
- A frozen entity presents neutral material, zero paid units, NORTH and
  bottom orientation operationally. Same-position CompoundTag equality
  is tested for normal save; this is not a claim of byte-identical NBT
  files or supported breaking/pickup of unknown future data.
- `writeSafe` keeps Create's component-stripping behavior so schematics
  carry version + material without smuggling item components, and never
  exports paid ledger as reusable currency.
- Current-version safe writes retain material/orientation with zero paid
  units; frozen-version safe writes project neutral/zero-credit instead.
  Actual schematicannon printing and future-version drop capsules remain
  unverified or unimplemented, respectively.

## Client render contract (implemented, batch 6-10)

- `client/ContourModel` (extends Create `CopycatModel`) wraps every
  blockstate + inventory model; emits quads from the shape's own 2x2x2
  occupancy grid (`ShapeQuads`), never a blind crop.
- Owned view refresh: `refreshMaterialAppearance()` is the single seam
  that updates aux light + light engine + (client) model-data
  invalidation AND `level.sendBlockUpdated` remesh. Reused by
  `afterOwnedMutation`, `onLoad`, and `read` - the stripped
  `super.read` never reaches Create's private redraw, which is what
  made painted shapes render gray until a reload.
- Item binding survives the real renderer: `applyTransform` delegates
  pose but returns `this` (NF's wrapper would return the unbound
  original); `getRenderPasses`/`getRenderTypes(ItemStack)` forward the
  resolved material.
- CTM UV caution: Create's `CTModel` writes shifted UVs into quad
  vertices while `quad.getSprite()` keeps the original - so sprite
  bounds are NOT the UV region. `ShapeQuads` detects UV-shifted sources
  and clamps to the observed quad bounds, not the declared sprite.

## Explicit non-commitments

- Both target profiles compile; that does not establish runtime parity
  with the full Rustic Craft 2 client/server or its renderer stack.
- No custom networking unless issue 03 produces proof the standard BE sync
  packet is insufficient.
- No support promise for arbitrary modded dynamic BE materials.
