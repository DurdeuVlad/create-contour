# Issue #6 - Neutral texture candidates (owner sign-off)

All pixels originally authored (procedural, this repo). No third-party
or Create art imported, traced, or recolored. 16x16 RGB, same size as
the current `contour_neutral.png`.

See `preview-sheet.png` (4x, left-to-right: current, A, B, C).

## Candidates

- **A - Plate** (`candidate-a-plate.png`): 2x2 assembled panels, thin
  recessed seams, gentle top-light. Reads as constructed material;
  seams stay legible on small forms.
- **B - Cast** (`candidate-b-cast.png`): quietest option. Uniform
  mid-gray, faint deterministic mottle, dark rim. Best on tiny forms;
  least "patterned".
- **C - Frame** (`candidate-c-frame.png`): beveled border (top-left
  lit, bottom-right shadowed), quiet center. Strongest geometry
  legibility via edge contrast; border could read heavy on byte-size
  forms.

## Owner decision needed

1. Pick A / B / C (or "iterate: <direction>").
2. I will bind the pick to `contour_neutral.png`, update provenance +
   Testing.md, and run the render smoke for in-world evidence.

Non-goal remains: painted material mapping is untouched - this only
replaces the unpainted fallback.
