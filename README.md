# Create: Contour

A NeoForge addon for [Create](https://github.com/Creators-of-Create/Create) that
adds 114 Create-native shape blocks that copy the material of any block you
paint them with - slabs, stairs, panels, slopes, doors, sliding and folding
doors, catwalks and more - with the Contour Placer, Codex, and low-click
workflows designed for decorating trains and contraptions.

- Display name: **Create: Contour**
- Mod id: `create_contour`
- Never use the bare name/id `contour`: it is already taken on Modrinth by an
  unrelated mod. See [docs/Research.md](docs/Research.md) for the name check.

## Status

**Alpha `0.2.0`**: 114 shapes and all requested QoL systems are implemented
and verified — `runGameTestServer` 144/144, `runClientQA` render smoke
318,503 checks / 0 failed. Released as a GitHub prerelease
(`v0.2.0-alpha`); CurseForge publishing is prepared in
[docs/publishing/curseforge-page.md](docs/publishing/curseforge-page.md).

Known roadmap gaps (documented, not bugs): true multipart plates and
kinetic/fluid disguises - see
[docs/issues/13-deferred-families.md](docs/issues/13-deferred-families.md)
and ADR-015/016 in [docs/Decision.md](docs/Decision.md).

## Build targets and evidence

Two target profiles (see [docs/Compatibility.md](docs/Compatibility.md)
and [docs/Research.md](docs/Research.md)). Statuses distinguish executed
development-profile proof from the un-executed full-pack gate.

| Profile | Declared composition | Status |
| --- | --- | --- |
| **Primary (pack)** - declared target: Rustic Craft 2 stable 2.2.2 pack profile | MC 1.21.1, NeoForge 21.1.248, Create 6.0.10 (jar manifest requires NF >= 21.1.219, Ponder >= 1.0.82; bundled Registrate +67, Flywheel 1.0.6, Ponder 1.0.82), JEI 19.57.0.449 optional | build PASS; 286-mod RC2 server `Done` boot PASS; client world join PASS; sustained TPS not measured |
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

## Shape catalog (summary)

114 shipped forms: slabs, vertical slabs, stairs, walls, panes, slopes,
layers (1-8), half-layers, slices, panels and byte-panels, frames, beams,
pillars, catwalks, doors and trapdoors (normal + iron), sliding and folding
doors, and more. Remaining families - multipart occupancy and kinetic/fluid
disguises - are spec'd in
[docs/issues/13-deferred-families.md](docs/issues/13-deferred-families.md).
