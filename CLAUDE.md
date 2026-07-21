# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. It's the top of a tree of `CLAUDE.md` files — read the child doc for whatever subsystem you're actually touching; this file only covers what's true repo-wide.

## Documentation tree

```
CLAUDE.md                      — this file: repo layout, module map, commands, versioning
aci/CLAUDE.md                  — shared core library: plugin-manager base, tooltip tree system, language wiring
ali/CLAUDE.md                  — ALI mod: loot/trade data-scan, plugin wiring, config schema, GLM, mod-compat shims, networking (canonical)
ali/common-emi/CLAUDE.md       — canonical EMI/JEI/REI viewer-integration pattern (shared by all 6 common-<viewer> modules) + EMI specifics
ali/common-jei/CLAUDE.md       — JEI specifics (references ali/common-emi/CLAUDE.md for the shared pattern)
ali/common-rei/CLAUDE.md       — REI specifics: Category/Display split, filler/predicate registration
ali/common-lootjs/CLAUDE.md    — optional LootJS compatibility module
ali/fabric/CLAUDE.md           — ALI's Fabric loader glue
ali/forge/CLAUDE.md            — ALI's Forge loader glue
awi/CLAUDE.md                  — AWI mod: worldgen data-scan (incl. surface-rule reverse engineering), plugin wiring, networking (mirrors ali/CLAUDE.md)
awi/common-emi/CLAUDE.md       — EMI specifics for AWI (references ali/common-emi/CLAUDE.md)
awi/common-jei/CLAUDE.md       — JEI specifics for AWI
awi/common-rei/CLAUDE.md       — REI specifics for AWI
awi/fabric/CLAUDE.md           — AWI's (only) Fabric loader glue
```

Cross-cutting mechanisms are documented **once**, in whichever doc owns them, and referenced (not restated) everywhere else: the tooltip tree system lives in `aci/CLAUDE.md`; the recipe-viewer integration pattern lives in `ali/common-emi/CLAUDE.md`; the networking pattern lives in `ali/CLAUDE.md` (AWI's is a byte-for-byte structural mirror, documented as a diff in `awi/CLAUDE.md`). When editing one of these, check whether the change belongs in the canonical doc or a per-instance one before writing anything.

## Repo/branch layout

This is a single mod (source: `https://github.com/yanny7/AdvancedLootInfo`) developed across multiple Minecraft versions in parallel, one version per git branch (`1.20.1`, `1.21.1`, `1.21.5`, `1.21.8`, `1.21.10`, `1.21.11`, `26.1.2`, `master` for the latest/in-development version, plus archived `archive/1.2x.y` branches). Each Minecraft version is checked out into its own sibling directory (this one, `ali_1_20_1/`, is the `1.20.1` branch).

The mod's architecture, package layout, and plugin model described across this doc tree are identical across all these branches — they should stay accurate regardless of which version branch they're read from. What legitimately differs per branch:
- Which loaders are available/enabled (see Module layout below) — e.g. NeoForge modules only exist on branches for Minecraft versions that support NeoForge; Forge support has been getting phased out on newer branches in favor of NeoForge. On this `1.20.1` branch, ALI ships `fabric`+`forge` (no `neoforge`), and AWI ships `fabric` only (as always).
- Loader/dependency versions in `gradle.properties` (`minecraft_version`, `forge_version`, `fabric_version`, `neoforge_version`, EMI/JEI/REI/architectury versions, etc.).
- Minor Minecraft-API glue inside `fabric`/`forge`/`neoforge` modules and datagen.

## Project overview

This is a Minecraft mod monorepo built on the **Architectury MultiLoader template**. It produces two related but independently-versioned mods:

- **ALI** (`AdvancedLootInfo`, group `com.yanny.ali`) — a recipe-viewer (EMI/JEI/REI) plugin that displays detailed information about loot tables and villager trades. See `ali/CLAUDE.md`.
- **AWI** (`AdvancedWorldInfo`, group `com.yanny.awi`) — a recipe-viewer plugin that displays worldgen information. See `awi/CLAUDE.md`.

Both mods share a common core library, **ACI** (`com.yanny.aci`, under `aci/`), which provides the generic, mod-agnostic building blocks (plugin manager, tooltip tree builder, registries, widgets) that ALI and AWI each specialize. See `aci/CLAUDE.md`.

## Module layout

Each mod (`ali/`, `awi/`) follows the same subproject pattern:

- `common` — platform-agnostic mod logic (loader-independent). Depends on `aci:common`.
- `common-emi`, `common-jei`, `common-rei` — optional integrations for each supported recipe viewer, enabled independently via `gradle.properties` flags (`<viewer>_enabled` + `<platform>_<viewer>_enabled`).
- `common-lootjs` (ALI only) — optional LootJS compatibility module.
- `fabric`, `forge`, `neoforge` — per-loader entry points/glue code. ALI ships whichever of these three are enabled on the current branch; **AWI only ever ships `fabric`** — it has no `forge`/`neoforge` module on any branch.

`aci/common` has no per-loader modules — it is a pure shared library consumed by `ali:common` and `awi:common`.

Which optional subprojects get included is controlled entirely by `settings.gradle` reading flags from `gradle.properties` (e.g. `emi_enabled`, `jei_enabled`, `rei_enabled`, `lootjs_enabled`, `fabric_enabled`, `forge_enabled`, `neoforge_enabled`). The root `build.gradle` further gates per-project availability with `<platform>_<viewer>_enabled` properties and wires in the correct `commonProjects` dependency list for each loader module.

## Platform abstraction pattern

Loader-specific behavior is isolated via a `ServiceLoader`-based expect/actual pattern:

- `common` modules declare a `services` interface (e.g. `com.yanny.ali.platform.services.IPlatformHelper`) and a static accessor (`com.yanny.ali.platform.Services.getPlatform()`).
- Each `fabric`/`forge`/`neoforge` module provides the concrete implementation and registers it via `META-INF/services`.
- Never call Fabric-, Forge-, or NeoForge-specific APIs directly from a `common` module — go through the platform service interface instead. The one sanctioned exception is depending on `fabric-loader` in `common` build scripts purely to get `@Environment` annotations/mixin support — do not use other Fabric loader classes from `common`.

See `aci/CLAUDE.md`'s `platform` section for the shared `ICorePlatformHelper` contract, and `ali/fabric/CLAUDE.md`/`ali/forge/CLAUDE.md`/`awi/fabric/CLAUDE.md` for the concrete per-loader implementations.

## Plugin/extension architecture

Both ALI and AWI expose a plugin API (`com.yanny.<mod>.api.IPlugin`, discovered via `Services.getPlatform().getPlugins()`) so other mods can register their own loot/worldgen compatibility without needing a recipe-viewer-specific integration. Classes annotated `@AliEntrypoint` / equivalent are the registered extension points.

Core flow, per mod (see `ali/CLAUDE.md` / `awi/CLAUDE.md` for the concrete instantiation):
- `manager.PluginManager` (extends `aci`'s `CorePluginManager`) drives three registries built from all discovered `IPlugin`s: `CommonRegistry` (shared/common-side registration), `ClientRegistry` (client-only rendering/widget registration), `ServerRegistry` (server-side data collection built per `ServerLevel`).
- `plugin/server` — turns domain entries (loot-table entries/functions/conditions/ingredients for ALI; worldgen types for AWI) into `TooltipBuilder`/`TooltipNode` trees (the generic tooltip tree model lives in `aci.tooltip`, documented in full in `aci/CLAUDE.md`).
- `plugin/client` — client-side widget/rendering utilities.
- ALI additionally has `plugin/glm` (Global Loot Modifier compatibility) and `plugin/mods` (reflection-based third-party compat shims) — see `ali/CLAUDE.md`.

Server-collected data is sent to the client over custom networking (`network` package) — the client requests data on demand rather than the server eagerly pushing it. See `ali/CLAUDE.md`'s networking section (canonical) and `awi/CLAUDE.md`'s (the same pattern, diffed).

Mod compatibility for ALI's built-in loot categories is data-driven: `ali_config.schema.json` documents the datapack-based configuration format (loot categories, ingredients, tags) that ALI's `configuration`/`datagen` packages read and generate — see `ali/CLAUDE.md`. AWI has no equivalent config surface.

## Common commands

Build (from repo root, all subprojects):
```
./gradlew build
```

Build/work on a single mod or module:
```
./gradlew :ali:common:build
./gradlew :aci:common:build
```

Run the game (client) with a given loader/viewer combination — generated per-platform tasks follow the pattern `run<Ali|Awi><Fabric|Forge|NeoForge><Emi|Jei|Rei>Client`:
```
./gradlew runAliForgeJeiClient
./gradlew runAliFabricEmiClient
./gradlew runAliNeoforgeReiClient
./gradlew runAwiFabricReiClient
```
(Only combinations enabled via `gradle.properties` flags on the current branch are registered as tasks — e.g. `runAli*Neoforge*` tasks only exist on branches where NeoForge is enabled, and no `run Awi*Forge*`/`run Awi*Neoforge*` task ever exists.)

Run all tests (JUnit 5 via `junit-platform-suite`, in `common` modules only — recipe-viewer modules have empty/placeholder test dirs):
```
./gradlew :ali:common:test
./gradlew :awi:common:test
```

Run a single test class:
```
./gradlew :ali:common:test --tests "com.yanny.ali.test.NodeTest"
```

Tests are organized behind JUnit Platform `@Suite`/`@SelectClasses` runners (`TooltipTestSuite` in each mod's `common` test tree) that bootstrap Minecraft's registries/resources (`Bootstrap.bootStrap()`, `SharedConstants.setVersion(...)`) once before delegating to individual `@Test` classes — run the suite class, not only an individual test class, if a test depends on that shared bootstrap state.

Generate data (recipes/loot/lang, per loader) via the IDE run configurations in `.idea/runConfigurations/Minecraft_Data_*.xml`, or the equivalent `run<Platform><Loader>Datagen`-style Gradle tasks wired by the `architectury-loom` plugin.

## Versioning

ALI and AWI version independently (`ali_version` / `awi_version` in `gradle.properties`), each with its own `CHANGELOG.md` (`ali/CHANGELOG.md`, `awi/CHANGELOG.md`). Update the relevant changelog and bump the relevant version property when shipping a change to that mod. Because the same fix/feature is typically ported across the active version branches, the same `ali_version`/`awi_version` bump and changelog entry commonly land on several branches — check whether a change belongs on other branches too, not just the one you're on.

## Publishing

`upload.py` at the repo root pushes built jars to Modrinth and CurseForge; it reads mod metadata from `gradle.properties`. Treat running it as a release action, not something to invoke incidentally.
