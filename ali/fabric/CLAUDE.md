# ali/fabric/CLAUDE.md

Guidance for `ali/fabric` (`com.yanny.ali.fabric`) — ALI's Fabric loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/neoforge/CLAUDE.md` — the loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the other. (`ali/forge` is the third implementation but is excluded from the build and unported on this branch — see `ali/forge/CLAUDE.md`.)

## Entry points

- `CommonAliMod implements ModInitializer` — holds `static final Server SERVER`; `onInitialize` calls `FabricCommonBusSubscriber.registerEvents(SERVER)`, `NetworkUtils.registerCommon(SERVER)`, `PluginManager.registerCommonEvent()`.
- `ClientAliMod implements ClientModInitializer` — holds `static final Client CLIENT`; the same three steps on the client side.
- `FabricCommonBusSubscriber` — registers `ServerWorldEvents.LOAD` (guarded by a `serverLoaded` flag because it fires per level, unlike NeoForge's `ServerStartingEvent`; `registerServerEvent` + `SERVER.readLootTables(server.reloadableRegistries())`), `ServerLifecycleEvents.SERVER_STOPPING` (resets the flag + `deregisterServerEvent`), `ServerLifecycleEvents.END_DATA_PACK_RELOAD` (on success: `reloadServer()` + `readLootTables` — this is the event the other loaders lack, which is why they need a `MixinMinecraftServer` instead), and a `ResourceManagerHelper.get(SERVER_DATA)` reload listener under the id `ali:fake_loot_loader` that wraps `SERVER.getFakeLootDataManager(provider)`, taking the `HolderLookup.Provider` straight from the listener-factory callback (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`). That parameter is why the old `FabricPlatformHelper.PROVIDER` static and its `ReloadableServerResourceMixin` writer are gone.
- `FabricClientBusSubscriber` — `ClientPlayConnectionEvents.JOIN`/`DISCONNECT` → `clientRegistry.loggingIn(ClientPlayNetworking.canSend(RequestLootDataMessage.TYPE))`/`loggingOut()`.
- `datagen.DataGeneration implements DataGeneratorEntrypoint` — registers `LanguageProvider` + `FakeLootProvider`.

## Platform + networking implementation

- `platform.FabricPlatformHelper implements IPlatformHelper` — three methods: `getPlugins` (Fabric `EntrypointContainer`s for key `"ali"`, filtered by `isModLoaded(plugin.getModId())`; this is the loader difference that matters in practice — Fabric discovers plugins through the `ali` **entrypoint** in `fabric.mod.json`, NeoForge through the `@AliEntrypoint` **annotation**, so a new plugin class must be declared in both places), `getConfiguration` (`FabricLoader.getConfigDir()`), `getSpawnEggItem` (vanilla `SpawnEggItem.byId`, no `ForgeSpawnEggItem` indirection). No `getLookupProvider` — that method is gone from ALI's `IPlatformHelper` entirely.
- `network.{Client,Server,NetworkUtils}` — implement `ali.network.AbstractClient`/`AbstractServer` (see `ali/CLAUDE.md`'s networking section). The 4 messages are native `CustomPacketPayload`s owned by `ali/common`, so `NetworkUtils` only declares them (`PayloadTypeRegistry.playC2S()`/`playS2C().register(TYPE, CODEC)`) and attaches handlers via `ClientPlayNetworking`/`ServerPlayNetworking.registerGlobalReceiver(TYPE, handler)` — no channel builder, no codec casting, no `setPacketHandled` boilerplate.

## Mixins

`ali.fabric.mixins.json` — two `client` mixins, no common ones:

- `MixinClientPlayNetworkAddon` (client) — injects `ClientPlayNetworkAddon.receive` HEAD and re-dispatches `StartMessage`/`LootDataChunkMessage`/`DoneMessage` **on the networking thread** (hand-building the `ClientPlayNetworking.Context`, then cancelling), to avoid the deadlock Fabric's default main-thread hand-off causes for the bulk data sync. It touches Fabric's `impl` package, so it is the most upgrade-fragile piece in this module. `awi/fabric` carries a copy.
- `MixinMinecraft` (client) — `setLevel` HEAD → `EntityStorage.onUnloadLevel()` when a level is already loaded. NeoForge does this with `LevelEvent.Unload` in its client bus subscriber instead.

There is no longer a `ReloadableServerResourceMixin`: it existed only to capture a `HolderLookup.Provider` into a static, and the provider now arrives as a parameter (see the reload listener above).

## Fabric-only compat plugins

`plugin/mods/` holds third-party compatibility that is genuinely Fabric-only because it targets Fabric ports of Forge APIs — don't try to move these to `ali/neoforge` without checking whether the target API even exists there.

⚠️ On this branch neither is actually active: `fabric.mod.json`'s `ali` entrypoint array lists only `com.yanny.ali.plugin.Plugin`. Since Fabric discovers plugins by entrypoint and the `@AliEntrypoint` annotation alone does nothing there, both classes below are currently dead code — re-add them to that array to bring them back.

- `plugin/mods/farmers_delight/Plugin` (mod id `farmersdelight`) — registers reflected loot functions (`CopySkilletFunction`, `SmokerCookFunction`), the `CanItemPerformAbilityCondition` condition, and `FDItemListing` as both item listing and item-listing collector, all via `PluginUtils`/`ReflectionUtils` (see `ali/CLAUDE.md`'s `plugin/mods` section).
- `plugin/mods/porting_lib/loot/Plugin` (mod id `porting_lib_loot`) — the Fabric GLM story: Porting Lib's `LootModifier`/`LootModifierManager`/`LootTableIdCondition`/`IGlmPlugin` are reached reflectively (`Class.forName` in a static block, guarded so the plugin degrades quietly when the mod is absent) and fed into the loader-agnostic `plugin/glm` bridge. This is Fabric's counterpart to `ali/neoforge`'s `NeoForgePlugin` GLM registration.

LootJS is **not** wired here on this branch: `lootjs_enabled=false` excludes `ali/common-lootjs` from the build entirely (and `fabric_lootjs_enabled=false` too), so it is not shadowed into the Fabric jar and `ali.lootjs.mixins.json` is absent from `fabric.mod.json` (`lootjs` appears only under `suggests`). See `ali/common-lootjs/CLAUDE.md` — on this branch no loader builds that module.

## Boilerplate vs genuine glue

`CommonAliMod`/`ClientAliMod`, `NetworkUtils` registration, and the `FabricPlatformHelper` scaffolding are structurally identical rewrites of the same contract implemented independently in `ali/neoforge` — treat changes here as needing a mirrored change there, not a shared refactor. Genuinely loader-specific and non-portable: the two mixins above, the `FakeLootDataManager` reload wiring (lifecycle events on Fabric, `AddServerReloadListenersEvent` on NeoForge), and the third-party compat plugins.
