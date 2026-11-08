# Issue 13 - Deferred Families (the last parity gap)

Status: draft (spec-boundary reached - each family needs its accepted
contract before code)

Milestone: M5

## Intent

The 111-shape catalog now covers every geometrically bounded form on
the parity map. Three families remain, and each is deferred for a
specific architectural reason - not forgotten. This issue records the
contract each needs before implementation so the backlog is honest:
"everything" cannot be claimed until these land or are formally
rejected.

## Family A - True multipart panels (boards / box)

Geometric note: the named flat/edge/frame forms are all shipped
as single-occupant blocks - boards read as byte/panel/half-panel
sheets, the box frame as contour_frame, and contour_catwalk as
the grated walkway with rail lips. What remains under this
family is genuine multi-occupancy: two or more occupants sharing
one cell.

Problem: several flat-panel occupancy patterns in one block cell
(two byte panels, panel + half panel, board + board). The public
parity list calls these boards, the box, and the catwalk.

Why deferred: one-occupant-per-cell is baked into the architecture -
blockstate properties enumerate every occupant combination, and a
24-state byte panel squared is not representable. A real multipart
needs BE-side occupant lists (state list + material list + per-occupant
paid ledger), a revised drop/loot path, occupancy-aware collision and
a multipart render path (ContourBoxModel already consumes boxesFor
per state; a multipart needs boxes per occupant).

PARTIAL IMPLEMENTATION (2026-11-08): the Byte octant channel shipped -
occupant #0 in blockstate OCTANT, occupants 1..7 in BE `byteOccupants`
with per-occupant material/paid, merge-on-free-octant placement, union
shape, per-occupant drops/requirements/writeSafe, targeted paint and
wrench (ADR-016a IMPLEMENTED note; `byteComposition` GameTest). The
panel/flat-pane channel matrix remains owner-input pending in
ADR-016a's proposal and GH issue #5.

Acceptance contract when picked up:
- New `ContourMultipartBlock` + occupant list on a dedicated BE type;
  `getShape` unions occupant boxes; each occupant keeps its own
  material and paid-unit ledger entry.
- Placement targets the clicked sub-volume; occupancy conflicts
  reject (no overlap, no z-fight); wrench cycles the hit occupant.
- Drops = one blank host + summed material per occupant ledger.
- Schematic: occupant list round-trips through writeSafe/read.
- Six-material matrix + transform matrix on every occupant kind.

## Family B - Sliding / folding doors

Family-B status: contour_sliding_door SHIPPED under ADR-015
(state toggle: OPEN parks the leaf in the hinge-side pocket half,
closed is the full plate; pairing, ledger, redstone, drops and
transforms inherited from the hinged door). Folding also SHIPPED under the same contract
(contour_folding_door: OPEN folds the leaf into a half-span
double-thickness stack at the jamb). Family B is CLOSED.

Problem: doors that translate (slide along a track) or accordion
(fold into a stack) instead of hinging.

Why deferred: a self-animating block needs a motion contract -
intermediate collision during travel, entity push handling, redstone
edge behavior, and whether animation is client-visual-only with a
state toggle (cheap) or physically swept (a moving contraption,
expensive). Undecided, and the wrong choice breaks the ledger and
pathing invariants every other openable holds.

Acceptance contract when picked up:
- Design decision recorded first: state-toggle visual slide vs
  contraption-driven physical move.
- OPEN/CLOSED states keep the same ledger/payload shape as the
  hinged door; redstone edge semantics identical.
- Paired/multi-block segments drop once from the canonical segment.
- Pathing, collision, rotate/mirror and transform tests same as the
  hinged door suite.

## Family C - Functional disguises (cogwheel / shaft / fluid pipe)

Problem: copycat-wrapped Create kinetic and fluid blocks - a cog or
pipe that takes on neighbor material looks.

Why deferred: these wrap Create's own block entities (kinetic network,
fluid transport). A Contour-branded fake would not join the kinetic
network and would silently break contraptions. Doing it right means
extending or decorating Create's BEs with a material payload - a
design decision about wrapping vs mixing into Create classes, with
real contraption/movement/save-load consequences.

Acceptance contract when picked up:
- ADR first: subclass Create cogwheel/pipe BE vs Contour host with
  delegated kinetic/fluid behavior.
- Painted material renders on the rotating/moving parts, not just a
  static shell.
- Kinetic network, stress propagation, fluid containment and
  contraption carry all verified on a real Create fixture - not just
  render smoke.
- Schematic + rotation through contraption transforms round-trip the
  material payload.

## Verification

- Each family ships with its family's spec section above satisfied:
  geometry, ledger, drops, transforms, and the six-material matrix.
- The three-family backlog closing is the completion condition for
  the "everything" claim; until then the honest statement is
  "every geometrically bounded form shipped; three architecture-
  dependent families deferred under this issue."
