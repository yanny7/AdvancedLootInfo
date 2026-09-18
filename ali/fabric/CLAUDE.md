# ali/fabric/CLAUDE.md

Guidance for `ali/fabric` (`com.yanny.ali.fabric`) — ALI's Fabric loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/neoforge/CLAUDE.md` — the loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the other. (`ali/forge` is the third implementation but is excluded from the build and unported on this branch — see `ali/forge/CLAUDE.md`.)

## Entry points

- `CommonAliMod implements ModInitializer` — holds `static final Server SERVER`; `onInitialize` calls `FabricCommonBusSubscriber.registerEvents(SERVER)`, `NetworkUtils.registerCommon(SERVER)`, `PluginManager.registerCommonEvent()`.
- `ClientAliMod implements ClientModInitializer` — holds `static final Client CLIENT`; the same three steps on the client side.
- `FabricCommonBusSubscriber` — registers `ServerWorldEvents.LOAD` (guarded by a `serverLoaded` flag because it fires per level, unlike NeoForge's `ServerStartingEvent`; `registerServerEvent` + `SERVER.readLootTables(server.reloadableRegistries())`), `ServerLifecycleEvents.SERVER_STOPPING` (resets the flag + `deregisterServerEvent`), `ServerLifecycleEvents.END_DATA_PACK_RELOAD` (on success: `reloadServer()` + `readLootTables` — this is the event the other loaders lack, which is why they need a `MixinMinecraftServer` instead), and a `ResourceManagerHelper.get(SERVER_DATA)` reload listener under the id `ali:fake_loot_loader` that wraps `SERVER.getFakeLootDataManager(provider)`, taking the `HolderLookup.Provider` straight from the listener-factory callback (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`). That parameter is why the old `FabricPlatformHelper.PROVIDER` static and its `ReloadableServerResourceMixin` writer are gone.
- `FabricClientBusSubscriber` — `ClientPlayConnectionEvents.JOIN`/`DISCONNECT` → `clientRegistry.loggingIn(ClientPlayNetworking.canSend(RequestLootDataMessage.TYPE))`/`loggingOut()`.
- `datagen.DataGeneration implements DataGeneratorEntrypoint` — registers `LanguageProvider` + `FakeLootProvider`.

## Platform + networking implementation

- `platform.FabricPlatformHelper implements IPlatformHelper` — three methods: `getPlugins` (Fabric `EntrypointContainer`s for key `"ali"`, filtered by `isModLoaded(plugin.getModId())`; this is the loader difference that matters in practice — Fabric discovers plugins through the `ali` **entrypoint** in `fabric.mod.json`, NeoForge through the `@AliEntrypoint` **annotation**, so a new plugin class must be declared in both places; the scan is memoized with `Suppliers.memoize`, because a GLM driver calls `getPlugins` again per server registry build on top of `PluginManager`'s startup call, and an unmemoized scan re-instantiates every plugin and re-logs the discovery each time), `getConfiguration` (`FabricLoader.getConfigDir()`), `getSpawnEggItem` (vanilla `SpawnEggItem.byId`, no `ForgeSpawnEggItem` indirection). No `getLookupProvider` — that method is gone from ALI's `IPlatformHelper` entirely.
- `network.{Client,Server,NetworkUtils}` — implement `ali.network.AbstractClient`/`AbstractServer` (see `ali/CLAUDE.md`'s networking section). The 4 messages are native `CustomPacketPayload`s owned by `ali/common`, so `NetworkUtils` only declares them (`PayloadTypeRegistry.playC2S()`/`playS2C().register(TYPE, CODEC)`) and attaches handlers via `ClientPlayNetworking`/`ServerPlayNetworking.registerGlobalReceiver(TYPE, handler)` — no channel builder, no codec casting, no `setPacketHandled` boilerplate.

## Mixins

`ali.fabric.mixins.json` — two `client` mixins plus the ingredient accessors:

- `MixinClientPlayNetworkAddon` (client) — injects `ClientPlayNetworkAddon.receive` HEAD and re-dispatches `StartMessage`/`LootDataChunkMessage`/`DoneMessage` **on the networking thread** (hand-building the `ClientPlayNetworking.Context`, then cancelling), to avoid the deadlock Fabric's default main-thread hand-off causes for the bulk data sync. It touches Fabric's `impl` package, so it is the most upgrade-fragile piece in this module. `awi/fabric` carries a copy.
- `MixinMinecraft` (client) — `setLevel` HEAD → `EntityStorage.onUnloadLevel()` when a level is already loaded. NeoForge does this with `LevelEvent.Unload` in its client bus subscriber instead.
- `MixinCombinedIngredient`/`MixinComponentsIngredient`/`MixinCustomDataIngredient`/`MixinDifferenceIngredient` — pure `@Accessor` interfaces onto Fabric API's builtin custom ingredients (`impl.recipe.ingredient.builtin`), read by `plugin/FabricIngredientTooltipUtils`. `CombinedIngredient` is package-private, hence the string `targets` form.

There is no longer a `ReloadableServerResourceMixin`: it existed only to capture a `HolderLookup.Provider` into a static, and the provider now arrives as a parameter (see the reload listener above).

## Fabric-only compat plugins

`plugin/` holds third-party compatibility that's genuinely Fabric-only because it targets Fabric-specific APIs — don't try to port these to `ali/forge`/`ali/neoforge` without checking whether the target API even has an equivalent there:
- `FabricPlugin` + `FabricIngredientTooltipUtils` — registers a tooltip for Fabric API's `CustomIngredientImpl`, the wrapper every custom ingredient loaded from JSON or a packet arrives in (`CustomIngredient.toVanilla`). It renders the Any/All/Difference/Components/Custom-Data builtins itself, hands a wrapped custom ingredient that is also an `Ingredient` back to `getIngredientTooltip` so a tooltip registered for its own class applies, and falls back to the auto-detected JSON tooltip for anything else. A custom ingredient that implements only `CustomIngredient` has no registration of its own: `registerIngredientTooltip` is bound to `Ingredient`. LootJS compat itself lives in `ali/common-lootjs` (see `ali/common-lootjs/CLAUDE.md`).

Per-target-mod compatibility is **not** here — it ships in the separate ALICompat jar, see `alicompat/CLAUDE.md`. Neither is the Global Loot Modifier path: Fabric has no GLM machinery of its own, so ALI reads Porting Lib's, and that whole driver lives in ALICompat's `portinglib` source set. Without that optional jar a Fabric client shows loot unmodified by any GLM.

LootJS is **not** wired here on this branch: `lootjs_enabled=false` excludes `ali/common-lootjs` from the build entirely (and `fabric_lootjs_enabled=false` too), so it is not shadowed into the Fabric jar and `ali.lootjs.mixins.json` is absent from `fabric.mod.json` (`lootjs` appears only under `suggests`). See `ali/common-lootjs/CLAUDE.md` — on this branch no loader builds that module.

## Boilerplate vs genuine glue

`CommonAliMod`/`ClientAliMod`, `NetworkUtils` registration, and the `FabricPlatformHelper` scaffolding are structurally identical rewrites of the same contract implemented independently in `ali/neoforge` — treat changes here as needing a mirrored change there, not a shared refactor. Genuinely loader-specific and non-portable: the two mixins above, the `FakeLootDataManager` reload wiring (lifecycle events on Fabric, `AddServerReloadListenersEvent` on NeoForge), and the third-party compat plugins.
