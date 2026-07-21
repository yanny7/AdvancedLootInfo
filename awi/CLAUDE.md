# awi/CLAUDE.md

Guidance for working on **AWI** (`AdvancedWorldInfo`, `com.yanny.awi`) — the recipe-viewer plugin that displays worldgen information (placed features, placement modifiers, height providers, block predicates, biome surface blocks, ...). See the repo-root `CLAUDE.md` for the multi-mod monorepo layout, and `aci/CLAUDE.md` for the shared tooltip/plugin-manager mechanism this mod instantiates (read that first — this doc assumes it). AWI's architecture mirrors ALI's almost 1:1 at the plugin/registry/network layer — where it does, this doc says so and points at `ali/CLAUDE.md` instead of restating.

## Module layout

- `awi/common` — platform-agnostic mod logic, covered by this file.
- `awi/common-emi`, `awi/common-jei`, `awi/common-rei` — recipe-viewer integrations. See `awi/common-emi/CLAUDE.md`, `awi/common-jei/CLAUDE.md`, `awi/common-rei/CLAUDE.md` (all reference `ali/common-emi/CLAUDE.md` for the shared pattern).
- `awi/fabric` — AWI's **only** loader module, on every branch (no `forge`/`neoforge` ever). See `awi/fabric/CLAUDE.md`.

AWI has no `common-lootjs` equivalent, no `configuration` package, and no user-facing config file at all — there's nothing to filter, since worldgen registries are enumerated exhaustively rather than matched against user-declared categories (contrast with ALI's `ali_config.schema.json`-driven filtering).

## `awi/common` package map (`com.yanny.awi`)

### Root
`Utils.java` — mod-id constant + `modLoc(String)` `ResourceLocation` helper, used for every node ID.

### `api`
Mirrors ALI's `api` package, specialized to worldgen value types: `IPlugin`, `@AwiEntrypoint` — plugin registration entrypoints. `ICommonRegistry`/`ICommonUtils`, `IClientRegistry`/`IClientUtils`/`IWidgetUtils`, `IServerRegistry`/`IServerUtils` — thin extensions of `aci`'s generic `ICoreXxx<...>` interfaces, adding AWI-specific registration methods (see `plugin/Plugin.java` below). `IDataNode` (extends `aci.api.ICoreDataNode<IServerUtils>`), `ListNode` (abstract, extends `aci.api.CoreListNode`) — the recursive server-built/client-synced tree node base; `ListNode.encode` writes child count + each child's `ResourceLocation` id + payload, tolerating per-node encode failures (catches `Throwable`, rewinds writer index, logs, adjusts count — this defensive-encode behavior has no ALI equivalent). `IBlockNode` (`getBlock()`/`getChance()`, implemented by `BlockNode`). `FeatureHolder` (record: items + conditions tooltip), `ListWidget`.

### `manager`
Mirrors ALI's `manager` package exactly in shape:
- `PluginManager extends CorePluginManager<...>` — singleton, wires `AwiCommonRegistry`/`AwiClientRegistry`/`AwiServerRegistry`, plugins sourced from `Services.getPlatform().getPlugins()`.
- `AwiServerRegistry` — holds `ManagedRegistry<Class<?>, BiFunction<...>>` maps for feature/state-provider block collectors and for tooltips of: feature configs, placement modifiers, int providers, rule tests, height providers, block predicates, plus a `ClassKeyedMap` for arbitrary "value" tooltips. Falls back to `MissingTooltipUtils` when a class has no registered handler (see `aci/CLAUDE.md`'s "Missing-entry fallback" section).
- `AwiClientRegistry` — client-side counterpart holding decoded `IDataNode`s.

### `plugin/Plugin.java` — the `@AwiEntrypoint`
`registerServer` wires: value tooltips for `IntProvider`, `RuleTest`, `HeightProvider`, `BlockPredicate`, `OreConfiguration.TargetBlockState`, `BlockState`, `Vec3i`; registry-keyed category tooltips for `Block`, `Fluid`, `PlacementModifierType`, `IntProviderType`, `RuleTestType`, `HeightProviderType`, `BlockPredicateType`; feature-config tooltips (`CountConfiguration`, `OreConfiguration`, ...); per-subtype int-provider/height-provider/rule-test/block-predicate/placement-modifier tooltips (~30 concrete Mojang classes); and `registerFeatureBlockCollector`/`registerStateProviderBlockCollector` for ~20 `FeatureConfiguration` subclasses and 6 `BlockStateProvider` subclasses.

**Important**: there are no surface-rule, structure, carver, or noise-settings *registrations* here — those aren't pluggable value types with a 1:1 vanilla registry, so they're not handled through the two-tier dispatch pattern at all. They're handled procedurally by `plugin/common/nodes.NodeUtils` (below), which is why you won't find a `SurfaceRules.RuleSource` category anywhere in `Plugin.java`.

### `plugin/common/nodes` — the AWI data-scan engine
Server-side tree construction; each class doubles as codec (server constructor + `IClientUtils`/`FriendlyByteBuf` decode constructor + `encodeNode`, per `aci/CLAUDE.md`'s dual-constructor convention).

- `LevelStemNode` (entry-level node) — walks `RegistryAccess.registryOrThrow(Registries.LEVEL_STEM)` per dimension; for a `NoiseBasedChunkGenerator`, spins up a thread pool and calls `NodeUtils.getBaseBlocksForBiome` per biome in parallel.
- `BiomeNode` → iterates `BiomeGenerationSettings.features()` per `GenerationStep.Decoration` → `GenerationStepNode` → `PlacedFeatureNode` (splits into `ConfiguredFeature`/`FeatureConfiguration` blocks via `utils.collectBlocks`, and `PlacementModifier` tooltips) → `BlockNode` (leaf).
- `BaseTerrainNode` — leaf list of blocks discovered by the surface-rule scanner below.
- **`NodeUtils`** — the real worldgen-data-collection engine, and the single most distinctive piece of AWI's architecture: there is no `BuiltInRegistries.PLACED_FEATURE`-style enumeration for surface blocks (features come from a biome's own `BiomeGenerationSettings`, reached only through the per-dimension `LevelStem`→generator→biome path above). Instead, `DimensionContext` builds a mock `SurfaceRules.Context`/`ProtoChunk`/`NoiseChunk`, and `walkColumn` empirically **fires the biome's compiled `SurfaceRules.SurfaceRule` over synthetic canonical columns** (swept surface heights, spiral-sampled XZ, ceiling/overhang probes) to reverse-engineer which blocks the rule places — there is no vanilla API that just answers "what blocks can this biome's surface rule produce," so AWI empirically probes it. Each discovered block gets classified as RELATIVE/ABSOLUTE/LAYERED with water/floor-ceiling constraints.
- `FakeChunkAccess`/`FakeChunkGenerator`/`FakeWorldGenLevel` — mock `ChunkAccess`/`ChunkGenerator`/`WorldGenLevel` implementations that let this probing safely invoke vanilla worldgen code (structure/feature placement checks) without touching a real world.

### `plugin/server`
Pure `IServerUtils`-based utility classes registered by `Plugin.java`, following the same one-static-method-per-Mojang-subtype convention as ALI: `TooltipUtils`, `ValueTooltipUtils`, `RegistriesTooltipUtils` (registry-keyed name/id lookups via `CoreTooltipUtils.getBuiltInRegistryTooltip`), `FeatureConfigurationTooltipUtils`/`CollectorUtils`, `PlacementModifierTooltipUtils`, `IntProviderTooltipUtils`, `HeightProviderTooltipUtils`, `BlockPredicateTooltipUtils`, `RuleTestTooltipUtils`, `BlockStateProviderCollectorUtils`, `PlacedFeatureUtils`, `MissingTooltipUtils` (fallback).

### `plugin/client` / `plugin/client/widget`
GUI-side rendering: `ClientUtils`, `WidgetUtils` (dispatch to per-node-id widget), widgets `LevelStemWidget`, `BiomeWidget`, `GenerationStepWidget`, `PlacedFeatureWidget`, `BlockWidget`, `BaseTerrainWidget`, `TextureWidget`.

### `compatibility`
`AbstractScrollWidget` (custom scrollbar widget base), `GenericUtils` (gunzip + decode the reassembled network buffer client-side into `Map<ResourceLocation, LevelStemNode>` — the recipe-viewer-integration glue analogous to ALI's `compatibility/common`, consumed by `awi/common-emi`/`common-jei`/`common-rei`).

### `datagen`
Only `LanguageHolder` — registers `Lang.*` enum classes into `aci.CoreLang.TRANSLATION_MAP` (see `aci/CLAUDE.md`'s "Language wiring"). No `configuration` package exists in AWI at all: no user-config surface, just tooltip/translation registration.

### `mixin`
Single `MixinClientPlayNetworkHandler` — injects into `handleUpdateTags` (tail) to trigger `clientRegistry.reloadData()` on resource reload (same role as ALI's mixin of the same name).

### `platform` / `platform/services`
`Services` (`ServiceLoader`-based platform accessor) + `IPlatformHelper extends ICorePlatformHelper<IPlugin>` — a smaller interface than ALI's (only `getPlugins`/`getConfiguration`, no loot-pool/spawn-egg/loot-table-parsing methods since AWI has no loot domain). Fabric-only implementation lives in `awi/fabric` (see `awi/fabric/CLAUDE.md`).

## Data-scan entry point

`network.AbstractServer.readWorldgenInfo(ServerLevel level)` is where AWI's whole scan starts: gets `AwiServerRegistry`, iterates the `Registries.LEVEL_STEM` registry, builds one `LevelStemNode` per dimension into a `Map<ResourceLocation, IDataNode>`, drops empty/optimized-away nodes (`ListNode.optimizeList`), encodes the whole tree plus the server's `TooltipCache` into a `FriendlyByteBuf` (palette-first — see `aci/CLAUDE.md`'s tree-model section), gzips it, and slices it into `WorldgenDataChunkMessage` chunks cached in memory for later per-player sync.

## Networking

Mirrors `ali/CLAUDE.md`'s networking pattern exactly — same `AbstractServer`/`AbstractClient` template-method shape, same chunked-gzip-payload-with-palette-first-in-buffer design — this is a conceptually identical, copy-pasted (not code-shared) pattern in both mods' `network` packages. The only differences are the payload/entry-point names:

| ALI | AWI |
|---|---|
| `AbstractServer.readLootTables(LootDataManager)` | `AbstractServer.readWorldgenInfo(ServerLevel)` |
| `LootDataChunkMessage(int, byte[])` | `WorldgenDataChunkMessage(int, byte[])` |
| `RequestLootDataMessage()` | `RequestWorldgenDataMessage()` |
| `syncLootTables` server method name | also named `syncLootTables` — **not renamed for AWI**, a naming leftover from copy-pasting ALI's sync logic (`onLootDataChunk`/`startLootData` client-side callbacks are similarly still loot-named). Don't be confused by "loot" naming showing up in AWI's worldgen sync path — it's cosmetic, not a sign the code is misplaced. |

Actual packet/channel registration lives in `awi/fabric` (see `awi/fabric/CLAUDE.md`), same division of responsibility as ALI.
