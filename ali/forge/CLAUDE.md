# ali/forge/CLAUDE.md

Guidance for `ali/forge` (`com.yanny.ali.forge`) — ALI's Forge loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/fabric/CLAUDE.md` — the two loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the other.

## Entry point

Single `AliMod` (`@Mod`). Its constructor statically builds the `SimpleChannel` (`NetworkRegistry.ChannelBuilder`, protocol version `"2"`), constructs `SERVER`, registers message handlers via `network.NetworkUtils`, wires `FMLJavaModLoadingContext` mod-bus listeners (`DataGeneration::generate`, common/client setup → `PluginManager`), and adds a Forge-bus `@SubscribeEvent onAddReloadListener` that registers `SERVER.getFakeLootDataManager()` as a reload listener (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`).

## Platform + networking implementation

- `platform.ForgePlatformHelper implements IPlatformHelper` — discovers plugins via annotation scanning (`ModFileScanData`/`AliEntrypoint` ASM type match, memoized with `Suppliers.memoize`), gets loot pools via `MixinLootTableForge`, uses `ForgeHooks.loadLootTable`, `ForgeSpawnEggItem`.
- `network.{Client,Server,NetworkUtils}` — wrap the same 4 messages as `ali/fabric` through `SimpleChannel.registerMessage`/`.send`/`.sendToServer`, using Forge's `Supplier<NetworkEvent.Context>` handler signature (extra `setPacketHandled(true)` boilerplate Fabric doesn't need).

## Mixins

Larger set than Fabric's: `MixinLootTableForge`, `MixinMinecraftServer` (calls `readLootTables`), `MixinForgeInternalHandler`, `MixinLootModifier`, `MixinLootTableIdCondition`, `MixinCanToolPerformAction`.

## Forge-only compat plugins

`compatibility.ReiCompatibilityWrapper` and `plugin.{ForgePlugin, GlobalLootModifier, IForgePlugin}` — Forge-specific REI/GLM glue mirroring `ali/fabric`'s `plugin/` package, but targeting Forge's native GLM API (`ali/CLAUDE.md`'s `plugin/glm` package is the loader-agnostic half of this).

## Boilerplate vs genuine glue

The `AliMod` entrypoint shape, `NetworkUtils` registration, and `ForgePlatformHelper` scaffolding are the Forge-side rewrite of the same contract `ali/fabric` implements. Genuinely loader-specific: the mixins (Forge's `LootTable`/internal-handler class shapes differ from Fabric's), the reload-listener wiring, and the Forge-only compat plugins above.
