# awi/fabric/CLAUDE.md

Guidance for `awi/fabric` (`com.yanny.awi.fabric`) — AWI's Fabric loader entry point; its counterparts are `awi/forge` and `awi/neoforge` (see `awi/forge/CLAUDE.md`, `awi/neoforge/CLAUDE.md` — the loader modules implement the same contract independently, there's no shared loader-glue base class, so a change to one commonly needs a matching change to the others). See `awi/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. Structurally near-identical to `ali/fabric` (see `ali/fabric/CLAUDE.md`), but slimmer: AWI has no loot domain, no fake-loot reload listener, no `HolderLookup.Provider` plumbing, and no third-party compat plugins.

## Entry points

- `CommonAwiMod implements ModInitializer` / `ClientAwiMod implements ClientModInitializer` — hold `static final Server SERVER` / `Client CLIENT`; each `onInitialize*` calls the matching `Fabric*BusSubscriber.registerEvents()`, `NetworkUtils.registerCommon/registerClient(...)`, then `PluginManager.registerCommonEvent()`/`registerClientEvent()`.
- `FabricCommonBusSubscriber` — `ServerWorldEvents.LOAD` (guarded by a `serverLoaded` flag since it fires per level, unlike Forge/NeoForge's `ServerStartingEvent`; `registerServerEvent(world)` + `SERVER.readWorldgenInfo(server.overworld())`), `ServerLifecycleEvents.SERVER_STOPPING` (resets the flag + `deregisterServerEvent`), `ServerLifecycleEvents.END_DATA_PACK_RELOAD` (on success: `reloadServer()` + `readWorldgenInfo`). Unlike `ali/fabric` it registers **no** resource-reload listener.
- `FabricClientBusSubscriber` — `ClientPlayConnectionEvents.JOIN`/`DISCONNECT` → `clientRegistry.loggingIn(ClientPlayNetworking.canSend(RequestWorldgenDataMessage.TYPE))`/`loggingOut()`.
- `datagen.DataGeneration implements DataGeneratorEntrypoint` — registers `LanguageProvider` only (no `FakeLootProvider` equivalent). The generated `src/main/generated/assets/awi/lang/en_us.json` here is the reference copy the other loader modules are kept in sync with.

## Platform + networking implementation

- `platform.FabricPlatformHelper` — implements only `getPlugins` (Fabric `EntrypointContainer`s for key `"awi"`, filtered by `isModLoaded`, memoized with `Suppliers.memoize` so repeated calls neither re-instantiate the plugins nor re-log the discovery) and `getConfiguration` (`FabricLoader.getConfigDir()`); AWI's `IPlatformHelper` is smaller than ALI's — no loot-pool/spawn-egg/loot-table-parsing methods and no `getLookupProvider`, since AWI reads the `HolderLookup.Provider` off the `ServerLevel` it is already scanning. Plugin discovery is by **entrypoint** here vs by `@AwiEntrypoint` **annotation** on Forge/NeoForge, so a new plugin class must be declared in both places.
- `network.{Client,Server,NetworkUtils}` — the 4 worldgen messages (`WorldgenDataChunkMessage`, `StartMessage`, `DoneMessage`, `RequestWorldgenDataMessage`; see `awi/CLAUDE.md`'s networking table for the ALI↔AWI name mapping) are native `CustomPacketPayload`s owned by `awi/common`, so `NetworkUtils` just declares them (`PayloadTypeRegistry.playC2S()`/`playS2C().register(TYPE, CODEC)`) and attaches handlers via `ClientPlayNetworking`/`ServerPlayNetworking.registerGlobalReceiver(TYPE, handler)` — no channel builder and no codec casting, unlike `awi/forge`.

## Mixins

`awi.fabric.mixins.json` declares exactly one `client` mixin: `MixinClientPlayNetworkAddon` — a copy of `ali/fabric`'s, injecting `ClientPlayNetworkAddon.receive` HEAD to re-dispatch `StartMessage`/`WorldgenDataChunkMessage`/`DoneMessage` **on the networking thread** (avoiding the deadlock Fabric's default main-thread hand-off causes for the bulk data sync), then cancelling the original call. It touches Fabric's `impl` package, so it is the most upgrade-fragile piece in this module; its injected method is still named `ali$reload`, a copy-paste leftover.

There is no `MixinMinecraftServer` here (`END_DATA_PACK_RELOAD` covers what `awi/forge`/`awi/neoforge` need a mixin for) and no client level-unload mixin (AWI has no per-level client cache, unlike `ali/fabric`'s `MixinMinecraft` → `EntityStorage`).
