# ali/fabric/CLAUDE.md

Guidance for `ali/fabric` (`com.yanny.ali.fabric`) — ALI's Fabric loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/forge/CLAUDE.md` and `ali/neoforge/CLAUDE.md` — the loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the others.

## Entry points

- `CommonAliMod implements ModInitializer` — holds `static final Server SERVER`; `onInitialize` calls `FabricCommonBusSubscriber.registerEvents(SERVER)`, `NetworkUtils.registerCommon(SERVER)`, `PluginManager.registerCommonEvent()`.
- `ClientAliMod implements ClientModInitializer` — holds `static final Client CLIENT`; the same three steps on the client side.
- `FabricCommonBusSubscriber` — registers `ServerWorldEvents.LOAD` (guarded by a `serverLoaded` flag because it fires per level, unlike Forge's `ServerStartingEvent`; sets `FabricPlatformHelper.PROVIDER = server.registryAccess()`, then `registerServerEvent` + `SERVER.readLootTables(server.reloadableRegistries())`), `ServerLifecycleEvents.SERVER_STOPPING` (resets the flag + `deregisterServerEvent`), `ServerLifecycleEvents.END_DATA_PACK_RELOAD` (on success: `reloadServer()` + `readLootTables` — this is the event the other loaders lack, which is why they need a `MixinMinecraftServer` instead), and a `ResourceManagerHelper.get(SERVER_DATA)` reload listener wrapping `SERVER.getFakeLootDataManager()` under the id `ali:fake_loot_loader` (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`).
- `FabricClientBusSubscriber` — `ClientPlayConnectionEvents.JOIN`/`DISCONNECT` → `clientRegistry.loggingIn(ClientPlayNetworking.canSend(RequestLootDataMessage.TYPE))`/`loggingOut()`.
- `datagen.DataGeneration implements DataGeneratorEntrypoint` — registers `LanguageProvider` + `FakeLootProvider`.

## Platform + networking implementation

- `platform.FabricPlatformHelper implements IPlatformHelper` — `getPlugins` (Fabric `EntrypointContainer`s for key `"ali"`, filtered by `isModLoaded(plugin.getModId())`; this is the loader difference that matters in practice — Fabric discovers plugins through the `ali` **entrypoint** in `fabric.mod.json`, Forge/NeoForge through the `@AliEntrypoint` **annotation**, so a new plugin class must be declared in both places), `getConfiguration` (`FabricLoader.getConfigDir()`), `getLookupProvider` (returns the mutable static `PROVIDER`, written from both `ServerWorldEvents.LOAD` and `ReloadableServerResourceMixin`), `getSpawnEggItem` (vanilla `SpawnEggItem.byId`, no `ForgeSpawnEggItem` indirection).
- `network.{Client,Server,NetworkUtils}` — implement `ali.network.AbstractClient`/`AbstractServer` (see `ali/CLAUDE.md`'s networking section). The 4 messages are native `CustomPacketPayload`s owned by `ali/common`, so `NetworkUtils` only declares them (`PayloadTypeRegistry.playC2S()`/`playS2C().register(TYPE, CODEC)`) and attaches handlers via `ClientPlayNetworking`/`ServerPlayNetworking.registerGlobalReceiver(TYPE, handler)` — no channel builder, no codec casting, no `setPacketHandled` boilerplate.

## Mixins

`ali.fabric.mixins.json` — two `client` mixins and one common:

- `MixinClientPlayNetworkAddon` (client) — injects `ClientPlayNetworkAddon.receive` HEAD and re-dispatches `StartMessage`/`LootDataChunkMessage`/`DoneMessage` **on the networking thread** (hand-building the `ClientPlayNetworking.Context`, then cancelling), to avoid the deadlock Fabric's default main-thread hand-off causes for the bulk data sync. It touches Fabric's `impl` package, so it is the most upgrade-fragile piece in this module. `awi/fabric` carries a copy.
- `MixinMinecraft` (client) — `setLevel` HEAD → `EntityStorage.onUnloadLevel()` when a level is already loaded. Forge/NeoForge do this with `LevelEvent.Unload` in their client bus subscriber instead.
- `ReloadableServerResourceMixin` — `loadResources` HEAD → captures `layeredRegistryAccess.compositeAccess()` into `FabricPlatformHelper.PROVIDER`; the Fabric stand-in for Forge/NeoForge's `AddReloadListenerEvent`.

## Fabric-only compat plugins

`plugin/mods/` holds third-party compatibility that is genuinely Fabric-only because it targets Fabric ports of Forge APIs — don't try to move these to `ali/forge`/`ali/neoforge` without checking whether the target API even exists there. Both are `@AliEntrypoint` classes **and** listed in `fabric.mod.json`'s `ali` entrypoint array (on Fabric the annotation alone does nothing):

- `plugin/mods/farmers_delight/Plugin` (mod id `farmersdelight`) — registers reflected loot functions (`CopySkilletFunction`, `SmokerCookFunction`), the `CanItemPerformAbilityCondition` condition, and `FDItemListing` as both item listing and item-listing collector, all via `PluginUtils`/`ReflectionUtils` (see `ali/CLAUDE.md`'s `plugin/mods` section).
- `plugin/mods/porting_lib/loot/Plugin` (mod id `porting_lib_loot`) — the Fabric GLM story: Porting Lib's `LootModifier`/`LootModifierManager`/`LootTableIdCondition`/`IGlmPlugin` are reached reflectively (`Class.forName` in a static block, guarded so the plugin degrades quietly when the mod is absent) and fed into the loader-agnostic `plugin/glm` bridge. This is Fabric's counterpart to `ali/neoforge`'s `NeoForgePlugin` GLM registration.

LootJS is **not** wired here on this branch: `fabric_lootjs_enabled=false`, so `ali/common-lootjs` is not shadowed into the Fabric jar and `ali.lootjs.mixins.json` is absent from `fabric.mod.json` (`lootjs` appears only under `suggests`). See `ali/common-lootjs/CLAUDE.md`; NeoForge is the loader where that module is actually built (`neoforge_lootjs_enabled=true`).

## Boilerplate vs genuine glue

`CommonAliMod`/`ClientAliMod`, `NetworkUtils` registration, and the `FabricPlatformHelper` scaffolding are structurally identical rewrites of the same contract implemented independently in `ali/forge`/`ali/neoforge` — treat changes here as needing a mirrored change there, not a shared refactor. Genuinely loader-specific and non-portable: the three mixins above, the `PROVIDER`/`FakeLootDataManager` reload wiring (lifecycle events on Fabric, `AddReloadListenerEvent` elsewhere), and the third-party compat plugins.
