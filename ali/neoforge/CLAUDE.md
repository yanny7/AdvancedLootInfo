# ali/neoforge/CLAUDE.md

Guidance for `ali/neoforge` (`com.yanny.ali.neoforge`) — ALI's NeoForge loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/fabric/CLAUDE.md`/`ali/forge/CLAUDE.md` — the loader modules implement the same contract independently, with no shared glue base class, so a change to one commonly needs a mirrored change to the others. `awi/neoforge/CLAUDE.md` is this module with the loot domain removed; it documents the NeoForge-specific mechanics (payload registration, access transformers, mixin-config declaration) that both modules share.

## Entry point

Single `AliMod` (`@Mod`, constructor takes the mod `IEventBus`). Unlike Forge it builds **no** `SimpleChannel`: `SERVER`/`CLIENT` are plain fields and payloads are registered in `RegisterPayloadHandlersEvent` (`event.registrar(MOD_ID).optional().versioned("2")`). Mod-bus listeners: `DataGeneration::generate`, common/client setup → `PluginManager`. It also registers itself on `NeoForge.EVENT_BUS` for `@SubscribeEvent onAddReloadListener(AddReloadListenerEvent)`, which does two things ALI cannot do without a game-bus event: publishes `event.getRegistryAccess()` into `NeoForgePlatformHelper.PROVIDER` (the backing store for `getLookupProvider()`) and registers `SERVER.getFakeLootDataManager()` as a reload listener (see `ali/CLAUDE.md`'s `configuration`/`datagen` section).

`NeoForgeCommonBusSubscriber` (`ServerStartingEvent` → `registerServerEvent` + `SERVER.readLootTables(server.reloadableRegistries())`, `ServerStoppingEvent` → `deregisterServerEvent`) and `NeoForgeClientBusSubscriber` (`ClientPlayerNetworkEvent.LoggingIn`/`LoggingOut` → `clientRegistry.loggingIn`/`loggingOut`, plus `LevelEvent.Unload` → `EntityStorage.onUnloadLevel()` for client levels) are the game-bus halves, both `@EventBusSubscriber(bus = Bus.GAME)`.

## Platform + networking implementation

- `platform.NeoForgePlatformHelper implements IPlatformHelper` — `getPlugins` (annotation scanning: `ModFileScanData`/`@AliEntrypoint` ASM type match, memoized with `Suppliers.memoize`), `getConfiguration` (`FMLPaths.CONFIGDIR`), `getLookupProvider` (returns the mutable static `PROVIDER` set from `AddReloadListenerEvent` — note it is `null` until the first reload-listener event fires), `getSpawnEggItem` (vanilla `SpawnEggItem.byId`, no `ForgeSpawnEggItem` indirection needed).
- `network.{Client,Server,NetworkUtils}` — the same 4 messages as the other loaders, registered as native `CustomPacketPayload`s: `registrar.executesOn(HandlerThread.NETWORK).playToClient/playToServer(TYPE, CODEC, handler)`. No codec casting (NeoForge accepts `awi`/`ali`-common's `StreamCodec<RegistryFriendlyByteBuf, ...>` directly) and no `setPacketHandled` boilerplate, which is why these classes are much shorter than `ali/forge`'s. Sends go through `PacketDistributor.sendToPlayer`/`sendToServer`, each guarded by `hasChannel(<payload>.TYPE)`.

## Mixins

`ali.neoforge.mixins.json` (`useLegacyMixinAp = true`, refmap `ali.neoforge.refmap.json`):

- `MixinMinecraftServer` — `reloadResources` TAIL → `PluginManager.reloadServer()` + `SERVER.readLootTables(server.reloadableRegistries())` after a successful `/reload`.
- `MixinLootModifier`, `MixinLootTableIdCondition`, `MixinCanItemPerformAbility`, `MixinAddTableLootModifier` — `@Accessor` interfaces over NeoForge's GLM/condition classes (`LootModifier.conditions`, `LootTableIdCondition.targetLootTableId`, `CanItemPerformAbility.ability`, `AddTableLootModifier.table`). Note `MixinCanItemPerformAbility` needs `remap = false`.
- `MixinNeoForgeEventHandler` — `@Invoker` for the package-private static `NeoForgeEventHandler.getLootModifierManager()`, the only way to enumerate loaded GLMs.

Mixin configs are declared **once**, in `META-INF/neoforge.mods.toml`'s `[[mixins]]` blocks (`ali.mixins.json`, `ali.emi.mixins.json`, `ali.neoforge.mixins.json`, `ali.lootjs.mixins.json`) — no `loom { forge { mixinConfig ... } }`/manifest duplication like on Forge. That list is unconditional, so it assumes `neoforge_emi_enabled`/`neoforge_lootjs_enabled` stay `true`; turning either off leaves the toml pointing at a config that is no longer shadowed in.

## Access transformers

NeoForge does not read access wideners, so `META-INF/accesstransformer.cfg` is a hand-maintained translation of `ali/common`'s `ali.accesswidener` (`accessible field X y Ldesc;` → `public X y`, `accessible method X y (desc)ret` → `public X y(desc)ret`, `accessible class X` → `public X`), plus one NeoForge-only entry (`CanToolPerformAction.action`). It is deliberately **not** a full 1:1 copy — the `# TESTS` block of the accesswidener is test-only and stays out. **Any entry added to `ali.accesswidener` for production code must be mirrored here**, or the shadowed `ali/common` code will compile (it compiles against the widened jar) and then fail at runtime on NeoForge with `IllegalAccessError`. `awi/neoforge` carries the same duplicated file for the same reason.

## NeoForge-only compat plugins

- `plugin.NeoForgePlugin` (`@AliEntrypoint`, mod id `neoforge`) — registers condition tooltips for `CanItemPerformAbility`/`LootTableIdCondition`, a value tooltip for `ItemAbility`, and the whole GLM bridge via `registry.registerLootModifiers`. That bridge walks `LootModifierManager.getAllLootMods()`, maps each GLM class through a registry populated by `IForgePlugin`s, hands `AddTableLootModifier` a built-in handler, and falls back to `GlobalLootModifierUtils.getMissingGlobalLootModifier` for unknown classes (logging `Missing GLM for ...` once per class — the signal that a new compat shim is worth writing). `ali/CLAUDE.md`'s `plugin/glm` package is the loader-agnostic half.
- `plugin.IForgePlugin` — extra `IPlugin` extension (`registerGlobalLootModifier(IRegistry, ILootTableIdConditionPredicate)`) that GLM-aware compat plugins implement; kept under the `IForge*` name shared with `ali/forge` even on NeoForge.
- `plugin.GlobalLootModifier` — `BaseAccessor<LootModifier>` base for reflective GLM shims, pre-reading `conditions`.
- `plugin.mods.farmers_delight` — the one bundled third-party shim (`@AliEntrypoint` `Plugin` for mod id `farmersdelight`): 4 GLM classes (`AddItemModifier`, `FDAddTableLootModifier`, `PastrySlicingModifier`, `ReplaceItemModifier`) plus 2 reflected loot functions (`CopySkilletFunction`, `SmokerCookFunction`).
- `compatibility.ReiCompatibilityWrapper` — `@REIPluginClient` subclass of `ali/common-rei`'s `ReiCompatibility`; the annotation is loader-specific and cannot live in the common module. EMI (`@EmiEntrypoint`) and JEI (`@JeiPlugin`) need no wrapper.

## Build specifics

`build.gradle` is the only loader module with a live list of `modCompileOnly "curse.maven:..."` dependencies (LootJS/KubeJS/Rhino, Aether, Deeper and Darker, Farmer's Delight, Immersive Engineering, Moonlight, Repurposed Structures, Sawmill, Snow! Real Magic!, The Bumblezone) — `ali/forge`'s equivalents are all commented out, so **NeoForge is where the third-party compat code is actually compiled** and the first place a mod-version bump breaks the build. All three viewers are enabled here (`neoforge_emi_enabled`/`neoforge_jei_enabled`/`neoforge_rei_enabled=true`), so `runAliNeoforgeEmiClient`/`...JeiClient`/`...ReiClient` all exist. Datagen output lives in `src/main/generated` (lang + the `fake_loot` entity JSONs from `FakeLootProvider`); `src/main/resources/assets/ali/lang/{es_ar,zh_cn}.json` are the hand-maintained translations shipped per loader module.
