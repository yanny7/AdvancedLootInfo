# aci/fabric/CLAUDE.md

Guidance for `aci/fabric` (`com.yanny.aci.fabric`) — ACI's Fabric loader wrapper; its Forge counterpart is `aci/forge` (see `aci/forge/CLAUDE.md`). See `aci/CLAUDE.md` for the library this packages. Unlike `ali/fabric` and `awi/fabric` this module carries **no mod logic at all**: ACI has no `ModInitializer`, no client initializer, no mixins, no access widener and no platform-service implementation (`ICorePlatformHelper` is implemented by each *mod's* own `IPlatformHelper`, not here). Its whole job is to shadow `aci:common` into a jar the loader accepts.

## What is in it

- `src/main/resources/fabric.mod.json` — `"id": "${aci_mod_id}"`, `"environment": "*"`, `depends` on `fabricloader`/`fabric`/`minecraft`/`java`, and a single `fabric-datagen` entrypoint. No `main`/`client` entrypoints; Fabric accepts a mod with none.
- `datagen.DataGeneration` (`DataGeneratorEntrypoint`) + package-private `datagen.LanguageProvider` — generate `assets/aci/lang/en_us.json` from `aci.datagen.LanguageHolder`. Structurally the same pair as `awi/fabric`'s.

## Fabric API dependency

`build.gradle` takes `modApi "net.fabricmc.fabric-api:fabric-api"` and the metadata declares `"fabric": "*"`, purely because the datagen provider classes come from `net.fabricmc.fabric.api.datagen.v1`. It costs nothing in practice — ALI and AWI both already require Fabric API — but it does mean ACI is not a Fabric-API-free library.

## Gotchas

Fabric datagen initialises the `fabric-datagen` entrypoint of **every** loaded mod, not only the one named by `-Dfabric-api.datagen.modid`. A broken or missing `DataGeneration` class here therefore breaks `:ali:fabric:runDatagen` and `:awi:fabric:runDatagen` too, not just ACI's own.

Loom caches remapped mod dependencies under `.gradle/loom-cache/remapped_mods/<mappings>/com/yanny/aci/fabric/<version>/`, keyed on version. Because `aci_version` does not move during development, a dev run keeps loading the first copy ever remapped: edit `aci/common`, rebuild, and `:ali:fabric:runDatagen` still fails with a `ClassNotFoundException` or `NoSuchMethodError` for code that is demonstrably in the built jar. Clear it with `rm -rf .gradle/loom-cache/remapped_mods/*/com/yanny/aci` after any ACI change a dev run must see.
