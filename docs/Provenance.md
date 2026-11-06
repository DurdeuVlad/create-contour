# Provenance - Create: Contour

Register of third-party or adapted elements in this repository. Each entry
records the file(s), origin, license, and identity evidence. Elements not
listed here are original works of this project (MIT).

Limit (per issue 02 / AGENTS.md): this audit documents origin and
attribution; it cannot mathematically prove the absence of similarity.

## Third-party build tooling

| File(s) | Source | License | Evidence |
| --- | --- | --- | --- |
| `gradlew`, `gradlew.bat` | Official `gradle/gradle` repository, tag `v8.14.6` (`raw.githubusercontent.com/gradle/gradle/v8.14.6/{gradlew,gradlew.bat}`) | Apache-2.0 | SHA-256 `fb49f8cb2e5b1d83fba3dcc2c0dd0934c5655cbaf8bd1510ada7cc02692095ca` (gradlew), `49792578a7942e08b708df3d3902ddbfc49d2203775c0161ef0c24b23fe4aeae` (gradlew.bat); original files copied byte-for-byte incl. copyright headers |
| `gradle/wrapper/gradle-wrapper.jar` | Official `gradle/gradle` repository, tag `v8.14.6` (`raw.githubusercontent.com/gradle/gradle/v8.14.6/gradle/wrapper/gradle-wrapper.jar`) | Apache-2.0 | SHA-256 `7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172`; the services-published `gradle-8.14.6-wrapper.jar` artifact does not exist (NoSuchKey) - repo tag used instead |
| `gradle/wrapper/gradle-wrapper.properties` | Authored here; `distributionUrl` + `distributionSha256Sum` are official Gradle facts | MIT (this project) | `distributionSha256Sum=7988ed071b2a07900e2ec715fca15c6ab72bce6db433af301fa7fa7e9407bd24` from `services.gradle.org/versions/8` |

The full Apache License 2.0 text is bundled at
`licenses/Gradle-Apache-2.0.txt` (fetched from apache.org) as required for
the Gradle wrapper components.

## Adapted semantics and attribution

| Element | Source | License | Notes |
| --- | --- | --- | --- |
| Create API surface (base classes `WaterloggedCopycatBlock`, `CopycatBlockEntity`, `IBE`, `CopycatModel`, `StructureTransform`) | `com.simibubi.create:create-1.21.1:6.0.10-280` / `6.0.8-168` (maven.createmod.net) | MIT (code), assets ARR | Subclass/extension use only, no source text copied; full license shipped in jar as `licenses/Create-MIT.txt` |
| Vanilla stair corner/mirror/rotate semantics (`ContourStairsBlock`) | Mojang `StairBlock`/`StairsShape` semantics (Minecraft 1.21.1 sources, NF artifacts) | Minecraft assets/data are proprietary to Mojang | Semantically-equivalent implementation authored here (occupancy-grid collision + own connection rule); NO Mojang code text copied. Recorded so no MIT claim is made over vanilla behavior |
| `textures/block/contour_neutral.png` | Authored in-repo (procedurally generated 16x16 warm-gray with border/seam) | MIT | Original asset, not recolored from Create or any competitor |

The full Create license (code MIT + assets ARR) is bundled at
`licenses/Create-MIT.txt` from the official Create repository.

No other third-party code or assets are copied into this repository; all
addon-authored code and resources are original (MIT), and any future
adapted MIT code will carry the full applicable copyright and permission
text, not just a URL.
