# Decisions - Create: Contour

Durable architecture/product decision records. Dates are decision dates, not
schedules. All issue-level priorities and dates remain proposed/unscheduled
until the owner approves them.

## ADR-001: Name and mod id

- Status: accepted (bounded). Date: 2026-11-02.
- Context: the project needs a display name and mod id. Name research on
  2026-11-02 found no exact "Create: Contour" / `create_contour` /
  `create-contour` addon on checked surfaces, but the bare name `contour` is
  taken on Modrinth by an unrelated mod
  (<https://modrinth.com/mod/contour>, "Lights on! Visible players and armor
  stands get the spotlight treatment.", source metadata only). CurseForge
  direct checks returned HTTP 403 and remain unsupported.
- Decision: display name **Create: Contour**; mod id `create_contour`.
  The bare name/id `contour` is never used. Full name-check table:
  [Research.md](Research.md).
- Consequences: the collision check is bounded, not a legal clearance; the
  CurseForge check and a fresh Modrinth/GitHub re-check must run before any
  publication decision. No external name reservation was made.

## ADR-002: ModDevGradle, single NeoForge project

- Status: accepted. Date: 2026-11-02.
- Context: build tooling for a NeoForge 1.21.1 mod.
- Decision: use [ModDevGradle](https://github.com/neoforged/ModDevGradle)
  with a single NeoForge project.
- Alternatives: NeoGradle (viable, no advantage for this scope);
  Architectury multi-loader (rejected: NeoForge-only constraint, extra
  complexity with no target second loader).
- Consequences: one toolchain to learn and pin; bootstrap is issue 01.

## ADR-003: Extend Create copycat machinery with own BE type and neutral fallback

- Status: accepted. Date: 2026-11-02.
- Context: pinned Create 6.0.8-168 sources were read (four classes, see
  [Research.md](Research.md)). `CopycatBlock` is abstract,
  `IBE<CopycatBlockEntity>` + `IWrenchable`; the block entity type getter
  returns Create's `AllBlockEntityTypes.COPYCAT`; `CopycatBlockEntity` keeps
  `material` and `consumedItem` private; `CopycatModel.getMaterial` falls back
  to Create's `COPYCAT_BASE`, not our own neutral texture.
- Decision: register our own `BlockEntityType` + `ContourBlockEntity extends
  CopycatBlockEntity`; each block overrides the BE type/class getters (the
  IBE generic seam must be verified during implementation). Own neutral
  baked-model assets supply the fallback instead of `COPYCAT_BASE`. Override
  only where Create behavior fails our contract (wrench stripping, pick-block
  returning the material item, material acceptance for non-full shapes), with
  a short rationale comment on each override; inherit everything else
  (material delegation of sound/light/tint/particles, contraption transform,
  SafeNBT/writeSafe, getRequiredItems).
- Consequences: strong upstream reuse at the cost of tracking Create API
  changes; the 6.0.10 cross-compile profile (issue 01) guards drift.

## ADR-004: First-three gate before expansion

- Status: accepted. Date: 2026-11-02.
- Context: each new shape multiplies geometry, model, resource, and
  regression-test surface.
- Decision: implement and fully prove slab, vertical_slab, and stairs first
  (milestones M1-M2). Shape 4+ work remains gated until issue 09's
  real-game matrix produces a PASS verdict (which requires issue 13's
  pack proof). Later families (issue 12) are backlog until each family's geometry
  is specified in its own sub-issue.
- Consequences: slower breadth, far lower risk of scaling an unproven
  material/model pipeline.

## ADR-005: Original assets only; MIT code

- Status: accepted. Date: 2026-11-02.
- Context: Create's code is MIT but its art assets are All Rights Reserved
  (<https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md>).
- Decision: license this project's code MIT ("Create: Contour contributors",
  2026). All textures/models/lang are original works; the neutral fallback
  texture is authored here, not copied or recolored from Create or any other
  mod. Any adapted third-party MIT snippet is recorded in the provenance
  register (issue 02).
- Consequences: no dependency on Create asset licensing; provenance is
  auditable.

## ADR-006: Versioned block-entity NBT schema

- Status: accepted (concept; not yet compiled/verified). Date: 2026-11-02.
- Context: material accounting and orientation metadata must survive
  save/reload, cycling, schematics, and future mod updates.
- Decision: the block entity writes an explicit schema `version` field
  (v1 initially); reads migrate 0 -> 1 and tolerate missing/partial/
  malformed tags by falling back to neutral. On an unknown FUTURE version
  the policy is preserve-raw: keep the unknown fields and the version on
  save so no future data is silently lost. Non-destructive rejection/
  quarantine is the accepted alternative where preservation is
  impossible - a best-effort "load known fields" downconvert that
  silently drops future data is explicitly rejected.
- Consequences: migration policy is defined before the first save exists;
  exact key names are an implementation detail of issue 03.

## ADR-007: Local private planning; publication undecided

- Status: accepted. Date: 2026-11-02.
- Context: the owner wants a local repo and a plan in issues/milestones; no
  external records were authorized.
- Decision: repository stays local and private; no remotes, commits (beyond
  local history if later authorized), tags, or publications. MIT intent does
  not authorize public hosting. Any future publication requires explicit
  owner authorization plus the pre-publication re-checks in ADR-001.
- Consequences: issue drafts live as Markdown under `docs/issues/` until the
  owner decides otherwise.

## ADR-008: Rustic Craft 2 pack compatibility is a mandatory gate

- Status: accepted (user requirement, 2026-11-02; pack profile settled).
- Context: the owner requires the addon to be compatible with our modpack,
  Rustic Craft 2. Compatibility must be proven against the pack's exact
  installed/catalog profile, not approximated. The settled profile
  (RusticCraft2 2.2.2 stable, NF 21.1.248, Create 6.0.10, declared mod
  list, jar hash evidence, config surfaces) is recorded in
  [Compatibility.md](Compatibility.md) from a read-only manifest read.
- Decision: RC2 compatibility is part of the strict M2 expansion/release
  gate for the first three shapes. Proof = issue 13 running the addon on
  hash-verified isolated copies of the RC2 client and server runtime,
  covering join, renderer/shader, KubeJS recipe, train, schematic, and
  save/reload regressions; issue 09 is the gate that requires it. Testing
  never mutates the original modpack repo or live worlds, and any
  live/external server action requires explicit permission. A clean
  minimal profile is retained as a reproduction/bisection environment
  only - it does not prove pack compatibility (Testing.md L4m).
- Consequences: two planned target profiles (primary pack
  6.0.10/NF21.1.248; legacy 6.0.8-168/NF21.1.213); the requested
  NF21.1.213/Ponder1.0.66 tuple is invalid under 6.0.10's declared
  minimums; every declared pack risk surface becomes a required acceptance
  case; "Connector smoke test" is not an acceptable substitute; no pack
  mod becomes a hard dependency.

## ADR-009: Material credit model - unpaid TEMPLATE vs PREPAID ledger

- Status: superseded by ADR-014 (2026, owner directive "items only
  exist without texture; breaking returns both"). The in-world paid
  ledger on the block entity survives; item-carried paint does not.
  Historical text kept for the design rationale that still applies to
  the BE-side ledger.
- Context: "consume exactly one at application/placement" would
  double-charge a prepaid pickup, and Create's single `consumedItem`
  (clamped to `copyWithCount(1)`) cannot represent the two paid units of a
  merged double slab.
- Decision: a painted shape's material exists as an unpaid TEMPLATE
  (sneak-copy, creative pick-block only; zero credit) or a PREPAID ledger
  (paid units of verified server origin: sneak-wrench pickup, break
  drop). Ledger is server-authoritative; client payloads never trusted.
  Rules: TEMPLATE paint consumes one matching item (consumption on);
  PREPAID placement transfers credit exactly once; no matching item ->
  reject without consuming; config-off writes zero paid units while
  preserving pre-existing credit across toggles; break/pickup drops the
  materialled item with ledger, never ledger + loose refund; double-slab
  break splits two shape items' ledgers; schematics never import ledger
  as currency and on print record only units actually consumed. Paid unit
  count is stored independently of base `consumedItem`; own
  `getRequiredItems`/`read`/`write`/`writeSafe`/drop overrides.
- Consequences: binding rules restated in Business.md rules 2-7 and
  Architecture.md MaterialPolicy; issue 07 acceptance criteria test every
  edge (no double-charge, no credit minting, split on double break,
  toggle preservation).

## ADR-010: Agent-selected UX implementation defaults

- Status: accepted (agent-selected implementation defaults, reversible;
  not separately owner-approved). Only the project name/id and the pack
  compatibility requirement were explicitly user-selected; the following
  are working defaults chosen for implementation, not user mandates.
- Decisions:
  - Java package root: `net.createcontour` (provisional namespace; no
    domain-ownership claim). Formerly OD-3.
  - No disambiguating modifier: sneak gestures are already disjoint -
    sneak-click WITH A WRENCH = pickup preserving data; sneak-click WITH
    A SHAPE ITEM = copy material into held item. Formerly OD-4.
  - Mixed-material slab merge: REJECT - consume and place nothing on
    that target; adjacent placement only where click context allows.
  - No double vertical slab state.
  - Stairs connect to our stairs AND vanilla `StairBlock` when half and
    orientation match vanilla corner rules, independent of paint
    material; connections recompute on neighbour removal.
  - Expansion order is final as listed in issue 10 with per-shape
    train-use rationale (carriage skin, station edge, ballast edging,
    frame supports, column posts, barriers, glazing); per-shape
    dimensions may stay draft/needs-spec entering issue 10.
- Consequences: open-decision list shrinks to genuinely open items;
  implementers do not guess these defaults.

## ADR-011: Black-box isolated pack runtime preparation

- Status: accepted by owner, 2026-11-03, through "Allow Isolated Pack Setup".
- Authority: copy/load existing pack binaries solely as runtime test
  dependencies and download missing artifacts from official pack sources
  into this project's ignored test workspace.
- Limits: no competing source/art inspection, hashing, decompilation,
  comparison or reuse in Contour. No original-repository/world writes,
  publication, commits or real-world service actions are authorized.
- Identity: retain catalog file IDs/names and effective loaded-version
  metadata for third-party runtime jars. Verify the permitted Create
  artifact against the recorded identity. Do not claim that the entire
  runtime is byte-hash-verified; the no-competing-hash constraint remains.
- Execution: baseline without Contour first, then the built addon.
  Missing/private/blocked artifacts and baseline failures are explicit
  blockers, not permission to omit mods or substitute versions.

## ADR-012: Labeled diagnostic pack profile

- Status: accepted by owner, 2026-11-03, through "Allow Diagnostic Profile".
- Scope: continue isolated Contour diagnostics with
  `-Dcustomskinloader.ignorePatchFailure=true` after preserving the strict
  pre-addon failure. The owner also requested autonomous reversible
  technical decisions rather than repeated questions.
- This permits a separately labeled diagnostic baseline and addon
  comparison, not a waiver of issue 13 or proof that CustomSkinLoader's
  skipped transformations/skin behavior work.
- Keep all pack jars/versions and clean-room boundaries unchanged. Do not
  alter the original pack repository, original worlds or live servers.
  No publication, commits or external service actions are authorized.
- Strict pack acceptance and shape-4 expansion remain blocked until their
  actual requirements are satisfied. Record diagnostic results separately.

## ADR-013: Expansion spec defaults - positional panels, codex, automation

- Status: accepted (agent-selected spec defaults, reversible; not
  separately owner-approved). Date: 2026-11-03. Owner direction:
  "more blocks, panels at edges/middle/diagonally, a book to select
  blocks, manual + Create-automated crafting".
- Decisions:
  - Positional panels (issue 14) are ONE panel per block space with a
    `position` blockstate enum (face/edge/middle/diagonal). True
    multipart (several panels sharing a block space) is a separate
    later-family subsystem decision, deliberately deferred.
  - Sub-block shapes keep the flat ledger rule: 1 material item
    consumed = 1 paid unit = 1 ledger item on drop, regardless of
    rendered volume (slab precedent; integer invariant preserved).
  - The selector (issue 15) is a book item (`contour_codex`) that emits
    TEMPLATE items (paid 0) only, in all game modes - ADR-009 forbids
    GUI-minted paid units. Shape + material selection both allowed.
  - Issue-10 shape order stays final (ADR-010); the panel family joins
    as its own spec (issue 14) and must resolve its overlap with
    `carriage_panel`/`pane` before implementation.
  - Crafting (issue 16): recipe-produced PREPAID is ledger-legit only
    when the recipe provably consumes the material input; deployer
    painting reuses MaterialPolicy, never a machine-only side path.
  - Spec docs land now; implementation stays gated behind issue 09
    PASS per ADR-004 (docs work is not implementation).
  - Golden rule for ALL shipped forms (owner, 2026-11-03): every shape
    must sit correctly next to any other block - occlusion, culling,
    lighting, and connectivity right against arbitrary neighbors.
    Recorded as a per-family acceptance criterion in issue 12.
  - Breadth strategy: prefer parametric families (position/thickness/
    size enums) over per-shape blocks, matching how the genre achieves
    form coverage - expanded catalog in issue 12 from a feature-level
    public survey (names only; no source/assets/binaries inspected).
- Consequences: expansion contract is written before code; all three
  issues are `draft`/`dependency-blocked`, none authorize starting
  shape 4.

## ADR-014: Owner-lifted shape gate and the AABB-box shape architecture

- Status: accepted (implementation authority: owner instruction
  "implement all the shapes of blocks that a builder might ever need",
  2026-11-05; verification gates in issue 09 remain required evidence,
  they are deferred not waived). Date: 2026-11-05.
- Context: ADR-004 hard-gated shape 4+ behind the first-three pack
  verification (issue 09 PASS). The owner's explicit all-shapes
  directive supersedes the ordering constraint: implementation may
  proceed, but the issue-09/issue-13 evidence obligations now apply to
  ALL shipped shapes before any pack-compatibility claim.
- Decisions:
  - Render and collision share ONE geometry source per shape:
    pixel-space AABB box lists. `ShapeQuads.emitBoxes` generalizes the
    octant emitter; connective shapes build their VoxelShape from the
    same list the model renders (render == collision by construction).
  - `ContourModel.boxesFor(state)` is the new geometry hook alongside
    `maskFor`; `extraQuads` covers non-AABB faces (diagonal plates).
  - `ContourBlock.wrenchTarget` is the wrench hook: family-chain hops
    for the slab family, same-block property cycles (LAYERS, AXIS,
    OCTANT, POSITION) for parametric shapes. `carryOrientation` is
    dispatched on the destination block (each shape maps facing/top to
    its own properties).
  - Waves shipped: layers (1-8), post (axis), byte (octant), panel
    (39 positions incl. 6 diagonals), wall, pane, fence (shared
    connective base), slope (4 facing x 2 half, stepped collision +
    true wedge render), trapdoor (click/redstone openable, paint
    clicks still route through useItemOn), ladder (climbable 2px
    plate, sturdy-back survival rule). All reuse the shared
    BE/ledger/payload pipeline; flat 1-unit ledger per placed host
    (ADR-013).
  - Diagonal panels: stepped 5-slice collision approximation; band
    faces rendered double-sided, corner strips as AABB boxes.
    Documented approximation, not exact 45-degree collision.
- Consequences: 13 registered shapes; remaining catalog families
  (corner slopes, doors, fence gates, slices, functional
  attachments, specials, multipart) stay in issue 12 as backlog.
  Pack verification is owed on all 13 before any compatibility claim.

## ADR-014: Blank item economy - paint lives on the world block only

- Status: accepted by owner directive ("remove the idea of having the
  contour block with texture in inventory; you only have them without
  texture and when breaking a contour block you get them both back").
- Decision: shape items are ALWAYS blank. Nothing may write
  `contour_material`/`contour_paid` onto an inventory stack: no
  sneak-copy gesture, no pick-block payload, no codex template, no
  drop-carried ledger.
  - Break / sneak-wrench pickup / placer swoop share `ledgerDrops`:
    `hostUnits` blank shape items + `paidUnits` material items.
  - The paid ledger stays on the world block entity only: it records
    whether the applied paint was charged, which drives the refund.
  - Codex emits blank shapes only; its material strip is removed.
  - Placement still resolves paint from offhand then clicked-block
    look (escrowed, charged once); painting a placed shape with a held
    BlockItem via MaterialPolicy is unchanged.
  - Legacy stacks carrying a `contour_*` payload remain parseable and
    their escrow placement paths are retained so old saves neither
    crash nor silently void paid credit; nothing mints new ones.
- Consequences: ADR-009's TEMPLATE/PREPAID item states are dead;
  `sampleOrPaint` and the item-side sneak gestures are removed; the
  Contour Placer consumes `contour_blank` feedstock and returns
  feedstock + material on swoop.

## ADR-015: Sliding-door motion is a state toggle, never a sweep

- Context: issue-13 family B (sliding/folding doors) needs a motion
  contract before code. Two options were weighed: physically swept
  motion (a moving contraption or per-tick animated sub-block) vs an
  instant OPEN/CLOSED state swap whose leaf renders translated along
  the frame.
- Decision: sliding and folding doors are state toggles. OPEN and
  CLOSED are distinct baked states whose `boxesFor` geometry places
  the leaf along the track/pocket instead of across the opening, and
  the swap is instantaneous - exactly how the hinged door already
  swaps its leaf onto the hinge side. No per-tick animation, no
  moving block entity, no intermediate collision states.
- Rationale: every openable in the catalog already toggles
  instantaneously, so pathing, redstone edge semantics, ledger and
  payload rules, drops, rotate/mirror and schematic transforms are
  inherited unchanged. A physically swept variant would need an
  entirely new BE/motion architecture for no builder-visible gain at
  block scale; sliding doorways read as "leaf in the pocket" either
  way between two clicks.
- Consequences: `contour_sliding_door` subclasses the hinged door
  and differs only in `boxesFor` (open leaf slides along the wall
  plane instead of pivoting); `contour_folding_door` follows the same
  contract stacking the leaf against the jamb. Issue-13 family B
  acceptance contract: ledger identical to hinged door, canonical-
  segment drops, same interactive test suite.
- Folding remains optional under the same contract; sliding ships
  first.

## ADR-016: Multipart occupancy is slot-based, not free geometry

- Context: issue-13 family A (true multipart panels) needs an
  occupancy model. Options: free-form occupant boxes with arbitrary
  overlap tests vs a fixed slot grid whose cells either merge or
  reject.
- Decision: occupants live on a fixed slot grid - the 24 byte-panel
  slots (6 faces x 4 quadrants). Panel-family members declare a slot
  footprint: `byte_panel` = 1 slot, `half_panel` = the 2 adjacent
  slots on its face half, `panel` = all 4 on its face, `flat_pane`
  = a full-face sheet that rejects all other slots on that face.
  A new occupant is accepted iff its slot set is disjoint from all
  existing occupants'. Everything else (non-panel shapes, full
  blocks) remains strictly one-occupant-per-cell.
- Representation: `ContourMultipartBlock` + `ContourMultipartBlockEntity`
  carrying an occupant list (slot id, panel kind, material,
  paidUnits) instead of blockstate combinations - 24^N states are
  not representable. The blockstate carries WATERLOGGED only.
  Placement raycasts into the cell to resolve the clicked
  face+quadrant; conflicts reject and fall through to the adjacent
  cell like every other merge. Wrench hits the targeted occupant.
- Ledger/drops: each occupant keeps its own paidUnits; breaking
  drops one blank host per occupant + summed material via the same
  `ledgerDrops` path. Schematic serializes the occupant list through
  the existing writeSafe/read NBT contract.
- Consequences: this closes the "boards/box" parity semantics - any
  combination of panel-family plates in one cell - without a new
  render architecture (the model emits one box per occupied slot
  through the existing ContourBoxModel path via BE model data).
  Catwalk and all non-panel shapes stay single-occupant.

## Open decisions (tracked, not resolved)

| ID | Question | Where resolved |
| --- | --- | --- |
| OD-1 | ~~6.0.10 Maven coordinate~~ RESOLVED 2026-11-03: `create-1.21.1:6.0.10-280` - manifest Git-Hash matches the recorded source commit (compile identity). Jar bytes differ from pack cache; cause uninvestigated - pack runtime uses the exact recorded jar + hash or provider-verified artifact | - |
| OD-2 | ~~Registrate +62 vs +67~~ RESOLVED 2026-11-03 (technical deviation, not owner-approved): +62 absent from tterrag maven; Create bundles +67 `[+67,)`; legacy uses REQUIRED +67. Ponder 1.0.66 kept as explicit legacy runtime pin | - |
| OD-3 | ~~Package root~~ RESOLVED: `net.createcontour` provisional (ADR-010) | - |
| OD-4 | ~~Sneak gesture disambiguation~~ RESOLVED: gestures disjoint, no modifier (ADR-010) | - |
| OD-5 | Publication target(s) and timing | Owner only |
| OD-6 | ~~RC2 pack profile tuple~~ RESOLVED: settled inventory in Compatibility.md; residual - live-server deployed tuple unverified (issue 13 captures effective runtime) | Issue 13 |
