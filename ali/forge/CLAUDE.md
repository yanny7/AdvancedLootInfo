# ali/forge/CLAUDE.md

Guidance for `ali/forge` (`com.yanny.ali.forge`) — ALI's Forge loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/fabric/CLAUDE.md` and `ali/neoforge/CLAUDE.md` — the loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the others.

## Entry point

Single `AliMod` (`@Mod`). Its static initializer builds the `SimpleChannel` (`net.minecraftforge.network.ChannelBuilder.named(...)`, int protocol version `2`, `.optional()`), constructs `SERVER`, and registers message handlers via `network.NetworkUtils`. The constructor wires `FMLJavaModLoadingContext` mod-bus listeners (`DataGeneration::generate`, common/client setup → `PluginManager`) and registers itself on `MinecraftForge.EVENT_BUS` for `@SubscribeEvent onAddReloadListener(AddReloadListenerEvent)`, which publishes `event.getRegistries()` into `ForgePlatformHelper.PROVIDER` (the backing store for `getLookupProvider()`) and registers `SERVER.getFakeLootDataManager()` as a reload listener (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`).

## Platform + networking implementation

- `platform.ForgePlatformHelper implements IPlatformHelper` — `getPlugins` (annotation scanning: `ModFileScanData`/`@AliEntrypoint` ASM type match, memoized with `Suppliers.memoize`), `getConfiguration` (`FMLPaths.CONFIGDIR`), `getLookupProvider` (returns the mutable static `PROVIDER` set from `AddReloadListenerEvent` — `null` until that event first fires), `getSpawnEggItem` (`ForgeSpawnEggItem.fromEntityType`, the one place a Forge-specific class is genuinely required; `ali/neoforge` uses vanilla `SpawnEggItem.byId`).
- `network.{Client,Server,NetworkUtils}` — the same 4 messages as `ali/fabric`, registered through `SimpleChannel.messageBuilder(...).codec(...).consumerNetworkThread(...).add()` and sent via `PacketDistributor.PLAYER.with(player)`/`PacketDistributor.SERVER.noArg()`, using Forge's `CustomPayloadEvent.Context` handler signature (with the `setPacketHandled(true)` boilerplate Fabric doesn't need). The messages are `CustomPacketPayload`s owned by `ali/common` with `StreamCodec<RegistryFriendlyByteBuf, ...> CODEC`, but `messageBuilder` wants a `StreamCodec<FriendlyByteBuf, ...>`, so `NetworkUtils` casts each codec through `(Object)`.

## Mixins

`ali.forge.mixins.json` (all in `mixin/`):

- `MixinMinecraftServer` — `reloadResources` TAIL → `PluginManager.reloadServer()` + `SERVER.readLootTables(server.reloadableRegistries())`.
- `MixinLootModifier` — `@Accessor` for `LootModifier.conditions`.
- `MixinForgeInternalHandler` — `@Invoker` (with `remap = false`) for the package-private static `ForgeInternalHandler.getLootModifierManager()`, the only way to enumerate loaded GLMs.
- `MixinCompoundIngredient`/`MixinDifferenceIngredient`/`MixinIntersectionIngredient`/`MixinPartialNBTIngredient`/`MixinStrictNBTIngredient` — `@Accessor` interfaces over Forge's `common.crafting.ingredients` classes, read by `plugin/ForgeIngredientTooltipUtils`. Note the package is `...crafting.ingredients` on this branch (it was `...crafting` on `1.20.1`), and `PartialNBTIngredient.items` is a `List`, not a `Set`.

This is a *smaller* set than `ali/neoforge`'s, which also needs accessors for `LootTableIdCondition`/`CanItemPerformAbility`/`AddTableLootModifier`: on Forge those conditions are records whose components are public (`cond.id()`, `cond.action()`), so `ForgePlugin` reads them directly.

## Forge-only compat plugins

`plugin.{ForgePlugin, GlobalLootModifier, IForgePlugin}` plus `plugin.mods.farmers_delight` — Forge-specific GLM glue mirroring `ali/neoforge`'s `plugin/` package but targeting Forge's native GLM API (`ali/CLAUDE.md`'s `plugin/glm` package is the loader-agnostic half). `ForgePlugin` registers the `CanToolPerformAction`/`LootTableIdCondition` condition tooltips, the five ingredient tooltips below, and the GLM bridge over `MixinForgeInternalHandler.getLootModifierManager()`.

Forge's custom ingredients extend `AbstractIngredient extends Ingredient`, so they dispatch through plain `registerIngredientTooltip(Class, ...)` — no unwrapper hook, unlike NeoForge (see `ali/neoforge/CLAUDE.md`). Forge 1.21.1 has **no** `BasicItemListing`, so unlike `1.20.1` and unlike NeoForge there is no item-listing registration here; don't port that part back.

`compatibility/` holds only a `package-info` on this branch: REI is disabled for Forge (`forge_rei_enabled=false`, only `runAliForgeJeiClient` is generated), and `@REIPluginClient` is loader-specific so it cannot live in `ali/common-rei` — a `compatibility.ReiCompatibilityWrapper` must be (re)added if that flag is ever flipped on, mirroring `ali/neoforge`'s. `awi/forge` is in the same state for the same reason.

## Boilerplate vs genuine glue

The `AliMod` entrypoint shape, `NetworkUtils` registration, and `ForgePlatformHelper` scaffolding are the Forge-side rewrite of the same contract `ali/fabric` implements. Genuinely loader-specific: the mixins (Forge's internal-handler/GLM class shapes differ from NeoForge's), the reload-listener wiring, and the Forge-only compat plugins above.
