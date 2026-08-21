# aci/forge/CLAUDE.md

Guidance for `aci/forge` (`com.yanny.aci.forge`) — ACI's Forge loader wrapper. See `aci/CLAUDE.md` for the library this packages, and `aci/fabric/CLAUDE.md` for the Fabric half. Like that half it carries **no mod logic**: no networking, no mixins, no access widener, no platform-service implementation. Its whole job is to shadow `aci:common` into a jar Forge accepts.

## What is in it

- `AciMod` — `@Mod(Utils.MOD_ID)`. Forge's `javafml` loader requires an entrypoint class, so this exists even though the only thing its constructor does is add the `GatherDataEvent` listener (`DataGeneration::generate`) to the mod event bus. `modLoader="lowcodefml"` would avoid the class entirely; `javafml` plus a near-empty class is the more predictable option.
- `datagen.DataGeneration` + package-private `datagen.LanguageProvider` — generate `assets/aci/lang/en_us.json` from `aci.datagen.LanguageHolder`. `LanguageProvider`'s `super(output, Utils.MOD_ID, locale)` call is why `aci:common` carries a `Utils.MOD_ID` constant at all.
- `src/main/resources/META-INF/mods.toml` — `modId="aci"`, `modLoader="javafml"`, `displayTest="IGNORE_ALL_VERSION"`, `[[dependencies.aci]]` for `forge` and `minecraft` only. No `[[mixins]]` blocks.
- `gradle.properties` — a single line, `loom.platform=forge`.

## Gotchas

`gradle.properties` with `loom.platform=forge` is **required** and easy to miss, because the Fabric module has no counterpart (Fabric is loom's default platform). Without it the build fails in escalating ways depending on what you reach for: `RunConfigSettings.data() is only usable on Forge` from the `runs.data` block, then `Could not find method forge()` for the `forge "net.minecraftforge:forge:..."` dependency, then `Loom is not running on Forge` if you try to force it with an empty `loom.forge {}` block. Neither `architectury { forge() }` nor `platformSetupLoomIde()` sets the platform.

`runData` writes `src/main/generated/assets/aci/lang/en_us.json` correctly and then hangs instead of exiting — collect the file and kill the task.
