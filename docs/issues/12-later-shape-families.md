# Issue 12: Later connected/interactive/curved shape families (backlog)

| Field | Value |
| --- | --- |
| Status | draft (backlog - not ready for implementation) |
| Milestone | M4 |
| Type | epic (bounded families) |
| Suggested labels | `shape`, `backlog`, `needs-spec` |
| Priority | proposed-low |
| Owner | Unassigned |
| Dates | Unscheduled; no promised dates |

## Intent

Bound the remaining shape roadmap so later work is explicit and split into
per-family specs BEFORE any implementation - no silent scope growth.

## Expectation

- Candidate families (pane already shipped in issue 10 - no duplicates):
  fence, fence_gate, trapdoor, door, layers 1..8, slopes
  (straight + inner + outer), quarter-round (+corner variant).
- The positional panel family (face/edge/middle/diagonal positions) has
  its own spec at issue 14; its implementation gate and issue-10 panel
  overlap are resolved there, not here. True multipart panels (several
  sharing one block space) remain an unscoped later decision (ADR-013).
- Expanded form catalog (2026-11-03 feature-level survey of public
  feature lists - names only, NO source/assets/binaries inspected):
  - fractional/parametric: byte (1/8 cube), resizable slices,
    stacked half-layers - prefer parametric families over per-shape
    blocks (competitor breadth pattern, ours follows the same idea via
    position/thickness enums).
  - sloped: slope, corner/inner slope, prism corners, slope-slab
    hybrids, sloped prism.
  - linear: half pillar, post, rod.
  - connective: bars, lattice, chain.
  - openable: sliding/folding door variants.
  - functional attachments: plate, button, lever, ladder, sign, torch,
    item frame, cogwheel, shaft, pipe (Create-flavored; big UX surface).
  - special: ghost (no collision), one-way window, glow-face, secret
    storage, collapsible.
- GOLDEN RULE (owner, 2026-11-03): every shipped form must sit
  correctly next to ANY other block - no z-fighting, no face-culling
  leaks, correct occlusion/lighting/connection against arbitrary
  neighbors. This is an acceptance criterion for every family spec,
  not an aspiration.
- Each family gets its own geometry/interaction spec sub-issue before
  implementation; this epic tracks them.
- Interactive states (door halves, gate open) carry full behavioral proof.
- A separate publication-authorization gate applies before any release
  work under this milestone (ADR-007).

## Acceptance criteria (epic level)

1. Per family, a sub-issue exists covering: geometry + blockstate
   properties, model/cropped-quads spec, connectivity rules, interaction
   states, wrench family membership, resource list, and the standard
   six-material + safe-failure + transform matrix.
2. Interactive families specify and verify:
   - door: paired halves, hinge side + open state consistency, redstone
     response, pathing/navigation impact, no data loss between halves;
   - fence_gate: open/closed collision correctness, redstone response,
     state preserved under transforms;
   - trapdoor: open/closed + half + facing correctness;
   - fence/pane-style connectivity: neighbour add AND removal.
3. Layers 1..8: every thickness state renders = collides; no z-fighting
   or volume loss between adjacent states.
4. Slopes (straight/inner/outer) and quarter-round (+corner): mirrored/
   rotated correctness incl. collision; texture rotation config honored.
5. Every family runs the full per-shape proof standard of issue 10
   (resources, six-material matrix, workflows, transforms, safe failure)
   plus relevant pack re-verification where it touches declared risk
   surfaces.
6. No losses or duplications of material/data under any interaction,
   including paired-half door operations.
7. Publication readiness steps (packaging, metadata, name re-checks per
   ADR-001) are their own gate and require explicit owner authorization.

## Context the code cannot infer

- This epic intentionally stays `draft`: families are bounded but no
  geometry is final until each sub-issue is written and approved.
- Issue ordering inside the epic is by train usefulness, decided per
  family spec.

## Scope

### In scope

- Sub-issue specs for the listed families; later, their implementation
  under the same proof standard.

### Non-goals

- Implementation now; any unlisted shape family; any publication action.

## Dependencies and open decisions

- Depends on: 10 complete (pane shipped; total ten proven).
- Feeds: potential release candidates (owner authorization required).
- Open: per-family sub-issue specs; family ordering; publication target.

## Landed status (2026-11-05)

102 shapes registered. Families shipped under this epic: layers 1-8,
post, byte, panel (issue 14), wall, pane, fence, fence_gate, slope,
trapdoor, ladder, lever, door, slice, corner_slope, button,
inner_slope, pressure_plate, ghost, torch, one_way, pillar, trim,
frame, pyramid, lamp, half_slope, notch, rod, lattice, quarter_round,
chain, round_corner, dome, ridge, tube, gutter, bracket,
vertical_stairs, arch, cylinder, pilaster, stair_corner, sphere,
cone, capital, bullnose, cornice, baluster, finial, scotia, ogee,
sign, torus, crenellation, barrel_vault, cog, wheel, scaffold,
gothic_arch, louver, elbow, handrail, newel, balustrade,
i_beam, portcullis, flange, buttress, arrow_slit, dentil, tee,
palisade, quoin, truss, sill, bollard, mullion,
coffer, keystone, pedestal, lancet, valley, cross, pipe cap, pediment, chimney, half timber, spandrel, arc wall, rosette, flying buttress, coping, oculus, awning, buffer, smokestack, semaphore, pilot.
Verified: 126/126 GameTests, client smoke 306251/0 checks, 7-angle
visual scene (run/screenshots/wave-visual-*.png; wave-visual-g shows
the wall sign rendering "CONTOUR / SIGNS / WORK" in the block-entity
renderer plus the torus ring, crenellation teeth, and the barrel
vault's half-cylinder shell with its gable end cap). The sign family
is the first behavioral shape: dedicated ContourSignBlockEntity
(4 editable lines, NBT+writeSafe persistence), SignTextPayload
network update, ContourSignEditScreen, and ContourSignRenderer for
client-side text on the plate face.
The cog and wheel add Create-flavored mechanical-silhouette shapes
(decorative, not real gearboxes); the scaffold adds the first
mobility behavior - NeoForge isLadder on every face plus a walkable
top platform, so it climbs free-standing unlike the wall-mounted
ladder. The gothic arch completes the arch family (semicircle +
pointed two-centered); the louver adds the slatted-vent sheet. The
elbow completes the pipe-run family with contour_tube: a quarter-
torus bend on all 12 cube edges, reusing ContourTrimBlock.Edge for
placement, state, rotate/mirror, and wrench cycling; its mouth
disks are centered at 8px to match tube cross-sections. The
handrail is the stair-run companion: a sheared 2px rail at the
stair pitch (26.57 degrees) plus posts at both ends, FACING the
ascent direction with mirroring for all four facings. The newel
is the turned anchor post (plinth, stem, collar, ball-knob apex via
emitLathe); the balustrade is the flat counterpart of the handrail -
top rail, foot rail, three balusters, and half end posts that pair
into full posts between adjacent segments. The i-beam is the
industrial girder primitive (flanges + web on 3 axes); the
portcullis is the castle drop-gate (pointed bars + straps in a
wall-parallel panel), completing the fortification kit with
crenellation and gothic arch. The flange completes the pipe-run
family (tube + elbow + flange): an r7 ring plate with r3 bore and
four bolt studs on all six faces. The buttress is the stepped wall
rib (16/12/8/4 descending bands) that completes the fortification
kit for gothic exteriors. The arrow slit is the last missing
fortification opening (slit panel with sill + lintel); the dentil
completes the classical moulding set (cove, scotia, ogee, bullnose,
cornice, dentil) with TOP/BOTTOM half mounting. The tee
completes the pipe manifold family (tube, elbow, flange, tee): a
straight r3 run plus a perpendicular r3 branch on 12 orientations,
with three centered mouths matching tube cross-sections. The
palisade is the pointed-log stockade (four posts with two-stage
tips and back rails) - the early-fort wall counterpart of the
portcullis. The quoin wraps any cube edge in alternating long/short
corner stones (the classical dressed-stone corner). The truss is an
open triangular roof frame - bottom chord, king post, web posts,
and sheared top chords - for roofs, gables, and industrial sheds.
The sill is a sloped window ledge (weather-shedding top, flat
bottom, drip edge) using the wall-side FACING convention. The
bollard is a chunky turned post (plinth, shaft, collar, dome) for
docks and street furniture. The mullion is a casement window
grille (frame + cross, four open panes) completing the window kit
with the sill. The coffer is a recessed border-frame panel on all
six faces for ceilings, wainscot, and floor medallions. The
keystone is the tapered voussoir crowning any arch or gothic
arch. The pedestal is a square plinth/shaft/cap stand for
statues, table bases, and rail terminals. The lancet is the
gothic narrow window (jambs, stepped pointed head, sill) pairing
with the mullion for openings. The valley is the V-flashing where
two roof slopes meet - the inverted counterpart of the ridge. The
cross is the four-way manifold junction completing the small-bore
pipe family (elbow, flange, tee, cross); the pipe cap is the
closed end plug (r3 stub, r4 plate) sealing any pipe run. The
pediment is the stepped triangular gable face crowning walls and
doorways, completing the roof grammar. The chimney is the
roofline stack (plinth, tapered shaft, lip band) on three axes
for upright and lying-down flues. The half timber is the
Tudor framing panel (perimeter frame plus a diagonal brace that
flips rising/falling), and the spandrel is the right-triangle
corner panel filling stair and arch junctions - the asymmetric
counterpart of the pediment. The arc wall is the
quarter-annulus curved wall segment for round towers and bays -
its radial end faces land on the block faces so straight walls
join flush. The rosette is the turned medallion (disc plate plus
domed boss) mountable on any face. The flying buttress is
the gothic flyer - a pier topped by a quarter-arc strut springing
into a wall abutment, the arched counterpart of the stepped
buttress. The coping is the stepped wall-top cap finishing flat
walls the way the ridge caps a gable.
Remaining backlog: sliding/folding doors, Create-flavored
functional attachments, multipart panels (ADR-013).

## Verification

- Epic-level: presence + completeness of sub-issue specs.
- Per-family: same evidence standard as issue 10, recorded per
  docs/Testing.md.

## PR contract

Full restatement per `.github/pull_request_template.md`.
