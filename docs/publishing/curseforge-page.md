# CurseForge Publishing Package - Create: Contour

Everything the owner needs to publish. The agent prepared these artifacts;
only the owner can create the project and upload files (CurseForge account).

## Project page fields

- **Name**: Create: Contour
- **Summary**: Create-native paintable shape blocks - 114 forms that copy any
  block's material - plus the Contour Placer and Codex.
- **License**: MIT (LICENSE file included in jar and repo)
- **Categories**: Create addons; Building; Cosmetic (suggest Create + Building)
- **Avatar**: `docs/publishing/curseforge-avatar.png` (128x128 preview of
  `src/main/resources/logo.png`; upload the 400x400 source if asked)
- **Issues**: https://github.com/DurdeuVlad/create-contour/issues
- **Source**: https://github.com/DurdeuVlad/create-contour

## Project description (paste into the page body)

Create: Contour adds **114 Create-native shape blocks** that copy the material
of any block you paint them with - slab, vertical slab, stair, panel,
half-panel, byte-panel, flat pane, slope, layer, slice, frame, wall, fence,
ladder, pillar, catwalk, door, trapdoor, iron door, iron trapdoor, sliding
door, folding door and many more architectural forms.

**How it works**

- Craft blank **Contour** shapes, place them, then apply any full cube block
  to paint the shape with that material.
- The **Contour Placer** turns placement into a tool: pick the shape on its
  rear display, left-click to place, right-click to swoop a shape back into
  blank ammo and refund its material.
- The **Contour Codex** documents every shape and gesture in-world.
- Layers, half-layers and slices stack like snow when the material matches.
- Wrench cycles orientation; all shapes waterlog; doors pair, sync and obey
  redstone (iron variants are redstone-only).

**Create integration**

- Contraptions, trains and Create schematics supported: the schematic
  checklist lists required materials and paid ledger credit survives
  serialize/deserialize.
- Mechanical-crafting recipes for every shape.

**Requirements**: Minecraft 1.21.1, NeoForge 21.1.x, Create 6.0.x.

*Alpha: all 114 shapes and QoL systems are implemented and tested
(144/144 GameTests, 318k+ render checks). True multipart plates and
kinetic/fluid disguises are on the roadmap.*

## File upload metadata (per release)

- **Release type**: Alpha (while 0.x) / Beta (once multipart lands)
- **Game versions**: 1.21.1
- **Mod loader**: NeoForge
- **Required dependencies**: Create (create) 6.0.x — Flywheel and Ponder ship
  inside Create, do not mark separately
- **File to upload**: `create_contour-X.Y.Z.jar` from `build/libs/`
- Attach the `.sha256` and `.sha512` files in the file description or a
  checksums text block (CF does not take separate checksum uploads; paste
  them into the changelog instead)

## Changelog template (for the file page)

    ## X.Y.Z
    - bullets of changes
    ## Verification
    - runGameTestServer: N/N
    - runClientQA: N checks / 0 failed

## Owner checklist (cannot be automated)

1. Create the CurseForge project (name/avatar/categories/license=MIT).
2. Set the GitHub source + issues links.
3. Paste the project description above.
4. Upload the jar, set release type Alpha, game version 1.21.1,
   loader NeoForge, mark Create as required dependency.
5. Paste the changelog; include the checksum lines from
   `build/libs/*.sha256` and `*.sha512`.
6. Optional later: add `curseforge` gradle upload task with an API key
   (owner-held secret - not stored in the repo).

## Repo-side state (done)

- [x] LICENSE: MIT, in repo root and jar
- [x] mods.toml: displayName, description, authors, logoFile, displayURL,
      issueTrackerURL, dependencies (neoforge/minecraft/create)
- [x] logo: src/main/resources/logo.png (original art, 400x400)
- [x] avatar: docs/publishing/curseforge-avatar.png
- [x] README refreshed for public readership
