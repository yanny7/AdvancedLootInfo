# ali/fabric/CLAUDE.md

Guidance for `ali/fabric` (`com.yanny.ali.fabric`) — ALI's Fabric loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here.

## Entry points

- `CommonAliMod implements ModInitializer` — holds `static final Server SERVER`.
- `ClientAliMod implements ClientModInitializer` — holds `static final Client CLIENT`.
- `FabricCommonBusSubscriber`/`FabricClientBusSubscriber` — wire Fabric lifecycle events (`ServerWorldEvents.LOAD`, `SERVER_STOPPING`, `END_DATA_PACK_RELOAD`) and register a fake-loot-table `IdentifiableResourceReloadListener` delegating to `SERVER.getFakeLootDataManager()` (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`).

## Platform + networking implementation

- `platform.FabricPlatformHelper implements IPlatformHelper` — loot pools via the `MixinLootTableFabric` accessor mixin, plugin discovery via Fabric `EntrypointContainer`s for key `"ali"` (memoized with `Suppliers.memoize`, like Forge's — `getPlugins` is called both by `PluginManager` at startup and by ALICompat's Porting Lib GLM driver per server registry build, and an unmemoized scan re-instantiates every plugin and re-logs the discovery each time), config dir via `FabricLoader`, plus `SpawnEggItem.byId`/`Gson.fromJson` helpers for loot-table parsing.
- `network.{Client,Server,NetworkUtils}` — implement `ali.network.AbstractClient`/`AbstractServer` (see `ali/CLAUDE.md`'s networking section) using `ServerPlayNetworking`/`ClientPlayNetworking` over 4 `ResourceLocation` channel IDs.

## Mixins

`MixinLootTableFabric` (accessor), `MixinMinecraft`, plus accessors for Fabric API's builtin custom ingredients (`MixinCombinedIngredient`, `MixinDifferenceIngredient`, `MixinNbtIngredient`).

## Fabric-only compat plugins

`plugin/` holds third-party compatibility that's genuinely Fabric-only because it targets Fabric-specific APIs — don't try to port these to `ali/forge` without checking whether the target API even has a Forge equivalent:
- `FabricPlugin` + `FabricIngredientTooltipUtils` — registers a tooltip for Fabric API's `CustomIngredientImpl`, unwrapping its Any/All/Difference/Nbt builtins. LootJS compat itself lives in `ali/common-lootjs` (see `ali/common-lootjs/CLAUDE.md`).

Per-target-mod compatibility is **not** here — it ships in the separate ALICompat jar, see `alicompat/CLAUDE.md`. Neither is the Global Loot Modifier path: Fabric has no GLM machinery of its own, so ALI reads Porting Lib's, and that whole driver lives in ALICompat's `portinglib` source set. Without that optional jar a Fabric client shows loot unmodified by any GLM.

## Boilerplate vs genuine glue

`CommonAliMod`/`ClientAliMod`, `NetworkUtils` registration, and the `PlatformHelper` scaffolding (config dir, plugin loading) are structurally identical rewrites of the same contract implemented independently in `ali/forge` — treat changes here as needing a mirrored change there, not a shared refactor (there's no shared loader-glue base class). Genuinely loader-specific and non-portable: the mixins (loader-specific class shapes — e.g. `LootTable`'s pool field differs Fabric vs Forge), the `FakeLootDataManager` reload-listener wiring, and the third-party compat plugins above.
