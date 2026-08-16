# ali/CLAUDE.md

Guidance for working on **ALI** (`AdvancedLootInfo`, `com.yanny.ali`) — the recipe-viewer plugin that displays loot table and villager trade information. See the repo-root `CLAUDE.md` for the multi-mod monorepo layout, and `aci/CLAUDE.md` for the shared tooltip/plugin-manager mechanism this mod instantiates (read that first — this doc assumes it).

## Module layout

- `ali/common` — platform-agnostic mod logic, covered by this file.
- `ali/common-emi`, `ali/common-jei`, `ali/common-rei` — recipe-viewer integrations. See `ali/common-emi/CLAUDE.md` (canonical pattern doc), `ali/common-jei/CLAUDE.md`, `ali/common-rei/CLAUDE.md`.
- `ali/common-lootjs` — optional LootJS compatibility module. See `ali/common-lootjs/CLAUDE.md`.
- `ali/fabric`, `ali/forge`, `ali/neoforge` — per-loader entry points. See `ali/fabric/CLAUDE.md`, `ali/forge/CLAUDE.md`, `ali/neoforge/CLAUDE.md`.

## `ali/common` package map (`com.yanny.ali`)

### `api`
Public contract layer implemented/consumed by plugins: `IPlugin` (entrypoint: `getModId`, `registerCommon/Client/Server`), `@AliEntrypoint` (ServiceLoader discovery marker), `ICommonRegistry`/`IClientRegistry`/`IServerRegistry` (registration surfaces extending `aci`'s generics), `ICommonUtils`/`IClientUtils`/`IServerUtils` (runtime helper contexts), `IDataNode`/`IItemNode`/`ITradeNode`/`ListNode` (tree node contracts, encode/decode over `FriendlyByteBuf`), `ListWidget` (client widget contract), `ILootModifier`/`IOperation` (GLM modifier abstraction, see `plugin/glm` below).

### `manager`
Wires ALI's registries into `aci`'s generic `CorePluginManager` machinery.
- `PluginManager extends CorePluginManager<...>` — singleton; sources plugins from `Services.getPlatform().getPlugins()` and constructs `AliCommonRegistry`/`AliClientRegistry`/`AliServerRegistry`.
- `AliCommonRegistry` — holds `AliConfig` (via `ConfigUtils.readConfiguration()`) and an entity-variant registry (e.g. sheep colors).
- `AliClientRegistry` — client-side widget/data-node factories.
- `AliServerRegistry` — the workhorse: item collectors, entry/condition/function tooltip builders, value tooltips, chance/count/itemstack modifiers, item listings, and the loot-table/trade parsing entry points `parseTable`/`parseTrade`, plus the tooltip cache.

  `getIngredientTooltip` has one wrinkle: before the usual exact-class dispatch it runs the `registerIngredientUnwrapper` hooks, each of which either returns the object a loader-specific composite `Ingredient` really wraps or `null`. That exists because `registerIngredientTooltip` is keyed by `Class<? extends Ingredient>`, which only reaches custom ingredients on loaders where they *are* `Ingredient` subclasses (Fabric's `CustomIngredientImpl`, Forge's `AbstractIngredient`). NeoForge's `ICustomIngredient` is not one — every custom ingredient there is the same plain `Ingredient` wrapping one — so `ali/neoforge` registers an unwrapper plus ordinary **value** tooltips for its ingredient types. Deliberately *not* solved by having the loader plugin re-register `Ingredient.class`: that override would win only if it happened to land after `ali/common`'s, and plugin order comes from an unordered annotation/entrypoint scan. Nor by making `ingredientTooltips` a `ClassKeyedMap` — supertype resolution cannot bridge a wrapper→wrapped hop, and it would also turn the missing-entry JSON fallback for unrecognised `Ingredient` subclasses into a blank vanilla tooltip.
- `FakeLootDataManager` — a `SimpleJsonResourceReloadListener` on the `fake_loot` datapack folder; merges "fake" loot pools declared by other mods' compat shims into real tables at read time (see `datagen.FakeLootProvider` below).

### `plugin/Plugin.java` — the `@AliEntrypoint`
This is where every category and value type from `aci`'s two dispatch tiers actually gets instantiated for ALI. At a survey level:
- `registerCommon` — registers all translation keys from `LanguageHolder.TRANSLATION_MAP`; registers entity variants (`EntityUtils::getSheepVariants`).
- `registerClient` — registers a widget + data-node factory pair for every node ID: loot side (`LootTableNode/LootPoolNode/ItemNode/EmptyNode/ReferenceNode/DynamicNode/AlternativesNode/SequenceNode/GroupNode/ModifiedNode/GlobalLootModifierNode/MissingNode`) and trade side (`TradeNode/TradeLevelNode/SubTradesNode/ItemsToItemsNode`).
- `registerServer` — the bulk of the file: item collectors per loot-entry class (`LootItem, TagEntry, AlternativesEntry, EntryGroup, SequentialEntry, EmptyLootItem, DynamicLoot, LootTableReference, SmeltItemFunction`), number-provider→`RangeValue` converters (`ConstantValue, UniformGenerator, BinomialDistributionGenerator, ScoreboardValue`), entry→node builders, ~18 condition tooltips and ~24 function tooltips (`ConditionTooltipUtils`/`FunctionTooltipUtils`), an ingredient tooltip, ~35 value tooltips (vanilla-registry values via `RegistriesTooltipUtils`, e.g. `Block/Item/EntityType/Enchantment/Attribute`; structural predicate/misc values via `ValueTooltipUtils`, e.g. `EntityPredicate/ItemPredicate/NumberProvider/MinMaxBounds`), chance/count/item-stack modifier appliers, and villager-trade `IItemListing` registrations covering all 10 `VillagerTrades.*` inner classes (most via generic `TradeUtils`; `EmeraldsForVillagerTypeItem` gets a dedicated node).

Check this file (and `awi/common/.../plugin/Plugin.java` for naming conventions) before adding a new category — see `aci/CLAUDE.md`'s "Adding a new category" recipe.

### `plugin/server` — ALI data-scan tooltip builders
Builds the `TooltipBuilder`/`TooltipNode` tree from vanilla loot-table entries/conditions/functions/ingredients, driven by the registrations above. Key classes: `TooltipUtils` (chance/count/itemstack modifier application — the central helper), `EntryTooltipUtils`, `ConditionTooltipUtils`, `FunctionTooltipUtils`, `IngredientTooltipUtils`, `ValueTooltipUtils`, `RegistriesTooltipUtils`, `GenericTooltipUtils`, `MissingTooltipUtils` (fallback), `ItemCollectorUtils` (walks raw loot-table entries to collect concrete `Item`s, for global-loot-modifier predicates and config catalyst/predicate filtering — **not** for the network payload, see the item pipeline below), `LootConditionTypes`/`LootFunctionTypes` (registry-type lookup helpers), `EnchantedRanges` (enchantment level range math). Convention: one `getXTooltip`/`collectX`/`applyX` static method per vanilla class, registered by exact `.class` in `Plugin.registerServer`.

### `plugin/common` (+ `nodes`, `trades`) — shared client/server tree model
`plugin/common/nodes`: one `IDataNode` implementation per node kind (`LootTableNode, LootPoolNode, ItemNode, AlternativesNode, SequenceNode, GroupNode, EmptyNode, DynamicNode, ReferenceNode, ModifiedNode, GlobalLootModifierNode, MissingNode`, plus `CompositeNode` base). Convention: each declares `public static final ResourceLocation ID = Utils.modLoc("<snake_case_name>")`, the shared key between its data-node factory and widget factory registered in `Plugin.registerClient`. `plugin/common/trades`: villager-trade equivalents (`TradeNode, TradeLevelNode, SubTradesNode, ItemsToItemsNode, EmeraldsForVillagerTypeItemNode`), plus `TradeUtils` (`getNode` for most `VillagerTrades.ItemListing` subclasses). `NodeUtils`/`EntityUtils` are top-level helpers (entry→node dispatch, sheep-variant entity spawning).

`EntityLootTableResolver` maps a loot table id back to the entity types that can drop it — the inverse of asking every entity type what it drops, and the reason neither the server scan nor the viewer registration constructs sample entities any more (that construction, one entity per registered type with goals/attributes/brains, used to be the single most expensive part of the scan). It needs no instance because `EntityType.getDefaultLootTable()` is derived from the registry key and `Mob#getLootTable` is `final`, delegating to `getDefaultLootTable`; the only types that can drop something else are those overriding it, and vanilla's sole case (`Sheep`) puts its per-color tables one path segment below the type's own table, so trimming trailing segments off an unmatched `entities/` table finds the owner **without any variant registration**. A table that still resolves to nothing falls back to building the entities of *that table's namespace only* (`scanNamespace`), so the cost tracks the number of unusual mods, not the registry size — vanilla data never triggers it.

The config's `entityLootTables` (entity type id → loot table ids) is seeded before anything derived and therefore wins over it — that is the escape hatch for a mod whose loot table id follows no convention at all, and it works for tables outside `entities/` too.

Consequences worth knowing before touching it: `registerEntityVariants` is now only needed for *rendering* a variant (an unregistered variant entry falls back to any instance of its type, see `EntityStorage`), a table under `entities/` is attributed to its type even when that type is not a `Mob`, and the one place still needing instances is the global-loot-modifier predicate (`ILootModifier<Entity>`), which `AbstractServer.processEntities` therefore evaluates lazily through `EntityLootTableResolver#getEntities` — never touched when no GLMs are installed.

### `plugin/client` (+ `widget`, `widget/trades`)
Client-side rendering. `ClientUtils`/`WidgetUtils` helpers; one `ListWidget`/`IWidgetFactory` per node ID mirroring `plugin/common/nodes` (`LootTableWidget, LootPoolWidget, ItemWidget, AlternativesWidget, SequentialWidget, GroupWidget, EmptyWidget, DynamicWidget, ReferenceWidget, ModifiedWidget, GlobalLootModifierWidget, MissingWidget, TextureWidget`), plus `widget/trades` (`TradeWidget, TradeLevelWidget, SubTradesWidget, ItemListingWidget`).

### `plugin/glm` — Global Loot Modifier compatibility
Forge/NeoForge GLM datapack modifiers layer: `IGlobalLootModifierPlugin` (extra `IPlugin` extension GLM-integrating mods implement, `registerGlobalLootModifier`), `IGlobalLootModifierAccessor`/`IGlobalLootModifierWrapper`/`ILootTableIdConditionPredicate` (reflection-friendly accessors over Forge's GLM classes), `GlobalLootModifierUtils` (applies modifiers as extra `ILootModifier` entries onto matching block/entity/loot-table nodes, matching predicates like `LootItemBlockStatePropertyCondition` against `LootItemCondition`s).

### `plugin/mods` — reflection-based compatibility shims
For reading private/mod fields without a hard dependency: `ReflectionUtils` (core helpers), `ClassAccessor`/`FieldAccessor`/`BaseAccessor` (typed wrappers over reflected classes/fields), `SingletonContainer`, `PluginUtils`, `ConditionalFunction`, and feature-detection marker interfaces used by GLM/other integrations (`IChanceModifier, ICountModifier, IItemStackModifier, IEntry, IEntryItemCollector, IFunctionItemCollector, IFunctionTooltip, IConditionTooltip, IIngredientTooltip, IItemListing, INumberProvider`).

### `compatibility/common`
Shared DTOs consumed by ALI's recipe-viewer integrations (`common-emi`/`common-jei`/`common-rei`) to expose parsed loot as viewer categories: `IType` (`entry()/inputs()/outputs()`), `BlockLootType`, `EntityLootType`, `GameplayLootType`, `TradeLootType` (records pairing a vanilla key — Block/EntityType/profession/ResourceLocation — with an `IDataNode` and item stacks). `GenericUtils` (villager profession → job-site blocks/requested items) and functional interfaces (`QuadConsumer`/`QuintConsumer`/`TriConsumer`).

### `configuration` / `datagen` — data-driven mod compatibility
Backs the datapack-based `ali_config.schema.json` format:
- `AliConfig` — root config (`configVersion`, `blockCategories/entityCategories/gameplayCategories/tradeCategories` lists, `disabledEntities`, `logMoreStatistics`, `showInGameNames`, `hideDefaultBlockLoot` + `defaultBlockLootConditions`/`defaultBlockLootFunctions`, `ignoredPredicateConditions`, `entityLootTables`); ships hardcoded defaults (`plant_loot`, `block_loot`, `entity_loot`, `chest_loot`, `fishing_loot`, `archaeology_loot`, `hero_loot`, `cat_morning_gift`, `piglin_bartering`, `sniffer_digging`, `gameplay_loot`, `trade_loot`).
- `LootCategory<T>` — abstract base (key/icon/type/hide/catalysts, JSON (de)serialization, abstract `validate(T)`). Subclasses match the schema's 4 category kinds: `BlockLootCategory` (block/tag list), `EntityLootCategory` (entity-type/tag list), `GameplayLootCategory`/`TradeLootCategory` (regex `pattern` list matched against the loot table's `ResourceLocation`). The schema's `type` enum (`BLOCK/ENTITY/GAMEPLAY/TRADE`) maps 1:1 to `LootCategory.Type`.
- `ConfigUtils` — reads/writes `config/ali/ali_common.json` (`Utils.COMMON_CONFIG_NAME`), handles `CURRENT_VERSION` migration by renaming the outdated file to `.bak` and regenerating defaults. The read/rotate/re-create logic itself is `aci.configuration.CoreConfigUtils` (shared with AWI); this class only supplies `AliConfig.CODEC` and the `HolderLookup.Provider`-backed `DynamicOps` its `Ingredient` catalysts need. `AliConfig` implements `aci.configuration.ICoreConfig`; a missing or `null` field falls back through the codec's own `orElse`/`orElseGet`.
- `network.AbstractServer.readLootTables` filters blocks/entities/loot-tables/trades against these category lists on every reload — this is where the config actually gates ALI's data scan.
- `datagen.FakeLootProvider` — a `DataProvider` generating datapack JSON under `fake_loot/` for drops not expressible as a real loot table (e.g. mob-equipment drops for fox/piglin/skeleton/zombie/wither/drowned/etc), consumed back by `FakeLootDataManager`.
- `datagen.LanguageHolder` — aggregates `aci`'s `CoreLang` translations plus ALI-specific EMI category names and chest/loot-table display names into `TRANSLATION_MAP` (see `aci/CLAUDE.md`'s "Language wiring").

### `language`
`Lang` enum (nested tooltip-key groups: `Conditions, Functions, Value, Branch, Description, Group, Multi, Error`) registered into `aci`'s `CoreLang` by `datagen.LanguageHolder`.

## Networking (canonical pattern — `awi/CLAUDE.md` mirrors this)

`ali/common/.../network` implements ALI's data-sync protocol — on-demand in its *transfer* only, the data itself is built eagerly at server start (see the root `CLAUDE.md` and step 3 below); packet registration/codec wiring itself lives in the loader modules (`ali/fabric/CLAUDE.md`, `ali/forge/CLAUDE.md`, `ali/neoforge/CLAUDE.md`), this package holds the shared logic and abstract template.

**Packets**: `StartMessage(int totalMessages)`, `DoneMessage()` (empty marker), `LootDataChunkMessage(int index, byte[] data)` (chunk payload), `RequestLootDataMessage()` (empty client→server request marker).

**Flow**:
1. Client-side, `compatibility.common.GenericUtils.register(...)` — called while the recipe viewer builds its own data (viewer plugin registration at client/world load, **not** when a loot category is opened) — checks `PluginManager.getInstance().clientRegistry.getCurrentDataFuture()`; if not yet resolved, it calls `AbstractClient.INSTANCE.sendLootDataToPlayer(new RequestLootDataMessage())` and blocks up to 30s per attempt, 3 attempts.
2. Server-side, the loader's `Server` class receives the request (wired via the loader module's own `NetworkUtils.registerCommon` — unrelated to `aci.network.NetworkUtils`, same name, different class) and calls `AbstractServer.syncLootTables(player)`.
3. `AbstractServer.readLootTables(LootDataManager)` (called at world-load/reload time, not per-request) parses all block/entity/gameplay loot tables and trades into `IDataNode`s, writes the tooltip palette header (see `aci/CLAUDE.md`'s tree-model section) followed by the node data into a `FriendlyByteBuf` (both write steps go through `aci.network.NetworkUtils`, see `aci/CLAUDE.md`), gzips it, and slices it into ≤32KB chunks, cached in memory.
4. `syncLootTables` sends `StartMessage(chunks.size())`, then each `LootDataChunkMessage(index, data)`, then `DoneMessage()`.
5. `AbstractClient.onStart`/`onLootDataChunk`/`onDone` forward into `PluginManager.getInstance().clientRegistry` (`startLootData`, `addChunkData`, `doneLootData`), which decompresses and decodes the reassembled buffer into the cached node tree (client-side chunk reassembly itself is `aci.compatibility.DataReceiver`, used inside `CoreClientRegistry`).

### The item pipeline: nothing item-shaped is sent

The payload carries the node tree and nothing else — the flat item lists a recipe viewer needs (a recipe's outputs, a trade's costs and results, the reverse ingredient index) are derived **client-side** from that same tree, by `compatibility/common/GenericUtils.collectItems`/`collectTradeItems`. This is the same arrangement as AWI's block-tag pipeline (`awi/CLAUDE.md`), for the same two reasons: the lists cannot drift from what is rendered, and a tag is resolved against the *client's* current registry, so it re-resolves on every datapack/tag reload instead of being frozen at scan time.

- `IItemNode.getItem()` is what the slot stands for (`Either<ItemStack, TagKey<? extends ItemLike>>`), `getItems()` the stacks it resolves to (tag members expanded, empty stacks dropped, list mutable), `retainItems(Predicate)` drops the members the viewer hides. Resolution goes through `NodeUtils.resolveItems`, used by both sides.
- `GenericUtils.pruneHiddenItems`/`pruneHiddenTrades` must run before anything asks for the lists — pruning narrows `getItems()` in place, which is what keeps tree, outputs and index in agreement. A node standing for an empty stack is kept: it is the placeholder second input of a single-input trade, and `requiresAllChildren` would drop the whole trade with it.
- **Inputs vs outputs live on `ITradeNode`, not on the leaf.** A leaf is one stack and cannot know which side of a trade it sat on; `ItemsToItemsNode` adds its children cost-first and encodes how many of the leading ones are costs, so the split survives the wire without relying on child count. The order itself survives because every child carries chance 1 and `CoreListNode`'s decode sorts by chance with a stable sort.
- The server still walks the tree once (`AbstractServer.collectItems`) — only to tell an empty loot table from a real one in `removeEmptyLootTable`.
- `IServerRegistry.registerItemListingCollector`, `IServerUtils.collectItems(ItemListing)`, the `tradeItemCollectors` registry and the `collectItems` half of the `IItemListing` reflection shim are **gone** — a trade's two sides come from `ITradeNode` now, so there is nothing left for a per-listing collector to answer. `registerItemListing` (the node factory) is untouched.

`AbstractServer`/`AbstractClient` are template-method base classes: they hold all the shared logic above and declare `protected abstract send*` methods that each loader's `Server`/`Client` subclass implements over its native networking API (Fabric's `ServerPlayNetworking`/`ClientPlayNetworking`, Forge's `SimpleChannel`).

## `mixin`
`MixinBushBlock` (invoker accessor exposing protected `mayPlaceOn` for loot-modifier predicate checks). `MixinClientPlayNetworkHandler` (injects into `handleUpdateTags` tail to trigger `clientRegistry.reloadData()` on tag reload).

## `platform` / `platform/services`
`Services` (static holder loading `IPlatformHelper` via `ServiceLoader`), `IPlatformHelper` (loader-specific operations: get plugins, config dir, fake-loot-table parsing) — implemented per-loader, see `ali/fabric/CLAUDE.md`/`ali/forge/CLAUDE.md`/`ali/neoforge/CLAUDE.md`.
