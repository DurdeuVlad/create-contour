# Isolated Diagnostic Pack First Pass

Date: 2026-11-03. Authority: ADR-012.

## Verdict

INCOMPLETE. Contour 0.1.0 is discovered in both installed production
runtimes, the isolated addon server reaches Done, and ContourQA1 joins.
These are diagnostic startup/join observations only. The client uses
`-Dcustomskinloader.ignorePatchFailure=true`; strict compatibility remains
blocked as recorded in [Pack-Baseline-Assessment.md](Pack-Baseline-Assessment.md).

In the initial addon-join batch, no shape rendering or native painting/light
acceptance was credited. Those reviewed images show Game Menu over a blurred world.
Filenames such as `pack-baseline-unobstructed.png` and `pack-matrix-*.png`
are misleading labels, not evidence that the intended view was captured.

## Observed evidence

| Check | Observation | Evidence |
| --- | --- | --- |
| Corrected diagnostic baseline | Baseline client/server logs retained separately before Contour installation | `logs/pack-diagnostic-baseline-client.log`, `logs/pack-diagnostic-baseline-server.log` |
| Addon discovery | Loader lists Create: Contour 0.1.0 on client and server | `logs/pack-diagnostic-addon-client.log`, `logs/pack-diagnostic-addon-server.log` |
| Addon server startup | Done at 21:51:32 on the owned loopback fixture | `logs/pack-diagnostic-addon-server.log` |
| Local join | ContourQA1 joined at 21:56:33; client connection targets 127.0.0.1:25610 | Same server log and addon client log |
| Fixture invocation | `Running function qa:diagnostic-scene` at 21:52:50; this alone is not per-cell verification | Addon server log |
| Anchor BE | Query at 109,-60,112 returns ContourVersion=1, neutral Material=create:copycat_base, PaidUnits=0 after attempted input | Addon server log, 21:58 onward |
| Native matrix screenshots | NOT ACCEPTED: menu/blur occludes the requested world view | `screenshots/pack-matrix-{overview,slab,vertical,stairs,refs}.png` |
| Native paint/light | NOT VERIFIED: unchanged anchor plus unresolved input/window state does not distinguish missing input from other interception | Addon server log; attempted `pack-paint-*` screenshots |

The launcher now distinguishes strict baseline, diagnostic baseline and
diagnostic addon modes. `--with-contour` requires the explicit diagnostic
flag and the installed Contour jar; baseline modes reject the addon.
The complete helper was source-reviewed. Logs are exclusive-create so a
repeated timestamp cannot overwrite an earlier attempt.

## Limits and next checks

### Instrumented slab paint observed

The bounded v2 probe case now has reviewed evidence of real client/server
painting on one slab, not merely a predicted interaction result:

- `probe/stone-pre-4.json`: first-person, real BLOCK raycast at 109,-60,112,
  neutral material, paid 0, held stone 1.
- `probe/stone-use-4.json`: actual `MultiPlayerGameMode.useItemOn` invocation.
- `logs/pack-probe-server-v2.log`: the post-use block query returns
  `minecraft:stone` and `PaidUnits: 0`; player inventory retains stone 1.
- `probe/stone-post-4.json` and `probe/stone-view-4.json`: later client
  material is stone, paid 0, held stone 1, screen none.
- `screenshots/contour-qa-stone-view-4.png`: real framebuffer reviewed;
  the painted slab is visible without menu/blur.

Fidelity: INSTRUMENTED DIAGNOSTIC, one creative paint/sync case. It does not
exercise prepaid-frame placement, survival consumption, other shape families,
light transitions or OS input. Do not call it native survival or a pack pass.

The server uses normal core jar SHA-256
`44e210bcd85f28a996d4f921323d80b7ba97771968412b0e26a1b8686d2d3351`.
The v2 client uses an instrumented copy with SHA-256
`5f20a6399a0282abbd92a87b88b53f5d60c3ca1ee62c08bf98a5a5d28f61148f`.
`logs/probe-overlay-receipt-v2.json` establishes that every original own-jar
entry is byte-identical and only `PackClientProbe.class` was added.
The normal pack and legacy build artifacts are not instrumented.

Failed preconditions are retained in `probe/stone-pre-{1,2,3}.json`;
no use was sent for them. The first-person switch followed by acknowledged
teleport established the successful actual raycast. Earlier command-delivery
or camera-transfer hypotheses are not independently proven root causes.

Effective config metadata: `resourcePacks:["fabric"]`, Iris
`enableShaders=true` with empty `shaderPack`. Solas and Excalibur are not
credited. The `RecipeBookCategories` dedicated-server error is present in
the earlier no-Contour baseline (`logs/pack-baseline-server.log`, line 6707)
as well as addon logs; it is not a newly established Contour regression,
and is not waived.

### Accounting checkpoint and instrumented continuation

### Interrupted material matrix: leaf painting mismatch

Verdict: FAILED / INCOMPLETE, not a successful three-shape matrix.
Lead review of frozen raw client files and server queries establishes:

- Stone, oak slab, glass and oak log paint transitions on slab and vertical
  slab have matching later material states, paid0 and held1.
- The FIRST material mismatch is `matrix-slab-leaves`: client predicts
  SUCCESS, but the post/view material and server query remain oak_log axisY.
  The same mismatch later occurs on vertical leaves.
- The original report of all six slab materials / ten clean cases is
  rejected. The verifier failed on slab leaves. See the failure record and
  exact bounded observations in `logs/material-matrix-lead-observations.json`.
- The sequence continued after the required first stop. Slab casing is
  retained only as an isolated after-failure observation, not proof of a
  completed prescribed sequence. Stairs and survival checks were not run.
- A real persistent leaf block is present beside the vertical host; a
  subsequent UP-face control places one above it too while its material
  stays log. This establishes world changes, not which handler created them.
- Current NOT_CROUCHING predicate markers test entity POSE, not the actual
  secondary-use / shift flag. They do not rule out every bypass route.

`logs/matrix-results.txt` retains raw server queries. The two failed leaf
pre/use/post records, later neighbor/control records and real PNGs are
retained. `screenshots/material-matrix-prefix-contactsheet-derived.png`
labels failure and after-failure frames; it does not replace originals.

### Passive trace: collision-based validation rejects leaves

The subsequent trace is a DIFFERENT fidelity: both sides instrumented.
`logs/probe-overlay-receipt-server-v1.json` verifies every original normal44e
entry unchanged, plus only `PackServerProbe.class`. Server overlay SHA-256:
`c6262143164cf72f063e62b32a9bddd0d35d9ae0b5b213a0ef1082e9268e3ae7`.
The server uses explicit `-Dcreate_contour.packServerProbe=true`; the
client remains v2. Earlier normal-server evidence is retained separately.

The raw inspect responses and events in
`logs/pack-probe-server-trace-v1.log` establish, on a new neutral vertical
fixture at 138,-60,130:

- Default oak leaves have empty collision and `leafAccepted:false`.
  The owned block's held-material acceptance returns REJECTED for leaves.
- Actual shiftKeyDown and secondaryUseActive are false; the host is not
  frozen. The earlier pose predicate was not the relevant flag.
- RightClickBlock observations have default block/item use, no cancellation;
  ITEM_BEFORE_BLOCK, BLOCK and ITEM_AFTER_BLOCK phases are observed.
  Their default cancellationResult fields are NOT measurements of normal
  handler return values.
- The server and later client keep the neutral host material, and a real
  persistent leaf appears in the adjacent cell.

Together with the inspected owned `getAcceptedBlockState` and MaterialSafety
code, this identifies the collision-based rejection and normal item fallback.
It is not a shape-specific handler gap or an identified third-party event
cancellation. Which pack component changes leaf collision is not established
and its implementation has not been inspected.

The trace recipe's log baseline use was omitted: log acceptance was queried,
not painted in that trace. This deviation is not credited as a baseline pass;
the prior independent ordinary log-paint observations remain separate.

The bounded correction now uses selection/outline geometry for visual
eligibility, retaining other safety exclusions and exception fail-closed
behavior. A registered, noncolliding visual-source regression was run red
before the change and green after it.

### Fixed-jar re-verification: bounded instrumented matrix PASS

The fixed normal server jar
(`dd827df1c3d238d8b16d397d7348724c5ea5583ec96c8e915d418bc9e13a859f`,
NO server probe) ran with the fixed-v2 instrumented client overlay
(`b991a431fba73e676392cc18b9044273425d7127b476001ff4167580067bbb6e`;
receipt `logs/probe-overlay-receipt-fixed-v2.json` verifies all new core
entries identical, only PackClientProbe.class added).

On fresh hosts at 100/104/108,-60,120, all 18 creative shape/material cases
(3 shapes x 6 materials) and all 3 survival consumption cases completed:

- Every case has pre/use/post/view JSON, a real PNG, and an rc=0 scoped
  client-field receipt (`logs/fixed-*-client-check.json`).
- Raw server NBT confirms every material state, including normalized
  `oak_leaves[distance=7,persistent=false,waterlogged=false]` on all three
  shapes - the earlier rejection is resolved.
- Survival: stone 2 -> 1 consumed, `PaidUnits: 0 -> 1` server-side on each
  shape; old unpaid casing was not refunded.
- One operator deviation: the first case's checker ran before its evidence
  copy (FileNotFoundError, not a data mismatch). The slab-stone case was
  re-checked with rc=0 and the ordering corrected. Raw data complete.
- Lead reviewed the derived contact sheet
  `screenshots/fixed-material-matrix-contactsheet-derived.png` against the
  originals; all three leaf cases render as painted material, not adjacent
  blocks.

Evidence: `logs/pack-fixed-matrix-{server,client}.log`,
`logs/fixed-matrix-results.txt`, `probe/fixed-*.json`,
`screenshots/contour-qa-fixed-*.png`.

Fidelity stays instrumented real-client, not OS-input, not strict baseline,
not two-client, and not cycle/break/native placement coverage.

### Same-world save/reload persistence: PASS (bounded)

On the same normal dd827 server + fixed-v2 instrumented client, the slab was
repainted to oak_leaves (paid0, wiping its paid1 stone ledger - expected
accounting), `save-all flush` acknowledged, the server stopped gracefully
and restarted on the identical jar/args/world/ports, and a fresh client
rejoined:

- Post-restart server NBT: `100 = oak_leaves[distance=7,persistent=false,
  waterlogged=false] paid0`, `104 = stone paid1`, `108 = stone paid1` -
  identical to pre-save state.
- Post-rejoin client observes match server exactly on all three hosts
  (real BLOCK hits, FIRST_PERSON).
- Three DISTINCT persisted states prove neither a stale base, a missing
  block entity, nor a lost paid ledger.

Evidence: `logs/pack-fixed-reload-{server,client}.log`,
`logs/pack-fixed-reload-results.txt`, `probe/reload-*.json`,
`screenshots/contour-qa-reload-*.png`, preserved pre-restart
`logs/pack-fixed-matrix-{server,client}-final.log`.

Recorded operator deviations (acceptable, none affecting the verdict):
replacement client for an expired one under the same overlay/flags; silent
FIFO loss of two `gamemode` sends with the creative gameType verified via
`data get` instead; one allowed per-host tp where the post-relogin raycast
did not reach; an owned orphaned FIFO reader closed before restart; a
pre-existing pack `Failed to load access transformers` warning also present
in baseline logs.

Fidelity: normal fixed jar + instrumented client, same isolated world.
Missing-material fixture and resource-reload remesh are separate NOT RUN
cases.

### Two-client block-entity synchronization: PASS (bounded)

A second instrumented client (ContourQA2, `client-b` sandbox, same
fixed-v2 overlay, offline-mode identity) joined the same normal dd827
server while ContourQA1 remained online - `list` showed 2 players at two
recorded points:

- Client B observed all three hosts' BE state matching the server with no
  B action: `oak_leaves paid0`, `stone paid1`, `stone paid1`.
- Client A repainted the vertical slab to glass via the real use path;
  server NBT changed `stone/paid1` -> `glass/paid0`, and client B then
  observed `glass paid0` on the same host WITHOUT performing any
  interaction - live cross-client sync through the standard block-entity
  update path.

Evidence: `logs/pack-two-client-{server,clientA,clientB}.log`,
`logs/pack-two-client-results.txt`, `probe/tc-*.json`,
`screenshots/contour-qa-tc-*.png`.

Recorded operator deviations: one early B observe ran in
THIRD_PERSON_BACK and its raycast hit the floor neighbor (the material
field still read the queried host BE correctly); one sync observe had no
hit fields while the material read stayed correct; world-default creative
verified via `playerGameType` data get rather than a gamemode ack line;
known yaw-drift signature on one capture; heavy paging while both 8G
clients ran (all processes stayed up, none unowned touched).

Fidelity: same-host offline-mode diagnostic with two instrumented clients;
not WAN multiplayer, not a regression sweep, not strict baseline.

The four accounting regressions found during this work are corrected at
development-test fidelity: see
[Accounting-Regression-Assessment.md](Accounting-Regression-Assessment.md).
Older pack runtime logs do not verify the rebuilt artifacts.

The bounded input-repair captures `pack-paint-v2-stone.png`,
`pack-paint-v2-stone2.png` and `pack-paint-v2-stone3.png` were visually
reviewed: all still show Game Menu and blur. They are not painting,
unobstructed rendering or click-delivery proof.

The next diagnostic uses an opt-in, lead-authored client probe, not more
unbounded OS-focus attempts. Its source and execution contract live under
`.cache/pack-qa/`. It is added only to a private copy of our pack jar;
the normal pack and legacy build artifacts remain unchanged. Before use,
the overlay verifier must establish that every original own-jar entry is
byte-identical and the only added entry is the probe class.

The probe explicitly closes screens, records raw client state, captures the
real framebuffer and invokes the client's use-on path with its actual
camera raycast. It never writes material/credit NBT, assigns a fake hit or
grades a result. The profile is restricted to the owned loopback server,
synthetic QA players and a bounded scene. A client interaction result alone
does not prove server acceptance; preserved server queries and later client
observations must agree.

Any resulting evidence must be labeled INSTRUMENTED DIAGNOSTIC, not native
OS-input proof, unmodified installed-jar proof or a strict compatibility
pass. The CustomSkinLoader bypass and all remaining gates still apply.

### Native break/drop trace (server-probe v2 + client-probe v4)

The first native break/drop contract (instrumented client `place`/`break`
through the real client destroy path) reached a paid-host survival break
whose ledger drop never appeared: host went air, no item entity, no pickup.
A dev-environment GameTest (`survivalBreakDropsLedgerItem`, real
`Level.destroyBlock`) passes, so the Contour drop path was correct in
isolation and the investigation moved to the pack runtime.

Server-probe v2 adds read-only `BlockDropsEvent` and `ItemEntity`
join/leave observers (receipt `probe-overlay-receipt-server-v2.json`;
client-probe v4 receipt `probe-overlay-receipt-fixed-v4.json`; log
`pack-drop-trace2-server.log`; probe records `probe/dt*.json`). Observed:

- `loot mine` on a painted host emitted exactly one ledger-carrying shape
  stack (`contour_material`, `contour_paid`, facing preserved) -> `getDrops`
  / `ledgerDrops` are correct on the pack runtime.
- Survival break of a paid stone stairs with a stone pickaxe: `blockdrops`
  `canceled:false dropCount:1` carrying `contour_paid:1` + material, then
  `itemjoin` of the same stack. PASS.
- Survival break of an unpaid glass vertical slab with a stone pickaxe:
  same ledger drop with `contour_paid:0`. PASS (completes contract case 4).
- Bare-hand survival break of a contour host: no `BlockDropsEvent` at all -
  `playerDestroy` never ran because the pack denies harvest for
  `mineable/pickaxe` blocks without a correct tool.
- Controls: bare-hand `vbreak` of `minecraft:dirt` produced a drop;
  bare-hand `vbreak` of `create:andesite_casing` produced NO drops and the
  same ~156-tick break time as our stairs.

Conclusion: the earlier zero-drop was pack harvest rules applied uniformly
to pickaxe-mineable blocks - identical to Create's own andesite_casing -
not a Contour accounting defect. The `mineable/pickaxe` tag stays, matching
Create convention. The contract's bare-hand expectation is corrected:
survival break of a Contour host drops the ledger item when a correct tool
is used, exactly like pack stone-type blocks. Evidence:
`pack-drop-trace2-server.log` lines 4732 (paid pickaxe), 4771-4772 (dirt
control), 4786-4787 (unpaid pickaxe), 4718 (loot mine join).

A `vbreak` (vanilla-block) client-probe action exists now solely as a
diagnostic control; it has no Contour interaction semantics.

### Outstanding gates

- All pack versions and binaries remain in place; no competing source or
  art was inspected. Effective loaded metadata is available in logs but
  complete per-side configuration acceptance is not established.
- Baseline resource/data errors are not waived, and a comprehensive
  addon-versus-baseline regression judgment is not complete.
- Own-window/input repair is bounded. Do not force NBT material changes
  and present them as native painting or guess that a click reached play.
- Wrench journeys, train/contraption, schematicannon, Excalibur/Solas and
  missing-material-fixture handling remain NOT RUN in the pack diagnostic
  profile. The instrumented 3x6 paint matrix, one survival consumption case
  per shape, a same-world save/reload persistence check, a same-host
  two-client BE sync check, and the native place/break/drop trace (tool-
  required per pack rules) now pass on the fixed jar (bounded subset only).
- Shape 4 remains blocked. No production or original-pack clearance.
