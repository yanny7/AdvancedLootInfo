# awi/forge/CLAUDE.md

Guidance for `awi/forge` (`com.yanny.awi.forge`) — AWI's Forge loader entry point. See `awi/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `awi/fabric/CLAUDE.md` — the two loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a matching change to the other.

## Entry point

Single `AwiMod` (`@Mod`). Its constructor statically builds the `SimpleChannel` (`NetworkRegistry.ChannelBuilder`, protocol version `"1"`), constructs `SERVER`, registers message handlers via `network.NetworkUtils`, and wires `FMLJavaModLoadingContext` mod-bus listeners (`DataGeneration::generate`, common/client setup → `PluginManager`).

`ForgeCommonBusSubscriber` (`ServerStartingEvent` → `registerServerEvent` + `SERVER.readWorldgenInfo(overworld)`, `ServerStoppingEvent` → `deregisterServerEvent`) and `ForgeClientBusSubscriber` (`ClientPlayerNetworkEvent.LoggingIn`/`LoggingOut` → `clientRegistry.loggingIn`/`loggingOut`) are the Forge-bus halves. Unlike Fabric's `ServerWorldEvents.LOAD`, `ServerStartingEvent` fires exactly once, so no "already loaded" guard is needed here. There is no client level-unload handler — AWI has no per-level client cache to drop.

## Platform + networking implementation

- `platform.ForgePlatformHelper implements IPlatformHelper` — implements only `getPlugins` (annotation scanning: `ModFileScanData`/`AwiEntrypoint` ASM type match, memoized with `Suppliers.memoize`) and `getConfiguration` (`FMLPaths.CONFIGDIR`), since AWI's `IPlatformHelper` has no loot-pool/spawn-egg/loot-table-parsing methods. This is the loader difference that matters in practice: Fabric discovers plugins through the `awi` **entrypoint** in `fabric.mod.json`, Forge through the `@AwiEntrypoint` **annotation** — a new plugin class must be declared in both places.
- `network.{Client,Server,NetworkUtils}` — the 4 worldgen messages (`WorldgenDataChunkMessage`, `StartMessage`, `DoneMessage`, `RequestWorldgenDataMessage`) registered through `SimpleChannel.registerMessage`, sent via `PacketDistributor.PLAYER`/`sendToServer`, using Forge's `Supplier<NetworkEvent.Context>` handler signature (with the `setPacketHandled(true)` boilerplate Fabric doesn't need).

## Mixins

`mixin.MixinMinecraftServer` only — hooks `reloadResources` TAIL to re-run `PluginManager.reloadServer()` + `SERVER.readWorldgenInfo(server.overworld())` after a successful `/reload`. Forge has no `END_DATA_PACK_RELOAD` event equivalent, which is why this is a mixin here and an event handler in `awi/fabric`.

## Viewer wiring

`compatibility.ReiCompatibilityWrapper` (`@REIPluginClient` subclass of `awi/common-rei`'s `ReiCompatibility`) is the only viewer glue Forge needs — EMI (`@EmiEntrypoint`) and JEI (`@JeiPlugin`) discover their plugin classes from the shadowed common modules by annotation.

Mixin-config registration on Forge happens twice over, which is worth knowing when adding a config: `loom { forge { mixinConfig "..." } }` in `build.gradle` writes every listed config into the jar's `MANIFEST.MF` `MixinConfigs` attribute (and into the dev-run launch args), while `META-INF/mods.toml`'s `[[mixins]]` blocks list only `awi.mixins.json`/`awi.forge.mixins.json`. The EMI-only config `awi.emi.mixins.json` is therefore active in production purely via the manifest — a new config added to `build.gradle` needs no `mods.toml` entry. When EMI is absent, `MixinRecipeScreen`'s missing `@Mixin` target only produces a `@Mixin target ... was not found` WARN (Mixin skips it; `"required": true` does not make that fatal), so no `IMixinConfigPlugin` gate is needed.
