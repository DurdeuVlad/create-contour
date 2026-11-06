# Issue 01: Minimal reproducible NeoForge + Create build foundation and pack isolation prep

| Field | Value |
| --- | --- |
| Status | in-progress |
| Milestone | M0 |
| Type | task (executable research + bootstrap) |
| Suggested labels | `foundation`, `build`, `compatibility` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Criterion status (2026-11-03, evidence: docs/evidence/manifest.md)

AC1 DONE - `gradlew build` passes on BOTH profiles (pack default; legacy
with the +67 Registrate deviation + explicit Ponder 1.0.66 runtime pin).
AC2 DONE - minimal resolved set; no non-required dev-POM transitives.
AC3 DONE - OD-1 resolved: `create-1.21.1:6.0.10-280`, Git-Hash `ac0c444d`.
AC4 DONE - pins verified against jar/jarjar metadata; invalid NF213 +
Ponder1.0.66 + 6.0.10 tuple never configured; +62 abandoned (deviation
recorded, not silent). AC5 DONE - dev dedicated server `Done (6.339s)!`
on :25599, addon + Create loaded, no client-class errors. AC6 NOT DONE -
isolated pack copies not prepared (pack download deferred). AC7 DONE -
no pack repo or world files touched.

## Intent

Establish a reproducible, minimal, legal build workspace under the two
planned target profiles so every later issue compiles against known pins - and
prepare hash-verified isolated copies of the Rustic Craft 2 pack runtime
so the mandatory pack-compatibility gate (ADR-008, issue 13) has a clean
target.

## Expectation

- ModDevGradle wrapper project (single NeoForge project) builds on Java 21.
- Primary profile = pack: Create 6.0.10 / NeoForge 21.1.248 / bundled
  Registrate +67, Flywheel 1.0.6, Ponder 1.0.82 / JEI 19.57.0.449 optional.
- Legacy profile = prompt baseline: Create 6.0.8-168 / NeoForge 21.1.213 /
  Ponder 1.0.66 / Registrate +62 requested (conflict unresolved).
- Dependencies are declared explicitly and minimally: Create plus its true
  required runtime dependencies, JEI compile-time-only. Non-required
  integration entries exposed by the upstream dev POM (FTB, JourneyMap,
  Architectury, etc. - dev-POM entries, not Maven optional flags) must
  NOT be resolved as required runtime deps. The pack's Architectury
  presence does not authorize Architectury for this addon.
- Isolated copies of the RC2 pack client and server runtime exist and are
  hash-verified against the settled snapshot (docs/Compatibility.md).

## Acceptance criteria

1. `gradlew build` succeeds on BOTH profiles; full logs captured under
   `docs/evidence/` and produced jar SHA-256s recorded.
2. Dependency resolution report shows ONLY the declared minimal set; any
   non-required dev-POM integration appearing in the resolved runtime set
   is treated as a failure and excluded
   explicitly.
3. The 6.0.10 coordinate resolves to an official Maven artifact whose git
   hash matches the pack-recorded `ac0c444d9828da3453ae8cc65338e8de063286fb`
   (or, failing that, a tested-compatible coordinate recorded with
   evidence). Metadata lists builds 217..281; the pack cache jar
   (SHA-256 `EF87FE5709F1BA1F5B8BB20A2925B5AFB4669E178FD6D8BF10C167759EEFE37A`)
   is a candidate reference - never claim the exact production build is
   pinned without the match or a compatibility test.
4. Version pins verified against jar/POM metadata with a recorded
   comparison table. The Registrate mismatch (requested +62, bundled +67)
   is either satisfied on the legacy profile or escalated in writing -
   never silently changed. Build props alone are not used to claim a
   runtime minimum; the 6.0.10 jar's declared NF `[21.1.219,)` / Ponder
   `[1.0.82,)` minimums are honored (NF 213 + Ponder 1.0.66 + 6.0.10 is
   invalid and must not be configured).
5. A dedicated server run of the built jar plus minimal deps completes
   boot with zero client-only class load errors; log captured.
6. Isolated copies of the RC2 pack client and server runtime are created
   OUTSIDE the pack repo, proper side resolution applied (client-only /
   both / server mods per catalog), and hash-verified against the
   Compatibility.md snapshot; verification commands + outputs recorded.
7. No files in the modpack repo or any live world are modified at any
   point.

## Context the code cannot infer

- Settled pack profile, jar hash evidence, config surfaces, and isolation
  rules: docs/Compatibility.md. Version contradiction table:
  docs/Research.md.
- Create dev branch is 6.0.11 - not valid pinned API evidence.
- Do not silently downgrade pack versions to older prompt values, and do
  not publish support claims from metadata alone.

## Scope

### In scope

- ModDevGradle bootstrap, gradle.properties pins, two build profiles,
  empty mod entrypoint + `neoforge.mods.toml`, dependency hygiene,
  server-boot smoke, isolated pack copy preparation.

### Non-goals

- No gameplay code, no shapes, no assets beyond what smoke-boot needs.
- No CI workflows, no publishing, no remote creation.
- No mutation of the modpack repository or any world.
- No compatibility claims - that proof is issue 13.

## Dependencies and open decisions

- Depends on: none.
- Feeds: every later issue (build), issue 13 (isolated pack copies).
- Open: OD-1 (exact 6.0.10 Maven coordinate), OD-2 (Registrate +62 on
  legacy profile). Package root settled: `net.createcontour` (ADR-010).

## Verification

- Executed commands + captured logs for each AC; jar SHA-256 list;
  dependency report excerpt; server boot log; pack-copy hash verification
  output.

## PR contract

Body must restate intent, expectation, AC status, non-code context, scope
and non-goals, affected areas/ownership, dependencies, verification
evidence, and open decisions per `.github/pull_request_template.md`.
