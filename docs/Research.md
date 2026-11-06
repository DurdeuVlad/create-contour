# Research - Create: Contour

Settled findings as of 2026-11-02. Facts below are observed evidence, not
assumptions; contradictions and unresolved items are called out explicitly.

## Pinned Create source reading (authoritative API evidence)

Source: `create-1.21.1-6.0.8-168-sources.jar` from
<https://maven.createmod.net/com/simibubi/create/create-1.21.1/6.0.8-168/>.
Four classes read in full (package `com.simibubi.create.content.decoration.copycat`):

| Class | Key observations |
| --- | --- |
| `CopycatBlock` | `abstract`, extends `Block`, implements `IBE<CopycatBlockEntity>` + `IWrenchable`. `getBlockEntityType()` returns `AllBlockEntityTypes.COPYCAT` (we must override). `useItemOn`: `getAcceptedBlockState` -> `prepareMaterial` -> set material + `setConsumedItem(stack)`, `shrink(1)` unless creative; same-block material triggers `cycleMaterial()` on the material state. `setPlacedBy` auto-applies the OFFHAND item as material (upstream already implements the offhand precedence tier). `onWrenched` refunds `consumedItem` to inventory and resets to `COPYCAT_BASE`; `onSneakWrenched` calls `onWrenched` first. `getCloneItemStack` returns `new ItemStack(this)` only when material is `COPYCAT_BASE` or player sneaks - otherwise returns the MATERIAL item. `onRemove` pops `consumedItem`; `playerWillDestroy` clears it for creative. Delegates sound/friction/light/harvest/explosion/particles/tint (`WrappedBlockColor`) to material. `getAcceptedBlockState` rejects: non-BlockItem, `CopycatBlock` (recursion), `COPYCAT_DENY` tag, `EntityBlock`, `StairBlock`, and non-full-bounds/empty-collision shapes (when level available) - unless `AllBlockTags.COPYCAT_ALLOW` or `isAcceptedRegardless` override. Applies FACING/HORIZONTAL_FACING/AXIS/HORIZONTAL_AXIS from the clicked face. |
| `CopycatBlockEntity` | Extends `SmartBlockEntity`; implements `SpecialBlockEntityItemRequirement`, `TransformableBlockEntity`, `PartialSafeNBT`. `material` and `consumedItem` are PRIVATE; constructor defaults material to `COPYCAT_BASE`. `setMaterial` unifies with matching neighbour copycats and assumes `level` non-null. `read` parses `consumedItem` (`Item` tag), returns early clearing it when `Material` tag absent; server-side validates material against `getAcceptedBlockState` and on rejection resets material AND clears `consumedItem` (the base behavior our config-off accounting must work around). `getRequiredItems` -> `ItemRequirement(CONSUME, consumedItem)` for schematics. `transform` applies `StructureTransform` to material. `writeSafe` saves `consumedItem` with `DataComponentPatch.EMPTY` (strips components). `getModelData` exposes `CopycatModel.MATERIAL_PROPERTY`. `cycleMaterial` order: trapdoor HALF (open) -> FACING -> HORIZONTAL_FACING -> AXIS -> HORIZONTAL_AXIS -> LIT -> RoseQuartzLamp POWERING. |
| `CopycatModel` | `abstract`, extends `BakedModelWrapperWithData`. `MATERIAL_PROPERTY` public; `OCCLUSION_PROPERTY` and `WRAPPED_DATA_PROPERTY` private. `getMaterial(ModelData)` returns `COPYCAT_BASE` when null/absent - NOT our own neutral texture. `getQuads` handles occlusion, render-type matching, and `shouldFaceAlwaysRender` (Rubidium-compat notes in source); delegates to abstract `getCroppedQuads`. `getParticleIcon` delegates to material model. |
| `CopycatStepModel` | Concrete cropped-quad example: `getCroppedQuads` crops the material model's quads into 4 pieces (top x front) using `BakedModelHelper.cropAndMove` + `cloneWithCustomGeometry`, driven by `FACING` + `HALF` state properties. This is the adaptation pattern for our shapes. |

Implications (carried into Architecture.md and issue drafts):

- Own `BlockEntityType` + `ContourBlockEntity` subclass is required; verify
  the `IBE<T>` generic seam when implementing.
- Render guarding must cover: null item/inventory state, empty/null
  `ModelData`, missing material, air/unregistered ids, material-model
  exceptions, and resource reload - handled at the narrow material boundary,
  never a blanket `catch (Throwable)`.
- Slab/stair materials and slab/stair SHAPES need documented
  `getAcceptedBlockState` acceptance overrides and per-shape cropped-quad
  geometry adaptation.
- Default wrench strips material; default pick-block returns the material
  item. Neither meets the QoL contract (ADR-003 overrides).

## Version facts and contradictions

Two planned target profiles were settled after reading the RC2 pack manifests
and the cached Create 6.0.10 jar (full evidence:
[Compatibility.md](Compatibility.md)):

| Item | Requested | Observed | Resolution |
| --- | --- | --- | --- |
| Create | 6.0.8-168 + compile/runtime on 6.0.10 | 6.0.8-168 exists; maven metadata lists 6.0.10 builds 217..281; RESOLVED 2026-11-03: official Maven `6.0.10-280` manifest Git-Hash `ac0c444d9828da3453ae8cc65338e8de063286fb` (= tag). Maven jar SHA-256 `192c7b0b...87a7` differs in bytes from pack cache `EF87FE...`; cause uninvestigated, source-commit label matches | Two profiles; pack pins `create-1.21.1:6.0.10-280` |
| NeoForge | 21.1.213 | Pack declares 21.1.248; 6.0.10 jar `neoforge.mods.toml` requires NF `[21.1.219,)`; tag props: 6.0.8 -> 21.1.200, 6.0.10 -> 21.1.219 | 21.1.213 + 6.0.10 is INVALID (jar-declared minimum). Primary pack profile NF 21.1.248; NF 21.1.213 only on legacy 6.0.8-168 profile |
| Ponder | 1.0.66 | 6.0.10 jar requires `[1.0.82,)` and bundles 1.0.82; 6.0.8-168 POM: 1.0.64 | INVALID for 6.0.10; legacy only |
| Registrate | MC1.21-1.3.0+62 | Confirmed 2026-11-03: +62 jar 404 on maven.tterrag.com (literal + %2B); artifact metadata lists no MC1.21 versions; both Create jars bundle +67 `[+67,)` | OD-2 resolved 2026-11-03 (technical deviation, not owner-approved): +62 absent from tterrag maven and Create bundles +67 `[+67,)` -> legacy uses REQUIRED +67 via JarJar; requested Ponder 1.0.66 kept as explicit legacy runtime pin `Ponder-NeoForge-1.21.1:1.0.66` (verified resolvable) |
| Flywheel | 1.0.5 | 6.0.8 tag: 1.0.5; 6.0.10 bundles 1.0.6 | pack profile uses bundled 1.0.6 |
| Java | 21 | Consistent with NF 21.1.x | Use 21 |
| JEI | 19.21.0.247, soft | Pack catalog declares JEI 19.57.0.449 | Optional; test against pack version |

Other constraints recorded:

- The 6.0.8-168 POM lists many NON-REQUIRED integrations exposed by the
  upstream dev POM (FTB, JourneyMap, Architectury, and more). These are
  dev-time entries visible in the published POM - not Maven
  `optional=true`-flagged dependencies - and must not be treated as
  required runtime metadata. Do not blindly resolve transitives; declare
  the minimal explicit runtime set - Create plus its true required
  dependencies.
  The pack's own Architectury 13.0.11 is a dependency of OTHER mods and does
  not authorize Architectury for this addon (ADR-002 stands).
- Current Create dev branch is 6.0.11 - do NOT use it as pinned API
  evidence. Tags used: `mc1.21.1-6.0.8` (SHA 5f1b407a97467157e7a0da0c560c7d1d5f378929),
  `mc1.21.1-6.0.10` (SHA ac0c444d9828da3453ae8cc65338e8de063286fb).
- Do not silently downgrade the pack to older prompt versions, and do not
  publish support claims from metadata alone - only observed runs count.

## Rustic Craft 2 pack inventory

Settled from the lead's read-only manifest/catalog read at pack HEAD
`637cd3ce6f5a2c10358217c21eca0dbd22d497a8` (branch
`chore/versioning-rule-release`, tree clean). Full tables, jar hash
evidence, config surfaces, and isolation rules live in
[Compatibility.md](Compatibility.md). Headline facts: RusticCraft2 2.2.2
stable, MC 1.21.1, NF 21.1.248, Create 6.0.10; risk surfaces include
Sodium/Iris/Solas shaders, Connector, JEI 19.57, KubeJS recipe rewrites,
Create Connected, Aeronautics, Threaded Trains, and an explicitly
`unverified:true` Schematic Checker entry - each becomes a required
acceptance case in issue 13, none become hard dependencies.

## Build tooling

- Chosen: ModDevGradle, single NeoForge project (ADR-002). Reference: official
  README <https://github.com/neoforged/ModDevGradle>.
- Licensing: Create code MIT, assets ARR
  (<https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md>);
  all our assets original (ADR-005).

## Name check (bounded)

Checked 2026-11-02. Conclusion: no EXACT "Create: Contour" /
`create_contour` / `create-contour` addon found on the checked surfaces.
This is a bounded negative finding - not a uniqueness guarantee, legal
clearance, or reservation.

| Surface | Query | Result |
| --- | --- | --- |
| Modrinth API search | "Create Contour", `create_contour`, `create-contour` | total_hits = 0 |
| Modrinth API project | `/v2/project/create-contour`, `/create_contour` | 404 |
| Modrinth | bare `contour` | TAKEN: <https://modrinth.com/mod/contour> - "Lights on! Visible players and armor stands get the spotlight treatment." (source URL metadata only: github.com/MinchexBG/Contour; no code read) |
| Modrinth vicinity | Contour Shaders, Crisp Contours | Nearby names exist; reinforces never using bare `contour` |
| GitHub repo search | "Create Contour" minecraft | 0 exact matches; unrelated: Justinius/CreateContour (C#, 2017), keplergt/CreateContour (Python CSV contours, 2017), ducitman/AutoCreateContourSPEC (unrelated, uninspected) |
| Web search | exact addon name | No exact addon found |
| CurseForge | `/minecraft/mc-mods/create-contour`, `create_contour` | HTTP 403 - direct check UNSUPPORTED; remains a pre-publication check |
| CurseForge indexed | "contour" projects | Other contour projects exist, no exact match confirmed |

## Open unknowns (do not pretend resolved)

1. ~~Exact Maven coordinate for Create 6.0.10~~ RESOLVED (OD-1):
   `create-1.21.1:6.0.10-280`, Git-Hash `ac0c444d` verified in manifest
   and in-game log. Note: maven jar bytes DIFFER from the pack cache jar;
   cause uninvestigated; full-pack runtime must use the exact recorded
   pack jar + hash or a provider-identity-verified artifact.
2. ~~Registrate +62~~ RESOLVED (OD-2, technical deviation): +62
   unsatisfiable; legacy uses required +67 via JarJar; Ponder 1.0.66
   declared explicitly as the legacy runtime pin.
3. `IBE` generic seam for a BE subclass - verify in issue 03 implementation.
4. CurseForge name availability - direct check unsupported (403); rerun
   before any publication.
5. Whether the cached 6.0.10 jar hash matches the deployed live server -
   catalog is the player pack, not a server export; issue 13 captures the
   effective runtime instead of asserting it.
6. Every pack interaction (renderer, Connector, KubeJS, Connected,
   Aeronautics, Threaded Trains, Schematic Checker) - declared only;
   actual-tested results are all NOT RUN until issue 13 executes.
