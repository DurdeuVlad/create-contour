# Issue 02: Legal hygiene, provenance register, and original neutral assets

| Field | Value |
| --- | --- |
| Status | ready-for-handoff |
| Milestone | M0 |
| Type | task |
| Suggested labels | `legal`, `assets`, `foundation` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Make the project's legal posture auditable and produce the original neutral
fallback assets (texture, model, lang) the render safety contract depends
on, before any rendering code exists.

## Expectation

- MIT `LICENSE` present and correct ("Create: Contour contributors", 2026).
- Credits acknowledge Create (code MIT / assets ARR) without reusing its
  assets.
- A provenance register lists every adapted or third-party snippet with
  source URL and license; an empty register is acceptable only if
  everything is verified original.
- Original neutral fallback texture + baked model JSON + lang fixture are
  authored and inventoried.
- Zero competitor code or assets anywhere in the repo.

## Acceptance criteria

1. `LICENSE` matches the approved MIT text; README/credits link the Create
   repository and state the MIT-code/ARR-assets distinction.
2. `docs/Provenance.md` (or NOTICE) records every adapted/third-party
   element: file, source URL, license, author. Any code copied or adapted
   from Create (MIT) carries the FULL applicable MIT copyright and
   permission text in the file or co-located notice - a source URL alone
   is not sufficient attribution. An empty register is acceptable only
   with an explicit "all original" attestation and how that was verified.
3. Asset inventory lists each shipped texture/model/lang file and asserts
   original authorship; the neutral texture is authored in this repo (not
   recolored from Create or any other mod).
4. A recorded forbidden-reference/dependency scan of THIS repo only:
   search our sources, build files, and resource trees for references,
   imports, package names, license headers, or dependency declarations
   pointing at competitor projects (forbidden set per AGENTS.md) - method
   and result documented; zero matches. This scan MUST NOT open, read,
   hash, decompile, or diff any competitor source or asset: no
   source-level comparison is ever performed (doing so would itself
   breach the clean room).
5. The provenance audit documents original-authorship claims and
   attributions; it cannot mathematically prove absence of similarity and
   must not claim to. State that limit explicitly in the register.
6. Neutral fallback visually verified in a real client render-neutral
   screenshot - this item depends on issue 01 providing a runnable client;
   all other items can complete in parallel.
7. No claim of trademark or legal clearance; name-collision status stays
   as documented in docs/Research.md (bounded, recheck pre-publication).

## Context the code cannot infer

- Clean-room rules and the forbidden-source list live in AGENTS.md.
- The neutral fallback is the "no material" identity for every shape;
  it must look intentionally neutral, not like a missing texture.
- Publication is undecided; this issue creates no external records.

## Scope

### In scope

- LICENSE/credits, provenance register, asset inventory,
  repo-internal forbidden-reference scan, neutral texture/model/lang
  authoring.

### Non-goals

- No shape-specific models/textures (issues 04+).
- No trademark search, legal advice, or publication.

## Dependencies and open decisions

- Depends on: none for authoring; AC 6 depends on issue 01.
- Feeds: issue 03 (fallback boundary), all shape issues (asset conventions).
- Open: final neutral texture art direction (owner review).

## Verification

- Files present and reviewed; commit only if explicitly authorized.
  Repo-internal forbidden-reference scan command + output recorded (the
  scan reads only this repo); render screenshot path for AC 6 (or
  `BLOCKED on 01` honestly).

## PR contract

Full restatement per `.github/pull_request_template.md`, including the
provenance register diff and the forbidden-reference scan evidence.
