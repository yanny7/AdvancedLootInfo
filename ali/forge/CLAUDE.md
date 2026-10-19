# ali/forge/CLAUDE.md

Guidance for `ali/forge` (`com.yanny.ali.forge`) — ALI's Forge loader entry point. See `ali/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `ali/fabric/CLAUDE.md` — the two loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a mirrored change to the other.

## Entry point

Single `AliMod` (`@Mod`). Its constructor statically builds the `SimpleChannel` (`NetworkRegistry.ChannelBuilder`, protocol version `"2"`), constructs `SERVER`, registers message handlers via `network.NetworkUtils`, wires `FMLJavaModLoadingContext` mod-bus listeners (`DataGeneration::generate`, common/client setup → `PluginManager`), and adds a Forge-bus `@SubscribeEvent onAddReloadListener` that registers `SERVER.getFakeLootDataManager()` as a reload listener (see `ali/CLAUDE.md`'s `configuration`/`datagen` section for `FakeLootDataManager`).

`ForgeCommonBusSubscriber` and `ForgeClientBusSubscriber` are the Forge-bus halves (server start/stop; client level unload and login/logout). The server-start scan runs on `ServerStartedEvent` at `EventPriority.LOWEST`, after other mods' listeners, since a mod may still fill `VillagerTrades`, its own trade data or loot data up to that event; the dedicated server's watchdog is already running then and counts the scan towards `max-tick-time`. `ForgeClientModBusSubscriber` (mod bus, client only) registers the identity factory for `ScrollableTooltip` that JEI on Forge needs (see `aci/CLAUDE.md`); it is a separate `Dist.CLIENT` class because the event references client-only types.

## Platform + networking implementation

- `platform.ForgePlatformHelper implements IPlatformHelper` — discovers plugins via annotation scanning (`ModFileScanData`/`AliEntrypoint` ASM type match, memoized with `Suppliers.memoize`), gets loot pools via `MixinLootTableForge`, uses `ForgeHooks.loadLootTable`, `ForgeSpawnEggItem`.
- `network.{Client,Server,NetworkUtils}` — wrap the same 4 messages as `ali/fabric` through `SimpleChannel.registerMessage`/`.send`/`.sendToServer`, using Forge's `Supplier<NetworkEvent.Context>` handler signature (extra `setPacketHandled(true)` boilerplate Fabric doesn't need).

## Mixins

Larger set than Fabric's: `MixinLootTableForge`, `MixinMinecraftServer` (calls `readLootTables`), `MixinForgeInternalHandler`, `MixinLootModifier`, `MixinLootTableIdCondition`, `MixinCanToolPerformAction`.

## Forge-only compat plugins

`compatibility.ReiCompatibilityWrapper` and `plugin.{ForgePlugin, GlobalLootModifier}` — Forge-specific REI/GLM glue mirroring `ali/fabric`'s `plugin/` package, but targeting Forge's native GLM API (`ali/CLAUDE.md`'s `plugin/glm` package is the loader-agnostic half of this). `ForgePlugin` also registers the loot-context preparer that writes the page's table id through `LootContext.setQueriedLootTableId`, which is what lets the probe decide Forge's `LootTableIdCondition` (whose tooltip reads the target id through `MixinLootTableIdCondition`) — the carrier is loader-specific, so `ali/common` cannot register it.

## Boilerplate vs genuine glue

The `AliMod` entrypoint shape, `NetworkUtils` registration, and `ForgePlatformHelper` scaffolding are the Forge-side rewrite of the same contract `ali/fabric` implements. Genuinely loader-specific: the mixins (Forge's `LootTable`/internal-handler class shapes differ from Fabric's), the reload-listener wiring, and the Forge-only compat plugins above.

## Gametest

The `gametest` source set (see `ali/CLAUDE.md`'s gametest section) is wired into the `gametest` loom run differently from Fabric:

- The run sets `environment "gametestserver"` together with `forgeTemplate "gameTestServer"`. Loom takes the launch target from `launch.cfg` by environment, so `server()` would start the dedicated server (`forgeserveruserdev`), which stops at the EULA and never exits.
- A per-run `mods { main { … } }` puts the gametest source set into the `ali` mod, so Forge scans its annotations; outside this run the classes do not exist. The plugin in it, `GameTestPlugin`, therefore reports `ali` as its mod id — plugins of a mod that is not loaded are skipped.
- `forge.enabledGameTestNamespaces=ali` enables the test, which is `@GameTestHolder("ali")` with `@PrefixGameTestTemplate(false)` and uses the structure `ali:empty` (`data/ali/structures/empty.nbt`); Forge ships no empty structure.
- The Forge-only fixtures are the global loot modifiers: `GameTestLootModifiers` registers three codecs (`ali:gametest_add_item`, `ali:gametest_remove_item`, `ali:gametest_unadapted`), `GameTestPlugin` adapts the first two and leaves the third to auto-detection, and the datapack under `src/gametest/resources/data/` binds them to test tables, a block page, an entity page and no page at all. The wrapper adds the `show_unbounded_global_loot_modifiers` variant, which has nothing to show on Fabric.
