# awi/forge/CLAUDE.md

Guidance for `awi/forge` (`com.yanny.awi.forge`) — AWI's Forge loader entry point. See `awi/CLAUDE.md` for the mod logic this glues into, and `aci/CLAUDE.md`'s "platform" section for the `ICorePlatformHelper`/`Services` abstraction being implemented here. See also `awi/fabric/CLAUDE.md` and `awi/neoforge/CLAUDE.md` — the loader modules implement the same contract independently; there's no shared loader-glue base class, so a change to one commonly needs a matching change to the others.

## ⚠️ Not built on this branch

`forge_enabled=false` in `gradle.properties`, so `settings.gradle` never includes `awi:forge` — Forge has been dropped on `1.21.5` in favour of NeoForge. The sources are still here but have never been compiled against 1.21.5, so treat everything below as a description of the last working (1.21.1) state; `datagen.DataGeneration` still uses the un-split `GatherDataEvent` + `event.includeClient()` shape that `awi/neoforge` had to abandon, which is the first thing to check if Forge is ever revived here. `ali/forge` is in the same state (see `ali/forge/CLAUDE.md`).

## Entry point

Single `AwiMod` (`@Mod`). Its constructor statically builds the `SimpleChannel` (`net.minecraftforge.network.ChannelBuilder`, int protocol version `1`, `.optional()`), constructs `SERVER`, registers message handlers via `network.NetworkUtils`, and wires `FMLJavaModLoadingContext` mod-bus listeners (`DataGeneration::generate`, common/client setup → `PluginManager`).

`ForgeCommonBusSubscriber` (`ServerStartingEvent` → `registerServerEvent` + `SERVER.readWorldgenInfo(overworld)`, `ServerStoppingEvent` → `deregisterServerEvent`) and `ForgeClientBusSubscriber` (`ClientPlayerNetworkEvent.LoggingIn`/`LoggingOut` → `clientRegistry.loggingIn`/`loggingOut`) are the Forge-bus halves. Unlike Fabric's `ServerWorldEvents.LOAD`, `ServerStartingEvent` fires exactly once, so no "already loaded" guard is needed here. There is no client level-unload handler — AWI has no per-level client cache to drop.

## Platform + networking implementation

- `platform.ForgePlatformHelper implements IPlatformHelper` — implements only `getPlugins` (annotation scanning: `ModFileScanData`/`AwiEntrypoint` ASM type match, memoized with `Suppliers.memoize`) and `getConfiguration` (`FMLPaths.CONFIGDIR`), since AWI's `IPlatformHelper` adds nothing to `aci`'s `ICorePlatformHelper` (ALI's adds only `getSpawnEggItem`; `getLookupProvider` used to live there but is gone on this branch — both mods read the `HolderLookup.Provider` off the `ServerLevel` being scanned). This is the loader difference that matters in practice: Fabric discovers plugins through the `awi` **entrypoint** in `fabric.mod.json`, Forge through the `@AwiEntrypoint` **annotation** — a new plugin class must be declared in both places.
- `network.{Client,Server,NetworkUtils}` — the 4 worldgen messages (`WorldgenDataChunkMessage`, `StartMessage`, `DoneMessage`, `RequestWorldgenDataMessage`) registered through `SimpleChannel.messageBuilder(...).codec(...).consumerNetworkThread(...).add()`, sent via `PacketDistributor.PLAYER.with(player)`/`PacketDistributor.SERVER.noArg()`, using Forge's `CustomPayloadEvent.Context` handler signature (with the `setPacketHandled(true)` boilerplate Fabric doesn't need). The messages themselves are `CustomPacketPayload`s owned by `awi/common` with `StreamCodec<RegistryFriendlyByteBuf, ...> CODEC`; Forge's `messageBuilder` wants a `StreamCodec<FriendlyByteBuf, ...>`, so `NetworkUtils` casts each codec through `(Object)` — the same unchecked-cast trick `ali/forge`'s `NetworkUtils` uses.

## Mixins

`mixin.MixinMinecraftServer` only — hooks `reloadResources` TAIL to re-run `PluginManager.reloadServer()` + `SERVER.readWorldgenInfo(server.overworld())` after a successful `/reload`. Forge has no `END_DATA_PACK_RELOAD` event equivalent, which is why this is a mixin here and an event handler in `awi/fabric`.

## Viewer wiring

Forge needs no hand-written viewer glue: JEI (`@JeiPlugin`) discovers its plugin class from the shadowed common module by annotation, and only JEI was ever enabled for Forge (`forge_jei_enabled`/`forge_emi_enabled`/`forge_rei_enabled` are all `false` now, and with `forge_enabled=false` no `run*Forge*` task is generated at all). REI is the exception that would need glue — `@REIPluginClient` is Forge-specific and cannot live in `awi/common-rei` — so a `compatibility.ReiCompatibilityWrapper` must be (re)added if `forge_rei_enabled` is ever flipped on, mirroring `ali/neoforge`'s. `ali/forge` is in the same state for the same reason.

Mixin-config registration on Forge happens twice over, which is worth knowing when adding a config: `loom { forge { mixinConfig "..." } }` in `build.gradle` writes every listed config into the jar's `MANIFEST.MF` `MixinConfigs` attribute (and into the dev-run launch args), while `META-INF/mods.toml`'s `[[mixins]]` blocks list only `awi.mixins.json`/`awi.forge.mixins.json`. `build.gradle` adds the EMI-only `awi.emi.mixins.json` only when EMI is enabled for Forge (not on this branch); when it is added, it is active in production purely via the manifest — a new config added to `build.gradle` needs no `mods.toml` entry. When EMI is absent, `MixinRecipeScreen`'s missing `@Mixin` target only produces a `@Mixin target ... was not found` WARN (Mixin skips it; `"required": true` does not make that fatal), so no `IMixinConfigPlugin` gate is needed.
