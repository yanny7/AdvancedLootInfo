# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repo/branch layout

This is a single mod (source: `https://github.com/yanny7/AdvancedLootInfo`) developed across multiple Minecraft versions in parallel, one version per git branch (`1.20.1`, `1.21.1`, `1.21.5`, `1.21.8`, `1.21.10`, `1.21.11`, `26.1.2`, `master` for the latest/in-development version, plus archived `archive/1.2x.y` branches). Each Minecraft version is checked out into its own sibling directory (this one, `ali_1_20_1/`, is the `1.20.1` branch).

The mod's architecture, package layout, and plugin model described below are identical across all these branches — this file should stay accurate regardless of which version branch it's read from. What legitimately differs per branch:
- Which loaders are available/enabled (see Module layout below) — e.g. NeoForge modules only exist on branches for Minecraft versions that support NeoForge; Forge support has been getting phased out on newer branches in favor of NeoForge.
- Loader/dependency versions in `gradle.properties` (`minecraft_version`, `forge_version`, `fabric_version`, `neoforge_version`, EMI/JEI/REI/architectury versions, etc.).
- Minor Minecraft-API glue inside `fabric`/`forge`/`neoforge` modules and datagen.

## Project overview

This is a Minecraft mod monorepo built on the **Architectury MultiLoader template**. It produces two related but independently-versioned mods:

- **ALI** (`AdvancedLootInfo`, group `com.yanny.ali`) — a recipe-viewer (EMI/JEI/REI) plugin that displays detailed information about loot tables and villager trades.
- **AWI** (`AdvancedWorldInfo`, group `com.yanny.awi`) — a recipe-viewer plugin that displays worldgen information.

Both mods share a common core library, **ACI** (`com.yanny.aci`, under `aci/`), which provides the generic, mod-agnostic building blocks (plugin manager, tooltip tree builder, registries, widgets) that ALI and AWI each specialize.

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

## Plugin/extension architecture

Both ALI and AWI expose a plugin API (`com.yanny.<mod>.api.IPlugin`, discovered via `Services.getPlatform().getPlugins()`) so other mods can register their own loot/worldgen compatibility without needing a recipe-viewer-specific integration. Classes annotated `@AliEntrypoint` / equivalent are the registered extension points.

Core flow, per mod:
- `manager.PluginManager` (extends `aci`'s `CorePluginManager`) drives three registries built from all discovered `IPlugin`s:
  - `CommonRegistry` — shared/common-side registration.
  - `ClientRegistry` — client-only rendering/widget registration.
  - `ServerRegistry` — server-side data collection (loot pools, entries, tooltips) built per `ServerLevel`.
- `plugin/server` — turns loot table entries/functions/conditions/ingredients into `TooltipBuilder`/`TooltipNode` trees (the generic tooltip tree model lives in `aci.tooltip`).
- `plugin/client` — client-side widget/rendering utilities.
- `plugin/glm` — Global Loot Modifier (Forge/NeoForge datapack loot modifiers) compatibility layer.
- `plugin/mods` — reflection-based compatibility shims (`ReflectionUtils`, `ClassAccessor`, `FieldAccessor`) for reading loot data out of third-party mods that don't expose a clean API.

Server-collected loot/tooltip data is sent to the client over custom networking (`network` package) — the client requests data on demand rather than the server eagerly pushing it (see `ali/CHANGELOG.md` history around network optimization).

Mod compatibility for built-in loot categories is data-driven: `ali_config.schema.json` documents the datapack-based configuration format (loot categories, ingredients, tags) that `configuration`/`datagen` packages read and generate.

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
