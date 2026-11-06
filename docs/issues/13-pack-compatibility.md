# Issue 13: Rustic Craft 2 full-pack compatibility proof (mandatory gate input)

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M2 |
| Type | verification |
| Suggested labels | `compatibility`, `pack`, `verification`, `gate` |
| Priority | proposed-high (feeds the expansion gate) |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Prove the addon actually runs inside Rustic Craft 2 - the real declared
profile, not an approximation - by executing every pack risk surface as a
required acceptance case on isolated copies identified under ADR-011:
permitted official Create hashes plus catalog and actual loaded metadata
for competing runtime-only jars, never whole-runtime byte-hash claims.

## Expectation

- Isolated copies of the RC2 client and server runtime (stable 2.2.2,
  NF 21.1.248, Create 6.0.10, full declared mod list per
  docs/Compatibility.md) run the addon.
- A dedicated pack server plus two pack clients exercise join, render,
  recipe, train, schematic, and save/reload scenarios.
- A pack boot baseline WITHOUT the addon is recorded first; baseline
  failures are documented, never blamed on the addon and never waived.
- The issue passes only when every required scenario is observed.

## Acceptance criteria

1. Isolation verified against the Compatibility.md snapshot using ADR-011's
   permitted identity evidence (official Create hash, catalog identity and
   loaded metadata for competing jars); proper side resolution applied
   (client-only vs both vs server mods); addon jar added on top. No
   mutation of the original pack repo, tracked worlds, or live servers;
   any live/external server action requires recorded owner permission.
2. Baseline: unmodified isolated pack boots server + client; any baseline
   failures recorded with logs before the addon is introduced.
3. Dedicated pack server + 2 pack clients join with the addon; zero join
   regressions; effective loaded mod lists and client-effective configs
   captured per side (server config files alone are not proof).
4. Connector + ConnectorExtras present and functioning; the addon loads
   alongside them with no errors (they are pack members, never added to
   the minimal profile).
5. JEI 19.57.0.449: `create_contour` recipes visible and correctly
   namespaced; game unaffected with JEI absent in a variant profile.
6. KubeJS: pack script reload runs clean with the addon; original Create
   recipes unbroken (incl. `create_netherless_pyro.js` chains); our
   recipe tags coexist.
7. Render profiles: shaders OFF and Solas ON (SolasShaderV3.7b per
   catalog); Sodium/Iris are never disabled as a workaround; tinted
   materials (leaves), transparency (glass), and axis materials correct
   under both profiles; resource reload (incl. Excalibur resource pack
   active) produces no missing/stale models - appearance inspected, not
   asserted.
8. Trains: shape-decorated train assembled, driven, disassembled on the
   pack runtime incl. Threaded Trains 1.0.0 loaded; no regressions vs
   baseline.
9. Adjacent geometry with Create Connected 1.3.3 blocks: placement,
   connection, and render behave correctly next to the other addon's
   blocks.
10. Create Aeronautics 1.3.2: at minimum assembly / render while
    assembled / disassembly of a contraption containing our shapes -
    observed and recorded.
11. Create Schematic Checker 1.21.27-6.0-neoforge (catalog entry is
    `unverified:true`): allow-list handling and item requirements for our
    shapes verified, OR an explicit blocker recorded with evidence.
12. World save/reload on pack runtime incl. missing-material fixture;
    chunk remesh across repeated resource reloads.
13. All 3 shapes x 6 materials place/paint/cycle/break verified on the
    pack runtime, both clients, screenshots + logs.
14. Every case recorded per docs/Testing.md template; verdict per case:
    pass/fail/unsupported. Required-unsupported or failed cases keep the
    gate blocked - this issue passes only when all required scenarios
    are observed.

## Context the code cannot infer

- Declared-vs-tested distinction: catalog metadata declares the player
  pack; it does NOT prove the deployed live server tuple - capture the
  effective runtime instead of asserting it (Compatibility.md).
- `create-client` `explainRenderErrors=false` may be enabled only inside
  the disposable test copy.
- Fixtures stay within pack `create-server` `maxBlocksMoved=2048`.
- Risk surfaces become acceptance cases; none become hard dependencies.

## Scope

### In scope

- Pack runtime verification only, on isolated copies, per the case list.

### Non-goals

- Fixing pack baseline failures (recorded and routed to the owner).
- Minimal-profile runs (issue 09 owns those as reproduction env).
- Any claim about the live server deployment.

## Dependencies and open decisions

- Depends on: 01 (isolated copies prepared), 07, 08 (mechanics proven on
  dev env).
- Feeds: 09 (the expansion gate requires this issue - one direction only,
  no cyclic dependency).
- Open: live-server tuple (residual OD-6) remains unverified by design.
- Current blocker: strict pre-addon client crashes in CustomSkinLoader.
  The isolated server starts with baseline errors. A modified patch-failure
  bypass profile joined but is not accepted against criterion 2.
  See `docs/evidence/Pack-Baseline-Assessment.md`; no strict addon pack pass.
  The instrumented diagnostic passes its bounded matrix on the fixed jar:
  18 creative shape/material paints + 3 survival consumption cases
  (leaves resolved via selection-outline eligibility), a bounded
  same-world save/reload persistence pass, and a same-host two-client
  block-entity sync pass, and a native place/break/drop trace pass with a
  correct tool (the pack denies bare-hand drops for `mineable/pickaxe`
  blocks - identical control on `create:andesite_casing`; the ledger drop
  carries material+paid+facing). Native wrench cycles,
  missing-material fixture, regression sweep and renderer/profile gates
  remain NOT RUN; this does not clear the baseline or full
  first-three acceptance criteria. See
  `docs/evidence/Pack-Diagnostic-Assessment.md`.

## Verification

- The full Testing.md evidence set: per-case records with commands,
  versions, fixtures, screenshots, logs and the identity evidence permitted
  by ADR-011. Whole-runtime hash identity is not established.

## PR contract

Full restatement per `.github/pull_request_template.md`.
