# Prototype QA Assessment

Date: 2026-11-03. Scope: slab, vertical slab, stairs and their shared core.

## Contract and verdict

- Goal: implement and inspect the first-three-shape foundation, with honest
  evidence and no expansion before the required full-pack/end-to-end gate.
- System: source review, dedicated GameTests, both build profiles, actual
  development-client rendering and native in-world interactions.
- Constraints: private/local work only; no commits, publication, competing
  source/art inspection or comparison, other-repository writes or live worlds.
- Evaluation: accept only observed checks at their actual environment level.

Overall verdict: **INCOMPLETE**. The bounded prototype checkpoint passes its
recorded development checks. The original product request, first-three
end-to-end acceptance and Rustic Craft 2 compatibility are NOT complete.
These jars are not cleared for production worlds or modpack installation.

## Evidence reviewed

- `runGameTestServer`: 23 required tests passed in
  `logs/gametest-leaf-green.log`. This includes origin-slot/component
  conservation, incompatible and noncanonical-empty slot interference,
  one-shot refunds, future-payload preservation and frozen mutation refusal.
  Four accounting regressions were reproduced before correction; details
  are in [Accounting-Regression-Assessment.md](Accounting-Regression-Assessment.md).
  The visual-source regression was red in `logs/gametest-leaf-red.log`
  before replacing physical collision with selection-outline eligibility.
  GameTest interactions use mock players on a real dedicated test server.
- Both `build -PcompatProfile=pack` and `build -PcompatProfile=legacy`
  succeed: `logs/build-final-pack.log`, `logs/build-final-legacy.log`.
  Their archived `headless-proof-*.xml` each report 4 logic/math tests,
  zero failures and zero errors. These are not in-world material tests.
- `runClientQA`: 4292 assertions passed, zero failed in
  `logs/runclient-qa-leaf.log`. Source review covers item-model binding through
  transforms/passes/types, owned neutral fallback, UV/grid geometry and
  the owned client appearance-refresh seam.
- Native Minecraft screenshots were inspected, not merely collected:
  all three material rows, painted items, F3+T reload, and all three shapes
  painted stone then glowstone with immediate appearance/light changes.
  Exact image and log paths are in [manifest.md](manifest.md).
- Initial packaged jar inventories were inspected in
  `logs/jar-contents-proof.log`. Artifacts:
  `build/libs/pack/create_contour-0.1.0.jar` and
  `build/libs/legacy/create_contour-0.1.0-legacy.jar`.
  Current post-outline-fix identities are preserved in
  `logs/contour-jar-sha256-leaf.txt`; earlier pack runtime logs use an older
  jar and do not verify these rebuilt artifacts in the pack.

## Fidelity and residual risk

For Contour, the strongest environment used is an isolated local development client and
dedicated test server: Minecraft 1.21.1, NeoForge 21.1.248, Maven Create
6.0.10-280 and its dependencies, without JEI. The client uses development
classes, not an installed release jar. The legacy profile has build/math
proof only. No production/live server was used.

This is below a full Rustic Craft 2 client/server replica. Its exact cached
Create artifact differs in bytes from the Maven development artifact.
The declared pack inputs were subsequently staged and baseline-tested
without Contour. The strict client baseline crashes in CustomSkinLoader;
a modified patch-failure-bypass profile joined locally, which does not
clear the compatibility gate. See
[Pack-Baseline-Assessment.md](Pack-Baseline-Assessment.md) for commands,
artifacts, fixture deviations and uncertainty. The owner authorized
runtime-only black-box copies and missing official artifact downloads
(ADR-011). ADR-012 subsequently permits the modified diagnostic comparison:
Contour is discovered, its isolated server starts and one client joins.
Native matrix/paint screenshots are not accepted (menu/blur), so this is
startup/join evidence only; see
[Pack-Diagnostic-Assessment.md](Pack-Diagnostic-Assessment.md). Competing
source/art inspection, hashing and decompilation remain forbidden.

Remaining required work:

- BLOCKED: strict full-pack client baseline (CustomSkinLoader transformer).
- INCOMPLETE: diagnostic addon startup/join; native rendering/input checks.
- NOT RUN: strict addon full-pack client/server, trains/contraptions,
  schematicannon requests/printing/transforms, multiplayer/two-client sync,
  shader/resource-pack profiles, full native survival placement/break/drop,
  and the complete native waterlogging/corner-placement matrix.
- NOT RUN: injected throwing-material-model fixtures, CTM neighbor
  connectivity, JEI-on recipe visibility and native onboarding inspection.
- NOT IMPLEMENTED: original Ponder scenes and future-version drop capsules.
  Frozen wrench pickup is refused; unknown-version normal breaking is not
  a proven lossless round trip.

Source/ownership choices stay local: `ContourBlockEntity` owns view refresh,
`MaterialPolicy.Reservation` owns escrow/origin slots, and the model wrapper
owns item binding. No extra service or custom packet was introduced.
Process provenance is not mathematical proof of absence of similarity.
