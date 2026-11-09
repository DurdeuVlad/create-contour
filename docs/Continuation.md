# Continuation - Create: Contour

The ADR-017 seam contract made enumerable: which edge profiles each
shape family presents on which faces, and which profiles a face can
continue into on the neighboring cell. "Continue" is the term from
[Decision.md](Decision.md) ADR-017: two forms on adjacent cells
continue when their interface shares a lattice plane, overlapping
lattice area, and a compatible edge profile.

Status of this file: spec surface, not evidence. Every mate pair below
is derived from the registered geometry (each shape's `boxesFor` /
`getShape` box list in `src/main/java/net/createcontour/block/`) and is
marked `pending render verification` - the continuation gallery
fixture (GitHub issue #10) captures `continuum-*` pair screenshots as
the acceptance evidence. No pair in this file is verified yet.

## 1. Lattice recap

- The cell is a 16x16x16 px grid. Every shape's collision/shape boxes
  have vertices on integer-px coordinates; every planar face is
  axis-parallel or a documented lattice diagonal whose start and end
  edges are lattice-aligned ([Decision.md](Decision.md) ADR-017,
  lattice contract).
- Occupancy channels inside one cell
  ([Decision.md](Decision.md) ADR-016a + ADR-017):
  - Octant channel: 8 disjoint 8px cubes (x<8/x>=8) x (y<8/y>=8) x
    (z<8/z>=8). SHIPPED in 0.2.1: `contour_byte` occupants 1..7 live in
    `ContourBlockEntity.byteOccupants` (`ByteExtras`); the
    `byteComposition` GameTest is the recorded evidence
    ([Testing.md](Testing.md)).
  - Face-quadrant channel: 24 slots (6 faces x 4 quadrants). Single-
    occupant members shipped (`contour_panel`, `contour_half_panel`,
    `contour_byte_panel`); multi-occupant composition is pending the
    owner matrix in ADR-016a / issue 5.
  - Interior-plane channel: mid-planes on X/Y/Z axes (each a 2px-thick
    center sheet). Single-occupant member shipped (`contour_flat_pane`,
    the y=7..9 sheet); multi-plane composition is pending the owner
    matrix in ADR-016a / issue 5.
- Channels never overlap within a cell; cross-channel co-residence is
  allowed only where the geometries cannot intersect (section 4).
- Boundary continuation happens between cells, never inside one:
  inside-cell composition is the channel question above, not a mate
  pair.

## 2. Edge-profile vocabulary

The named edge kinds a shape can present on a cell face. A profile is
described by its covered region on the shared face plane plus its
edge kind.

| Profile | Definition (one line) |
| --- | --- |
| flat-full | The whole 16x16 face is covered flush to the cell boundary (full-face contact). |
| flat-half | One half of the face is covered flush - an 8px band across the full face width (slab/vertical-slab side). |
| step-N | An N-riser staircase edge built from lattice-aligned boxes; N counts risers (N = 2..8). |
| diagonal-45 | A 45-degree lattice diagonal: the profile rises exactly 1 px per 1 px (wedges, diagonal panels). |
| diagonal-shallow | A non-45-degree diagonal whose slope is documented per shape (e.g., handrail stair pitch, awning drop). |
| lip/rail | A raised band running along a face edge or panel rim (catwalk rail lips, coping cap, parapet). |
| round/lathe | A curved profile - arc or revolved surface - tangent to the cell grid (cylinder, dome, quarter round). |
| open/frame | A hollow edge: bars, plates, or openings with voids between members (frame, lattice, truss, arch). |
| occupied-octant | Face coverage expressed as a subset of the four face-adjacent octant slots (byte compositions). |

Coverage finer than a half-face is named by the slot it fills rather
than a new profile: an 8x8 patch is one face-quadrant slot (it mates
exactly with occupied-octant coverage of the same quadrant), and an
N-px flat band is a restricted flat profile that only mates with equal
-or superset coverage at the same position.

Compatibility rule used by the tables below:

- A profile continues into flat-full whenever its covered region is a
  subset of the face - flat-full faces (solid cubes, double slabs,
  closed face plates, sturdy walls) are the universal mate.
- Same-kind profiles continue when their covered regions overlap
  exactly: flat-half at the same band, step-N at the same pitch and
  phase, diagonal-45 rising the same direction, open/frame members on
  the same grid, occupied-octant covering the same slots.
- Different-kind profiles with equal covered regions are also mates:
  four lower octants = flat-half low band; a quadrant patch = one
  occupied-octant slot.
- A smaller patch seats against a larger flush face when its covered
  region nests inside it (subset rule, not only flat-full): a 4x4 rod
  end seats on an 8x8 post end.
- Everything else is a documented non-continuation (section 5), not a
  defect.

## 3. Per-family mate tables

Sixteen families cover all 114 paintable shape blocks registered in
`src/main/java/net/createcontour/registry/ContourRegistries.java`
(registry-wave order inside each family). The registry holds 115
`DeferredBlock` entries in total; the 115th, `contour_blank`, is
placer feedstock - a plain neutral cube, not a shape - and is
therefore not in a family. "Side faces" means the four horizontal
directions; "ends" means the two faces on a run axis.
Every pair is `pending render verification` until the issue-10
continuation gallery captures the `continuum-*` pair shot.

### 3.1 Flat sheets - slabs and layered plates (6)

Members: `contour_slab`, `contour_vertical_slab`, `contour_layers`,
`contour_half_layer`, `contour_slice`, `contour_corner_slice`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_slab` | attach face (DOWN or UP): flat-full; side faces: flat-half on the occupied band |
| `contour_vertical_slab` | attach face: flat-full; the four perpendicular faces: flat-half (vertical strip) |
| `contour_layers` | DOWN: flat-full; side faces: flat band of 2 x LAYERS px (flat-half at LAYERS=4, flat-full at 8) |
| `contour_half_layer` | DOWN: flat-full; side faces: flat band of LAYERS px (1..8) |
| `contour_slice` | FACING face: flat-full; perpendicular faces: flat band of 2 x LAYERS px |
| `contour_corner_slice` | corner-adjacent faces: stepped wedge silhouette (stepped diagonal-45); the two faces it hugs: flat bands |

| Source | Mate | Status |
| --- | --- | --- |
| flat-half side band | flat-half at the same band: slab, stairs base course, layers LAYERS=4, half_panel, four lower/upper octants of a byte cell | pending render verification |
| flat-half side band | flat-full (solid cubes, double slab, closed face plates, sturdy walls) | pending render verification |
| flat band of N px | equal-height flat band (same LAYERS count), flat-full | pending render verification |
| corner_slice stepped wedge | corner_slice on the same corner line, notch recessed corner | pending render verification |

### 3.2 Panels and sheets - 2px plates (4)

Members: `contour_panel`, `contour_half_panel`, `contour_byte_panel`,
`contour_flat_pane`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_panel` FACE_* | mount face: flat-full (2px plate); perpendicular faces: 2px rim band |
| `contour_panel` EDGE_* | the two faces meeting at the edge: 2px edge strip bands |
| `contour_panel` MID_* | no boundary contact - interior mid-plane |
| `contour_panel` DIAG_* | diagonal-45 sheet through opposite edge pairs |
| `contour_half_panel` | mount face: flat-half (8x16); perpendicular faces: 2px rim band |
| `contour_byte_panel` | mount face: one 8x8 face-quadrant patch; perpendicular faces: 1px rim band |
| `contour_flat_pane` | side faces: 2px band at mid-height (interior plane exits the cell) |

| Source | Mate | Status |
| --- | --- | --- |
| face plate (2px) | face plate back-to-back across the boundary, flat-full, flat_pane deck edge | pending render verification |
| half_panel flat-half | flat-half bands at the same position (slab band, other half_panel), flat-full | pending render verification |
| byte_panel quadrant patch | occupied-octant coverage of the same quadrant (byte cell), matching byte_panel, flat-full | pending render verification |
| EDGE_* strip | matching EDGE_* on the shared edge, trim/quoin legs on the same edge, flat-full | pending render verification |
| DIAG_* diagonal-45 | diagonal-45 sheets/panels sharing the diagonal (slope face, corner_slice), documented inherent seam | pending render verification |
| flat_pane mid band | flat_pane edge-to-edge deck run, flat-full wall | pending render verification |

### 3.3 Octant cells - byte compositions (2)

Members: `contour_byte`, `contour_notch`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_byte` | every face: occupied-octant (the occupied quadrants of that face; 1..4 slots per face, disjoint by construction) |
| `contour_notch` | faces adjacent to the removed octant: flat-full minus one quadrant; other faces: flat-full |

| Source | Mate | Status |
| --- | --- | --- |
| occupied-octant slots | occupied-octant slots covering the same quadrants in the neighbor cell | pending render verification |
| occupied-octant slots | equal-area flat profiles: 4 lower octants = flat-half low band; quadrant patch = byte_panel plate | pending render verification |
| occupied-octant slots | flat-full (byte cluster seats against a solid face) | pending render verification |
| notch face minus quadrant | flat-full, or a quadrant member aligned to the missing slot | pending render verification |

### 3.4 Full-cube forms (3)

Members: `contour_ghost`, `contour_one_way`, `contour_lamp`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| all three | flat-full on all six faces (`contour_ghost` renders a full cube though it has no entity collision; `contour_one_way` emits no quads on its FACING side but the cell silhouette is still a full cube) |

| Source | Mate | Status |
| --- | --- | --- |
| flat-full | every profile in this file whose coverage is a subset - the universal mate | pending render verification |

### 3.5 Stairs and steps (5)

Members: `contour_stairs`, `contour_vertical_stairs`,
`contour_stair_corner`, `contour_vertical_step`, `contour_beam`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_stairs` | FACING face: flat-full; back face: flat-half low; side faces: step-2 (8px full course + 8px half riser); DOWN: flat-full |
| `contour_vertical_stairs` | FACING-opposite faces: flat-half L; facing-side faces: step-2 upright L |
| `contour_stair_corner` | side faces: flat-half low plus occupied-octant corner patches (INNER keeps three octants, OUTER one) |
| `contour_vertical_step` | wall face (FACING opposite): flat-full thin plate; side faces: flat-half low tread band |
| `contour_beam` | run-axis ends: 8x8 patch seated in the lower half on X/Z, 8x8 post cross-section on Y; side faces: flat-half low bar on X/Z |

| Source | Mate | Status |
| --- | --- | --- |
| stairs side step-2 | stairs with same facing and half (straight run), flat-full backing | pending render verification |
| stairs back flat-half low | slab, layers LAYERS=4, flat-half band at low | pending render verification |
| stair_corner octant patches | stair inner/outer corners per vanilla cornering, occupied-octant coverage | pending render verification |
| vertical_step tread band | vertical_step side-by-side run at same facing, flat-full wall | pending render verification |
| beam end patch | beam on the same axis (bar run), post/pillar shaft seats, flat-full | pending render verification |

### 3.6 Slopes and roofs - diagonal faces (7)

Members: `contour_slope`, `contour_half_slope`, `contour_corner_slope`,
`contour_inner_slope`, `contour_pyramid`, `contour_ridge`,
`contour_valley`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_slope` | FACING (high) face: flat-full; back face: no area contact; side faces: diagonal-45 triangle; DOWN: flat-full (HALF=TOP inverts vertically) |
| `contour_half_slope` | same layout at half height: side faces diagonal-45 (8px rise), FACING face flat-half low |
| `contour_corner_slope` | the two apex-adjacent faces: diagonal-45 rising to the corner; the two far faces: no area contact; base flat-full |
| `contour_inner_slope` | the two apex-adjacent faces: flat-full; the two far faces: concave diagonal-45 crease profile |
| `contour_pyramid` | all four side faces: diagonal-45 triangle; base flat-full |
| `contour_ridge` | run ends: peaked gable profile (diagonal-45 pair); side faces: diagonal-45 ramps to the ridge line |
| `contour_valley` | run ends: V profile (two diagonal-shallow drops to center); side faces: diagonal-shallow slopes |

| Source | Mate | Status |
| --- | --- | --- |
| slope diagonal-45 side | slope with the same facing (ramp run), corner_slope matching edge, flat-full | pending render verification |
| slope high flat-full face | flat-full (roof deck into wall/gable), another slope's high face | pending render verification |
| half_slope low diagonal-45 | half_slope run, slope base edge at matching height | pending render verification |
| ridge gable end | ridge on the same axis (gable run), pediment stepped gable | pending render verification |
| valley V end | valley on the same axis, gutter channel end | pending render verification |
| inner_slope crease | corner_slope/inner_slope roof corners at matching pitch | pending render verification |

### 3.7 Mouldings and edge trims (14)

Members: `contour_trim`, `contour_quarter_round`,
`contour_round_corner`, `contour_bullnose`, `contour_scotia`,
`contour_ogee`, `contour_cornice`, `contour_dentil`, `contour_quoin`,
`contour_coping`, `contour_sill`, `contour_bracket`, `contour_pediment`,
`contour_spandrel`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_trim` | the two faces meeting at the wrapped edge: lip/rail L-legs (4x3px) |
| `contour_quarter_round` | the two faces at the edge: flat-full legs; silhouette: round/lathe cove arc |
| `contour_round_corner` | the three faces at the corner: flat corner patches under a round/lathe sphere octant |
| `contour_bullnose` | the two faces at the edge: flat-full minus corner strip under a round/lathe r6 arc |
| `contour_scotia` | the two faces at the edge: flat legs with a round/lathe r5 concave groove |
| `contour_ogee` | the two faces at the edge: flat legs under a round/lathe S-curve on the 8px corner square |
| `contour_cornice` | wall face: flat bands; silhouette: step-4 crown (10/8/6/2 px) |
| `contour_dentil` | wall face: flat band plus hanging teeth (open/frame tooth row below the band) |
| `contour_quoin` | the two faces at the edge: alternating step blocks (long 8px legs / short 4px cubes) |
| `contour_coping` | wall top: lip/rail cap overhanging both faces; seat on the wall |
| `contour_sill` | wall face: flat back; ledge top: diagonal-shallow shed slope; underside flat |
| `contour_bracket` | wall face: flat-full 2px plate; arm silhouette: step-2 corbel |
| `contour_pediment` | wall face: centered 3px panel; face silhouette: step-4 gable |
| `contour_spandrel` | wall face: centered 3px panel; face silhouette: step-4 right triangle |

| Source | Mate | Status |
| --- | --- | --- |
| edge legs (trim, quarter_round, bullnose, scotia, ogee, quoin) | the same moulding on the adjacent cell along the edge axis - the profile chains along its run | pending render verification |
| edge legs | flat-full backing on the covered faces | pending render verification |
| round_corner corner cap | quarter_round ends meeting at the same corner | pending render verification |
| cornice/dentil band | cornice/dentil at the same half along the wall, flat-full | pending render verification |
| coping cap | coping along the wall axis, flat-full wall top (over wall/palisade runs) | pending render verification |
| sill ledge | sill side-by-side at the same facing (window run), flat-full | pending render verification |
| pediment gable | ridge gable end at matching rise, flat-full | pending render verification |
| spandrel triangle | stairs/arches at the matching corner geometry, flat-full | pending render verification |

### 3.8 Doors, trapdoors, gates - openable plates (7)

Members: `contour_door`, `contour_iron_door`, `contour_sliding_door`,
`contour_folding_door`, `contour_trapdoor`, `contour_iron_trapdoor`,
`contour_fence_gate`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| doors (all four), closed | hinge/jamb face: flat-full 3px plate on the FACING side of each cell |
| doors, open | leaf relocates to the hinge side / pocket / folded jamb - an open state, not a continuation face |
| trapdoors, closed | horizontal 3px plate: flat-full on the half face (DOWN if BOTTOM, UP if TOP); side faces: 3px band |
| trapdoors, open | vertical 3px plate against the FACING side: flat-full on that face |
| `contour_fence_gate` | span ends: 4px post bars matching fence rails; closed span: open/frame bar grid |

| Source | Mate | Status |
| --- | --- | --- |
| closed door leaf plate | the doorway frame: flat-full jambs, panel EDGE strips, second door leaf (double door across the pair) | pending render verification |
| closed trapdoor plate | trapdoor plate at the same half (flush floor/deck plates), flat-full | pending render verification |
| open leaf states | documented non-continuation - the leaf is parked on the hinge side by design | pending render verification |
| fence_gate end posts | fence/wall arms at matching heights (vanilla gate behavior) | pending render verification |

### 3.9 Catwalks and open frames (14)

Members: `contour_catwalk`, `contour_frame`, `contour_lattice`,
`contour_scaffold`, `contour_truss`, `contour_mullion`,
`contour_portcullis`, `contour_palisade`, `contour_arrow_slit`,
`contour_coffer`, `contour_half_timber`, `contour_lancet`,
`contour_keystone`, `contour_i_beam`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_catwalk` | rail sides: lip/rail (1px floor sheet + 8px rails); walk-axis ends: 1px floor band - open chain |
| `contour_frame` | all faces: open/frame (3px edge bars, open 10x10 fields) |
| `contour_lattice` | mount face: open/frame grate (1px bars, 4px openings) |
| `contour_scaffold` | all faces: open/frame (corner posts + mid rails); top: walkable plate |
| `contour_truss` | the FACING-normal panel: open/frame triangular members |
| `contour_mullion` | the FACING-normal panel: open/frame casement grille, four open panes |
| `contour_portcullis` | the FACING-normal panel: open/frame bar grid with pointed tips |
| `contour_palisade` | the FACING-normal panel: open/frame pointed-post row plus back rails |
| `contour_arrow_slit` | the FACING-normal panel: flat field with a 2px vertical opening (open/frame) |
| `contour_coffer` | the FACING face: lip/rail border frame with 1px-recessed center field |
| `contour_half_timber` | the FACING-normal panel: open/frame perimeter plus diagonal-shallow brace |
| `contour_lancet` | the FACING-normal panel: flat jamb field with open pointed window |
| `contour_keystone` | the FACING-normal panel: tapered wedge (step profile) in a 6px panel |
| `contour_i_beam` | run ends: I-section patch (web + flanges = open/frame section); flanges reach both side faces (flat-2px strips) |

| Source | Mate | Status |
| --- | --- | --- |
| catwalk rails + floor | catwalk along the same axis (rail lips chain, deck continues), flat-full | pending render verification |
| frame edge grid | frame identical cell, flat-full backing | pending render verification |
| lattice grate | lattice on a shared wall plane, mullion/window panels, flat-full | pending render verification |
| truss/mullion/portcullis/palisade/arrow_slit/lancet panel | the same panel member adjacent on the same plane (stockade/grille runs), flat-full backing, arch openings for portcullis | pending render verification |
| keystone wedge | arch/gothic_arch crown center (documented pairing), flat-full | pending render verification |
| i_beam section | i_beam on the same axis (column/girder runs), flange strips seat on flat-full | pending render verification |

### 3.10 Posts, columns and beams (6)

Members: `contour_post`, `contour_rod`, `contour_pillar`,
`contour_pilaster`, `contour_capital`, `contour_pedestal`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_post` | run ends: centered 8x8 patch; side faces: no boundary contact |
| `contour_rod` | run ends: centered 4x4 patch; side faces: no boundary contact |
| `contour_pillar` | run ends: capital/base flare patches (10px over 8px shaft) |
| `contour_pilaster` | wall face: flat-full chord; silhouette: round/lathe half-column |
| `contour_capital` | attach faces: stepped patches (6px column width -> 4px taper -> 6px abacus) |
| `contour_pedestal` | run ends: 12px plinth/cap patches; mid: 8px shaft |

| Source | Mate | Status |
| --- | --- | --- |
| post/rod end patch | same member on the same axis (post->post column), pillar shaft, capital seat, pedestal cap, flat-full | pending render verification |
| pillar flare ends | post shaft, capital/pedestal stacking (column kit), flat-full | pending render verification |
| pilaster wall chord | flat-full wall face; pilaster stacks vertically | pending render verification |
| capital abacus | cylinder/pillar shaft below, flat-full ceiling/floor above | pending render verification |

### 3.11 Lathe-turned and round forms (13)

Members: `contour_cylinder`, `contour_sphere`, `contour_dome`,
`contour_cone`, `contour_torus`, `contour_baluster`, `contour_finial`,
`contour_newel`, `contour_bollard`, `contour_rosette`,
`contour_smokestack`, `contour_oculus`, `contour_chimney`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_cylinder` | run ends: round/lathe r6 disc patch; side faces: tangent line contact only |
| `contour_sphere` | all faces: single-point tangent contact - no area coverage |
| `contour_dome` | FACING attach face: r8 disc patch; silhouette: round/lathe hemisphere |
| `contour_cone` | attach face: r8 base disc; silhouette: round/lathe cone to apex |
| `contour_torus` | run ends: annulus patch r2..r8; rim touches face centers |
| `contour_baluster` / `contour_finial` / `contour_newel` / `contour_bollard` / `contour_smokestack` | attach/run faces: lathe band patches at band radii; silhouette: round/lathe profile |
| `contour_rosette` | mount face: 14px disc plate; silhouette: domed boss |
| `contour_oculus` | the AXIS-normal rim faces: open annulus band (round/lathe ring, open bore) |
| `contour_chimney` | run ends: square band patches (plinth/shaft/lip) - the square lathe counterpart |

| Source | Mate | Status |
| --- | --- | --- |
| cylinder r6 end disc | cylinder on the same axis (column run), capital/pedestal seat, flat-full | pending render verification |
| dome/cone base disc | flat-full seat face (dome rivet on a slab, cone on a roof deck) | pending render verification |
| torus annulus | torus on the same axis, tube/flange ring at matching bore | pending render verification |
| turned-post base bands | railing runs (balustrade/handrail end posts), pedestal caps, flat-full | pending render verification |
| oculus ring | oculus on the same plane (ring grid), flat-full | pending render verification |
| chimney bands | chimney stacked on axis (stack run), smokestack counterpart | pending render verification |

### 3.12 Pipes and channels (7)

Members: `contour_tube`, `contour_elbow`, `contour_tee`,
`contour_cross`, `contour_pipe_cap`, `contour_flange`,
`contour_gutter`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_tube` | run ends: open/frame square ring mouth (2px walls around the 12x12 channel) |
| `contour_elbow` | the two leg faces: 6x6 tube mouths at [5,11], centered at 8px |
| `contour_tee` | three mouths: 6x6 tube ends at [5,11] (run pair + branch) |
| `contour_cross` | four mouths: 6x6 tube ends at [5,11] (the AXIS-closed plane) |
| `contour_pipe_cap` | FACING mouth: 6x6 stub at [5,11] - documented to seat pipe ends exactly |
| `contour_flange` | attach face: r7 ring plate with r3 bore plus bolt studs |
| `contour_gutter` | run ends: open U-channel section; FACING side: open trough top |

| Source | Mate | Status |
| --- | --- | --- |
| tube ring mouth | tube on the same axis (channel run), flat-full | pending render verification |
| small-bore [5,11] mouth | elbow/tee/cross mouths - the declared small-bore pipe family | pending render verification |
| pipe_cap stub | elbow/tee/cross mouths (documented seat at [5,11]) | pending render verification |
| flange ring | small-bore mouth at the r3 bore, flat-full face, pipe_cap plug | pending render verification |
| gutter channel | gutter on the same axis (channel run), valley end at matching drop | pending render verification |

### 3.13 Connective rails and walls (5)

Members: `contour_wall`, `contour_pane`, `contour_fence`,
`contour_balustrade`, `contour_handrail`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_wall` | side faces: 4px arm bands at connective heights; top/bottom: 4px post patches |
| `contour_pane` | side faces: 2px arm bands at connective heights |
| `contour_fence` | side faces: 4px post plus two 2px rail bands per side |
| `contour_balustrade` | run ends: half end posts that pair into full posts; silhouette: open/frame rail + balusters |
| `contour_handrail` | run ends: open rail section climbing 8px (diagonal-shallow at stair pitch); posts to the floor |

| Source | Mate | Status |
| --- | --- | --- |
| wall/pane/fence arms | the same connective family member (vanilla-rule connection), sturdy flat-full faces, fence_gate posts | pending render verification |
| balustrade half end posts | balustrade adjacent segment (paired full post - declared), flat-full | pending render verification |
| handrail rail ends | handrail at the same facing over a stair run (declared continuous handrail), newel/baluster post tops | pending render verification |

### 3.14 Arches and vaults (4)

Members: `contour_arch`, `contour_gothic_arch`,
`contour_barrel_vault`, `contour_arc_wall`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_arch` | span-end faces: open/frame wall with arched opening (3px jambs + 3px arc band, outer r8 / inner r5); passage sides: open |
| `contour_gothic_arch` | span-end faces: open/frame wall with pointed opening (radius-8.9 drop-arch reveals) |
| `contour_barrel_vault` | run ends: round/lathe half-annulus shell (r8 half-cylinder); HALF picks floor-standing or inverted |
| `contour_arc_wall` | the two radial end faces: flat-full straight joins (declared); silhouette: quarter-annulus curve r12..r16 |

| Source | Mate | Status |
| --- | --- | --- |
| arch end silhouette | arch on the same span axis (arcade run), keystone at crown center, flat-full wall | pending render verification |
| gothic_arch end silhouette | gothic_arch run, keystone crown, lancet/portcullis infill | pending render verification |
| barrel_vault shell end | barrel_vault on the run axis (vault bay), arch band at matching radius | pending render verification |
| arc_wall radial ends | flat-full straight walls join flush (documented in the block contract) | pending render verification |

### 3.15 Fortifications (3)

Members: `contour_crenellation`, `contour_buttress`,
`contour_flying_buttress`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_crenellation` | FACING face: lip/rail parapet plus open crenels (open/frame teeth); seat: flat band; HALF=TOP crowns, BOTTOM machicolates |
| `contour_buttress` | wall face (FACING): flat-full rib back; silhouette: step-4 descent (16/12/8/4) |
| `contour_flying_buttress` | wall face (FACING): abutment contact; silhouette: 4px pier plus quarter-arc strut (round/lathe sweep) |

| Source | Mate | Status |
| --- | --- | --- |
| crenellation parapet run | crenellation at the same facing along the wall top (merlon spacing repeats per cell) | pending render verification |
| buttress rib back | flat-full wall face; buttress stacks vertically for taller walls | pending render verification |
| flying_buttress pier/abutment | flat-full wall at the abutment, gothic_arch spring line | pending render verification |

### 3.16 Attachments, redstone and decor (14)

Members: `contour_lever`, `contour_button`, `contour_pressure_plate`,
`contour_torch`, `contour_sign`, `contour_louver`, `contour_chain`,
`contour_cog`, `contour_wheel`, `contour_awning`, `contour_buffer`,
`contour_semaphore`, `contour_pilot`, `contour_ladder`.

| Shapes | Faces -> edge profiles |
| --- | --- |
| `contour_lever` / `contour_button` | mount face: small base patch (6x4 / 6x2); no face-filling edge |
| `contour_pressure_plate` | DOWN: 14x14 flat patch (1px plate); side faces: none at boundary |
| `contour_torch` | mount face/half: small stick patch |
| `contour_sign` | mount face: 14x10 plate band (wall), nib post (floor/ceiling) |
| `contour_louver` | mount face: 1.5px plate; slats tilted outward (diagonal-shallow edge) |
| `contour_chain` | run ends: 4x4 link patches |
| `contour_cog` | tooth faces: 4 edge stubs reach the boundary (declared mesh); AXIS ends: hub plate |
| `contour_wheel` | rim reaches the block edge on the wheel plane (declared); AXIS ends: hub |
| `contour_awning` | wall face (FACING): flat mount edge; canopy: diagonal-shallow 1.2px sheet |
| `contour_buffer` | FACING face: flat-full stop plate; rear: raked strut steps |
| `contour_semaphore` | mount face: pivot base; blade: thin plate toward FACING |
| `contour_pilot` | FACING face: step-3 wedge front; skid plate low band |
| `contour_ladder` | mount face (FACING opposite): 2px plate patch, wall-backed |

| Source | Mate | Status |
| --- | --- | --- |
| mount patches | flat-full host faces only - attachments seat on solid backing | pending render verification |
| cog edge stubs | cog on the same plane (documented visual mesh) | pending render verification |
| wheel rim edge | wheel on the same plane or a wall face (documented shared silhouette) | pending render verification |
| awning canopy edge | awning at the same facing (canopy run) | pending render verification |
| ladder plate | ladder stacked vertically on the same wall face | pending render verification |

## 4. Cross-channel co-residence

Within a cell, channels never overlap; across a boundary, continuation
is evaluated per face without regard to channel.

- Byte + panel cells never mix in v1
  ([Decision.md](Decision.md) ADR-016a): a cell is either a
  Byte-composition cell or a panel-composition cell, never both.
- Octant + octant co-residence is disjoint-only: a new byte occupant is
  accepted iff its octant slot is free (any subset of the 8 slots is
  legal by construction).
- Face-quadrant occupants accept iff their slot set is disjoint from
  all existing occupants' (ADR-016); multi-occupant composition is
  pending the owner matrix in ADR-016a.
- Interior-plane + face-quadrant co-residence is always allowed where
  proposed: a 1px face plate at the cell boundary and a 7..9 interior
  sheet physically cannot intersect (ADR-016a matrix proposal - owner
  confirmation pending).

## 5. Known non-continuations

- Diagonal texture seams are inherent
  ([Decision.md](Decision.md) ADR-017): lattice diagonals can never
  align perfectly, so the material seam along a diagonal-45 interface
  is documented, not a defect. This includes the stepped-collision
  approximations behind wedges and diagonal panels (ADR-014).
- round/lathe into flat faces: a curved surface meets a flat
  neighboring face in a tangent line, not an area - cylinder/sphere/
  torus side faces present no area coverage and continue into nothing
  on their sides. Their end patches continue only into matching discs
  or flat-full seats.
- `contour_sphere`: tangent to all six faces at single points - no
  area contact on any face; it never continues. Same for dome/cone
  silhouettes away from their attach face.
- open/frame into solid: a hollow edge does not continue into a flat
  face (the voids stay open); open members continue only into matching
  open/frame grids or seat visually against flat-full backing.
- diagonal-shallow only continues into the same documented slope
  (handrail's stair pitch matches `contour_slope` runs; awning's
  4.4px-per-8px drop matches only awning runs).
- step-N continues only into step-N at the same pitch and phase;
  a step edge meeting a flat-half band at a different height is a
  butt joint, not a continuation.
- Faces with zero area coverage (slope back edge, open passage sides)
  never continue - contact without area is not an interface.
- Open door/trapdoor leaves are parked states (ADR-015): the leaf sits
  on the hinge side and presents no continuation face while open.
- Mixed byte + panel cells are unsupported in v1 (ADR-016a) - no
  cross-channel mate exists inside one cell.

## 6. Verification surface

How mate pairs become proven:

- Geometry (L2): `byteComposition`-style GameTests assert the lattice
  contract per shape - `getShape`/`boxesFor` boxes on integer-px
  vertices, documented diagonals only, and per-pair overlap/coverage
  assertions derived from the tables in section 3. See
  [Testing.md](Testing.md) for the evidence-layer definitions (L0-L6)
  and the traceability matrix.
- Visual (L3): the continuation gallery fixture (GitHub issue #10)
  renders each declared mate pair adjacently under the six-material
  matrix and captures `continuum-<pair>.png` shots; rendered pair
  shots are the acceptance evidence, not prose claims (ADR-017,
  curated kits).
- Texture: painted-material quads should anchor to the cell grid so
  the same material flows across a mate seam; the UV audit precedes
  any anchoring claim (ADR-017 texture continuation - audit first).
- Honesty rule: a pair stays `pending render verification` until the
  gallery shot or GameTest evidence is recorded in
  [Testing.md](Testing.md) and `docs/evidence/manifest.md`. Unproven
  pairs are written as unproven; `NOT RUN` is the correct status until
  then.
