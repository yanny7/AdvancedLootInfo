# awi/fabric/CLAUDE.md

Guidance for `awi/fabric` (`com.yanny.awi.fabric`) — AWI's Fabric loader entry point, and AWI's **only** loader module on any branch (no `forge`/`neoforge` ever). See `awi/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. Structurally near-identical to `ali/fabric/CLAUDE.md`, but slimmer since AWI has no loot domain and no third-party compat plugins to wire in.

## Entry points

`CommonAliMod`/`ClientAliMod` (same naming as ALI's fabric entrypoints), `FabricCommonBusSubscriber`/`FabricClientBusSubscriber`.

## Platform + networking implementation

- `platform.FabricPlatformHelper` — implements only `getPlugins`/`getConfiguration` (AWI's `IPlatformHelper` interface is smaller than ALI's — no loot-pool/spawn-egg/loot-table-parsing methods, since there's no loot domain to support).
- `network.{Client,Server,NetworkUtils}` — worldgen packet IDs (`WorldgenDataChunkMessage`, `RequestWorldgenDataMessage`, etc. — see `awi/CLAUDE.md`'s networking table for the full ALI↔AWI packet-name mapping).

## Mixins

The `mixin` package is empty except for `package-info.java` — no Fabric mixins are needed for AWI (contrast with `ali/fabric`'s `MixinLootTableFabric`/`MixinCombinedIngredient`, which exist because ALI needs to reach into loot-table internals AWI has no equivalent of).
