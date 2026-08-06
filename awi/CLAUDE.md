# awi/CLAUDE.md

Guidance for working on **AWI** (`AdvancedWorldInfo`, `com.yanny.awi`) — the recipe-viewer plugin that displays worldgen information (placed features, placement modifiers, height providers, block predicates, biome surface blocks, ...). See the repo-root `CLAUDE.md` for the multi-mod monorepo layout, and `aci/CLAUDE.md` for the shared tooltip/plugin-manager mechanism this mod instantiates (read that first — this doc assumes it). AWI's architecture mirrors ALI's almost 1:1 at the plugin/registry/network layer — where it does, this doc says so and points at `ali/CLAUDE.md` instead of restating.

## Module layout

- `awi/common` — platform-agnostic mod logic, covered by this file.
- `awi/common-emi`, `awi/common-jei`, `awi/common-rei` — recipe-viewer integrations. See `awi/common-emi/CLAUDE.md`, `awi/common-jei/CLAUDE.md`, `awi/common-rei/CLAUDE.md` (all reference `ali/common-emi/CLAUDE.md` for the shared pattern).
- `awi/fabric` — Fabric loader module. See `awi/fabric/CLAUDE.md`.
- `awi/forge` — Forge loader module (enabled via `forge_enabled`). See `awi/forge/CLAUDE.md`.
- `awi/neoforge` — NeoForge loader module (enabled via `neoforge_enabled`). See `awi/neoforge/CLAUDE.md`.

AWI has no `common-lootjs` equivalent, and its `configuration` package is minimal compared to ALI's: `AwiConfig` (`configVersion`, `logMoreStatistics`, `showInGameNames`) plus `ConfigUtils` (same read/version-rotate/re-create logic as ALI's, minus the loot-category Gson adapters), written to `<config>/awi/awi_common.json`. There are no loot-category-style filters to configure, since worldgen registries are enumerated exhaustively rather than matched against user-declared categories (contrast with ALI's `ali_config.schema.json`-driven filtering).

## `awi/common` package map (`com.yanny.awi`)

### Root
`Utils.java` — mod-id constant + `modLoc(String)` `ResourceLocation` helper, used for every node ID.

### `api`
Mirrors ALI's `api` package, specialized to worldgen value types: `IPlugin`, `@AwiEntrypoint` — plugin registration entrypoints. `ICommonRegistry`/`ICommonUtils`, `IClientRegistry`/`IClientUtils`/`IWidgetUtils`, `IServerRegistry`/`IServerUtils` — thin extensions of `aci`'s generic `ICoreXxx<...>` interfaces, adding AWI-specific registration methods (see `plugin/Plugin.java` below). `IDataNode` (extends `aci.api.ICoreDataNode<IServerUtils>`), `ListNode` (abstract, extends `aci.api.CoreListNode`) — the recursive server-built/client-synced tree node base; `ListNode.encode` writes child count + each child's `ResourceLocation` id + payload, tolerating per-node encode failures (catches `Throwable`, rewinds writer index, logs, adjusts count — this defensive-encode behavior has no ALI equivalent). `IBlockNode` (`getBlock()`/`getChance()`, implemented by `BlockNode`). `FeatureHolder` (record: items + conditions tooltip), `ListWidget`.

### `manager`
Mirrors ALI's `manager` package exactly in shape:
- `PluginManager extends CorePluginManager<...>` — singleton, wires `AwiCommonRegistry`/`AwiClientRegistry`/`AwiServerRegistry`, plugins sourced from `Services.getPlatform().getPlugins()`.
- `AwiServerRegistry` — holds ~22 `ManagedRegistry` maps in three groups (all created with `registerClassKeyed`, most backed by the matching `BuiltInRegistries.*_TYPE` so `printRegistrationInfo` can report coverage):
  - **block collectors** (`List<Block>` results): feature config, block-state provider, root placer, tree decorator.
  - **tooltip categories** (`TooltipBuilder` results): feature config, placement modifier, int provider, float provider, rule test, height provider, block predicate, block-state provider, tree decorator, feature size, root placer, foliage placer, trunk placer, structure processor, plus the `ClassKeyedMap`-backed generic "value" tier (`aci/CLAUDE.md`'s tier 1).
  - **placement-summary propagators**: int span, height span, placement (see `plugin/server/summary` below).

  Falls back to `MissingTooltipUtils` when a class has no registered handler (see `aci/CLAUDE.md`'s "Missing-entry fallback" section). When adding a category, this list plus `IServerRegistry`/`IServerUtils` are what need touching — see `aci/CLAUDE.md`'s "Adding a new category".
- `AwiClientRegistry` — client-side counterpart holding decoded `IDataNode`s.

### `plugin/Plugin.java` — the `@AwiEntrypoint`
`registerServer` is one long wiring block; the call counts are the quickest map of where AWI's coverage actually sits: 55 `registerValueTooltip` (generic tier — `IntProvider`, `RuleTest`, `HeightProvider`, `BlockPredicate`, `OreConfiguration.TargetBlockState`, `BlockState`, `Vec3i`, registry-keyed `Block`/`Fluid`/`*Type` lookups, ...), 37 `registerFeatureTooltip` + 37 `registerFeatureBlockCollector` (one pair per `FeatureConfiguration` subclass), 16 `registerPlacementModifierTooltip`, 13 `registerBlockPredicateTooltip`, 11 each `registerStructureProcessorTooltip`/`registerFoliagePlacerTooltip`, 9 `registerTrunkPlacerTooltip`, 9 `registerPlacementPropagator`, 7 each `registerStateProviderBlockCollector`/`registerBlockStateProviderTooltip`, 6 each for tree-decorator tooltips/collectors, rule tests, int providers, height providers, `registerIntSpanPropagator`, `registerHeightSpanPropagator`, 4 `registerFloatProviderTooltip`, 2 `registerFeatureSizeTooltip`, 1 each root-placer tooltip/collector. `registerClient` registers 6 widget + 6 data-node factory pairs; `registerCommon` registers translation keys and calls `new CommonValueTooltip<>().registerAll(registry)`.

**Important**: there are no surface-rule, structure, carver, or noise-settings *registrations* here — those aren't pluggable value types with a 1:1 vanilla registry, so they're not handled through the two-tier dispatch pattern at all. They're handled procedurally by `plugin/common/nodes.NodeUtils` (below), which is why you won't find a `SurfaceRules.RuleSource` category anywhere in `Plugin.java`. Structure *processors* are the near-miss to keep straight: `StructureProcessor` does have a vanilla registry and is registered normally (11 tooltips) — it's whole structures that aren't.

### `plugin/common/nodes` — the AWI data-scan engine
Server-side tree construction; each class doubles as codec (server constructor + `IClientUtils`/`FriendlyByteBuf` decode constructor + `encodeNode`, per `aci/CLAUDE.md`'s dual-constructor convention).

- `LevelStemNode` (entry-level node) — walks `RegistryAccess.registryOrThrow(Registries.LEVEL_STEM)` per dimension; for a `NoiseBasedChunkGenerator`, spins up a thread pool and calls `NodeUtils.getBaseBlocksForBiome` per biome in parallel.
- `BiomeNode` → iterates `BiomeGenerationSettings.features()` per `GenerationStep.Decoration` → `GenerationStepNode` → `PlacedFeatureNode` (splits into `ConfiguredFeature`/`FeatureConfiguration` blocks via `utils.collectBlocks`, and `PlacementModifier` tooltips) → `BlockNode` (leaf).
- `BaseTerrainNode` — leaf list of blocks discovered by the surface-rule scanner below.
- **`NodeUtils`** — the real worldgen-data-collection engine, and the single most distinctive piece of AWI's architecture: there is no `BuiltInRegistries.PLACED_FEATURE`-style enumeration for surface blocks (features come from a biome's own `BiomeGenerationSettings`, reached only through the per-dimension `LevelStem`→generator→biome path above). Instead, `DimensionContext` builds a mock `SurfaceRules.Context`/`ProtoChunk`/`NoiseChunk`, and `walkColumn` empirically **fires the biome's compiled `SurfaceRules.SurfaceRule` over synthetic canonical columns** (swept surface heights, spiral-sampled XZ, ceiling/overhang probes) to reverse-engineer which blocks the rule places — there is no vanilla API that just answers "what blocks can this biome's surface rule produce," so AWI empirically probes it. Each discovered block gets classified as RELATIVE/ABSOLUTE/LAYERED with water/floor-ceiling constraints. (An earlier abandoned attempt to instead *run* feature placement against mock `Fake*` level/chunk implementations was removed as a dead end — empirically running `Feature.place()` is unreliable because placement reads the surrounding world; see the ASM approach in `plugin/server` below.)

Feature-placed blocks come from two complementary sources, both in `plugin/server`: static config introspection (`FeatureConfigurationCollectorUtils`, covers blocks stored in the `FeatureConfiguration`) and, additively for blocks hardcoded inside `Feature.place()` bytecode (the ones absent from config), `FeatureBytecodeScanner` (below).

### `plugin/server`
Pure `IServerUtils`-based utility classes registered by `Plugin.java`, following the same one-static-method-per-Mojang-subtype convention as ALI — one class per category in `AwiServerRegistry`: `TooltipUtils`, `ValueTooltipUtils`, `RegistriesTooltipUtils` (registry-keyed name/id lookups via `CoreTooltipUtils.getBuiltInRegistryTooltip`), `FeatureConfigurationTooltipUtils`/`FeatureConfigurationCollectorUtils`, `PlacementModifierTooltipUtils`, `IntProviderTooltipUtils`, `FloatProviderTooltipUtils`, `HeightProviderTooltipUtils`, `BlockPredicateTooltipUtils`, `RuleTestTooltipUtils`, `BlockStateProviderTooltipUtils`/`BlockStateProviderCollectorUtils`, `TreeDecoratorTooltipUtils`/`TreeDecoratorCollectorUtils`, `RootPlacerTooltipUtils`/`RootPlacerCollectorUtils`, `FoliagePlacerTooltipUtils`, `TrunkPlacerTooltipUtils`, `FeatureSizeTooltipUtils`, `StructureProcessorTooltipUtils`, `PlacedFeatureUtils`, `MissingTooltipUtils` (fallback).

#### `plugin/server/summary` — the placement summary
The second distinctive piece of AWI (after `NodeUtils`): instead of only dumping each `PlacementModifier`'s own tooltip, this derives the three numbers a player actually wants per placed feature — **how many per chunk, with what chance, in which vertical band** — without running worldgen. `PlacementSummaryUtils.summarize` walks a feature's modifier list, asks the `placementPropagators` registry for each modifier's `PlacementContribution` (each modifier fills at most one of count/chance/height; first count wins, chances multiply), and merges them into a `PlacementSummary(CountSpan, chancePercent, HeightSpan)` that `appendSummary` writes as top-level un-headed tooltip lines. `CountSpan`/`HeightSpan` carry both a `RangeValue` and a `Kind` (distribution shape: `CONSTANT`, `UNIFORM`, `BIASED_TO_BOTTOM`, `TRAPEZOID`, `CLAMPED_NORMAL`, `WEIGHTED`, `RELATIVE_TO_HEIGHTMAP`, ...), plus `HeightSpan.bestBand` (the highest-probability Y interval, collapsing to the mode for peaked distributions) and `CountSpan.details` (the modifier's own tooltip, shown when the range is unknown — e.g. `NoiseBasedCountPlacement`). `ColumnContext(minY, height)` reimplements `VerticalAnchor.resolveY` without a world, so the whole path is world-free and unit-testable (`PlacementSummaryTest`). Adding a modded count/chance/height modifier means registering a `PlacementPropagator` (and, if it wraps a provider, an int/height span propagator) — not touching this orchestration.

`FeatureBytecodeScanner` is the exception to the "read the config" rule: a **static ASM data-flow analysis of `Feature.place()` bytecode** that recovers blocks hardcoded in a feature's placement logic (absent from its `FeatureConfiguration` — e.g. iceberg ice, monster-room spawner/chest, dripstone, kelp). It tracks only `BlockState`/`Block` values that flow into a placement sink (`LevelWriter#setBlock`/`Feature#setBlock`), so read-only/checked blocks are not reported (no false positives). It is deliberately **mapping-agnostic** — nothing is matched by method/field/class name (those remap in production); sinks and value types are identified via `Class` assignability and concrete blocks by reflectively reading the referenced static field. ASM (`org.ow2.asm`) is already on the classpath transitively via loom/fabric-loader. `PlacedFeatureNode` calls it additively alongside `utils.collectBlocks`. PoC-level: no inter-procedural receiver binding, no lambda-body following, limited tag-lookup resolution (documented in the class Javadoc).

### `plugin/client` / `plugin/client/widget`
GUI-side rendering: `ClientUtils`, `WidgetUtils` (dispatch to per-node-id widget), widgets `LevelStemWidget`, `BiomeWidget`, `GenerationStepWidget`, `PlacedFeatureWidget`, `BlockWidget`, `BaseTerrainWidget`, `TextureWidget`.

### `compatibility`
`AbstractScrollWidget` (custom scrollbar widget base), `GenericUtils` (gunzip + decode the reassembled network buffer client-side into `Map<ResourceLocation, LevelStemNode>` — the recipe-viewer-integration glue analogous to ALI's `compatibility/common`, consumed by `awi/common-emi`/`common-jei`/`common-rei`).

### `datagen`
Only `LanguageHolder` — registers `Lang.*` enum classes into `aci.CoreLang.TRANSLATION_MAP` (see `aci/CLAUDE.md`'s "Language wiring"). Unlike ALI, AWI's `datagen` generates no config: the whole user-config surface is the two-field `AwiConfig` described under Module layout above.

### `mixin`
Single `MixinClientPlayNetworkHandler` — injects into `handleUpdateTags` (tail) to trigger `clientRegistry.reloadData()` on resource reload (same role as ALI's mixin of the same name).

### `platform` / `platform/services`
`Services` (`ServiceLoader`-based platform accessor) + `IPlatformHelper extends ICorePlatformHelper<IPlugin>` — a smaller interface than ALI's (only `getPlugins`/`getConfiguration`, no loot-pool/spawn-egg/loot-table-parsing methods since AWI has no loot domain). Per-loader implementations live in `awi/fabric`, `awi/forge` and `awi/neoforge` (see `awi/fabric/CLAUDE.md`, `awi/forge/CLAUDE.md`, `awi/neoforge/CLAUDE.md`).

## Data-scan entry point

`network.AbstractServer.readWorldgenInfo(ServerLevel level)` is where AWI's whole scan starts: gets `AwiServerRegistry`, iterates the `Registries.LEVEL_STEM` registry, builds one `LevelStemNode` per dimension into a `Map<ResourceLocation, IDataNode>`, drops empty/hidden nodes (`removeEmptyNodes` → `ListNode.optimizeList`, logging how many were skipped), encodes the whole tree plus the server's `TooltipCache` into a `FriendlyByteBuf` (palette-first — see `aci/CLAUDE.md`'s tree-model section), gzips it, and slices it into `WorldgenDataChunkMessage` chunks cached in memory for later per-player sync.

## Networking

Mirrors `ali/CLAUDE.md`'s networking pattern exactly — same `AbstractServer`/`AbstractClient` template-method shape, same chunked-gzip-payload-with-palette-first-in-buffer design — this is a conceptually identical, copy-pasted (not code-shared) pattern in both mods' `network` packages. The only differences are the payload/entry-point names:

| ALI | AWI |
|---|---|
| `AbstractServer.readLootTables(LootDataManager)` | `AbstractServer.readWorldgenInfo(ServerLevel)` |
| `LootDataChunkMessage(int, byte[])` | `WorldgenDataChunkMessage(int, byte[])` |
| `RequestLootDataMessage()` | `RequestWorldgenDataMessage()` |
| `syncLootTables` server method name | also named `syncLootTables` — **not renamed for AWI**, a naming leftover from copy-pasting ALI's sync logic (`onLootDataChunk`/`startLootData` client-side callbacks are similarly still loot-named). Don't be confused by "loot" naming showing up in AWI's worldgen sync path — it's cosmetic, not a sign the code is misplaced. |

Actual packet/channel registration lives in the loader modules (`awi/fabric`, `awi/forge`, `awi/neoforge`), same division of responsibility as ALI.
