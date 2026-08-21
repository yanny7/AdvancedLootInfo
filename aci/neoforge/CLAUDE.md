# aci/neoforge/CLAUDE.md

Guidance for `aci/neoforge` (`com.yanny.aci.neoforge`) — ACI's NeoForge loader wrapper. See `aci/CLAUDE.md` for the library this packages, and `aci/fabric/CLAUDE.md` / `aci/forge/CLAUDE.md` for the other two halves. Like them it carries **no mod logic**: no networking, no mixins, no access transformer, no platform-service implementation. Its whole job is to shadow `aci:common` into a jar NeoForge accepts.

## What is in it

- `AciMod` — `@Mod(Utils.MOD_ID)`, constructor taking the mod `IEventBus` (NeoForge injects it, unlike Forge's `FMLJavaModLoadingContext.get().getModEventBus()`). The only thing it does is add the `GatherDataEvent` listener (`DataGeneration::generate`).
- `datagen.DataGeneration` + package-private `datagen.LanguageProvider` — generate `assets/aci/lang/en_us.json` from `aci.datagen.LanguageHolder`. Same shape as `aci/forge`'s pair; only the `LanguageProvider` superclass differs (`net.neoforged.neoforge.common.data.LanguageProvider`).
- `src/main/resources/META-INF/neoforge.mods.toml` — `modId="aci"`, `modLoader="javafml"`, `displayTest="IGNORE_ALL_VERSION"`, `[[dependencies.aci]]` for `neoforge` and `minecraft` only. No `[[mixins]]` blocks. Note the filename and the `type="required"` dependency syntax both differ from Forge's `META-INF/mods.toml` / `mandatory=true`.
- `gradle.properties` — a single line, `loom.platform=neoforge`. Required for the same reason `aci/forge` needs `loom.platform=forge` — see `aci/forge/CLAUDE.md`'s gotcha.

## Gotchas

There is no access transformer here, unlike `ali/neoforge` and `awi/neoforge`: `aci:common` has no access widener to translate, so nothing needs mirroring when a widener entry is added to ALI's or AWI's.

`aci/neoforge/src/main/generated/assets/aci/lang/en_us.json` is byte-identical to `aci/forge`'s, and `runData` hangs after writing it (same as Forge) — copying the Forge file is the practical way to refresh it.

The stale-loom-cache trap documented in `aci/fabric/CLAUDE.md` applies to the NeoForge dev runtime too: `modImplementation project(":aci:neoforge")` is cached under `.gradle/loom-cache/remapped_mods/*/com/yanny/aci/neoforge/<version>/` and `aci_version` does not move during development.
