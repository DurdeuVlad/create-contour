# Agent Rules - Create: Contour

Operating rules for this repository. `docs/Decision.md` holds the accepted
ADRs; this file is the fast contract.

## Authority gate

- This is a **local, private, single-owner repository**. Publication is
  undecided.
- Never commit, push, tag, create remotes, create GitHub issues/PRs/releases,
  publish artifacts, or perform any outward or destructive action without
  explicit owner authorization.
- Never modify any other repository, including `rustic-craft-2-modpack`,
  `mc-pilot` (except when an issue's test procedure explicitly calls for its
  use), or any competitor project directory.

## Clean room

- Owner-approved runtime exception (2026-11-03): pack jars may be copied
  and loaded ONLY as black-box dependencies in an ignored, isolated test
  workspace. Missing runtime artifacts may be downloaded from official
  pack sources. This does not authorize inspecting competing source/art,
  hashing, decompiling, comparing or adapting competing mods, or bundling
  them in Contour. Original repositories and worlds remain untouched.
- Permitted references: official Create sources and POM/metadata from
  `maven.createmod.net` and `github.com/Creators-of-Create/Create`, official
  NeoForge/ModDevGradle documentation, and Minecraft/Mojang data.
- **Never read, copy, hash, decompile, diff, or adapt code or assets from
  competing or similar mods.** Forbidden sources include, without
  limitation, any "copycat"-style addon sources, and the local directories
  `copycats`, `Mantelcats`, `dwurdy*`, or any other look-alike project
  found on this machine or online. Metadata-only facts (a project exists,
  its name, its license field) may be recorded for collision checks;
  source content may not. Never perform source- or byte-level comparison
  against those projects - the comparison itself would read them and
  breach the clean room. Provenance scans run against THIS repo only
  (references, imports, package names, dependency declarations).
- A provenance audit documents original-authorship claims and required
  attributions; it cannot mathematically prove absence of similarity.
  State that limit; never claim it proves more.
- Any code copied or adapted from Create (MIT) must carry the FULL
  applicable MIT copyright and permission text in the file or a
  co-located notice. A source URL alone is not sufficient attribution.
- All textures, models, and other assets in this repo must be original.
  Create code is MIT-licensed; Create art assets are All Rights Reserved
  and must not be reused.

## Evidence discipline

- Never claim a test, build, or check passed without the executed command and
  inspected output. When something has not been run, write `NOT RUN` or
  `UNSUPPORTED`, not "should work".
- Never fabricate jar contents, version availability, screenshots, or logs.
- Record the evidence file or log path alongside each claimed result.
- Current stage: private three-shape prototype and isolated pack-test
  preparation. Only the precise checks in `docs/evidence/manifest.md`
  have executed. Full-pack, train, schematic and multiplayer acceptance
  remains `NOT RUN` until corresponding evidence is recorded.

## Documentation contract

- `docs/` is the specification surface. Code must not silently depend on an
  inferred rule that is not written down in Business, Decision, Architecture,
  or an issue draft.
- Issue drafts in `docs/issues/` carry the executable contract
  (Intent/Expectation/Acceptance/Verification). Statuses must be honest:
  `draft`, `ready-for-handoff`, `in-progress`, `dependency-blocked`,
  `blocked`, or `completed`.
- Any eventual PR must restate the full contract in its body using
  `.github/pull_request_template.md`; linking an issue is not a substitute.

## Style

- Documentation is plain ASCII Markdown. Keep files focused; no empty ceremony
  documents.
