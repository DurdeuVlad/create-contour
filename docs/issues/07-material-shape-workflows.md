# Issue 07: Low-click material/shape workflows and economic integrity

| Field | Value |
| --- | --- |
| Status | dependency-blocked |
| Milestone | M1 |
| Type | feature |
| Suggested labels | `qol`, `wrench`, `economy`, `config` |
| Priority | proposed-high |
| Owner | Unassigned |
| Dates | Unscheduled |

## Intent

Deliver the QoL layer that makes the shapes cheap to use at scale - fewer
clicks to paint and reshape - while implementing the settled credit model
so no material is ever lost, duplicated, double-charged, or minted.

## Expectation

- Material source precedence: stored item material > offhand > targeted
  block sample > neutral.
- Sneak-click with a shape item copies the clicked shape's material into
  the held item (an unpaid TEMPLATE) without changing the world.
- Plain wrench cycles the compatible family [slab, vertical_slab, stairs],
  preserving material and orientation; sneak-wrench picks the block up
  preserving data including its paid ledger.
- Pick-block returns the shape item carrying the material (not the
  material item, which is Create's default).
- Configs: material consumption (default true), texture rotation
  (default true).

## Acceptance criteria

### Material sources and precedence

1. Precedence implemented and demonstrated: stored item material wins
   over offhand; offhand wins over look-target sample; sample wins over
   neutral. Each tier tested.
2. Look-target sampling is captured at the placement hit before any world
   mutation and re-validated server-side (no client-only trust).
3. Sneak-click copy mutates only the held item's stored material (an
   unpaid TEMPLATE carrying zero paid credit); world unchanged; next
   placement paints the copied material. Evidence shows the copy never
   mints credit. Sneak-copy onto a PREPAID item is REJECTED - no
   mutation, and a tooltip explains to place it first or use a blank
   item - so a copy never destroys existing paid units; copying onto
   blank/template items is allowed. Both paths tested. Re-applying the
   same material in a different axis is allowed with no economic change.

### Credit model (ADR-009) - consumption ON (default)

4. TEMPLATE paint/placement consumes exactly one matching material item
   from offhand or inventory and records one paid unit.
5. PREPAID placement (item from sneak-wrench pickup or break drop)
   transfers its paid ledger exactly once - NO new charge; verified that
   a pickup-then-replace cycle charges nothing extra.
6. If no matching material item is available, the application is REJECTED
   without consuming the shape item or the material - never a silent
   free grant.
7. Ledger is server-authoritative: an arbitrary/forged client payload
   ledger is never trusted (test with a tampered payload if feasible,
   else document the server-side provenance check).

### Credit model - consumption OFF and creative

8. Consumption OFF: newly applied material records zero paid units; no
   refund or material request on break; the accounting survives reload,
   wrench cycling, and schematics.
9. Pre-existing paid credit is PRESERVED across config toggles in both
   directions - no retroactive charge, no destruction; toggling never
   mints credit.
10. Creative: application free, never refunds; creative pick-block yields
    a TEMPLATE (paid ledger cleared); survival pick-block only selects an
    existing matching item in inventory and never spawns a stack.

### Break, pickup, and double-slab accounting

11. Ordinary break and sneak-wrench pickup drop a materialled shape item
    carrying the server-paid ledger - never both the ledger and a loose
    material refund.
12. Double slab (same material): merge preserves two shape items' worth
    and up to two paid units (mixed paid/unpaid halves valid); breaking
    yields two shape items with the ledger split and no duplicated
    material; wrench-cycling a double is blocked.

### Shape cycling and orientation

13. Plain wrench cycles within the family only, preserving material and
    mapping facing/half via canonical orientation metadata where the
    destination shape lacks the source property; neighbour properties
    recompute after conversion.
14. No loss or duplication under any cycle path; conversions that would
    lose volume (doubled slab) are rejected.
15. Sneak-wrench pickup preserves all BE data (material, version, paid
    ledger) and is distinct from sneak shape-copy; inherited
    strip-material wrench behavior is fully overridden. No disambiguating
    modifier needed - the gestures are already disjoint (wrench vs shape
    item in hand), ADR-010.
16. Pick-block returns the shape item carrying the material payload;
    payload round-trips through canonical orientation metadata.

### Configs and inventory surface

17. Texture rotation config ON rotates material axes with shape
    transforms; OFF preserves texture orientation while the shape still
    transforms - logs/tests per config.
18. Creative tab contains exactly one neutral item per shape, sorted.

## Context the code cannot infer

- Upstream behavior being replaced (6.0.8-168 sources): `useItemOn`
  same-material property cycling; `setPlacedBy` offhand auto-apply;
  `onWrenched` strips material + refunds `consumedItem`;
  `getCloneItemStack` returns the material item unless sneaking;
  `onRemove` pops `consumedItem` as a loose drop.
- Canonical orientation metadata is shared with the held item payload and
  contraption transforms (Architecture.md).
- If any AC cannot be honored without further design (for example an
  upstream hook turns out non-virtual), mark this issue
  dependency-blocked with the concrete blocker rather than claiming
  implementation-ready.

## Scope

- In scope: placement/use/pick-block/wrench interactions, item payload
  (TEMPLATE + PREPAID ledger), config options, creative tab sorting,
  orientation mapping for the cycle family, all economy ACs above.
- Non-goals: new shapes (04-06 provide the family), contraption/schematic
  proof (08), pack proof (13/09).

## Dependencies and open decisions

- Depends on: 04, 05, 06. Feeds: 08, 11.
- Open: none - the credit model and gesture defaults are settled
  (ADR-009/010); only implementation seams may surface.

## Verification

- L3 client evidence per AC; L2 tests for consumption/ledger logic;
  Testing.md rows updated. Required coverage: prepaid pickup/re-place no
  double charge; copied template zero credit; survival pick no spawned
  stack; double merge + break correct unit counts; config toggles +
  schematic no minted credit.

## PR contract

Full restatement per `.github/pull_request_template.md`.
