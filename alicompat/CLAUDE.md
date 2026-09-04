# alicompat/CLAUDE.md

Guidance for working on **ALICompat** (`ALICompat`, `com.yanny.alicompat`) — the optional add-on jar carrying ALI compatibility for third-party mods that ship no ALI plugin of their own. See the repo-root `CLAUDE.md` for the monorepo layout and `ali/CLAUDE.md` for the plugin API this module builds on (read that first — this doc assumes it).

ALICompat is a consumer of ALI's public plugin API, nothing more: it registers no nodes, widgets or network packets of its own, and ALI/ACI know nothing about it.

## Module layout

- `alicompat/common` — the compat framework: `IModCompat`, `IGlmModCompat`, `ModCompatManager`, `ICompatTranslations`, `Utils`, `platform/{ICompatPlatform, Services}`, plus the `accessor` package (see Writing a shim). Depends on `ali:common` (and through it `aci:common`), and takes ALI's access widener (`loom.accessWidenerPath = project(":ali:common").loom.accessWidenerPath`) so `ConditionalFunction`/`SingletonContainer` can read the vanilla fields it opens.
- `alicompat/fabric`, `alicompat/forge`, `alicompat/neoforge` — loader entry points, the `ICompatPlatform` implementation, a `datagen` package (language only, see Translations), and one `src/compat/<slug>/` source set per target mod. `forge_enabled=false` on this branch, so `alicompat/forge` is not part of the build (and has not been ported to 1.21.11).

There are no viewer subprojects — the compat shims register into ALI's registries and ALI's own viewer modules render the result.

## The plugin

Each loader module holds one `@AliEntrypoint` `Plugin implements IGlobalLootModifierPlugin` whose `getModId()` is `alicompat` and whose four register methods delegate straight to `ModCompatManager`. It is registered like any other ALI plugin: Fabric through the `ali` entrypoint in `fabric.mod.json`, Forge through annotation scanning.

`getModId()` returning `alicompat` — not a target mod id — is what makes ALI keep the plugin loaded whatever the pack contains; the per-target gating happens one level down.

## `ModCompatManager` and `IModCompat`

`IModCompat` names one target mod (`targetModId()`) and carries the same three optional register hooks as `IPlugin` (`registerCommon`/`registerClient`/`registerServer`, all `default`-empty). `IGlmModCompat` adds `registerGlobalLootModifier` for target mods shipping Global Loot Modifiers.

`ModCompatManager` discovers implementations through `ServiceLoader`, keeps the enabled ones (`Services.getPlatform().isModLoaded(compat.targetModId())`) in a lazily built, immutable list, and fans each register call out over them.

Discovery walks the `ServiceLoader` **iterator** with `hasNext`/`next` wrapped in try/catch, rather than its `stream()`. A compat whose target mod is absent cannot even be resolved — `ServiceLoader` calls `getConstructor` on it, which loads the target-mod types named in its method signatures and throws `ServiceConfigurationError` before `isModLoaded` can be consulted. That error escapes `hasNext()`, so a `stream()` walk aborts on the first such provider and every later compat is lost; the iterator has already moved past the failing entry, so catching and continuing picks the rest up. Every call is wrapped: a compat that throws is logged and skipped, it never takes ALI's registration down with it. That is deliberate — these shims read other mods' internals, so a target mod's update breaking one of them must stay contained to that mod's tooltips.

## Adding a target mod

1. `gradle.properties`: add the slug to `compat_mods`, and add `<slug>_<loader>_dep` (a maven coordinate) for each loader the target mod exists on.
2. Create `alicompat/<loader>/src/compat/<slug>/java/com/yanny/alicompat/compat/<slug>/` with an `IModCompat`/`IGlmModCompat` implementation plus its accessors, and `alicompat/<loader>/src/compat/<slug>/services/com.yanny.alicompat.IModCompat` naming that implementation.
3. If the shim introduces tooltip keys, add an `ICompatTranslations` implementation plus a `services/com.yanny.alicompat.ICompatTranslations` fragment, and re-run datagen (`Minecraft Data` run of that loader module).

The root `build.gradle` (the `isAliCompat` branch) reads `compat_mods`, drops every slug with no `<slug>_<loader>_dep` for the loader being configured, and for the rest: adds `src/compat/<slug>/java` and `src/compat/<slug>/resources` to the main source set, adds the declared dependency as `modCompileOnly`, and merges the per-mod `services/<service name>` fragments into one `META-INF/services/<service name>` file each, since a `ServiceLoader` reads a single file per service (`generateCompatServices`). Dropping the dependency for one loader is therefore how a Fabric-only shim is expressed — the source set may stay in the tree, it is simply not compiled there.

On Fabric the dependency notation goes through the `explosion` plugin: Fabric mods bundle their libraries as jar-in-jar and a dev launch does not unpack them.

Target mods stay **compile-only** and are deliberately kept off the run classpath. Fabric's data generator constructs the `fabric-datagen` entrypoint of every mod it can see and calls its `buildRegistry` before `fabric-api.datagen.modid` filters anything, so a target mod in the dev runtime (Porting Lib ships such an entrypoint) runs its own datagen inside ALICompat's. To play-test a shim, drop the target mod's jar into the run's `mods/` directory by hand.

## Writing a shim

The compat classes are compiled against the target mod (`modCompileOnly`), so its public API is used directly; a `@ClassAccessor` string lookup is for the case the compile classpath cannot express — a target class that is not public at all. Reflection is still needed for a target's non-public fields, and that is what `com.yanny.alicompat.accessor` provides: `BaseAccessor`/`ClassAccessor`/`FieldAccessor`/`ReflectionUtils` (the reflection core), `ConditionalFunction`/`SingletonContainer` (typed bases over the vanilla classes a shim most often wraps), `PluginUtils`/`GlmAccessorUtils` (registration into ALI's registries) and the per-hook marker interfaces (`IEntry`, `IFunctionTooltip`, `IConditionTooltip`, `IIngredientTooltip`, `IItemListing`, `INumberProvider`, `ICountModifier`, `IChanceModifier`, `IItemStackModifier`, `IEntryItemCollector`, `IFunctionItemCollector`, `IGlobalLootModifierAccessor`). The `@FieldAccessor` shape check runs at compile time through the repo-wide `:processor` annotation processor:

- Extend `BaseAccessor<T>` (or `ConditionalFunction` for a `LootItemConditionalFunction`), typed on the target class.
- Fields that must be read reflectively are declared with `@FieldAccessor` and filled by `ReflectionUtils.copyClassData(accessorClass, instance, targetClass)` — the three-argument overload, which takes the target class as an argument instead of reading it off a `@ClassAccessor` annotation. A target class that is not visible from here (a `private` nested one, say) has no such argument to pass: annotate the accessor `@ClassAccessor("<binary name>")`, type it on the nearest visible supertype, and use the two-argument overload. The accessor's annotation processor cannot resolve that name either, so it skips field validation and a renamed field surfaces only at runtime.
- Where a field is already reachable (ALI's access widener opens `LootItemConditionalFunction.predicates`, for instance), construct the accessor directly and skip reflection entirely.
- GLM accessors implement `IGlobalLootModifierAccessor` and are registered through `GlmAccessorUtils.registerGlobalLootModifier(registry, targetClass, accessorClass, predicate)`. Their tooltips are built with ALI's own `GlobalLootModifierUtils.getLootModifier`, which stays in `ali:common` — the two classes are separate on purpose, the ALI one is the loot-side machinery every GLM path uses, this one is only the reflective registration.

A tooltip whose only branch can come out empty must call `showEmpty()` on the builder, otherwise `TooltipBuilder.build` collapses the whole node and the entry silently renders nothing (see `aci/CLAUDE.md`'s tree-model section).

## Translations

A shim owns the tooltip keys it introduces — they live in its own source set (`FarmersDelightLang` for Farmer's Delight), under the `alicompat.` prefix, not in `ali/common`'s `Lang`.

They are published through **`ICompatTranslations`, a service of its own** — deliberately not a method on `IModCompat`. An `IModCompat` implementation names the target mod's classes in its method signatures, so merely constructing it (as `ServiceLoader` must, to hand it over) loads them and throws `NoClassDefFoundError` when the target mod is absent — which is exactly the situation datagen runs in, since target mods are compile-only. An `ICompatTranslations` implementation must therefore stay free of any reference to the target mod; it names its target only by id (`targetModId()`).

`ModCompatManager.collectTranslations(boolean loadedOnly)` is the single reader:

- `registerCommon` calls it with `true` and pushes the keys through `registry.registerTranslationKey`. ALI turns the collected keys into a string→int dictionary sent with the tooltip payload (`aci`'s `CoreCommonRegistry`), and the dictionary freezes at first use — a key registered late is logged and dropped, and its tooltip has no way to reach the client.
- Each loader's `datagen.LanguageProvider` calls it with `false` — datagen must emit the keys whether or not the target mod is in the run — and writes them to `<loader>/src/main/generated/assets/alicompat/lang/en_us.json`. Only the shims built into the jar contribute, so `compat_mods` decides the file's contents.

Other locales stay hand-written next to the shim (`src/compat/<slug>/resources/assets/alicompat/lang/zh_cn.json`).

Register nothing ALI already registers: node ids, widgets and data-node factories for ALI's own node kinds (`ModifiedNode` and friends) come from `ali/common`'s `Plugin.registerClient`.

## Current targets

- `farmersdelight` — `CopySkilletFunction`/`SmokerCookFunction` tooltips (keys in `FarmersDelightLang`, one copy per source set). Only the **Fabric** set is built on this branch: Farmer's Delight has a refabricated build for 1.21.11 but no NeoForge one, so there is no `farmersdelight_neoforge_dep` and `alicompat/neoforge/src/compat/farmersdelight/` sits in the tree uncompiled (its `en_us.json` is empty for that reason, and the NeoForge jar carries the framework only). The NeoForge set adds the four Farmer's Delight GLMs (`AddItemModifier`, `FDAddTableLootModifier`, `PastrySlicingModifier`, `ReplaceItemModifier`). The Fabric set deliberately has **no** GLM adapters, and adding them would register dead code: the refabricated jar carries the same four modifier classes but reparented onto its own abstract `refabricated.LootModifier`, and nothing in the jar constructs them — the port drives those drops from `refabricated.LootModificationEvents`, which edits the `LootTable.Builder` through Fabric API's `LootTableEvents` at load time. ALI therefore already sees them as ordinary pools of the loot table itself. What the Fabric set adds instead is two refabricated-only shims:
  - the `CanItemPerformAbility` condition tooltip, which reads the condition record's public `ability()` and so needs no accessor of its own.
  - `FDItemListingAccessor`: Fabric's Farmer's Delight wraps each villager trade in a private `VillagerEvents$FDItemListing` record gating it on a config flag, so the accessor unwraps the delegate listing and hands it back to `IServerUtils.getItemListing`. The class is package-private, so it cannot be named as a compile type and this one shim keeps the `@ClassAccessor` string form plus a reflective `@FieldAccessor listing` — hence the compile-time `Target class ... is not on the compile classpath` note from `AccessorProcessor`, which resolves canonical names only. NeoForge's Farmer's Delight builds the same trades out of NeoForge's `BasicItemListing`, which `ali/neoforge` already covers.
- `farlanders` (NeoForge only) — The Farlanders ships no ALI plugin and its three traders (`farlanders:farlander`, `farlanders:elder_farlander`, `farlanders:wanderer`) are entities, not villager professions, so nothing scans them on its own. `FarlandersCompat` names their trade sets (`farlanders:<name>/level_<n>`, five levels for the two farlanders, one for the wanderer) through `IServerRegistry.registerTrades`; the keys are built from `Registries.TRADE_SET`, so the shim references no class of the target mod at all. It also ships a `fake_loot` table for `farlanders:mystic_enderman`, whose held-wand drops live in a table the mob only rolls when it holds one of the four wands.
- `portinglib` (Fabric only) — two compats over two Porting Lib modules, **present in the tree but not built on this branch**: Porting Lib stops at 1.20.4, so there is no `portinglib_fabric_dep` and `compat_mods` names only `farmersdelight`, which leaves the source set out of the Fabric jar. Its sources carry 1.21.11 signatures (`Identifier`, not `ResourceLocation`) even though nothing compiles them. `PortingLibCompat` (`porting_lib_tool_actions`) is the `CanToolPerformAction` condition tooltip. `PortingLibLootCompat` (`porting_lib_loot`) is ALI's **entire** Global Loot Modifier path on Fabric — Fabric has no GLM machinery of its own, so ALI reads Porting Lib's: the compat reads `LootModifierManager`'s modifier map, hands every `IGlobalLootModifierPlugin` among ALI's plugins (this jar's own `Plugin` included) a registry to declare its adapters into, and falls back to `GlobalLootModifierUtils.getMissingGlobalLootModifier` for modifier classes nobody wrote an adapter for. While it stays unbuilt, a Fabric client shows loot unmodified by any GLM — which costs nothing for a mod like Farmer's Delight that rewrites the tables directly, and everything for one that ships real Porting Lib modifiers.

So on this branch `alicompat/fabric` carries the Farmer's Delight shim and `alicompat/neoforge` the Farlanders one; the unbuilt `alicompat/forge` is the framework and nothing else.
