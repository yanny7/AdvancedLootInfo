# aci/CLAUDE.md

Guidance for working in `aci` (`com.yanny.aci`), the shared core library consumed by both `ali:common` and `awi:common`. See the repo-root `CLAUDE.md` for the overall project layout; this file is the single source of truth for everything described below — `ali`/`awi` never reimplement this mechanism, they only register their own domain types against it.

`aci` has no per-loader modules (no `fabric`/`forge`/`neoforge`) — it is pure platform-agnostic Java, built once and depended on by `ali:common`/`awi:common`.

## Package map (`aci/common/src/main/java/com/yanny/aci`)

### `api` — generic contracts implemented per-mod

- `ICorePlugin<TCommonRegistry, TClientRegistry, TServerRegistry>` — plugin entry-point contract (`getModId`, `registerCommon/Client/Server`); implemented by `ali.api.IPlugin` and `awi.api.IPlugin`.
- `ICoreCommonRegistry` / `ICoreClientRegistry` / `ICoreServerRegistry` and their `...Utils` counterparts (`ICoreCommonUtils`, `ICoreClientUtils`, `ICoreServerUtils`, `ICoreWidgetUtils`) — side-specific registration/runtime-helper contracts. Backed by the abstract `manager.Core*Registry` classes below, extended by `Ali*Registry`/`Awi*Registry`.
- `ICoreDataNode<TServerUtils>` — base for network-serializable, tooltip-bearing, chance-comparable data entries (`encode`, `getTooltip`, `getChance`, `Comparable`). Every mod-specific node type (`ali.plugin.common.nodes.*`, `awi.plugin.common.nodes.*`) implements this.
- `CoreListNode<TServerUtils,TDataNode,TClientUtils>` — abstract composite `ICoreDataNode` holding a sorted child-node list plus shared network encode/decode logic; base for `ali.api.ListNode`/`awi.api.ListNode` (both of which are now nothing but the two constructors). Its `encode` is `final`: it writes the child count + each child's `ResourceLocation` id and payload, then delegates to the subclass's `encodeNode` for the node's own payload. A child whose encode throws is dropped rather than corrupting the stream — the writer index rewinds to that child's start and the leading count is patched afterwards. It deliberately does **not** touch `TooltipContext`: that is set once per top-level entry by `network.NetworkUtils` and must stay ambient across the remaining siblings. Log lines are prefixed with `getId().getNamespace()`, which is the mod id.
- `IWidget` — GUI widget contract (`render`, `getRect`, `getTooltipComponents`) — the single rendering abstraction every recipe-viewer widget wrapper (EMI/JEI/REI, in both mods) adapts to its host viewer's native widget interface. See `ali/common-emi/CLAUDE.md` for how that adaptation works.
- `CoreListWidget<TDataNode,TWidgetUtils,TClientUtils>` — abstract widget that lays out a tree of child `IWidget`s and draws the connecting branch lines; base for `ali.*.ListWidget`/`awi.*.ListWidget`.
- `AbstractScrollWidget` — scrollable viewport + scrollbar (drag/click/wheel handling, scissored content rendering, `isOutsideViewport` culling); base for the `Emi/Jei/ReiScrollWidget` in all six recipe-viewer modules. Subclasses supply `renderWidgets` and `getTexture`.
- `Rect` (record) / `RelativeRect` (mutable, parent-chained) / `WidgetDirection` (enum) — geometry primitives for widget layout.
- `RangeValue` — immutable numeric range/score value type (min/max, "is range", "has score", "is unknown" flags), used throughout tooltip value formatting.

**Shared atlas layout.** `CoreListWidget` and `AbstractScrollWidget` both hardcode u/v offsets but take the `ResourceLocation` from an abstract `getTexture()`, so `ali`'s and `awi`'s `textures/gui/gui.png` must keep the shared sprites at identical coordinates: branch trunk at `(0,0)` 2×1 and branch arm at `(2,0)` 18×2 (`CoreListWidget`), scrollbar track at `(2,2)` 16×16 and scrollbar marker at `(18,0)` 12×17 (`AbstractScrollWidget`). Each mod is free to place its own sprites elsewhere in the atlas. This coupling is invisible to the compiler — changing a sprite means editing both atlases.

### `manager` — plugin-manager and registry base classes

- `ClassKeyedMap` / `ManagedRegistry` / `CorePluginManager` — the generic dispatch/discovery machinery; see "Tooltip system" below for the two dispatch tiers, and the repo-root `CLAUDE.md`'s "Adding a new category" recipe for how `ali`/`awi` build on top of these.
- `BaseRegistry` (package-private) — tracks every `ManagedRegistry` a mod's registry creates, exposing `clearData`/`printRegistrationInfo`/`printRuntimeInfo` (see "Missing-entry fallback and coverage reporting" below). Parent of `CoreCommonRegistry`/`CoreServerRegistry`.
- `CoreCommonRegistry<TConfig>` — implements `ICoreCommonRegistry`+`ICoreCommonUtils`; owns the translation-key dictionary (`HashBiMap<String,Integer>`) and config access. Extended by `AliCommonRegistry`/`AwiCommonRegistry`.
- `CoreServerRegistry<TConfig,TCommonUtils,TServerUtils>` — server-side utils base holding the `ServerLevel`, tooltip cache, and `HolderLookup.Provider`. Extended by `AliServerRegistry`/`AwiServerRegistry`.
- `CoreClientRegistry<...>` — client-side registry; owns widget/data-node factory maps, and uses `compatibility.DataReceiver` plus a `ScheduledExecutorService` to reassemble chunked payloads received over the network (see each mod's `network` package). Extended by `AliClientRegistry`/`AwiClientRegistry`.

### `compatibility`

- `DataReceiver` — accumulates indexed byte-array chunks into a `CompletableFuture<byte[]>`, with `forceDone`/`cancelOperation` timeout handling. Used only internally by `manager.CoreClientRegistry`; this is the *client-side reassembly* half of the chunked-sync pattern each mod's `network` package implements independently (see `ali/CLAUDE.md`'s networking section, mirrored by `awi/CLAUDE.md`).

### `network`

- `NetworkUtils` — the *server-side write/compress* half of the chunked-sync pattern, shared by both mods' `AbstractServer`: `writeMapData` (length-prefixed `Map<ResourceLocation, TNode>`, rewinding and re-patching the count when an entry fails), `writeEntryData`/`writeNodeData` (single entry / single node — the latter is what ALI's wandering-trader node uses, since it carries no `ResourceLocation` on the wire), and `compressAndStoreData` (gzip + slice into ≤32KB chunks, handed to a `BiConsumer<Integer, byte[]>` that wraps each chunk in the mod's own packet type). Every method takes a `String modId` first argument, used purely to prefix its log lines (`[ali]`/`[awi]`) — `NetworkUtils` logs through its own logger, so without it the two mods would be indistinguishable in the log.

Packet/channel definitions stay entirely mod-specific — the `*Message` records, channel ids and registration live in `ali/common/.../network` + the loader modules, and are duplicated in `awi`.

### `platform`

- `ICorePlatformHelper<T extends ICorePlugin<?,?,?>>` — `getPlugins()` (loader-specific plugin discovery) + `getConfiguration()` (config path). Implemented by `ali.platform.services.IPlatformHelper` and `awi.platform.services.IPlatformHelper`, each with per-loader implementations living outside `common` (see `ali/fabric/CLAUDE.md`, `ali/forge/CLAUDE.md`, `awi/fabric/CLAUDE.md`).

### `language`

Supporting types for the translation-key model (not the whole wiring mechanism, described below):
- `IMultiKey` — `singular()`/`plural()` accessor contract.
- `ITooltipKey extends IMultiKey` — adds `getTranslation()` plus English fallback accessors; implemented by generated lang enums (`ali.language.Lang`, `awi.language.Lang`).
- `Translation` (record: `sKey, pKey, sEng, pEng`) — the singular/plural key + English-fallback tuple.

### `tooltip`

`TooltipBuilder`, `TooltipNode`, `RawTooltipNode`, `TooltipNodePalette`, `TooltipContext`, `CacheKey`, `CommonValueTooltip`, `CoreTooltipUtils` — the tree model described in full below.

## Tooltip system

ALI and AWI both display their information the same way: a tree of `TooltipNode`s built once on the server and rendered into `Component`s on the client. The whole mechanism — tree model, registry-lookup dispatch, language wiring, network caching — lives here in `aci` and is identical for both mods. **This section is the single source of truth for how it works; don't re-derive or restate it in a mod-specific doc.** `ali`/`awi` only ever add their own domain-specific categories and builder methods on top of this — they never reimplement the mechanism itself.

### Tree model

- `TooltipBuilder` (`aci.tooltip`) is the fluent construction API — `value(...)`, `array(...)`, `branch(...)`, `keyOnly(...)`, `error(...)`, etc. — terminated by `.build(key)` to produce an immutable `TooltipNode`. `add(TooltipBuilder|TooltipNode)` silently drops empty children, so composing builders never produces stray empty headers.
- `TooltipNode` is the immutable tree node actually rendered (`CoreTooltipUtils.toComponents(node, indent, showAdvanced)`) or sent over the network. Nodes are deduplicated/interned per registry/client through `TooltipNodePalette.getOrCreate(...)` and referenced by integer id when encoded. This is why every `*Node` class (e.g. `PlacedFeatureNode`, `BlockNode`) stores a `TooltipNode` (or its id) rather than a raw `Component`, and why they all share the same dual-constructor shape: a server constructor that builds the tree, and a client constructor (`IClientUtils utils, FriendlyByteBuf buf`) that reads node ids back out of the palette.
- On the wire, the full deduplicated `TooltipNodePalette` is sent **upfront**, once per full sync (not lazily/per-request): each mod's `AbstractServer` data-build method calls `serverRegistry.getTooltipCache().encode(serverRegistry, buf)` as the very first thing written into the raw buffer, before any loot/trade/worldgen node tree. Client-side, `TooltipNodePalette.decode` reads that header first and rebuilds `idToNode` before any dependent node reconstruction resolves children by id lookup.

### Two dispatch tiers

Every mod's `IServerRegistry`/`IServerUtils` (each extends ACI's generic `ICoreServerRegistry<TServerUtils>` / `ICoreServerUtils<SELF>`) exposes tooltip lookup through two distinct mechanisms — don't conflate them:

1. **Generic value dispatch** — `registerValueTooltip(Class<T>, BiFunction<TServerUtils, T, TooltipBuilder>)` / `getValueTooltip(utils, value)`. Backed by `ClassKeyedMap` (`aci.manager`), which walks superclasses/interfaces to find the closest registered supertype, so registering `Collection.class` or `Enum.class` once covers every concrete subtype. `new CommonValueTooltip<TServerUtils, TServerRegistry>().registerAll(registry)` — called once at the top of each mod's `Plugin.registerServer` — pre-registers this tier for common JDK/Minecraft types (`Boolean`, `Enum`, `Collection`, `Optional`, `Holder`, `ResourceLocation`, `RangeValue`, ...). Each mod then layers its own domain value types on top (e.g. AWI adds `Block`, `Fluid`, `BlockState`, `Vec3i`; ALI adds its own loot-specific value types).
2. **Domain-specific category dispatch** — one `register<Category>Tooltip(Class<T>, BiFunction)` / `get<Category>Tooltip(utils, entry)` pair per category (AWI: `PlacementModifier`, `HeightProvider`, `BlockPredicate`, `IntProvider`, `RuleTest`, `FeatureConfiguration`; ALI: `LootItemFunction`, `LootItemCondition`, `Ingredient`, ...). Each category is its own `ManagedRegistry<Class<?>, BiFunction<...>>` field on the mod's `<Mod>ServerRegistry` (e.g. `AwiServerRegistry`, `AliServerRegistry`), created via `registerClassKeyed(label, reportMissing, HashMap::new, vanillaRegistry)`. This tier is **exact-class-keyed** (plain `HashMap`, no supertype walking) because every concrete implementation in these categories corresponds 1:1 with an entry in a vanilla `Registry` (`BuiltInRegistries.PLACEMENT_MODIFIER_TYPE`, `..._TYPE`, ...) — there's no polymorphism to resolve.

Both tiers follow the same three-part convention per registered type: a builder method (e.g. `PlacementModifierTooltipUtils.getHeightmapPlacementTooltip`), a `register...` call wiring it to its concrete class in `Plugin.registerServer`, and a shared `Missing...TooltipUtils` fallback for anything not registered.

### Missing-entry fallback and coverage reporting

`get<Category>Tooltip` always resolves through `.get(entry.getClass()).map(...).orElseGet(missingFallback)`. The fallback (`MissingTooltipUtils`) re-serializes the object through its own `Codec` (`RegistryOps` + `JsonOps`) and dumps the raw JSON under an "Auto-detected" key, so an unregistered type still shows *something* useful instead of nothing. Separately, `ManagedRegistry` (constructed with `reportMissing = true`) records every class that missed lookup; `printRuntimeInfo()` (called server-side after data collection, see each mod's `network/AbstractServer`) logs each miss, and `printRegistrationInfo()` (called from `CorePluginManager` at startup) logs `registered/total` counts against the backing vanilla registry — check these logs to find categories/types still missing a builder.

Some types are deliberately registered to return `TooltipBuilder.empty()` (e.g. AWI's `BiomeFilter`, `InSquarePlacement`) rather than left unregistered — do this when a type carries no information useful to the end user. Leaving it unregistered instead falls through to the JSON-dump fallback, which is noisy for something intentionally uninteresting.

### Adding a new category

1. Declare a `ManagedRegistry` field on `<Mod>ServerRegistry` via `registerClassKeyed(...)`, backed by the relevant vanilla `Registry` if one exists.
2. Add `register<Category>Tooltip`/`get<Category>Tooltip` to `I<Mod>ServerRegistry`/`I<Mod>ServerUtils` and implement them on `<Mod>ServerRegistry`.
3. Add a `Missing<Category>TooltipUtils` fallback (copy the codec-dump pattern from an existing one).
4. Write one builder method per concrete type in a `<Category>TooltipUtils` class, and wire each with `registry.register<Category>Tooltip(SomeType.class, ...)` in `Plugin.registerServer`.
5. Add any new leaf/branch `Lang` keys the new builders need (see below).

### Language wiring

Tooltip keys are `IMultiKey`/`ITooltipKey` enum constants (each mod's `language.Lang`, e.g. `Lang.Value`, `Lang.Branch`, one enum per semantic grouping) carrying a singular/plural key plus English fallback text (`Translation` record). `CoreLang.register(SomeEnum.class)` — called once per enum, from `<mod>.datagen.LanguageHolder`'s static initializer — merges those into a shared `TRANSLATION_MAP`, which both feeds datagen's generated `en_us.json` and gets registered into the per-node translation-key index (`Plugin.registerCommon` → `registry.registerTranslationKey`). That index lets `TooltipNode.encode` send a compact varint key index over the network instead of the full string (`FLAG_INDEX_KEY`). Forgetting to route a new `Lang` enum through `CoreLang.register` in `LanguageHolder` means its keys compile and even display server-side, but the client shows raw untranslated keys and lang datagen won't include them.

### Reference implementations

`aci` defines the mechanism once; `ali/common/.../manager/AliServerRegistry.java` and `awi/common/.../manager/AwiServerRegistry.java` are its two concrete instantiations, and `ali/common/.../plugin/Plugin.java` / `awi/common/.../plugin/Plugin.java` are where every category and value type actually gets wired up (see `ali/CLAUDE.md` and `awi/CLAUDE.md`). When adding tooltip coverage for a new Minecraft type, check the sibling mod's registry/`Plugin.java` for established category-naming and builder-file conventions before inventing new ones.
