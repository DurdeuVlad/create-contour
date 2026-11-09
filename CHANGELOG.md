# Changelog

All notable changes to Create: Contour will be documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Documentation

- Added an AI disclosure to the README.
- Reconciled stale statements: the repository is public (not "local,
  private"), ADR numbering is unique, and the pack-test claim now states that
  it was a diagnostic-profile observation.

## [0.2.1-alpha]

Per-release notes between 0.1.0-alpha and 0.2.1-alpha were not recorded when
those versions were cut. This entry lists only what the repository documents
today.

### Status

- Catalog: 114 paintable shapes (README, `docs/publishing/curseforge-page.md`);
  0.1.0-alpha listed 106.
- Verification as recorded in README.md and docs/Testing.md:
  `runGameTestServer` 144/144 required tests, `runClientQA` 318,503 checks /
  0 failed.
- Not yet exercised end-to-end: multiplayer two-client sync, train
  contraption journeys, in-client Ponder playback, a real schematicannon run.
- Open roadmap: true multipart plates (ADR-016) and kinetic/fluid disguises.

## [0.1.0-alpha] - 2026-11-06

First public alpha. Private prototype; play-testing and API feedback welcome -
recipes, ledger rules, and shape set may still change.

### Added

- 106 paintable Contour shapes across architectural, railway, and utility
  families: slabs/vertical slabs/stairs, fences, walls, panes, panels,
  doors/trapdoors, ladders, layers/slices, bytes, ghosts, mouldings,
  arches/vaults, roof forms, columns/capitals, windows (lancet, oculus,
  arrow slit, awning, mullion), train pieces (buffer, smokestack,
  semaphore, pilot), and utility forms (beam, vertical step, half panel,
  flat pane, i-beam, cog, wheel, lamp, lever, button, pressure plate,
  sign, torch, lattice, scaffold, tube/pipe forms, and more).
- Material-copying: right-click a Contour shape with a solid block to
  copy its appearance; wrench cycles orientation/family; sneak-wrench
  picks the block up.
- Blank-item economy (ADR-014): shape items are always unpainted;
  breaking, wrenching, or swooping returns the blank shape item(s) plus
  the copied material. Multi-unit hosts refund per unit.
- Contour Placer (`contour_placer`): places any catalog shape from
  `contour_blank` feedstock. Sneak+right-click opens the rear-screen
  selector, left-click places, right-click swoops a shape back into
  feedstock + material. Craftable manually and via mechanical crafting.
- Contour Codex: in-game shape catalog/selector for the full registry.
- Original Ponder tutorial scene for the placer (Create's ponder engine,
  registers on the placer and blank items).
- Create schematic compatibility: SafeNBT keeps material/orientation
  with the printed-ledger rule (paid credit mirrors what the checklist
  charges); material requirements union correctly with host items.
- Contour block entity: owned material/paid-ledger fields, schema
  versioning, frozen unknown-future-version projection, canonical
  orientation, transform support for rotation/mirror (trains,
  contraptions, schematics).
- Full recipe coverage (manual + Create mechanical crafting), creative
  tab, loot tables, waterlogging on every shape.

### Fixed

- Placement merge/sneak placement resolves the clicked Contour block's
  material correctly during context construction.
- Schematic checklist for multi-unit hosts (double slab) no longer
  under- or over-charges shape items.

### Verification

- 136/136 required GameTests pass (`.cache/gt_wave49.log`).
- 309,575/0 client render-smoke assertions across all blockstates,
  materials, item models, and grid masks (`.cache/client_qa89.log`).
- Ponder scenes register cleanly in-client.

## Pre-0.1.0 prototype history (historical)

The entries below describe the private prototype that preceded 0.1.0-alpha.
They are kept for the record and do not describe the current release.

### Added (prototype - private, NOT for installation)

- First three shape families (slab, vertical slab, stairs) with owned
  block entity (material, paid-ledger, canonical orientation, frozen
  future-version projection), reservation-based placement accounting,
  sneak sample/paint, wrench cycle/pickup, ledger-split drops.
- Client render pipeline: `ContourModel` (extends Create CopycatModel)
  wraps every blockstate + inventory model; 2x2x2 occupancy-grid quad
  emission (`ShapeQuads`); owned `refreshMaterialAppearance` seam for
  aux light + live remesh; item binding through `applyTransform` +
  render passes/types; CTM UV-shift handling; block/item colors;
  opt-in `create_contour.renderSmoke` runtime assertions.
- Verification: 23/23 GameTests (current log indexed in
  `docs/evidence/manifest.md`), 4292/4292
  client smoke (`runclient-qa-leaf.log`), earlier native live-paint screenshots
  (slab/vertical/stairs stone->glow), both build profiles green with
  persisted logs and own-jar inventories.
- Registrate pin settled to `+67` (recorded deviation: official Create
  6.0.8 declares +67, not the originally requested +62).

### Fixed

- Creative placement no longer copies an unconsumed prepaid frame's credit.
- Unsupported stored materials fall back to neutral before material billing.
- Explicit missing/invalid stored material cannot retarget stale credit to
  the offhand or looked-at material.
- Slab merges preserve pre-placement credit rather than trusting vanilla
  Ctrl+pick block-entity data imported during placement. Fresh neutral
  placements explicitly commit zero credit.
- These regressions were reproduced before correction; current development
  verification is recorded in `docs/evidence/Accounting-Regression-Assessment.md`.

### Added

- Noncolliding visual-source regression: registered torch eligibility and
  paid survival paint on all three hosts; empty-outline sources stay rejected.
- Corrected material validation to use selection outline, not collision.
  Pack leaf rejection was observed before correction; fixed-jar runtime
  re-verification is pending, not a strict pack compatibility pass.

- ModDevGradle bootstrap (historical foundation of the prototype above):
  Gradle 8.14.6 wrapper (checksum-pinned), Java 21 toolchain,
  `compatProfile` guard with `pack` (default; Create 6.0.10-280 /
  NeoForge 21.1.248) and `legacy` (Create 6.0.8-168 / NeoForge 21.1.213)
  profiles, minimal dependencies (Create transitive-off, JEI
  compile-time-only), `@Mod` entrypoint `net.createcontour.CreateContour`,
  own `neoforge.mods.toml` and MIT license resources.
- Planning documentation set: Business, Decision (ADRs), Milestones,
  Architecture, Testing, Research documents.
- Compatibility record for the Rustic Craft 2 pack profile snapshot.
- Local issue drafts 01-13 covering build foundation, legal/asset provenance,
  versioned block entity foundation, the first three shapes (slab, vertical
  slab, stairs), material/shape workflows, contraption/schematic fidelity,
  the Rustic Craft 2 pack compatibility proof, the expansion proof gate,
  expansion to ten shapes, discoverability, and later shape families.

### Status

- Bootstrap stage: pack profile builds and dev-server smoke verified;
  legacy profile builds with the +67 Registrate deviation. No shapes, no
  gameplay code, no in-game proof yet.
