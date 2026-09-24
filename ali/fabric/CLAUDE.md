# ali/fabric/CLAUDE.md

Guidance for `ali/fabric` (`com.yanny.ali.fabric`) — ALI's Fabric loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/forge/CLAUDE.md` and `ali/neoforge/CLAUDE.md` — the loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the others.

## Entry points

- `CommonAliMod implements ModInitializer` — holds `static final Server SERVER`; `onInitialize` calls `FabricCommonBusSubscriber.registerEvents(SERVER)`, `NetworkUtils.registerCommon(SERVER)`, `PluginManager.registerCommonEvent()`.
- `ClientAliMod implements ClientModInitializer` — holds `static final Client CLIENT`; the same three steps on the client side.
- `FabricCommonBusSubscriber` — registers `ServerWorldEvents.LOAD` (guarded by a `serverLoaded` flag because it fires per level, unlike Forge's `ServerStartingEvent`; sets `FabricPlatformHelper.PROVIDER = server.registryAccess()`, then `registerServerEvent` + `SERVER.readLootTables(server.reloadableRegistries())`), `ServerLifecycleEvents.SERVER_STOPPING` (resets the flag + `deregisterServerEvent`), `ServerLifecycleEvents.END_DATA_PACK_RELOAD` (on success: `reloadServer()` + `readLootTables` — this is the event the other loaders lack, which is why they need a `MixinMinecraftServer` instead), and a `ResourceManagerHelper.get(SERVER_DATA)` reload listener wrapping `SERVER.getFakeLootDataManager()` under the id `ali:fake_loot_loader` (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`).
- `FabricClientBusSubscriber` — `ClientPlayConnectionEvents.JOIN`/`DISCONNECT` → `clientRegistry.loggingIn(ClientPlayNetworking.canSend(RequestLootDataMessage.TYPE))`/`loggingOut()`.
- `datagen.DataGeneration implements DataGeneratorEntrypoint` — registers `LanguageProvider` + `FakeLootProvider`.

## Platform + networking implementation

- `platform.FabricPlatformHelper implements IPlatformHelper` — `getPlugins` (Fabric `EntrypointContainer`s for key `"ali"`, filtered by `isModLoaded(plugin.getModId())`; this is the loader difference that matters in practice — Fabric discovers plugins through the `ali` **entrypoint** in `fabric.mod.json`, Forge/NeoForge through the `@AliEntrypoint` **annotation**, so a new plugin class must be declared in both places). The scan is memoized with `Suppliers.memoize`, because a GLM driver calls `getPlugins` again per server registry build on top of `PluginManager`'s startup call, and an unmemoized scan re-instantiates every plugin and re-logs the discovery each time. Also `getConfiguration` (`FabricLoader.getConfigDir()`), `getLookupProvider` (returns the mutable static `PROVIDER`, written from both `ServerWorldEvents.LOAD` and `ReloadableServerResourceMixin`), `getSpawnEggItem` (vanilla `SpawnEggItem.byId`, no `ForgeSpawnEggItem` indirection).
- `network.{Client,Server,NetworkUtils}` — implement `ali.network.AbstractClient`/`AbstractServer` (see `ali/CLAUDE.md`'s networking section). The 4 messages are native `CustomPacketPayload`s owned by `ali/common`, so `NetworkUtils` only declares them (`PayloadTypeRegistry.playC2S()`/`playS2C().register(TYPE, CODEC)`) and attaches handlers via `ClientPlayNetworking`/`ServerPlayNetworking.registerGlobalReceiver(TYPE, handler)` — no channel builder, no codec casting, no `setPacketHandled` boilerplate.

## Mixins

`ali.fabric.mixins.json` — two `client` mixins and the common ones:

- `MixinClientPlayNetworkAddon` (client) — injects `ClientPlayNetworkAddon.receive` HEAD and re-dispatches `StartMessage`/`LootDataChunkMessage`/`DoneMessage` **on the networking thread** (hand-building the `ClientPlayNetworking.Context`, then cancelling), to avoid the deadlock Fabric's default main-thread hand-off causes for the bulk data sync. It touches Fabric's `impl` package, so it is the most upgrade-fragile piece in this module. `awi/fabric` carries a copy.
- `MixinMinecraft` (client) — `setLevel` HEAD → `EntityStorage.onUnloadLevel()` when a level is already loaded. Forge/NeoForge do this with `LevelEvent.Unload` in their client bus subscriber instead.
- `ReloadableServerResourceMixin` — `loadResources` HEAD → captures `layeredRegistryAccess.compositeAccess()` into `FabricPlatformHelper.PROVIDER`; the Fabric stand-in for Forge/NeoForge's `AddReloadListenerEvent`.
- `MixinCombinedIngredient`/`MixinComponentsIngredient`/`MixinCustomDataIngredient`/`MixinDifferenceIngredient` — pure `@Accessor` interfaces onto Fabric API's builtin custom ingredients (`impl.recipe.ingredient.builtin`), read by `plugin/FabricIngredientTooltipUtils`. `CombinedIngredient` is package-private, hence the string `targets` form.

## Fabric-only compat plugins

`plugin/` holds third-party compatibility that's genuinely Fabric-only because it targets Fabric-specific APIs — don't try to port these to `ali/forge`/`ali/neoforge` without checking whether the target API even has an equivalent there:
- `FabricPlugin` + `FabricIngredientTooltipUtils` — registers Fabric API's `Ingredient.getCustomIngredient()` (the injected `FabricIngredient` interface) as the ingredient unwrapper, and value tooltips for the Any/All/Difference/Components/Custom-Data builtins. Every custom ingredient loaded from JSON or a packet arrives in the same wrapper, `CustomIngredientImpl` (`CustomIngredient.toVanilla`), so exact-class ingredient dispatch cannot tell them apart; unwrapped, a mod's `CustomIngredient` is rendered by the value tooltip registered for its own class. One that is also an `Ingredient` matches the `Ingredient` value tooltip and goes back through `getIngredientTooltip`; one with no value tooltip gets the auto-detected JSON tooltip of its wrapper (`getMissingIngredientTooltip`). LootJS compat itself lives in `ali/common-lootjs` (see `ali/common-lootjs/CLAUDE.md`).

Per-target-mod compatibility is **not** here — it ships in the separate ALICompat jar, see `alicompat/CLAUDE.md`. Neither is the Global Loot Modifier path: Fabric has no GLM machinery of its own, so ALI reads Porting Lib's, and that whole driver lives in ALICompat's `portinglib` source set. Without that optional jar a Fabric client shows loot unmodified by any GLM.

LootJS is **not** wired here on this branch: `fabric_lootjs_enabled=false`, so `ali/common-lootjs` is not shadowed into the Fabric jar and `ali.lootjs.mixins.json` is absent from `fabric.mod.json` (`lootjs` appears only under `suggests`). See `ali/common-lootjs/CLAUDE.md`; NeoForge is the loader where that module is actually built (`neoforge_lootjs_enabled=true`).

## Boilerplate vs genuine glue

`CommonAliMod`/`ClientAliMod`, `NetworkUtils` registration, and the `FabricPlatformHelper` scaffolding are structurally identical rewrites of the same contract implemented independently in `ali/forge`/`ali/neoforge` — treat changes here as needing a mirrored change there, not a shared refactor. Genuinely loader-specific and non-portable: the three mixins above, the `PROVIDER`/`FakeLootDataManager` reload wiring (lifecycle events on Fabric, `AddReloadListenerEvent` elsewhere), and the third-party compat plugins.
