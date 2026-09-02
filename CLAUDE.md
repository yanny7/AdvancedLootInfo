# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. It's the top of a tree of `CLAUDE.md` files — read the child doc for whatever subsystem you're actually touching; this file only covers what's true repo-wide.

## Documentation tree

```
CLAUDE.md                      — this file: repo layout, module map, commands, versioning
aci/CLAUDE.md                  — shared core library mod: plugin-manager base, tooltip tree system, language wiring
aci/fabric/CLAUDE.md           — ACI's Fabric loader wrapper (no mod logic; datagen + metadata only)
aci/forge/CLAUDE.md            — ACI's Forge loader wrapper (no mod logic; datagen + metadata only)
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
awi/fabric/CLAUDE.md           — AWI's Fabric loader glue
awi/forge/CLAUDE.md            — AWI's Forge loader glue
alicompat/CLAUDE.md            — ALICompat mod: ALI compatibility for mods that ship no ALI plugin of their own
```

Cross-cutting mechanisms are documented **once**, in whichever doc owns them, and referenced (not restated) everywhere else: the tooltip tree system lives in `aci/CLAUDE.md`; the recipe-viewer integration pattern lives in `ali/common-emi/CLAUDE.md`; the networking pattern lives in `ali/CLAUDE.md` (AWI's is a byte-for-byte structural mirror, documented as a diff in `awi/CLAUDE.md`). When editing one of these, check whether the change belongs in the canonical doc or a per-instance one before writing anything.

## Repo/branch layout

This is a single mod (source: `https://github.com/yanny7/AdvancedLootInfo`) developed across multiple Minecraft versions in parallel, one version per git branch (`1.20.1`, `1.21.1`, `1.21.5`, `1.21.8`, `1.21.10`, `1.21.11`, `26.1.2`, `master` for the latest/in-development version, plus archived `archive/1.2x.y` branches). Branches are typically worked on as separate checkouts, one per Minecraft version — read `minecraft_version` in the repo-root `gradle.properties` to know which version the current checkout is.

The mod's architecture, package layout, and plugin model described across this doc tree are identical across all these branches — they should stay accurate regardless of which version branch they're read from. What legitimately differs per branch:
- Which loaders are available/enabled (see Module layout below): Fabric on every branch, Forge from `1.20.1` on, NeoForge from `1.21.1` on (Forge support has been getting phased out on newer branches in favor of NeoForge). On this `1.20.1` branch, ALI, AWI, ACI and ALICompat all ship `fabric`+`forge`.
- Loader/dependency versions in `gradle.properties` (`minecraft_version`, `forge_version`, `fabric_version`, `neoforge_version`, EMI/JEI/REI/architectury versions, etc.).
- Minor Minecraft-API glue inside `fabric`/`forge`/`neoforge` modules and datagen.

## Project overview

This is a Minecraft mod monorepo built on the **Architectury MultiLoader template**. It produces two related but independently-versioned recipe-viewer mods:

- **ALI** (`AdvancedLootInfo`, group `com.yanny.ali`) — a recipe-viewer (EMI/JEI/REI) plugin that displays detailed information about loot tables and villager trades. See `ali/CLAUDE.md`.
- **AWI** (`AdvancedWorldgenInfo`, group `com.yanny.awi`) — a recipe-viewer plugin that displays worldgen information. See `awi/CLAUDE.md`.

Both mods depend on a third, **ACI** (`AdvancedCoreInfo`, group `com.yanny.aci`, under `aci/`) — a library mod providing the generic, mod-agnostic building blocks (plugin manager, tooltip tree builder, registries, widgets) that ALI and AWI each specialize. It ships as its own jar and is a **mandatory** dependency of both, so users install three jars. See `aci/CLAUDE.md`.

A fourth jar, **ALICompat** (`ALICompat`, group `com.yanny.alicompat`, under `alicompat/`), is **optional**: it carries ALI compatibility for third-party mods that ship no ALI plugin of their own. It is a single ALI plugin whose per-target-mod shims are gated on the target mod being loaded. See `alicompat/CLAUDE.md`.

## Module layout

Each mod (`ali/`, `awi/`, `aci/`) follows the same subproject pattern:

- `common` — platform-agnostic mod logic (loader-independent). Depends on `aci:common`.
- `common-emi`, `common-jei`, `common-rei` — optional integrations for each supported recipe viewer, enabled independently via `gradle.properties` flags (`<viewer>_enabled` + `<platform>_<viewer>_enabled`).
- `common-lootjs` (ALI only) — optional LootJS compatibility module.
- `fabric`, `forge`, `neoforge` — per-loader entry points/glue code. Both ALI and AWI ship whichever of these are enabled on the current branch (`forge` from `1.20.1` on, `neoforge` from `1.21.1` on).

`alicompat` has `common` + the loader modules too, plus one extra source set per target mod (`<loader>/src/compat/<slug>/`) — see `alicompat/CLAUDE.md`.

`aci` has only `common` + the loader modules — no viewer or compat subprojects, since it registers nothing with a recipe viewer. Its loader modules carry no mod logic at all (no mixins, no access widener, no platform-service implementation); they exist to shadow `aci:common` into a loadable jar. `ali:common`/`awi:common` compile against `aci:common` directly (`configuration: "namedElements"`), while the ALI/AWI loader modules take `modImplementation project(":aci:fabric")` / `project(":aci:forge")` for the runtime — `aci:common` is deliberately **not** in their `commonProjects` list, so `com.yanny.aci.*` is not duplicated inside their jars.

Which optional subprojects get included is controlled entirely by `settings.gradle` reading flags from `gradle.properties` (e.g. `emi_enabled`, `jei_enabled`, `rei_enabled`, `lootjs_enabled`, `fabric_enabled`, `forge_enabled`, `neoforge_enabled`). The root `build.gradle` further gates per-project availability with `<platform>_<viewer>_enabled` properties and wires in the correct `commonProjects` dependency list for each loader module.

## Platform abstraction pattern

Loader-specific behavior is isolated via a `ServiceLoader`-based expect/actual pattern:

- `common` modules declare a `services` interface (e.g. `com.yanny.ali.platform.services.IPlatformHelper`) and a static accessor (`com.yanny.ali.platform.Services.getPlatform()`).
- Each `fabric`/`forge`/`neoforge` module provides the concrete implementation and registers it via `META-INF/services`.
- Never call Fabric-, Forge-, or NeoForge-specific APIs directly from a `common` module — go through the platform service interface instead. The one sanctioned exception is depending on `fabric-loader` in `common` build scripts purely to get `@Environment` annotations/mixin support — do not use other Fabric loader classes from `common`.

See `aci/CLAUDE.md`'s `platform` section for the shared `ICorePlatformHelper` contract, and `ali/fabric/CLAUDE.md`/`ali/forge/CLAUDE.md`/`awi/fabric/CLAUDE.md`/`awi/forge/CLAUDE.md` for the concrete per-loader implementations.

## Plugin/extension architecture

Both ALI and AWI expose a plugin API (`com.yanny.<mod>.api.IPlugin`, discovered via `Services.getPlatform().getPlugins()`) so other mods can register their own loot/worldgen compatibility without needing a recipe-viewer-specific integration. Classes annotated `@AliEntrypoint` / equivalent are the registered extension points.

Core flow, per mod (see `ali/CLAUDE.md` / `awi/CLAUDE.md` for the concrete instantiation):
- `manager.PluginManager` (extends `aci`'s `CorePluginManager`) drives three registries built from all discovered `IPlugin`s: `CommonRegistry` (shared/common-side registration), `ClientRegistry` (client-only rendering/widget registration), `ServerRegistry` (server-side data collection built per `ServerLevel`).
- `plugin/server` — turns domain entries (loot-table entries/functions/conditions/ingredients for ALI; worldgen types for AWI) into `TooltipBuilder`/`TooltipNode` trees (the generic tooltip tree model lives in `aci.tooltip`, documented in full in `aci/CLAUDE.md`).
- `plugin/client` — client-side widget/rendering utilities.
- ALI additionally has `plugin/glm` (Global Loot Modifier compatibility) — see `ali/CLAUDE.md`. The reflective accessor toolkit third-party compat shims are written against lives in `alicompat/common`'s `accessor` package — see `alicompat/CLAUDE.md`.

Server-collected data is sent to the client over custom networking (`network` package). **Only the transfer is on demand**: the whole data tree is built eagerly on the server thread at server start (and on datapack/tag reload) by `AbstractServer.readLootTables`/`readWorldgenInfo`, and the recipe viewer's `RequestLootDataMessage`/`RequestWorldgenDataMessage` merely starts streaming the already-built, gzipped chunks to that client. Scan cost is therefore server-startup cost — it is never deferred until a viewer asks. See `ali/CLAUDE.md`'s networking section (canonical) and `awi/CLAUDE.md`'s (the same pattern, diffed).

Mod compatibility for ALI's built-in loot categories is data-driven: `ali_config.schema.json` documents the datapack-based configuration format (loot categories, ingredients, tags) that ALI's `configuration`/`datagen` packages read and generate — see `ali/CLAUDE.md`. AWI's config surface is much smaller: `AwiConfig` (`configVersion`, `tooltipColors`, `logMoreStatistics`, `showInGameNames`, `showConfigConditionalBlocks`) in `awi/common`'s `configuration` package, documented by `awi_config.schema.json` — no datapack-driven categories.

## Common commands

Build (from repo root, all subprojects):
```
./gradlew build
```

Build/work on a single mod or module:
```
./gradlew :ali:common:build
./gradlew :aci:common:build
./gradlew :aci:fabric:build
./gradlew :aci:forge:build
```

After editing `aci`, a dev run may keep loading a stale copy: loom caches remapped mod dependencies keyed on version, and `aci_version` does not move during development. Clear it with `rm -rf .gradle/loom-cache/remapped_mods/*/com/yanny/aci`.

Run the game (client) with a given loader/viewer combination — generated per-platform tasks follow the pattern `run<Ali|Awi><Fabric|Forge|NeoForge><Emi|Jei|Rei>Client`:
```
./gradlew runAliForgeJeiClient
./gradlew runAliFabricEmiClient
./gradlew runAliNeoforgeReiClient
./gradlew runAwiFabricReiClient
./gradlew runAwiForgeJeiClient
```
(Only combinations enabled via `gradle.properties` flags on the current branch are registered as tasks — e.g. `run*Neoforge*` tasks only exist on `1.21.1`+ branches.)

Run all tests (JUnit 5 via `junit-platform-suite`, in `common` modules only — recipe-viewer modules have empty/placeholder test dirs):
```
./gradlew :aci:common:test
./gradlew :ali:common:test
./gradlew :awi:common:test
```

Run a single test class:
```
./gradlew :ali:common:test --tests "com.yanny.ali.test.NodeTest"
```

AWI's base-layout scan is guarded by a golden file instead of unit assertions, with two opt-in switches (see `awi/CLAUDE.md`'s test-harness section):
```
./gradlew :awi:common:test --tests "com.yanny.awi.test.BaseLayoutTest" -Dawi.baselayout.regenerate=true
./gradlew :awi:common:test --tests "com.yanny.awi.test.BaseLayoutSweepTest" -Dawi.baselayout.sweep=true
```

`aci:common` has its own small suite (`CoreTestSuite`) covering the mod-agnostic machinery — anything shared by both mods is tested there once rather than twice in ALI and AWI. It deliberately needs no Minecraft bootstrap, so it runs in a couple of seconds.

Tests are organized behind JUnit Platform `@Suite`/`@SelectClasses` runners (`TooltipTestSuite` in each mod's `common` test tree) that bootstrap Minecraft's registries/resources (`Bootstrap.bootStrap()`, `SharedConstants.setVersion(...)`) once before delegating to individual `@Test` classes — run the suite class, not only an individual test class, if a test depends on that shared bootstrap state.

Generate data (recipes/loot/lang, per loader) via the IDE run configurations in `.idea/runConfigurations/Minecraft_Data_*.xml`, or the equivalent `run<Platform><Loader>Datagen`-style Gradle tasks wired by the `architectury-loom` plugin. All four mods have datagen; ACI's generates only its four `aci.util.*` language keys, ALICompat's only the tooltip keys of the compat shims that are built into it. Fabric datagen initialises the `fabric-datagen` entrypoint of every loaded mod, so a broken ACI datagen class breaks ALI's and AWI's datagen runs too.

## Versioning

ALI, AWI, ACI and ALICompat version independently (`ali_version` / `awi_version` / `aci_version` / `alicompat_version` in `gradle.properties`), each with its own `CHANGELOG.md` (`ali/CHANGELOG.md`, `awi/CHANGELOG.md`, `aci/CHANGELOG.md`, `alicompat/CHANGELOG.md`). Every change to a mod gets a changelog entry there. Because the same fix/feature is typically ported across the active version branches, the same entry and version property commonly land on several branches — check whether a change belongs on other branches too, not just the one you're on.

A changelog section header states the release status of what is under it:

- `## [X.Y.Z]` — that version is **released**. Never append to it.
- `## []` — **unreleased** changes, the section the next release will be numbered. Add new entries here.

So: if the top section carries a version number, open a new `## []` section above it; if it is already `## []`, append to it. The version number is filled in when the mod is actually published.

`gradle.properties` follows from that. Bump the mod's version property only when the version currently in it has been published — i.e. when you are opening a new `## []` section. While an unreleased `## []` section already exists, the property already points at the coming release and stays untouched no matter how many further changes land. Size the bump to the change: a feature or other significant change raises the minor (`1.0.1` → `1.1.0`), a plain fix raises the patch.

### ACI is published API

ACI ships as its own jar and is a **mandatory** dependency of both ALI and AWI, so `com.yanny.aci.api`, `com.yanny.aci.tooltip` and `com.yanny.aci.manager` are published API — a breaking change there is not free.

1. `aci_version` is `MAJOR.MINOR.PATCH`.
2. **MAJOR** — source- or binary-incompatible change to those three packages: a removed or renamed public type/method, a changed signature or return type, a new abstract method on an interface others implement.
3. **MINOR** — additive only: new public types/methods, new default methods, new tooltip node kinds. Existing callers keep compiling and keep running.
4. **PATCH** — internal fixes with no API surface change.
5. A MAJOR bump also raises the **lower** bound in both mods' metadata: `aci_version_range=[<new major>.0,)` and `aci_version_range_fabric=>=<new major>.0`. A MINOR bump raises them only once the mods actually use the new API; PATCH never touches them.
6. Never widen a range to paper over a breakage — the point of the mandatory dependency is that the loader refuses a mismatched pair instead of failing later with `NoSuchMethodError`.
7. The network protocol in `aci.network` follows the same number: a wire-format change is a MAJOR bump even when the Java signatures are untouched.
8. Release order is always ACI first, then ALI/AWI, so their required-dependency reference never dangles.

Anything that stays only to keep older callers working — a superseded method or overload, a constant no longer read, a type left in place after its replacement landed — is marked `@Deprecated(forRemoval = true, since = "<the version the deprecation ships in>")` (with the replacement named in a `@deprecated` Javadoc line) instead of being changed or dropped, and no code in this repo calls it any more. Those annotations are the removal list — never keep a second one in a doc — and the members go away in the next MAJOR release and nowhere else.

The `testArtifacts` configuration on `aci:common` (the shared `TestUtils`) is not published API — it never leaves the repo, so its shape can change without touching `aci_version`.

## Wiki

The user-facing documentation is the project's GitHub wiki — a separate repository, not part of this one. It is a single wiki covering **all** supported Minecraft versions, split into a `Users/` section (no implementation details — no Gson/codecs/registries/API names there) and a `Developers/` section (plugin API). Version-specific behaviour is marked inline per Minecraft version; mod versions are never mentioned, only current behaviour is documented.

**Whenever a change adds, removes or alters user-visible behaviour, a config option, the config format, the datapack format (`fake_loot`), or the plugin API — tell the user explicitly that the wiki needs updating, and name which page(s).** Do not silently assume the wiki is fine; it is not part of this repo, so nothing else will catch the drift. The same applies to the two published schemas the wiki links to: `ali_config.schema.json`, which must stay in sync with `configuration/AliConfig` and the `LootCategory` subclasses, and `awi_config.schema.json`, which must stay in sync with `configuration/AwiConfig`.

## Publishing

`upload.py` at the repo root pushes built jars to Modrinth and CurseForge; it reads mod metadata from `gradle.properties`. Treat running it as a release action, not something to invoke incidentally. ACI is published as its own project and must go out **before** ALI/AWI, which declare it as a required dependency.

Before a release, list what is queued for removal:

```
grep -rn "forRemoval = true" --include=*.java --exclude-dir=build .
```

On a MAJOR bump of the mod that owns them, delete everything that prints — `since` says how long each has been carried. On any other release just read it, and check nothing in the repo calls those members: `./gradlew build 2>&1 | grep -i "deprecated and marked for removal"`, ignoring the hits from vanilla/loader classes.
