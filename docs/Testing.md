# Testing - Create: Contour

Phase status: **experimental prototype.** The three first shapes (slab,
vertical slab, stairs) plus shared material/accounting/persistence/wrench
and client paths are implemented and exercised by development gates:
18/18 GameTests, 4 headless logic tests per profile, 4292/4292 client
model assertions, and native live-paint screenshots on all 3 shapes.
This is development-profile proof only - addon full-pack, multiplayer,
contraption and CTM-neighbor gates remain **NOT RUN**. Statuses below
are per-row: PASS (executed), PARTIAL (some layer executed),
NOT RUN, BLOCKED, or NOT IMPLEMENTED. The pre-addon strict pack-client
baseline is BLOCKED on a CustomSkinLoader transformation failure; a
modified diagnostic join does not waive that gate. See
[Pack-Baseline-Assessment.md](evidence/Pack-Baseline-Assessment.md).

## Evidence layers

| Layer | What it proves | Environment |
| --- | --- | --- |
| L0 static | Docs/spec consistency, provenance, license | Shell/grep, no game |
| L1 compile | Code compiles under both planned target profiles (legacy 6.0.8-168/NF21.1.213 and primary pack 6.0.10/NF21.1.248) | `gradlew build` per profile, Java 21 |
| L2 automated | Unit/GameTest-level behavior that can run headless | Gradle test tasks |
| L3 real client | Rendering, models, particles, tooltips, Ponder, JEI | Launched client; headless cannot render - L2 never substitutes |
| L4 pack profile | PACK COMPATIBILITY (mandatory): addon on isolated RC2 client AND server inputs identified under ADR-011 (2.2.2 stable / NF 21.1.248 / exact cached Create 6.0.10 + declared mods); join, renderer, recipe, train, schematic, save/reload regressions. Only permitted official Create hashes; competing jars use catalog/loaded metadata, not byte-hash proof. | Real client/server, MC Pilot where available or owned-window native input; isolated copies only - never the original modpack repo or live worlds |
| L4m minimal profile | Clean reproduction/bisection env (loader + Create + deps + addon); does NOT prove pack compatibility | Separate minimal profile |
| L5 contraption/schematic | Trains, contraptions, rotate/mirror, schematicannon (on both pack and minimal fixtures where relevant) | Real world fixtures |
| L6 secondary profiles | JEI-on profile; Connector profile (never in the minimal profile) - supplementary reproduction only, explicitly not a replacement for the mandatory pack proof | Separate profiles only |

## Six-material matrix

Executed fixture (Contour QA superflat, y=-60): x97 neutral, 100 stone,
103 oak_slab, 106 glass (cutout), 109 oak_log axis X, 112 oak_leaves,
115 create:andesite_casing (CTM), 118 oak_log axis Z,
121 white_stained_glass (translucent); rows z100 slab / 104 vertical /
108 stairs; vanilla references row z96. Evidence: `qa-material-reference-fixed.png`,
`qa-live-sync-{stone,glow}.png` (slab), `qa-live-{vertical,stairs}-{stone,glow}.png`,
`qa-painted-items-transform-fixed.png`, `qa-resource-reload-items.png`.

Every shape issue must be tested against: `stone`, `oak_slab`, `glass`,
`oak_log`, `oak_leaves`, and Create `andesite_casing`. Per case also verify:
waterlogging where the shape supports it, light emission, sound, tint
(leaves), transparency (glass), collision shape equals rendered geometry,
and null/missing-registry/unloaded-neighbour variants.

## Traceability matrix

Status column values: PASS / PARTIAL / NOT RUN / BLOCKED / NOT IMPLEMENTED (honest per-layer).

| Requirement | Issue | Layer(s) | Status |
| --- | --- | --- | --- |
| Two compile profiles; primary vs secondary runtime deps explicit | 01 | L1 | PASS (both `gradlew build` + 4 logic tests each) |
| RC2 pack profile record: Compatibility.md settled snapshot + hash-verified isolated client/server copies prepared | 01 | L0 | NOT RUN |
| Two planned target profiles compile: primary pack (6.0.10/NF21.1.248/bundled+67/FW1.0.6/Ponder1.0.82/JEI19.57 opt) and legacy (6.0.8-168/NF21.1.213; +67 deviation, Ponder 1.0.66 explicit) | 01 | L1 | DONE 2026-11-03 (both `gradlew build`; logs in docs/evidence/logs/) |
| Dedicated server boots with no client-class loads | 01 | L4m (server only) | PASS (GameTest server + runserver-shapes.log; shipped `create_contour-0.1.0-alpha.jar` + Create 6.0.10-280 on clean NeoForge 21.1.248 install reached `Done (5.046s)` with zero mod errors - `.cache/server-prod/server.log`) |
| License/credits correct; provenance register; no competitor code/assets | 02 | L0 | PASS (licenses/Create-MIT.txt shipped, Provenance.md) |
| Original neutral texture/model renders when no material | 02, 03 | L3 | PASS (neutral-own-sprites smoke + visual) |
| Versioned BE NBT; migration 0->1; malformed/partial/future-version safety | 03 | L2 | PASS (bePersistence); L3 PARTIAL |
| Material survives load when the paid ledger is intentionally empty under consumption-off; base `read` would erase the material via null acceptance - override verified | 03, 07 | L2 | PASS (bePersistence; paid ledger is independent of the empty Item hint) |
| Recursive copycat material rejected | 03 | L2 | PASS (materialAcceptance) |
| Render safety: empty/null ModelData, inventory null state, no material -> neutral, no meshing crash | 03 | L3 | PASS; THROWING material model fixture NOT RUN |
| Model cache / chunk remesh correctness across resource reload | 03, 09 | L3 | PASS (F3+T reload, qa-resource-reload-items.png) |
| Slab top/bottom/double merge (same material only), waterlog, collision=render, resources | 04 | L2 + L3 | PARTIAL: L2 state/merge tests and L3 model variants/default material matrix PASS; complete native waterlogging/placement matrix NOT RUN |
| Vertical slab 4 facings, edge placement, waterlog, collision=render | 05 | L2 + L3 | PARTIAL: L2 state tests and L3 model variants/default material matrix PASS; complete native placement/waterlogging matrix NOT RUN |
| Stairs facing/half + straight/inner/outer connections incl. neighbour removal | 06 | L2 + L3 | PARTIAL: L2 vanilla parity/connections and L3 model variants/default material matrix PASS; complete native corner-placement matrix NOT RUN |
| Material precedence: stored item > offhand > look sample > neutral; server-validated sampling | 07 | L2 + L3 + L4 | PARTIAL: L2 itemPlaceAccounting PASS; native survival/full-pack cases NOT RUN |
| Items always blank (ADR-014): no game path writes material/paid onto a stack; legacy payloads remain parseable | 07 | L2 + L3 | PASS (codexEmitsBlank, pickBlockIsBlank, blankPickupAndReplace, survivalBreakDropsBlankAndMaterial, waveLedgerDrops in `.cache/gt_ledger2.log`) |
| Break/sneak-wrench/swoop drops blank shape items + paid material refund; double break yields 2 blanks + refund stack | 07 | L2 + L3 | PASS (survivalBreakDropsBlankAndMaterial, blankPickupAndReplace, waveLedgerDrops all 106 shapes, doubleMergeLedger drops assert) |
| In-world paint consumes exactly 1 item + records 1 paid unit (config on); placement escrow charges once | 07 | L2 + L3 + L4 | PARTIAL: L2 itemPlaceAccounting/paintEconomics PASS; native survival journey NOT RUN |
| No matching item: application rejected without consuming shape/material | 07 | L2 + L3 + L4 | PARTIAL: L2 missing-payment/rollback tests PASS; native survival/full-pack journey NOT RUN |
| Ledger server-authoritative; forged client payload never trusted | 07 | L4 | NOT RUN |
| Config off: zero paid units on new applications; accounting survives reload, cycling, schematic; toggles preserve pre-existing credit, no minting | 07, 08 | L4 + L5 | NOT RUN |
| Creative: free, no refund; pick-block yields BLANK item; survival pick-block selects existing only, no spawn | 07 | L2 + L3 | PARTIAL: L2 creativeBreakRefundAndBlankPlace + pickBlockIsBlank PASS; native creative paint PASS; complete native pick-block/survival journey NOT RUN |
| Wrench one-click family cycle preserves material + orientation; doubled-slab conversion rejected; no loss/dup | 07 | L2 + L3 + L4 | PARTIAL: L2 wrenchCycleAndPickup PASS; complete native/full-pack journey NOT RUN |
| Sneak-wrench pickup returns blank items + material refund | 07 | L2 + L3 | PARTIAL: L2 blankPickupAndReplace PASS; complete native journey NOT RUN |
| Sneak + shape item on a shape = plain adjacent placement (no stored-paint gestures) | 07 | L2 | PASS (sneakPlaceAdjacent) |
| Contour Placer: LMB place consumes contour_blank, RMB swoop refunds blank+material, sneak+RMB selector, selection validated server-side, non-shape targets ignored | 07 | L2 | PASS (placerPlace/placerNoAmmo/placerSwoop incl. painted refund/placerSwoopNonShape/placerSelect in `.cache/gt_ledger2.log`) |
| Texture rotation config on/off both correct | 07, 08 | L3 + L5 | NOT RUN |
| Creative tab: exactly one neutral item per shape, sorted | 07 | L3 | NOT RUN |
| Moving contraption assemble/render/disassemble; real train carriage ride | 08 | L5 | NOT RUN |
| Place-adjacent vs modify-assembled distinguished; latter explicitly unsupported | 08 | L5 | NOT RUN |
| 90/180/270 rotation + mirror on asymmetric materials (oak_log, stairs) | 08 | L5 | NOT RUN |
| Schematicannon: capture imports no ledger as currency; consume-on requests shape units + 1 material/unit (double=2); consume-off requests 0; print ledger = items actually consumed; no dup | 08 | L2 + L5 | PARTIAL: L2 requirement surface PASS (schematicRequirements in `.cache/gt_wave49.log`: painted slab req = material x1, double slab = material x2 + 0 host extras (default covers slab-double), unpainted = NONE; writeSafe exports PaidUnits=hostUnits when painted+consume-on, 0 otherwise, strips item hints). Real cannon capture/print journey NOT RUN |
| SafeNBT keeps version/material/orientation, strips paid credit and unwanted components; printed ledger derives from actually consumed inputs | 08 | L2 + L5 | PARTIAL: L2 PASS (writeSafe: material/facing/top kept; PaidUnits exported = hostUnits iff painted+consumeMaterial, else 0 - ledger mirrors what the checklist charges; Item hint always stripped). Known boundary: printing a consume-on schematic under consumeMaterial=false config grants the ledgered paint uncharged (server-config edge). Real print journey NOT RUN |
| PACK: isolated RC2 copies identified under ADR-011 (catalog/side metadata, permitted official Create hash); baseline WITHOUT addon recorded first - failures documented, never blamed on addon or waived | 13 | L4 | BLOCKED: input filename sets and Create identity verified; server starts with errors; strict client crashes in CustomSkinLoader; modified bypass join is not an accepted baseline |
| PACK: dedicated pack server + 2 pack clients join with addon, zero regressions (effective loaded mods/configs captured per side) | 13 | L4 | PARTIAL: two instrumented clients joined and bounded BE sync passed on fixed jar; regression sweep and per-side config capture remain NOT RUN |
| PACK: Connector/ConnectorExtras present; JEI 19.57 recipe visibility; KubeJS reload + original Create recipe regressions | 13 | L4 | NOT RUN |
| PACK: all 3 shapes x 6 materials place/paint/cycle/break on pack runtime, screenshots+logs both clients | 13 | L4 | PARTIAL: instrumented paint matrix PASS on fixed jar (18 creative + 3 survival, incl. leaves) + bounded two-client BE sync + native place/break/drop trace PASS with correct tool (pack denies bare-hand drops for mineable/pickaxe - identical to create:andesite_casing); wrench cycle and strict placement coverage remain NOT RUN; see Pack-Diagnostic-Assessment |
| PACK: shaders OFF and Solas ON render profiles; Sodium/Iris never disabled; tinted/glass/leaves correctness; resource reload + Excalibur pack appearance | 13 | L4 | NOT RUN |
| PACK: train incl Threaded Trains; adjacent Create Connected geometry; Aeronautics assembly/render/disassembly | 13 | L4 + L5 | NOT RUN |
| PACK: Schematic Checker allow-list/item requirements verified, or explicit blocker recorded (entry is catalog `unverified:true`) | 13 | L4 + L5 | NOT RUN |
| PACK: world save/reload incl missing-material fixture; chunk remesh across repeated resource reloads | 13 | L4 | PARTIAL: same-world save/reload PASS on fixed jar (distinct material+paid states persist server+client); missing-material fixture and repeated resource-reload remesh NOT RUN |
| Gate: issue 13 result consumed; minimal clean profile reproduces the same matrix as separate environment (not pack proof) | 09 | L4 + L4m | NOT RUN |
| Save/reload + two-client sync on pack runtime | 09 | L4 | PASS (bounded instrumented): world save/reload persisted distinct material+paid states; second client observed identical BE state + live cross-client repaint; same-host offline-mode, not WAN/strict |
| JEI recipes visible with JEI; full function without JEI | 11 | L6 | PARTIAL: clean client/server runs without JEI PASS; JEI-on recipe visibility NOT RUN |
| Two original Ponder scenes; concise per-shape wrench tooltips | 11 | L3 | PARTIAL: placer Ponder scene implemented (`ContourPonderScenes.placer` + `placer.nbt`, registered via `ContourPonderPlugin`/`PonderIndex` on placer + blank) - registration compiles, in-client render verification NOT RUN; second scene not yet written; tooltips implemented, native inspection NOT RUN |
| Wave catalog (114 shapes): all blockstate variants resolve, all quads finite/in-bounds/correctly wound, material xform + item models + live sync | 12 | L3 | PASS (`runClientQA` + `-Dcreate_contour.renderSmoke`, `Contour QA` world: `[contour-smoke] COMPLETE passed=318503 failed=0 total=318503`, log `.cache/client_qa94.log`; zero missing-texture/model errors incl. the 3D placer item model; `PonderIndex: Registering Ponder Scenes` clean (ContourPonderPlugin/placer board compiled, `.cache/client_qa94.log`)) |
| Wave catalog visual review: 102-shape painted scene screenshots | 12 | L3 | PASS (`run/screenshots/wave-visual-{a,b,c,d,e,f,g}.png` from `runClientQA` visual stage, `.cache/client_qa84.log`; the QA platform is now catalog-sized (rows*6+2 deep) with the camera pulled back, so every shape lands on stone; earlier grass-spill caveat resolved) close-up confirms cylinder round column, arch semicircular band + open passage, vertical stairs L-corner, gutter trough, bracket corbel, pilaster half-column, stair corner L-step, sphere ball, cone point, capital stepped flare, bullnose convex edge round, cornice stepped crown, baluster turned vase profile, finial pointed knob, scotia mini cove, ogee S-sweep, torus closed ring + open hole, crenellation merlon teeth, barrel vault half-cylinder shell + gable cap, scaffold lattice + platform, gothic arch pointed opening, elbow quarter-torus bend + tube-cross-section mouths, handrail tilted rail + posts, newel turned post + knob, balustrade rail/baluster/post line, i-beam flange/web girder section, portcullis pointed bar grid, flange ring plate + stud bumps, buttress descending steps, arrow slit panel gap, dentil tooth row, tee crossed-tube junction + three mouths, palisade pointed post row, alternating quoin corner stones, triangular truss frame, sloped window sill, turned bollard post, mullion casement frame, coffer recessed panel, keystone tapered wedge, pedestal stand, lancet pointed window, valley V-flashing, pipe cross 4-way, pipe cap end plug, pediment stepped gable, chimney stack, half-timber frame + diagonal brace, spandrel corner triangle, arc wall quarter-annulus curve, rosette domed medallion, flying buttress pier + arc strut, coping wall cap, oculus open ring, awning sloped canopy, buffer stop plate + raked struts, smokestack flared funnel + open bore, semaphore blade + counterweight, pilot stepped wedge; wave-visual-g shows the wall sign's block-entity renderer drawing "CONTOUR / SIGNS / WORK" on the plate face in earlier captures; z-fighting of interior double quads and per-orientation material alignment not assessable from stills) |
| Catalog-wide GameTest matrix: rotate CW90 x4 + mirror x2 round-trip, wrench preserves material+paid on all wave shapes | 12 | L2 | PASS (144/144 required tests incl. placer set, ADR-014 blank-drop economy, wave-49+50 shapes + schematicRequirements + iron-variant redstone gating + layer/slice snow-style stack-merge QoL + sliding+folding door leaf/pairing + catwalk grate/rails, `.cache/gt_wave53.log`) incl. cone stepped collision + capital flare + bullnose stepped cover + baluster/finial lathe bands + scotia/ogee edge-profile covers + sign BE type/lines/cap/NBT round-trip + torus annulus-corner/hole/rim cover + crenellation merlon/crenel/machicolation geometry + barrel vault crown/foot/inverted cover + cog plate/teeth/axis geometry + wheel rim/hub/axis silhouette + scaffold posts/platform/isLadder + gothic arch wall/apex/opening cover + louver plate/slat/pitch geometry + elbow leg/bend cover across all 12 edges + handrail band/post/mirror geometry + newel plinth/shaft/knob bands + balustrade rail/baluster/post/run-swap geometry + i-beam flange/web/Y-flange section + portcullis bar/tip/strap/panel-swap geometry + flange strip/stud/bore/mirror cover + buttress band/mirror steps + arrow slit bar/sill/lintel/open-gap/panel-swap + dentil band/teeth/half-flip geometry + tee main/branch tubes on all 12 orientations + palisade post/tip/rail/panel-swap geometry + quoin alternating legs/cubes on all 12 edges + truss members/slope/panel-swap + sill slope/step/facing-flip + bollard 4-band profile on 3 axes + mullion frame/cross/open-pane + coffer frame/field on all 6 faces + keystone taper/panel-swap + pedestal 3-band profile on 3 axes + lancet jamb/head/opening + valley V steps + cross arm/closed-axis + cap stub/plug + pediment stepped rows + chimney bands + half-timber rail/stile/brace-cover counts + spandrel shrink/mirror + arc wall end/open/corner-swap + rosette plate/boss + flyer pier/abut/arc counts + coping seat/cap axis-swap + oculus band/center-bore/ring-coverage/axis-swap + awning segment/facing-swap + buffer plate/foot/facing-swap + smokestack skirt/barrel/rim/axis-swap + semaphore blade/mast/counterweight/facing-swap + pilot skid/tip/top rows, `.cache/gt_wave48.log`; `waveLedgerDrops` covers all 106 wave blocks) |
| Sign behavior: dedicated ContourSignBlockEntity (4 lines, sanitize 24-char cap, NBT/writeSafe persistence), SignTextPayload server-authoritative update, edit screen, BER text on plate face per facing | 12 | L2 | PASS (`signLinesRoundTrip` GameTest in `.cache/gt_wave25.log`; visual text evidence `wave-visual-g.png` from `.cache/client_qa49.log`; the edit-screen interaction path is exercised only by hand - the smoke does not click the UI) |

## Evidence record template

Copy per executed case (fill every field; artifacts go under
`docs/evidence/` whose manifest is tracked and bulk captures ignored):

```text
Case id:          <requirement row + variant>
Result:           PASS | FAIL | UNSUPPORTED
Command/entry:    <exact command or in-game action>
Environment:      <MC/NeoForge/Create/addon versions, profile, dist>
World fixture:    <world name/version or schematic id>
Action:           <steps performed>
Expected:         <contract expectation>
Actual:           <observed result>
Artifacts:        <screenshot path> <log path> <jar SHA-256> <recording path>
Notes:            <assumptions, flakes, follow-up issue>
```

## Rules

- A unit test, GameTest, or compile profile is never a substitute for the
  L3/L4/L5 evidence the gate requires.
- Pack-proof isolation: L4 pack cases run only on isolated copies of the
  RC2 client and server runtime. Never mutate the original modpack repo,
  its tracked worlds, or live servers; any live/external server action
  requires explicit owner permission recorded in the case evidence.
- `UNSUPPORTED` is a recorded, blocking outcome where the matrix marks a
  case required (M2 gate) - not a skip.
- MC Pilot (issue 09) must only send input to the focused target window
  (user32 fallback); never launch or steal focus from unrelated apps.
