# awi/fabric/CLAUDE.md

Guidance for `awi/fabric` (`com.yanny.awi.fabric`) — AWI's Fabric loader entry point; its Forge counterpart is `awi/forge` (see `awi/forge/CLAUDE.md` — the two loader modules implement the same contract independently, there's no shared loader-glue base class, so a change to one commonly needs a matching change to the other). See `awi/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. Structurally near-identical to `ali/fabric/CLAUDE.md`, but slimmer since AWI has no loot domain and no third-party compat plugins to wire in.

## Entry points

`CommonAwiMod`/`ClientAwiMod` (`ModInitializer`/`ClientModInitializer`), `FabricCommonBusSubscriber`/`FabricClientBusSubscriber`.

## Platform + networking implementation

- `platform.FabricPlatformHelper` — implements only `getPlugins` (Fabric `EntrypointContainer`s for key `"awi"`, memoized with `Suppliers.memoize` so repeated calls neither re-instantiate the plugins nor re-log the discovery)/`getConfiguration` (AWI's `IPlatformHelper` interface is smaller than ALI's — no loot-pool/spawn-egg/loot-table-parsing methods, since there's no loot domain to support).
- `network.{Client,Server,NetworkUtils}` — worldgen packet IDs (`WorldgenDataChunkMessage`, `RequestWorldgenDataMessage`, etc. — see `awi/CLAUDE.md`'s networking table for the full ALI↔AWI packet-name mapping).

## Mixins

The `mixin` package is empty except for `package-info.java` — no Fabric mixins are needed for AWI (`awi/forge` does need one, `MixinMinecraftServer`, because Forge has no `END_DATA_PACK_RELOAD` event equivalent) (contrast with `ali/fabric`'s `MixinLootTableFabric`/`MixinCombinedIngredient`, which exist because ALI needs to reach into loot-table internals AWI has no equivalent of).
