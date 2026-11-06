# Create: Contour

A NeoForge addon for [Create](https://github.com/Creators-of-Create/Create) that
adds copycat-style shape blocks - slabs, vertical slabs, stairs, and more - that
can be painted with the material of another block, with low-click workflows
designed for decorating trains and contraptions.

- Display name: **Create: Contour**
- Mod id: `create_contour`
- Never use the bare name/id `contour`: it is already taken on Modrinth by an
  unrelated mod. See [docs/Research.md](docs/Research.md) for the name check.

## Status

**Private experimental prototype: slab, vertical slab, and stairs are
implemented.** Shared material/accounting/persistence/wrench and client
paths have executed development tests. This is not a completed
first-three end-to-end gate and is NOT cleared for production worlds or
Rustic Craft 2 installation.

Verified facts (exact evidence in `docs/evidence/manifest.md`):

- 23/23 GameTests on the official NeoForge GameTest server, including four
  accounting regressions and noncolliding visual material acceptance
  (`docs/evidence/logs/gametest-leaf-green.log`).
- 4 headless logic/math JUnit tests per build profile (pack + legacy).
- 4292/4292 client model assertions in the dev-client render smoke
  (`docs/evidence/logs/runclient-qa-leaf.log`) - NOT 4292 features.
- Native live paint + aux light proven on all three shapes by actual
  right-click in the QA world; painted item icons, material matrix and
  F3+T resource reload all pass on the minimal profile client.
- Both build profiles produce jars (`build/libs/pack/`,
  `build/libs/legacy/`).

Not implemented: Ponder onboarding scenes and future-version drop capsules
(frozen pickup is refused by design). Strict RC2 client baseline is BLOCKED
by CustomSkinLoader before Contour; diagnostic addon loading/join is PARTIAL,
not a compatibility pass. NOT RUN: pack gameplay acceptance,
trains/contraptions, schematicannon, two-client synchronization, CTM neighbor
connectivity, JEI-on recipe visibility and the full native survival break/drop
cycle. Pack diagnostics exposed collision-based leaf rejection; the validator
now uses selection outline, with a reproduced red/green regression.
Fixed-jar pack re-verification is pending. See
`docs/evidence/Pack-Diagnostic-Assessment.md` and the evidence manifest.

- Visibility: private local repository, single owner. Publication is undecided.
- No commits, tags, remotes, GitHub records, or publications without explicit
  owner authorization (see [AGENTS.md](AGENTS.md)).

## Build targets and evidence

Two target profiles (see [docs/Compatibility.md](docs/Compatibility.md)
and [docs/Research.md](docs/Research.md)). Statuses distinguish executed
development-profile proof from the un-executed full-pack gate.

| Profile | Declared composition | Status |
| --- | --- | --- |
| **Primary (pack)** - declared target: Rustic Craft 2 stable 2.2.2 pack profile | MC 1.21.1, NeoForge 21.1.248, Create 6.0.10 (jar manifest requires NF >= 21.1.219, Ponder >= 1.0.82; bundled Registrate +67, Flywheel 1.0.6, Ponder 1.0.82), JEI 19.57.0.449 optional | build + 4 logic tests PASS; minimal Create client+server proof PASS; **FULL RC2 NOT RUN** |
| **Legacy (prompt baseline)** | MC 1.21.1, NeoForge 21.1.213, Create 6.0.8-168, Ponder 1.0.66, Registrate +67 (requested +62; official Create 6.0.8 declares +67 - recorded deviation) | build + 4 logic tests PASS; legacy client AND server runtime NOT RUN |

Windows commands (run from repo root):

```powershell
./gradlew.bat build -PcompatProfile=pack
./gradlew.bat build -PcompatProfile=legacy
./gradlew.bat runGameTestServer
./gradlew.bat runClientQA
```

`runClientQA` preflight: launch a plain `./gradlew.bat runClient` once,
create a DISPOSABLE creative superflat+cheats world named `Contour QA`
inside `run/`, place `create_contour:contour_slab` at 109 -60 112,
`tp @s 109.5 -60 116.5 180 20`, save and close. The smoke asserts that
fixture exists on join and throws if missing.

Notes: the originally requested NF 21.1.213 + Ponder 1.0.66 tuple is INVALID
under Create 6.0.10's declared requirements; it applies only to the legacy
6.0.8 profile. Java 21 throughout. Build tooling: ModDevGradle, single
NeoForge project. The 6.0.10 coordinate is resolved:
`create-1.21.1:6.0.10-280` (manifest Git-Hash `ac0c444d` = the recorded
tag). The maven jar's bytes differ from the pack cache jar; cause
uninvestigated - pack runtime uses the exact recorded jar + hash or a
provider-verified artifact. Pack-catalog versions are declarations of the player pack, not proof
of a deployed live server tuple.

## Credits

- **Create** by the Create team / Creators of Create:
  <https://github.com/Creators-of-Create/Create>. Create's code is
  MIT-licensed; its art assets are All Rights Reserved and are NOT reused
  here.
- All addon-authored code and resources are original works of this
  project (MIT), except the third-party build tooling recorded in
  [docs/Provenance.md](docs/Provenance.md) (Gradle wrapper, Apache-2.0).
- This project is not affiliated with, endorsed by, or sponsored by the
  Create team, Modrinth, or CurseForge. Name-collision status is bounded,
  not a legal clearance (see [docs/Research.md](docs/Research.md)); this
  README offers no legal certainty.

## Document map

| Document | Purpose |
| --- | --- |
| [docs/Business.md](docs/Business.md) | Problem, users, value, success measures, non-goals |
| [docs/Decision.md](docs/Decision.md) | Accepted ADRs and open decisions |
| [docs/Milestones.md](docs/Milestones.md) | Outcome-based milestone plan M0-M4 |
| [docs/Architecture.md](docs/Architecture.md) | Implemented core, data ownership and planned integration boundaries |
| [docs/Testing.md](docs/Testing.md) | Evidence layers, traceability matrix, record templates |
| [docs/Research.md](docs/Research.md) | Settled source findings, version facts, name check |
| [docs/Compatibility.md](docs/Compatibility.md) | RC2 pack profile snapshot, declared-vs-tested distinction, isolation rules |
| [docs/Planning-Checks.md](docs/Planning-Checks.md) | Recorded docs-phase validation commands and results |
| [docs/issues/README.md](docs/issues/README.md) | Issue index and dependency graph |
| [CHANGELOG.md](CHANGELOG.md) | Unreleased private prototype notes |
| [AGENTS.md](AGENTS.md) | Operating rules: authority, clean room, evidence |

## Shape roadmap (summary)

1. First three, gated: `slab`, `vertical_slab`, `stairs`.
2. Next seven to ten total: `carriage_panel`, `platform_edge`, `rail_bed_trim`,
   `beam`, `pillar`, `wall`, `pane`.
3. Later families (backlog): fence, fence_gate, trapdoor, door, layers 1..8,
   slopes, quarter-round.

No shape beyond the first three may be implemented until the milestone M2
proof gate passes. The gate is mandatory and two-part:
[issue 13](docs/issues/13-pack-compatibility.md) proves compatibility on an
isolated copy of the Rustic Craft 2 client and server pack runtime, and
[issue 09](docs/issues/09-proof-gate.md) consumes that proof plus the
multiplayer/save/render matrix.
