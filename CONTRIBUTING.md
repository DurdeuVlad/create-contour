# Contributing to Create: Contour

## Build and test

```powershell
./gradlew.bat build
./gradlew.bat runGameTestServer   # required-test suite
./gradlew.bat runClientQA         # client render smoke (needs the QA world, see README history / docs/Testing.md)
```

`docs/Testing.md` defines the evidence model: never claim a test or build
passed without the executed command and log path. `NOT RUN` is an honest
status.

## Rules that are not negotiable

- **Clean room**: do not copy, decompile, diff, or adapt code or assets from
  competitor copycat-style mods. All textures/models are original work.
- **License**: contributions are MIT. Code adapted from Create (MIT) must
  carry the full applicable MIT permission text.
- **Docs**: `docs/` is the specification surface; code must not depend on an
  inferred rule that is not written down. Issue contracts live in
  `docs/issues/`.
- **Ledger semantics**: material/paid-unit accounting must survive place,
  break, merge, transform, schematic and save/load paths - tests must prove
  it.

## Pull requests

Use `.github/pull_request_template.md` and restate the contract your change
satisfies; linking an issue is not a substitute.
