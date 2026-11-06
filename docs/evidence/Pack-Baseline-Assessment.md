# Isolated Pack Baseline Assessment

Date: 2026-11-03. Contour was NOT installed in any run below.

## Verdict

BLOCKED for the strict full-pack client baseline. The isolated dedicated
server starts, but the client without a patch-failure bypass crashes in
CustomSkinLoader's SkinManager transformer. This occurs before Contour is
added, so it is not evidence of a Contour regression.

A modified diagnostic profile joined the local server with
`-Dcustomskinloader.ignorePatchFailure=true`. That is NOT a strict baseline
pass, skin-feature proof, or clearance to install Contour in Rustic Craft 2.

## Environment and isolation

- Frozen declared snapshot: Rustic Craft 2 2.2.2, Minecraft 1.21.1,
  NeoForge 21.1.248, Java 21.0.12+8.
- Runtime-only copies under ignored `.cache/pack-qa`, original pack
  repository and worlds unchanged. Common then role-specific overrides.
- Lead verified exact catalog filename sets: client 289 files, comprising
  288 jars and one Excalibur resource-pack ZIP; server 283 jars.
  These are file counts, not effective loaded-mod counts.
- Both official Create 6.0.10 copies match the permitted recorded cached
  artifact SHA256 EF87FE5709F1BA1F5B8BB20A2925B5AFB4669E178FD6D8BF10C167759EEFE37A.
  Competing mod jars were not hashed, inspected or decompiled.
- Production targets `forgeserver` / `forgeclient`, not development classes.
  Official installer/version metadata is retained in the ignored sandbox.
- New creative/peaceful flat fixture, loopback TCP 25610 and voice UDP
  25611; offline synthetic player ContourQA1. RCON/query disabled.
  These fixture/safety settings are not a production-server configuration.

## Executed checks

| Check | Observed outcome | Evidence |
| --- | --- | --- |
| Server production launch, Java 21 `-Xms1G -Xmx6G @server-absolute.args nogui`, sandbox/server working directory | Starts on 127.0.0.1:25610; Done at 21:07:28; Create 6.0.10 commit ac0c444d9828da3453ae8cc65338e8de063286fb. Baseline has errors, not a clean acceptance. | `logs/pack-baseline-server-before-contour.log` |
| Initial client without bypass | SkinManager transformation fails in CustomSkinLoader 15.0.1. | Archived first crash in `.cache/pack-qa/bootstrap/repair-csl-skip/` |
| Diagnostic bypass profile | Local join and voice handshake observed. Screenshot was reviewed: Game Menu over a blurred world, not unobstructed rendering proof. | `logs/pack-baseline-client.log`, `logs/pack-baseline-server.log`, `screenshots/pack-baseline-joined.png` |
| Strict retry without bypass, version placeholder corrected to 1.21.1 | Same SkinManager patch failure. Correcting that placeholder alone does not resolve the blocker. | `logs/pack-baseline-client-strict.log`, `logs/pack-baseline-client-strict-crash.txt` |

Strict failure:

```text
Failed to transform net/minecraft/client/resources/SkinManager
with customskinloader:skin-manager-patch
Patch 'customskinloader:skin-manager-patch:skin-manager.<init>.v1'
matched protocol 0 but did not modify any bytecode.
```

The strict JVM command is retained in
`logs/pack-baseline-client-strict-command.txt`; it uses `--version 1.21.1`,
`--launchTarget forgeclient` and no patch-failure bypass.

## Findings and uncertainty

- The server logs access-transformer, invalid-dist, loot/tag, TACZ and
  dimension data-map errors before Contour is present. Their complete
  causes and gameplay impact are not established; do not waive them.
- One flat-generator error was our fixture setup, not the pack:
  generator-settings initially omitted layers. Explicit vanilla plains
  layers were added for subsequent starts. The fallback-created owned
  world was not deleted or silently regenerated.
- The initial launcher used a nonstandard version placeholder. It was
  corrected using the official FML production-install recipe. The repeated
  crash does NOT rule out every other launcher/mappings/transformer-chain
  factor; no claim of a fully diagnosed upstream incompatibility is made.
- The diagnostic bypass skips failed CustomSkinLoader transformations,
  including another reported identifier patch; it does not prove skin
  behavior and must remain explicitly opt-in.
- Staging missed the original 40 GiB free-disk preflight. This preparation
  deviation was recorded; the later bounded bootstrap budget requires
  20 GiB free at entry, a 2 GiB transfer/copy cap and a 10 GiB pause floor.

## Still required

The owner subsequently approved the separately labeled diagnostic profile
(ADR-012). Continue its baseline/addon comparison autonomously; the strict
baseline still requires supported launcher settings or a resolved failure.
No Contour full-pack gameplay, train/contraption, schematicannon, second
client, Excalibur/Solas profile or first-three end-to-end acceptance follows
from these baseline attempts. Shape 4 remains blocked.
