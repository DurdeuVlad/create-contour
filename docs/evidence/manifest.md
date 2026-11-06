# Evidence manifest

Index of recorded verification evidence. Raw logs live in `logs/` (ignored
by git; kept on disk). The current checkpoint is a private three-shape
prototype, not a completed full-pack/end-to-end acceptance gate.

## Current prototype status (authoritative index)

| Check | Command/action | Result | Evidence |
| --- | --- | --- | --- |
| Dedicated GameTests | `./gradlew runGameTestServer` | 23 required tests passed, including accounting and noncolliding-visual-material regressions | `logs/gametest-leaf-green.log` |
| Client smoke | `./gradlew runClientQA` | 4292 assertions passed, 0 failed; assertions are not independent product features | `logs/runclient-qa-leaf.log` |
| Builds and headless tests | `./gradlew build -PcompatProfile=pack` and `-PcompatProfile=legacy` | Both builds successful; 4 logic/math JUnit tests per profile | `logs/build-leaf-pack.log`, `logs/build-leaf-legacy.log`, `headless-proof-pack.xml`, `headless-proof-legacy.xml` |
| Pack-target artifact | Built jar, inventory inspected | `build/libs/pack/create_contour-0.1.0.jar`; this is a version target, not full-pack runtime proof | `logs/jar-contents-proof.log` |
| Legacy artifact | Built jar, inventory inspected | `build/libs/legacy/create_contour-0.1.0-legacy.jar`; legacy client/server runtime NOT RUN | `logs/jar-contents-proof.log` |
| Native repaint/light | Right-click stone then glowstone without reload, on slab, vertical slab and stairs | All six images visually inspected: immediate material change and light-pool change | `screenshots/qa-live-sync-stone.png`, `screenshots/qa-live-sync-glow.png`, `screenshots/qa-live-vertical-stone.png`, `screenshots/qa-live-vertical-glow.png`, `screenshots/qa-live-stairs-stone.png`, `screenshots/qa-live-stairs-glow.png`; server queries in `logs/client-native-journey.log` for vertical slab/stairs |
| Matrix and item appearance | Real development client | Neutral, axis-sensitive logs, glass, leaves, isolated Create casing and painted items inspected | `screenshots/qa-slab-material-row.png`, `screenshots/qa-vertical-slab-material-row.png`, `screenshots/qa-stairs-material-row.png`, `screenshots/qa-painted-items-transform-fixed.png` |
| Resource reload | F3+T in the real development client | Material/item appearance retained | `screenshots/qa-resource-reload-items.png` |

See [QA-Assessment.md](QA-Assessment.md) for the bounded verdict, environment
limits and remaining gates. The client runs development classes; inspection
of a built jar is not proof that the packaged jar has run in the full pack.

## Historical source snapshots

Latest normal-artifact identities: `logs/contour-jar-sha256-leaf.txt`
(pack dd827df1..., legacy a20bafae...). The physical-collision regression
was red in `logs/gametest-leaf-red.log` before the minimal outline-based
fix (`logs/materialsafety-leaf-fix.diff`). Fixed-jar pack verification is
pending; earlier runtime evidence belongs to its recorded older jars.

The dated batches below record earlier source states. Their old hashes,
commands, warnings and limitations do not describe the current artifact.
Read the current index above first; historical bootstrap logs are not
substitutes for its current proof.

## 2026-11-03 - Issue 01 bootstrap (pack profile)

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| Java toolchain | `java -version`, `javac -version` | OpenJDK Temurin 21.0.12+8, javac 21.0.12 (`C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot`) | terminal |
| Gradle wrapper | `./gradlew --version` | Gradle 8.14.6 (rev 88c80c14), Kotlin 2.0.21, on JDK 21.0.12; dist checksum validated by `distributionSha256Sum` | `logs/gradlew-version.log` |
| Wrapper jar provenance | sha256sum | `gradle/wrapper/gradle-wrapper.jar` = `7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172` from official `gradle/gradle` repo tag `v8.14.6` (services `*-wrapper.jar` artifact is not published for 8.14.6 - NoSuchKey) | this file |
| Pack build | `./gradlew build` | BUILD SUCCESSFUL in 1m 47s (compatProfile=pack default) | `logs/build-pack.log` |
| Resolved deps (compile) | `./gradlew dependencies --configuration compileClasspath` | `com.simibubi.create:create-1.21.1:6.0.10-280` + `mezz.jei:jei-1.21.1-neoforge:19.57.0.449` (compileOnly); no FTB/JourneyMap/Architectury/dev-POM transitives | `logs/deps-compileClasspath.log` |
| Resolved deps (runtime) | `./gradlew dependencies --configuration runtimeClasspath` | Create `6.0.10-280` as our only declared runtime dep (JEI absent at runtime) plus expected NeoForge/Minecraft launcher libraries; no upstream non-required-integration transitives | `logs/deps-runtimeClasspath.log` |
| Profile guard | `./gradlew build -PcompatProfile=bogus` | BUILD FAILED: `Invalid -PcompatProfile='bogus'. Allowed values: pack, legacy.` | `logs/profile-invalid.log` |
| Built jar | `sha256sum`, `unzip -l` | `build/libs/create_contour-0.1.0.jar` SHA-256 `3347fdee247c70e5d10665d3e76a9bc8ec93d1946ee51bfae7aad752090af0aa`; contains only `META-INF/MANIFEST.MF`, `META-INF/neoforge.mods.toml`, `LICENSE`, `net/createcontour/CreateContour.class` - no bundled Create/assets | `logs/jar-sha256.log`, `logs/jar-contents.log` |
| Dedicated server smoke | `./gradlew runServer` (port 25599) | `Done (6.339s)!` - full boot. Mod list: Create 6.0.10, Create: Contour, Flywheel 1.0.6, Ponder 1.0.82+mc1.21.1, Minecraft 1.21.1, NeoForge 21.1.248. Log confirms `Create 6.0.10 initializing! Commit hash: ac0c444d9828da3453ae8cc65338e8de063286fb`. No client-class errors. No eula.txt required/written. Server stopped by us afterwards. | `logs/runserver-pack.log` |
| First server attempt | `./gradlew runServer` (default 25565) | FAILED TO BIND - port 25565 already LISTENING by pre-existing process PID 22480 (not ours, left untouched). Rerun on free port 25599. | `logs/runserver-portbind.log` |

## Resolved / verified dependency facts (2026-11-03)

| Fact | Evidence |
| --- | --- |
| OD-1 RESOLVED: official Maven `create-1.21.1:6.0.10-280` manifest `Git-Hash: ac0c444d9828da3453ae8cc65338e8de063286fb` = official `mc1.21.1-6.0.10` tag SHA (scanned manifests via HTTP range from build 281 down; 280 matched) | this file; `.cache/deps/` jars |
| Maven 6.0.10-280 jar SHA-256 `192c7b0be13f523dcec9a7a2393bbc2614c3c9bd3edb1f7720a97bcd4f0b87a7` - DIFFERENT artifact bytes from pack cache `EF87FE...`; cause uninvestigated; the source-commit label (manifest Git-Hash) matches. The full-pack runtime must use the exact recorded pack jar + hash or a provider-identity-verified artifact | `sha256sum` output |
| 6.0.10-280 jarjar bundles: Registrate `MC1.21-1.3.0+67` `[+67,)`, flywheel-neoforge `1.0.6` `[1.0.6,2.0)`, ponder `1.0.82+mc1.21.1` `[1.0.82+mc1.21.1,)`; mods.toml requires NF `[21.1.219,)`, MC `[1.21.1]` | extracted metadata |
| 6.0.8-168 jar sha256 `8c5e04ea9f6e37725af1596cdba16dd6ccb8bdffc35dafa47d357a897d2d4b10`, Git-Hash `5f1b407a...` (= tag); jarjar: Registrate `+67`, flywheel `1.0.5`, ponder `1.0.64`; requires NF `[21.1.200,)` | extracted metadata |
| OD-2 outcome: requested Registrate `MC1.21-1.3.0+62` - jar 404 on `maven.tterrag.com` (literal and `%2B`); artifact metadata lists no MC1.21 versions; Create bundles `+67` with range `[+67,)`. LEGACY exact tuple NOT satisfiable -> legacy profile BLOCKED (no pin change made) | curl status codes; metadata |
| JEI coordinates verified resolvable (HTTP 200): `jei-1.21.1-neoforge:19.57.0.449` (pack) and `19.21.0.247` (legacy) on `maven.blamejared.com` | curl status codes |

## 2026-11-03 (later same day) - Bootstrap corrections + legacy build

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| Pack rebuild | `./gradlew build` | BUILD SUCCESSFUL (compatProfile=pack) | `logs/build-pack-final.log` |
| Legacy build | `./gradlew build -PcompatProfile=legacy` | BUILD SUCCESSFUL in 1m22s | `logs/build-legacy.log` |
| Legacy runtime deps | `./gradlew dependencies --configuration runtimeClasspath -PcompatProfile=legacy` | `create-1.21.1:6.0.8-168` + explicit `Ponder-NeoForge-1.21.1:1.0.66` (Registrate +67 arrives via Create JarJar) | `logs/deps-runtimeClasspath-legacy.log` |
| Profile-separated artifacts | `sha256sum build/libs/*` | `create_contour-0.1.0.jar` (pack) `a833441bdb8fc50d18be5e347a744959a3a91aa52e0de4cf6610f21e8fa8ea1f`; `create_contour-0.1.0-legacy.jar` `5fdcade9b67e3f60ae3bdca05f9dc20d2ba26253c7859b88f7f0d61bd3cbe207` - cannot overwrite each other | `logs/jar-sha256-final.log` |
| Packaged mods.toml | `unzip -p` | version `0.1.0` (expanded via `${mod_version}`), minecraft `[1.21.1]` exact, license MIT, no issueTrackerURL, create `[6.0.8,6.1)` | `logs/packaged-toml.log` |
| Ponder 1.0.66 coordinate | curl HEAD | `net.createmod.ponder:Ponder-NeoForge-1.21.1:1.0.66` HTTP 200 on maven.createmod.net | terminal |
| Apache-2.0 license bundle | curl | `licenses/Gradle-Apache-2.0.txt` (202 lines, apache.org) for Gradle wrapper provenance | `docs/Provenance.md` |

## 2026-11-03 (batch 2) - First-three common/server pipeline

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| Pack build (shapes) | `./gradlew build` | BUILD SUCCESSFUL (after fixes: BE supplier signature, Ponder/Registrate compile API, Shapes import) | `logs/build-shapes-pack.log` |
| Legacy build (shapes) | `./gradlew build -PcompatProfile=legacy` | BUILD SUCCESSFUL in 13s (clearContent made portable - 6.0.8 CBE lacks it) | `logs/build-shapes-legacy.log` |
| Artifacts | `sha256sum`, `unzip -l` | pack `create_contour-0.1.0.jar` `0d274690...`, legacy `create_contour-0.1.0-legacy.jar` `0c23b843...`; jar packs all blockstates/models/texture/lang/recipes/loot/tags + code | `logs/jar-sha256-shapes.log` |
| Compile API additions | - | `ponder_coord_{pack,legacy}` compileOnly added (Create types implement Ponder interfaces); Registrate NOT needed - neutral resolved via `BuiltInRegistries` (`create:copycat_base`) | gradle.properties |

NOT RUN this batch: runtime loading, client rendering, GameTests, wrench
cycle, sneak-copy gestures, paid-ledger drop/pickup end-to-end.

## 2026-11-03 (batch 3) - QoL/accounting + test pass

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| Pack build + JUnit | `./gradlew build test` | BUILD SUCCESSFUL; HeadlessLogicTests 4/4 pass (cellMask cardinality/mirror-symmetry/facing geometry, ShapeGrid voxel) | `logs/build-test-pack.log` |
| Legacy build + tests | `./gradlew build -PcompatProfile=legacy` | BUILD SUCCESSFUL (tests also pass on legacy) | `logs/build-qol-legacy.log` |
| Server boot + datapack | `./gradlew runServer` (:25599) | `Done (3.247s)!`; mod list Create: Contour 0.1.0; **2819 recipes** (2816 baseline + our 3 = singular `recipe/` paths load); GameTest namespaces [] (not enabled on server run) | `logs/runserver-shapes.log` |
| GameTest execution | `./gradlew runGameTestServer` | **10/10 required tests passed** (521ms, real dedicated GameTestServer, `run-gametest/` dir): registryAndRecipes (3 blocks/BE + 3 recipe IDs + 3-tag server-loaded), stairsParity (all 40 states voxel+mirror+rotate vs vanilla oak_stairs oracle), stairsConnections (INNER form/unform), slabAndVerticalStates, bePersistence (full NBT roundtrip, axis, invalid->neutral+paid0, v0->v1, v99 frozen + writeSafe no-credit, paid clamp), materialAcceptance, paintEconomics (consume ON cost 1, missing-item atomic reject, repaint refund-after-consume, config-OFF paid 0, same-block re-axis free), wrenchCycleAndPickup (cycle preserves material/paid/facing/top, DOUBLE blocked), sneakTemplatePaintsTarget (one-click world paint, template kept), pickBlockClearsCredit (paid 0) | `logs/gametest-green.log` |
| GameTest harness notes | n/a | MDK-official `type = "gameTestServer"` run config (RunModel has no convenience method but accepts the type). Earlier "no run type" claim was WRONG and is corrected. `makeMockServerPlayerInLevel` unusable under Create (TrackGraphSync packet on playerLoggedIn) - tests use `makeMockPlayer`. `structures/`->`structure/` (1.21 singular); template NBT hand-generated gzipped (palette compound TAG_End + DataVersion required). `run-gametest/` gitignored | `logs/gametest-first-executed.log` (red->green trail preserved) |
| Datapack dirs | - | moved to singular 1.21.1 paths (recipe/, loot_table/blocks/, tags/block/, minecraft/tags/block/); vertical recipe pattern fixed to equal width | filesystem |
| Legal | - | licenses/Create-MIT.txt shipped in jar; Provenance.md records Mojang stair-semantics adaptation + PNG authorship | docs/Provenance.md |

NOT RUN this batch: GameTests (harness-blocked), client rendering/models,
vanilla-oracle stair parity in-world (authorial test registered only),
double-slab merge end-to-end in-world, prepaid drop/pickup cycles.

## 2026-11-03 (batch 4) - placement escrow + item-side sneak paths

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| GameTest execution | `./gradlew runGameTestServer` | **13/13 required tests passed** (582ms). New cases: `itemPlaceAccounting` (template cost 1 + frame consumed; stored>offhand>look priority; offhand-only payment; missing-item atomic FAIL; PREPAID transfer once; config-OFF paid 0), `doubleMergeLedger` (old paid 1 + template cost 1 = paid 2; getDrops splits 1+1; mixed-material merge rejected), `sneakItemUse` (ITEM.useOn sneak paints world target, held kept, 1 consumed; PREPAID resample reject; invalid paint no-ops; creative free+paid0) | `logs/gametest-placement.log` |
| Legacy compile | `./gradlew compileJava -PcompatProfile=legacy` | BUILD SUCCESSFUL | console |
| Focused diff | `git diff --no-index` vs `.cache/review-placement/` | 850-line patch of MaterialPolicy/ContourBlockItem/ContourBlock/ContourSlabBlock/ContourGameTests | `logs/placement-patch.diff` |
| Test-env findings | n/a | `makeMockPlayer` lacks `abilities.instabuild` (set explicitly); test anchor moved to (2,1,2) so all offsets stay inside the 5x5x5 structure (west(2)/north(2) were out-of-bounds, silently failing setBlock) | this file |

NOT RUN this batch: client render/models (deferred), future-version drop
capsule, in-world prepaid drop/pickup cycle beyond the double-merge drop
asserts.

## 2026-11-03 (batch 5) - reservation/pickup final-common fixes

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| GameTest execution | `./gradlew runGameTestServer` | **16/16 required tests passed** (716ms). New: `reservationRefundComponents` (component-preserving fragments, partial-stack reuse + drop-only-remainder, no duplication, one-shot refund), `mixedLedgerPickupAndReplace` (config-OFF merge keeps paid 1; sneak-wrench returns 2 items split 1+0 and clears world; prepaid re-place needs no material, paid 1 + canonical kept), `placementRollbackSnapshot` (failed placement leaves full frame payload/count untouched) | `logs/gametest-final-common.log` |
| Legacy compile | `./gradlew compileJava -PcompatProfile=legacy` | BUILD SUCCESSFUL | console |
| Focused diff | `git diff --no-index` vs `.cache/review-final-common/` | 692-line patch: MaterialPolicy (copyWithCount fragments before shrink, conservation-safe one-shot refund, paint uses Reservation), ContourBlockItem.place (context-resolved pos, full-frame rollback snapshot, canonical/top restore, frozen merge reject, dead conditions removed), ContourBlock (shared ledgerDrops for getDrops+onSneakWrenched, removeBlock-before-items, frozen pickup rejected, onWrenched rollback restores full BE NBT), ContourSlabBlock (sameFullState merge in canBeReplaced so mismatches fall to adjacent) | `logs/final-common-patch.diff` |

NOT RUN this batch: client render/models (next batch), future-version
drop capsule (frozen pickup is now explicitly REJECTED as the recorded
limitation), contraption/train interactions.

## 2026-11-03 (batch 6) - client render pipeline

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| Pack + legacy compile | `./gradlew compileJava` (both) | BUILD SUCCESSFUL both | console |
| GameTest regression | `./gradlew runGameTestServer` | **16/16 passed** - client package does not break the server path | `logs/gametest-render-batch.log` |
| Dev client launch | `./gradlew runClient` | Real MC client launched: texture atlases built, flywheel shaders + Create hats loaded; window captured via PowerShell screen grab | `logs/runclient.log`, `client-title.png` (507KB) |
| Client left running | - | MC dev client still up (java.exe PID ~31172, ~1.7GB) for next-proof inspection | this file |

New client package (`net.createcontour.client`): `ShapeQuads`
(2x2x2-grid face emission + affine UV projection preserving material
orientation), `ContourModel` (extends CopycatModel; own getQuads/
getRenderTypes/getParticleIcon/gatherModelData/getCroppedQuads; guarded
material lookup; neutral fallback), `ContourSlabModel`,
`ContourVerticalSlabModel`, `ContourStairsModel` (maskFor impls),
`ContourItemOverrides` (payload -> bound painted model), `ContourClient`
(ModifyBakingResult wrappers for all states + inventory models, block/
item colors, `create_contour.renderSmoke`-gated smoke). Diff:
`logs/render-patch.diff`.

NOT RUN this batch: in-world material-matrix screenshot (mc-pilot client
mod not injected into our dev client; PowerShell captures only the
window), real painted-shape-in-world visual proof, CTM/connected-texture
matrix, renderSmoke world-load path (needs a joined world).

## 2026-11-03 (batch 10) - reservation origin slots, frozen-view reset, QA hardening

| Check | Command | Result | Evidence |
| --- | --- | --- | --- |
| GameTest | `./gradlew runGameTestServer` | **18/18** (new `reservationRestoresOrigin`: main-slot / offhand / mutated-slot conservation; `bePersistence` strengthened: frozen operational reset over painted BE, whole-compound equality, fresh-BE clamp at different pos) | `logs/gametest-origin.log` |
| QA smoke | `./gradlew runClientQA` | **4292 passed / 0 failed** (fixture-wait bounded 200 ticks, throws if missing; live `handleUpdateTag` sync case asserts client aux 15/0 in-flight) | `logs/runclient-qa.log` (prior `runclient-qa-green-4291.log`) |
| Native live paint | in-world right-click stone/glowstone on vertical slab + stairs | screenshots captured + server `data get` confirms `glowstone` for both; **visual review by lead pending** | `screenshots/qa-live-{vertical,stairs}-{stone,glow}.png`, `run/logs/latest.log` |
| Builds | `./gradlew build` (both profiles) | BUILD SUCCESSFUL both | `logs/build-proof-{pack,legacy}.log`; jars `build/libs/{pack,legacy}/`; XML `headless-proof-*.xml`; inventory `logs/jar-contents-proof.log` |
| Reservation QoL | `MaterialPolicy.Reservation` | `Fragment(slot,stack)` restores origin slot first (empty or same-item-same-components, capped by `getMaxStackSize`), falls back to `add` then drops only true remainder | `logs/origin-patch.diff` (this batch) |

Matrix evidence (coords lead-verified): y=-60, x97 neutral, 100 stone,
103 oak_slab, 106 glass (cutout), 109 oak_log-X, 112 oak_leaves,
115 andesite_casing (CTM), 118 oak_log-Z, 121 white_stained_glass
(translucent); rows z100 slab / 104 vertical / 108 stairs; vanilla
references row z96. Canonical painted-items pass image:
`qa-painted-items-transform-fixed.png` (earlier `qa-painted-items-fixed.png`
was still gray - kept as diagnostic, not evidence).

NOT RUN (explicit): full RC2 baseline+addon client/server, Sodium/Iris/
Connector and Excalibur/Solas profiles, trains/contraptions, schematic
cannon requests, multiplayer, full survival break/drop cycle, Ponder
onboarding (not implemented), future-version drop capsule (frozen pickup
refused by design), CTM neighbor connectivity (only UV region + isolated
appearance verified).

## 2026-11-03 - isolated pack baseline, Contour absent

Verdict: strict client baseline **BLOCKED**. Server startup is observed,
with pre-existing resource/data errors. A patch-failure-bypass diagnostic
client joined locally, but is not an accepted strict baseline.

| Check | Result | Evidence |
| --- | --- | --- |
| Isolated production server, NeoForge 21.1.248 / exact cached Create 6.0.10 | Reaches Done on loopback 25610; errors recorded, not waived | `logs/pack-baseline-server-before-contour.log` |
| Strict production client retry, no Contour, no CSL bypass | CustomSkinLoader SkinManager transformation fails; corrected version placeholder does not resolve it | `logs/pack-baseline-client-strict.log`, `logs/pack-baseline-client-strict-crash.txt`, `logs/pack-baseline-client-strict-command.txt` |
| Modified diagnostic client with `-Dcustomskinloader.ignorePatchFailure=true` | Local join and voice handshake; skins not verified, no addon gameplay | `logs/pack-baseline-client.log`, `logs/pack-baseline-server.log` |
| Native screenshot review | Game Menu over a blurred joined world; not unobstructed render proof | `screenshots/pack-baseline-joined.png` |

Full scope, uncertainty, setup deviations and remaining gates:
[Pack-Baseline-Assessment.md](Pack-Baseline-Assessment.md).
Contour pack integration, shaders/resources, train/contraption,
schematicannon and two-client cases remain NOT RUN. Shape 4 remains blocked.

## 2026-11-03 - accounting regressions corrected

Four targeted accounting failures were reproduced before correction.
Current development gate: 22 required GameTests passed, 4 headless tests per
profile passed, both builds succeeded and 4292 client assertions passed.
This is not full-pack gameplay or strict compatibility proof.

| Evidence | Path |
| --- | --- |
| Red reproduction (preserved) | `logs/gametest-accounting-regressions.log` |
| Current GameTests | `logs/gametest-accounting-final.log` |
| Both profile builds | `logs/build-final-{pack,legacy}.log` |
| Current client smoke | `logs/runclient-qa-final.log` |
| Reviewed full deltas | `logs/accounting-fixed-{production,tests}.diff` |
| Own-jar SHA-256 receipts | `logs/contour-jar-sha256-final.txt` |
| Contract, findings and fidelity | [Accounting-Regression-Assessment.md](Accounting-Regression-Assessment.md) |

Older pack logs/screenshots do not verify the rebuilt accounting-fix artifacts.

## 2026-11-03 - instrumented pack diagnostic

Strict compatibility stays BLOCKED. The earlier no-addon CustomSkinLoader
failure is not waived. These are opt-in instrumented client observations on
the isolated pack, not OS-input proof or an unmodified installed-jar pass.

| Check | Observed result | Evidence |
| --- | --- | --- |
| One creative slab paint/sync | Real target raycast and use; server and later client stone/paid0/held1; actual framebuffer reviewed | `probe/stone-{pre,use,post,view}-4.json`, `screenshots/contour-qa-stone-view-4.png`, `logs/pack-probe-server-v2.log` |
| Client-only v2 overlay integrity | Normal44e own-jar entries identical; only PackClientProbe.class added | `logs/probe-overlay-receipt-v2.json` |
| Matrix attempt (superseded) | FAILED at slab leaves: predicted SUCCESS but host stays log; vertical leaves also fail | `logs/material-matrix-lead-observations.json`, `logs/matrix-results.txt`, `probe/matrix-{slab,vertical}-leaves-{pre,use,post}.json` |
| Leaf root cause + fix | Passive server trace: collision-shape rejection (`leafAccepted:false`); corrected to selection-outline eligibility; red/green regression | `logs/pack-probe-server-trace-v1.log`, `logs/probe-overlay-receipt-server-v1.json`, `logs/materialsafety-leaf-fix.diff`, `logs/gametest-leaf-{red,green}.log` |
| Fixed-jar matrix | PASS (bounded): 18 creative + 3 survival cases on normal dd827 jar + fixed-v2 client overlay; all receipts rc=0; leaf paints on all 3 shapes | `logs/pack-fixed-matrix-{server,client}.log`, `logs/fixed-matrix-results.txt`, `logs/fixed-*-client-check.json`, `logs/probe-overlay-receipt-fixed-v2.json`, `screenshots/fixed-material-matrix-contactsheet-derived.png` |
| Same-world save/reload | PASS (bounded): save-all + graceful stop + same-jar restart; three distinct material/paid states persist identically server-side and client-observed | `logs/pack-fixed-reload-{server,client}.log`, `logs/pack-fixed-reload-results.txt`, `probe/reload-*.json`, `logs/pack-fixed-matrix-{server,client}-final.log` |
| Two-client BE sync | PASS (bounded): ContourQA2 joined alongside ContourQA1 (server list x2); B observed identical material/paid on all hosts, then observed A's live glass repaint without acting | `logs/pack-two-client-{server,clientA,clientB}.log`, `logs/pack-two-client-results.txt`, `probe/tc-*.json`, `screenshots/contour-qa-tc-*.png` |
| Native break/drop trace | PASS (bounded, tool-required): bare-hand survival break produced no drops - traced to pack harvest rules for `mineable/pickaxe` blocks (identical `create:andesite_casing` control); pickaxe breaks dropped the ledger stack with material+paid+facing; `loot mine` confirms `getDrops`/`ledgerDrops` | `logs/pack-drop-trace2-server.log`, `logs/pack-drop-trace-clientA.log`, `probe/dt*.json`, `logs/probe-overlay-receipt-{server-v2,fixed-v4}.json`, dev GameTest `survivalBreakDropsLedgerItem` |
| Bounded positive observations | Stone, oak slab, glass and oak log on slab/vertical have matching material/paid0/held1 client fields and raw server queries; snapshots reviewed, not a full renderer gate | `logs/material-matrix-lead-observations.json`, `screenshots/material-matrix-prefix-contactsheet-derived.png`; original PNGs retained |
| Leaf mismatch discrimination | Real persistent leaf beside host; later UP control also places leaf above unchanged host. Exact handler/cause still unresolved | `probe/diag-vertical-leaf-neighbor.json`, `probe/control-vertical-leaf-{pre,use,post,view}.json`, server marker lines in `logs/pack-probe-server-v2.log` |

The initial operator claim of ten clean matrix cases was rejected by the
lead's full material-state comparisons. It overlooked the earlier slab-leaf
mismatch. Continuation after that required stop is not accepted as a completed
matrix. The casing photo is labeled as an after-failure observation.
The contact sheet is DERIVED presentation of originals, not a game screenshot.

Current scope and unresolved mechanism:
[Pack-Diagnostic-Assessment.md](Pack-Diagnostic-Assessment.md).

## Historical notes (superseded - keep for trace only)

- Batch-3 "GameTest harness BLOCKED" note is obsolete: the MDK-official
  `type = "gameTestServer"` run config works and 16/16 tests pass.
- The `0.0NONE` version warning seen in early server logs was fixed by
  `processResources` `${mod_version}` expansion; packaged toml shows 0.1.0.

## Known cosmetic notes

- Dev-run mod list shows `Create: Contour 0.0NONE` - `${file.jarVersion}`
  resolves from the packaged jar manifest (0.1.0), not the dev classpath;
  the built jar carries `Implementation-Version: 0.1.0`.
- First run generated `run/` content (world, server.properties, logs) in
  this repo only; `server-port` set to 25599 locally to avoid the
  pre-existing PID 22480 listener on 25565.
