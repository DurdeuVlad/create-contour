# Accounting Regression Assessment

Date: 2026-11-03. Scope: placement and slab-merge material accounting.

## Contract and verdict

- Goal: conserve real paid units across creative placement, invalid stored
  material, missing legacy material and vanilla Ctrl+pick block-entity import.
- System: inspect the transaction owner, reproduce failures with GameTests,
  make bounded fixes, inspect complete diffs and execute regression checks.
- Constraints: no schema expansion, fabricated native interaction, competing
  source/art inspection, original-pack writes or publication.
- Evaluation: each targeted failure must have executed red evidence, followed
  by green tests and rebuilt profile artifacts. Pack gameplay is a separate gate.

Verdict: PASS at the recorded development-test fidelity. Overall first-three
acceptance and strict Rustic Craft 2 compatibility remain INCOMPLETE.

## Reproduced defects and fixes

| Test | Pre-fix observed failure | Corrected authority |
| --- | --- | --- |
| `creativePrepaidNoDup` | Unconsumed held prepaid frame produced placed credit 1 | Creative placement adds zero new credit; held prepaid frame stays intact |
| `unsupportedStoredNoConsume` | Unsupported chest payload consumed the real chest | Stored state is validated before billing; neutral fallback is free |
| `missingLegacyNoRetarget` | Missing legacy material retargeted stale credit into the stone merge | Explicit invalid payload does not fall through to offhand/look sampling |
| `creativeCtrlPickNoMergeSeed` | Imported BE data seeded paid units 1 in an unpaid target | Merge uses its pre-placement snapshot, not post-import credit |

Red evidence: `logs/gametest-accounting-regressions.log` (four required tests
failed). This file is preserved, not replaced by green output.

The current tests also require real fallback placement and frame consumption,
fresh target observations, unchanged material inventory, creative held-frame
preservation and an actual double-slab Ctrl+pick merge. Temporary config and
operator state are restored. These are server GameTests, not OS-driven pack
survival journeys.

## Reviewed verification

| Executed command | Result | Evidence |
| --- | --- | --- |
| `./gradlew runGameTestServer` | All 22 required tests passed | `logs/gametest-accounting-final.log` |
| `./gradlew build -PcompatProfile=pack` | BUILD SUCCESSFUL; 4 headless logic tests, zero failures/errors/skips | `logs/build-final-pack.log`, `headless-proof-pack.xml` |
| `./gradlew build -PcompatProfile=legacy` | BUILD SUCCESSFUL; 4 headless logic tests, zero failures/errors/skips | `logs/build-final-legacy.log`, `headless-proof-legacy.xml` |
| `./gradlew runClientQA -PcompatProfile=pack` | 4292 assertions passed, zero failed | `logs/runclient-qa-final.log` |

Complete reviewed source deltas are in `logs/accounting-fixed-production.diff`
and `logs/accounting-fixed-tests.diff`. Their comparison snapshots predate
some earlier reviewed foundation fixes, so not every line is newly introduced
by these four regressions.

Own-jar identities are frozen in `logs/contour-jar-sha256-final.txt`:

- Pack: `44e210bcd85f28a996d4f921323d80b7ba97771968412b0e26a1b8686d2d3351`.
- Legacy: `0dd933ca5a42cca5f614b95f8ec181b57b0da894a540e79f59f75cf96af4a061`.

## Fidelity and remaining checks

The pack-profile tests use NeoForge 21.1.248, official Maven Create
6.0.10-280 and the development environment. Legacy has compile/headless proof,
not an in-game run. The renderer smoke covers real baked models and item-model
binding; it is not 4292 gameplay scenarios.

At this checkpoint the live diagnostic pack still uses the older artifact.
The rebuilt jars have not yet been accepted in a full-pack gameplay run.
Strict client baseline remains blocked by CustomSkinLoader; the diagnostic
bypass is not waived. Native pack input remains unresolved. Train, schematic,
two-client and full survival accounting acceptance remain outstanding.
