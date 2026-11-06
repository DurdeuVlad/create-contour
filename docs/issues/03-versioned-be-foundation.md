# Issue 03: Safe material model and own versioned block entity foundation

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M1 |
| Type | feature |
| Suggested labels | `blockentity`, `rendering`, `safety` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Build the shared foundation every shape uses: our own registered block
entity type with versioned NBT, plus a material/render boundary that fails
safely to the original neutral shape model in every bad-input case.

## Expectation

- `ContourBlockEntity extends CopycatBlockEntity`, registered as our own
  `BlockEntityType`; blocks override the BE type/class getters (the
  `IBE<T>` generic seam is verified in implementation, not assumed).
- NBT schema v1 written explicitly; missing version migrates 0 -> 1;
  malformed/partial tags and unknown future versions degrade safely.
- Standard `SmartBlockEntity` update packet is reused; custom networking
  only if this issue produces concrete proof it is insufficient.
- Render fallback is OUR neutral model, not Create's `COPYCAT_BASE`.

## Acceptance criteria

1. Own `BlockEntityType` registered; at runtime the placed BE is a
   `ContourBlockEntity`, not Create's `COPYCAT` type (asserted in a test
   or logged verification).
2. NBT roundtrip: version written; missing version treated as 0 and
   migrated to 1; partial/malformed tags, `Material` absent, air or
   removed/unregistered material id -> neutral fallback, no crash, no
   corrupt save.
3. Future-version NBT (e.g., version 99 fixture) PRESERVES the raw
   unknown fields and the version on save - no silent downconvert that
   loses future data. Where preservation is impossible, non-destructive
   rejection/quarantine; never a best-effort known-fields load (ADR-006).
4. Regression test: under consumption-off the paid ledger (`Item` tag)
   is legitimately empty while `Material` is set. Base `read` validates
   material via `getAcceptedBlockState(consumedItem)` and ERASES the
   material when `Item` is empty (empty stack -> null acceptance ->
   reset to `COPYCAT_BASE`). Our overridden read must keep the material
   surviving load with an empty paid ledger.
5. Recursive copycat material rejected: our block as material inside our
   block, and Create copycat material, both fail safely.
6. Real client render safety: empty `ModelData`, null `ModelData`, item
   (inventory) render with null state, no material set, particle icon,
   and a material model that throws inside the cropped-quad path - each
   renders the original neutral shape; log shows no chunk-mesh crash.
7. Failure handling is narrow (material boundary only); no blanket
   `catch (Throwable)` swallowing VM errors.
8. Model cache stays correct across a resource reload; no stale material
   quads or meshing errors.
9. `getModelData` supplies the material property consumed by the model
   boundary.
10. Data ownership (ADR-009): paid material unit count is stored
    independently of base `consumedItem` (base `setConsumedItem` clamps
    to `copyWithCount(1)` and cannot represent a double's two paid
    units); own `getRequiredItems`/`read`/`write`/`writeSafe`/drop
    overrides exist and are tested. Exact key names may defer; ownership
    does not.

## Context the code cannot infer

- Pinned 6.0.8-168 source findings (docs/Research.md): `material` and
  `consumedItem` are private; base `read` returns early clearing
  `consumedItem` when `Material` is absent, and erases MATERIAL when the
  (possibly empty) `consumedItem` fails `getAcceptedBlockState`
  validation; `getMaterial(ModelData)` defaults to `COPYCAT_BASE`;
  occlusion/wrapped-data properties are private; `getCroppedQuads` is
  abstract.
- Two-client sync proof is deferred to issues 08/09 - do not claim it here.

## Scope

### In scope

- BE subclass + type registration, versioned read/write + migration,
  read-override for accounting, acceptance rejection of recursive/invalid
  materials, model fallback boundary, render safety cases.

### Non-goals

- No PRODUCTION shape blocks (issues 04-06). A TEST-ONLY registered
  fixture block IS explicitly allowed and expected so the render-safety
  and NBT ACs above can produce real screenshots/fixtures; it must be
  clearly named/test-scoped, ship no production recipe/loot/lang, and
  must not smuggle a real shape in early.
- No wrench/pick-block behavior changes (issue 07).
- No two-client/dedicated-server proof (issues 08/09).

## Dependencies and open decisions

- Depends on: 01 (build), 02 (neutral assets).
- Feeds: 04-06, 07.
- Open: `IBE` generic seam verification; whether standard sync suffices
  for stored-material item payloads (else documented proof for custom
  packet).

## Verification

- L2 tests for NBT/migration/rejection cases; L3 real-client screenshots +
  logs for render safety and reload cases; each case recorded per
  docs/Testing.md template.

## PR contract

Full restatement per `.github/pull_request_template.md`.
