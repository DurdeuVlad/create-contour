# Compatibility - Rustic Craft 2 pack profile

This document separates **manifest-declared facts** (settled, read-only
evidence) from **actual-tested results**. Version-matched development
proof exists on the minimal profile (18/18 GameTests, 4292/4292 client
assertions, both-profile builds); that proof does NOT establish the full
RC2 pack tuple. Isolated baseline execution now reaches the server's Done
line, but the strict client crashes in CustomSkinLoader before Contour is
installed. A modified diagnostic profile joined locally; it is not an
accepted baseline. The separately authorized diagnostic addon profile now
starts the server and joins one client; native render/paint evidence is
not accepted yet. Strict addon pack checks remain NOT RUN. See
[Pack-Baseline-Assessment.md](evidence/Pack-Baseline-Assessment.md) and
[Pack-Diagnostic-Assessment.md](evidence/Pack-Diagnostic-Assessment.md).

## Snapshot provenance

| Field | Value |
| --- | --- |
| Pack repo | `C:\Users\Vlad\Documents\Github\rustic-craft-2-modpack` |
| Branch | `chore/versioning-rule-release` |
| HEAD | `637cd3ce6f5a2c10358217c21eca0dbd22d497a8` |
| Working tree | clean before and after the read-only inventory |
| Read date | 2026-11-02 |
| Access mode | read-only; no pack files were or may be modified |
| Declared pack | RusticCraft2, version 2.2.2, stable channel, MC 1.21.1, NeoForge 21.1.248 |

Caveat recorded from the pack's own catalog README: the catalog metadata is
the PLAYER pack manifest, not a live server export. The deployed server
tuple is therefore unverified; the pack test (issue 13) captures the
effective loaded mods/configs per client and server at runtime instead of
asserting them.

## Declared mod profile relevant to this addon

From `catalog/mods.json` (CurseForge project/file ids as evidence):

| Mod | Version | Side | Notes |
| --- | --- | --- | --- |
| Create | create-1.21.1-6.0.10.jar | both | project 328085, file 7963363 |
| Connector | 2.0.0-beta.17+1.21.1-full | client | project 890127, file 8654089 |
| ConnectorExtras | 1.12.1+1.21.1 | client | - |
| Sodium | 0.8.13+mc1.21.1 | client | renderer; must NOT be disabled to claim compat |
| Iris | 1.8.14-beta.1+mc1.21.1 | client | shader loader; same rule |
| JEI | 19.57.0.449 | client | optional for us; present in pack |
| ModernFix | 5.27.24 | both | perf |
| FerriteCore | 7.0.3 | both | perf |
| Athena | 4.0.6 | both | CTM library |
| Create Connected | 1.3.3 | both | adjacent Create-addon geometry risk surface |
| Create Aeronautics | bundled 1.3.2 | both | contraption/physics risk surface |
| Create Threaded Trains | 1.0.0 | both | train behavior risk surface |
| KubeJS | 2101.7.2-build.377 | both | script layer |
| KubeJS Create | 2101.3.1-build.18 | both | Create recipe integration |
| Create Schematic Checker | 1.21.27-6.0-neoforge | server | catalog entry explicitly `unverified:true` |
| Architectury | 13.0.11 | both | pack dependency of other mods; does NOT authorize Architectury for this addon |
| Excalibur_Create-6 Addon_Forge | v2.31 zip | client resource pack | affects render appearance; inspect, never copy |

Shader catalog (`catalog/shaders.json`): SolasShaderV3.7b zip, project
678384, file 8713668. Render profiles therefore cover BOTH shaders-off and
Solas-on. No `catalog/bundled-mods.json` or `server-unbundled.json` exists
in the pack repo at snapshot (pack rules reference them generically).

## Create 6.0.10 jar evidence

| Field | Value |
| --- | --- |
| Cached jar locations | `build\beta3-server-test\server\mods`, `dist\test_server_runtime\mods` (pack repo) |
| SHA-256 (both copies identical) | `EF87FE5709F1BA1F5B8BB20A2925B5AFB4669E178FD6D8BF10C167759EEFE37A` |
| Manifest Git-Hash | `ac0c444d9828da3453ae8cc65338e8de063286fb` (= official `mc1.21.1-6.0.10` tag SHA) |
| Manifest timestamp | `2026-04-21T22:19:52+0000` |
| Manifest Implementation-Version | `6.0.10` (no Maven build suffix) |
| `META-INF/neoforge.mods.toml` requires | MC `[1.21.1]`, NeoForge `[21.1.219,)`, Ponder `[1.0.82,)` |
| jarjar bundled | Registrate MC1.21-1.3.0+67, Flywheel 1.0.6, Ponder 1.0.82+mc1.21.1 |

Caveat: the cached jar is a strong candidate but has NOT been demonstrated
to match a live server or provider-file hash. UPDATE 2026-11-03: official
Maven `create-1.21.1:6.0.10-280` verified with manifest Git-Hash
`ac0c444d9828da3453ae8cc65338e8de063286fb` (= the tag SHA recorded above)
and used as the pack-profile pin; its jar SHA-256 `192c7b0b...87a7`
shows DIFFERENT artifact bytes from the pack cache `EF87FE...` - cause
uninvestigated, the source-commit label matches. The full-pack runtime
must use the exact recorded pack jar + hash or a
provider-identity-verified artifact.

## Profile discrepancy table (requested vs declared)

| Component | Prompt-requested | Pack-declared / jar-required | Resolution |
| --- | --- | --- | --- |
| NeoForge | 21.1.213 | pack declares 21.1.248; jar requires >= 21.1.219 | INVALID for 6.0.10. Two planned target profiles: primary pack NF 21.1.248; legacy NF 21.1.213 only with Create 6.0.8-168 |
| Create | 6.0.8-168 + 6.0.10 | pack declares 6.0.10 | Both planned target profiles kept; pack is primary target |
| Ponder | 1.0.66 | jar requires >= 1.0.82; bundled 1.0.82 | INVALID for 6.0.10; legacy only |
| Registrate | MC1.21-1.3.0+62 (prompt request) | bundled +67 | Settled deviation: official Create 6.0.8 declares +67, not +62; both profiles resolve +67. Requested-vs-resolved difference recorded, not erased |
| Flywheel | 1.0.5 | bundled 1.0.6 | pack profile targets bundled 1.0.6 |
| JEI | 19.21.0.247 | pack 19.57.0.449 | optional; test against pack's 19.57.0.449 |

## Declared pack config relevant to testing

| Source | Setting | Implication |
| --- | --- | --- |
| `overrides/server/config/flywheel-client.toml` | backend DEFAULT, workerThreads -1, lightSmoothness SMOOTH | baseline render path; do not change to pass tests |
| `create-client` config | `explainRenderErrors=false` | may be enabled only inside the disposable test copy if needed |
| `create-server` config | `maxBlocksMoved=2048` | test fixtures must stay within this limit |
| `overrides/common/kubejs/server_scripts/create_netherless_pyro.js` | rewrites Create processing/recipe chains | addon recipe/tag compatibility and JEI visibility must be verified IN pack; all addon recipes namespaced `create_contour`; existing Create recipes must not break |
| Server-side config files | declared only | do not prove client-effective settings; issue 13 captures effective loaded mods/configs per client at runtime |

## Isolation and reproducibility rules (binding)

1. Pack tests run ONLY on isolated copies of the pack client and server
   runtime. The original modpack repo, its tracked worlds, and any live
   server are never mutated.
2. Isolated copies use proper side resolution (client-only vs both vs
   server mods). Verify the permitted Create artifact against the snapshot
   above. Under owner-approved ADR-011, competing runtime jars are copied
   and loaded only as black boxes: no hashes or byte-level comparisons.
   Record catalog identity and loaded versions instead; never describe
   this as whole-runtime byte-hash verification.
3. The addon jar is added on top of the verified copy; a pack-boot
   baseline WITHOUT the addon is recorded first - baseline failures are
   documented, never blamed on the addon and never waived.
4. Any live or external server action requires explicit owner permission
   recorded in the evidence.
5. Compatibility claims come from observed runs (issue 13), never from
   metadata alone.

Strict pre-addon client compatibility remains BLOCKED in CustomSkinLoader.
The instrumented diagnostic (real client raycast/use, not OS input) now
passes its bounded matrix on the fixed jar: 18 creative shape/material
paints plus 3 survival consumption cases, including leaves on all shapes.
An earlier collision-based leaf rejection was traced, fixed and re-verified.
A same-world save/reload check also passes (material + paid ledger persist),
and a same-host two-client check shows the second client observing identical
block-entity state plus a live cross-client repaint. A native place/break/
drop trace passes with a correct tool: the pack denies bare-hand drops for
`mineable/pickaxe` blocks uniformly (identical `create:andesite_casing`
control), and pickaxe breaks emit the ledger-carrying shape stack.
Trains, schematics, wrench cycles, and full renderer/resource profiles are
not cleared. See
[Pack-Diagnostic-Assessment.md](evidence/Pack-Diagnostic-Assessment.md).

## Risk surfaces -> acceptance cases

Every declared risk surface maps to a required case in
[issue 13](issues/13-pack-compatibility.md): Connector/ConnectorExtras
presence, Sodium+Iris renderers with shaders off and Solas on, JEI 19.57
visibility, KubeJS recipe regressions, Threaded Trains train runs,
Create Connected adjacent geometry, Aeronautics assembly/render/
disassembly, Schematic Checker allow-list/item requirements (or explicit
blocker), Excalibur resource pack appearance across resource reloads,
world save/reload.
