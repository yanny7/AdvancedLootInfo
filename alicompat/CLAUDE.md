# alicompat/CLAUDE.md

Guidance for working on **ALICompat** (`ALICompat`, `com.yanny.alicompat`) — the optional add-on jar carrying ALI compatibility for third-party mods that ship no ALI plugin of their own. See the repo-root `CLAUDE.md` for the monorepo layout and `ali/CLAUDE.md` for the plugin API this module builds on (read that first — this doc assumes it).

ALICompat is a consumer of ALI's public plugin API, nothing more: it registers no nodes, widgets or network packets of its own, and ALI/ACI know nothing about it.

## Module layout

- `alicompat/common` — the compat framework: `IModCompat`, `IGlmModCompat`, `ModCompatManager`, `ICompatTranslations`, `Utils`, `platform/{ICompatPlatform, Services}`, plus the `accessor` package (see Writing a shim). Depends on `ali:common` (and through it `aci:common`), and takes ALI's access widener (`loom.accessWidenerPath = project(":ali:common").loom.accessWidenerPath`) so `ConditionalFunction`/`SingletonContainer` can read the vanilla fields it opens.
- `alicompat/fabric`, `alicompat/forge`, `alicompat/neoforge` — loader entry points, the `ICompatPlatform` implementation, a `datagen` package (language only, see Translations), and one `src/compat/<slug>/` source set per target mod.

There are no viewer subprojects — the compat shims register into ALI's registries and ALI's own viewer modules render the result.

## The plugin

Each loader module holds one `@AliEntrypoint` `Plugin implements IGlobalLootModifierPlugin` whose `getModId()` is `alicompat` and whose four register methods delegate straight to `ModCompatManager`. It is registered like any other ALI plugin: Fabric through the `ali` entrypoint in `fabric.mod.json`, Forge through annotation scanning.

`getModId()` returning `alicompat` — not a target mod id — is what makes ALI keep the plugin loaded whatever the pack contains; the per-target gating happens one level down.

On Fabric this jar carries ALI's **entire** Global Loot Modifier path: Fabric has no GLM machinery of its own, so the compat over Porting Lib's `porting_lib_loot` reads `LootModifierManager`'s modifier map, hands every `IGlobalLootModifierPlugin` among ALI's plugins a registry to declare its adapters into, and falls back to `GlobalLootModifierUtils.getMissingGlobalLootModifier` for modifier classes nobody wrote an adapter for. Without ALICompat installed, a Fabric client shows loot unmodified by any GLM.

## `ModCompatManager` and `IModCompat`

`IModCompat` names one target mod (`targetModId()`) and carries the same three optional register hooks as `IPlugin` (`registerCommon`/`registerClient`/`registerServer`, all `default`-empty). `IGlmModCompat` adds `registerGlobalLootModifier` for target mods shipping Global Loot Modifiers.

`ModCompatManager` discovers implementations through `ServiceLoader`, keeps the enabled ones (`Services.getPlatform().isModLoaded(compat.targetModId())`) in a lazily built, immutable list, and fans each register call out over them.

Discovery walks the `ServiceLoader` **iterator** with `hasNext`/`next` wrapped in try/catch, rather than its `stream()`. A compat whose target mod is absent cannot even be resolved — `ServiceLoader` calls `getConstructor` on it, which loads the target-mod types named in its method signatures and throws `ServiceConfigurationError` before `isModLoaded` can be consulted. That error escapes `hasNext()`, so a `stream()` walk aborts on the first such provider and every later compat is lost; the iterator has already moved past the failing entry, so catching and continuing picks the rest up. Every call is wrapped: a compat that throws is logged and skipped, it never takes ALI's registration down with it. That is deliberate — these shims read other mods' internals, so a target mod's update breaking one of them must stay contained to that mod's tooltips.

## Adding a target mod

1. `supported_mods.json` in the repo root: add an entry with the slug (`key`), the display `name`, the runtime `mod_ids` the shim gates on, and the CurseForge slug plus project id for every loader the mod ships on. It is the Minecraft-version-agnostic registry of everything ALICompat supports, described by `supported_mods.schema.json`, and both `check_versions.py` and `modpack.py` read it as their source of truth.
2. `python3 check_versions.py --scaffold <slug>`: resolves that one entry against CurseForge for this branch's Minecraft version, writes `<slug>_<loader>_dep` and the `compat_mods` entry into the generated block of `gradle.properties`, and scaffolds a compiling skeleton (`package-info.java`, an `IModCompat` implementation returning the mod id, the `services/com.yanny.alicompat.IModCompat` fragment) for each loader that has a file but no source set. The skeleton's class name follows a source set the shim already has on another loader, so the two halves of one shim keep the same name. A plain `--update` does the same for every entry at once.
3. Fill the skeleton in `alicompat/<loader>/src/compat/<slug>/java/com/yanny/alicompat/compat/<slug>/` with the real `IModCompat`/`IGlmModCompat` registrations plus its accessors.
4. If the shim introduces tooltip keys, add an `ICompatTranslations` implementation plus a `services/com.yanny.alicompat.ICompatTranslations` fragment, and re-run datagen (`Minecraft Data` run of that loader module).

Everything between the `# --- generated from supported_mods.json ... ---` and `# --- end of generated block ---` markers in `gradle.properties` is written by `check_versions.py --update`: the registry decides which mods exist, CurseForge decides which of them have a file for this Minecraft version, and the block is the resolution of the two. Editing it by hand only survives until the next run.

`--init` rebuilds that block from nothing: it treats every registry entry as enabled instead of reading `compat_mods`, and inserts the block after the `alicompat_*` properties if the file has none. It is for a branch being set up, not for one that was just merged — there it would switch every dormant shim on at once. It lists what it activated for that reason.

The root `build.gradle` (the `isAliCompat` branch) reads `compat_mods` and, per loader: adds `src/compat/<slug>/java` and `src/compat/<slug>/resources` to the main source set, adds the declared dependency as `modCompileOnly`, and merges the per-mod `services/<service name>` fragments into one `META-INF/services/<service name>` file each, since a `ServiceLoader` reads a single file per service (`generateCompatServices`). A slug with no source set or no dependency on a given loader is simply absent there — that is how a Fabric-only shim is expressed.

A shim is switched on by `compat_mods` and its `<slug>_<loader>_dep`, never by the presence of its files — so a slug that is not yet ported to a branch stays there as **source with no `compat_mods` entry**, compiled by nothing and shipped in no jar. That is what dormant means here, and it is the state most shims are in on the branches ahead of the one they were written on. `check_versions.py` recognises that state — a registry entry whose slug is missing from `compat_mods` while a source set for it exists is reported as dormant and left entirely alone, neither pinned nor scaffolded, so a run of `--update` after a merge never switches a shim on that nobody has ported yet. It still asks CurseForge about them, so the dormant section says which loaders each one has a file on for this Minecraft version: those are the shims worth porting here, the rest cannot be written until the target mod ships for this version. Do not delete a source set to express it: the directory is what the next merge up carries, and deleting it means the port has to be re-merged from a lower branch before it can be finished. Removing files is right only when the target mod genuinely has no file for that loader on that Minecraft version — a Forge-only shim on a branch that dropped Forge, say — and then the dependency line goes with it.

On Fabric the dependency notation goes through the `explosion` plugin: Fabric mods bundle their libraries as jar-in-jar and a dev launch does not unpack them.

Target mods stay **compile-only** and are deliberately kept off the run classpath. Fabric's data generator constructs the `fabric-datagen` entrypoint of every mod it can see and calls its `buildRegistry` before `fabric-api.datagen.modid` filters anything, so a target mod in the dev runtime (Porting Lib ships such an entrypoint) runs its own datagen inside ALICompat's. To play-test a shim, drop the target mod's jar into the run's `mods/` directory by hand.

## Writing a shim

The compat classes are compiled against the target mod (`modCompileOnly`), so its public API is used directly; a `@ClassAccessor` string lookup is for the case the compile classpath cannot express — a target class that is not public at all. Reflection is still needed for a target's non-public fields, and that is what `com.yanny.alicompat.accessor` provides: `BaseAccessor`/`ClassAccessor`/`FieldAccessor`/`ReflectionUtils` (the reflection core), `ConditionalFunction`/`SingletonContainer` (typed bases over the vanilla classes a shim most often wraps), `PluginUtils`/`GlmAccessorUtils` (registration into ALI's registries) and the per-hook marker interfaces (`IEntry`, `IEntryTooltip`, `IFunctionTooltip`, `IConditionTooltip`, `IValueTooltip`, `IIngredientTooltip`, `IItemListing`, `INumberProvider`, `ICountModifier`, `IChanceModifier`, `IItemStackModifier`, `IDestination`, `IGlobalLootModifierAccessor`). The `@FieldAccessor` shape check runs at compile time through the repo-wide `:processor` annotation processor:

- Extend `BaseAccessor<T>` (or `ConditionalFunction` for a `LootItemConditionalFunction`), typed on the target class.
- Fields that must be read reflectively are declared with `@FieldAccessor` and filled by `ReflectionUtils.copyClassData(accessorClass, instance, targetClass)` — the three-argument overload, which takes the target class as an argument instead of reading it off a `@ClassAccessor` annotation. A target class that is not visible from here (a `private` nested one, say) has no such argument to pass: annotate the accessor `@ClassAccessor("<binary name>")`, type it on the nearest visible supertype, and use the two-argument overload. The accessor's annotation processor cannot resolve that name either, so it skips field validation and a renamed field surfaces only at runtime.
- Where a field is already reachable (ALI's access widener opens `LootItemConditionalFunction.predicates`, for instance), the accessor reads it in its constructor and needs no reflection at all — `ConditionalFunction` is exactly that shape, and such an accessor carries no `@FieldAccessor` field. It is still registered through `PluginUtils`, by handing over its constructor instead of its class (`PluginUtils.registerFunctionTooltip(registry, <Target>.class, <Target>Accessor::new)`); passing the class would route it through `copyClassData`, whose field loop has nothing to copy and which would only replace a `new` with a reflective one.
- GLM accessors implement `IGlobalLootModifierAccessor` (`getLootModifier(IServerUtils)`) and are registered through `GlmAccessorUtils.registerGlobalLootModifier(registry, targetClass, accessorClass)`, or the two-argument overload for an accessor carrying `@ClassAccessor`. Their tooltips are built with ALI's own `GlobalLootModifierUtils.getLootModifier`, which stays in `ali:common` — the two classes are separate on purpose, the ALI one is the loot-side machinery every GLM path uses, this one is only the reflective registration. An accessor hands that call the target's conditions and an operation supplier and lets it pick the destination; a target mod's own loot-table-id-style condition is taught to ALI by registering an `IDestinationResolver` for it (`IServerRegistry.registerDestination`, see `ali/CLAUDE.md`'s `plugin/glm`) rather than by building the `ILootModifier` by hand.

A tooltip whose only branch can come out empty must call `showEmpty()` on the builder, otherwise `TooltipBuilder.build` collapses the whole node and the entry silently renders nothing (see `aci/CLAUDE.md`'s tree-model section).

## Tooltip code style

The shape below is `ali/common`'s `plugin/server` (over forty tooltip methods, not one of them shaped differently) and the shims already here. A shim that deviates reads as foreign code even when it works.

A tooltip method is `@NotNull public static TooltipBuilder get<Thing>Tooltip(IServerUtils utils, <Target> fun)` — `fun` for a function, `cond` for a condition, otherwise the thing's own name — and its body is a **single `return`**, with the group's key as the *last* argument of `TooltipBuilder.array`:

```java
@NotNull
public static TooltipBuilder getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
    return TooltipBuilder.array((b) -> {
        b.add(utils.getValueTooltip(utils, fun.source).build(Lang.Value.SOURCE));
        b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.PREDICATES));
    }, Lang.Functions.COPY_NAME);
}
```

No local variable, no `if` around the whole thing — branching goes inside the lambda. A target with one field collapses to one line. The accessor carries this method once, as the instance `getTooltip(IServerUtils)` its marker interface declares; it grows no static bridge for the registration to point at, because the registration does not point at a method — see below.

**Every nested object goes through `utils.getValueTooltip(utils, x)`** — never `String.valueOf`, never a private helper, never `toString`. When no renderer exists for that type, register one with `registerValueTooltip` even if a single method reads it: `ali/common`'s `Plugin` has fifty such registrations, `ApplyBonusCount.Formula` and `SetAttributesFunction.Modifier` among them, each used once. A private helper serves only the caller that knows about it, so the same object rendered from a GLM, a trade or another shim falls through to `MissingTooltipUtils.getMissingValueTooltip` instead.

`getValueTooltip` takes `@Nullable T` and answers `TooltipBuilder.empty()` for null, so a shim never guards a field before passing it. `aci`'s `CommonValueTooltip` already registers `Optional`, `OptionalInt`, `Collection`, `Holder` and every boxed primitive, and `AliServerRegistry` walks arrays itself — so hand the `Optional` over whole rather than calling `get()`, `orElse(null)` or `isPresent()`, hand the collection over rather than looping, and let an empty one render as empty. Unwrapping in the shim only loses the empty case that ACI already renders correctly.

`.build(Lang.Value.X)` labels a single value, `.build(Lang.Branch.X)` a branch with children, and a value that speaks for itself takes no key at all. `predicates` is always the last line and always `Lang.Branch.PREDICATES`; the lines above it follow the order of the fields in the target class, not the alphabet.

`TooltipBuilder.array` has two overloads and which one applies follows from whether the tooltip **names itself**.

A **named** tooltip is one the reader meets under its own heading: a condition, a function, an entry. It always returns `array(logic, KEY)`, and the wrapper is not optional however few values there are — all twenty-six methods of `FunctionTooltipUtils` and all eighteen of `ConditionTooltipUtils` return one, a one-value condition included (`array((b) -> b.add(utils.getValueTooltip(utils, cond.probability).build(Lang.Value.PROBABILITY)), Lang.Conditions.RANDOM_CHANCE)`) and a condition carrying no value at all (`array(TooltipBuilder::showEmpty, Lang.Conditions.KILLED_BY_PLAYER)`). The array is what gives it its own collapsible line under that key; returning the bare value with `.key(KEY)` instead renders it inline and it stops behaving like the entries beside it. Higher Minecraft versions add registrations faster than they add shapes, so a wrapper kept now survives the hook gaining children later.

A **value** tooltip names nothing — it is a fragment, and the *caller* labels it at the point of use with `.build(Lang.Value.X)` or `.build(Lang.Branch.X)`. It therefore never takes a key of its own: `ValueTooltipUtils` has fifteen `array`/`branch` calls and **not one** carries a key, and `RegistriesTooltipUtils` builds none at all. A value with children is the keyless `array(logic)` (`getModifierTooltip`); a value that is one thing is `TooltipBuilder.value(x)`, a delegation to another value tooltip (`getHolderTooltip`), or `TooltipBuilder.empty()`. Putting a key on a value tooltip labels it twice, since the caller is already going to.

`TooltipBuilder.keyOnly` is for a label **inside** a tree — `Lang.Group.ALL` and its siblings in `TooltipUtils`, a delimiter in `GenericTooltipUtils` — never for the whole tooltip of a registered condition or function. `.key()` likewise sits on a standalone value (weight, quality, chance, count) or on `MissingTooltipUtils`' fallback, not on a registration's result; it overwrites rather than nests, so a value key plus a condition key on one builder loses the first.

A registration names things; it never carries a body. **An accessor is registered through `PluginUtils`**, which owns the one `copyClassData` call site for every hook: `PluginUtils.registerFunctionTooltip(registry, <Target>.class, <Target>Accessor.class)`. The form taking the target class explicitly keeps the `@FieldAccessor` processor's compile-time validation; the form taking only the accessor class is for one annotated `@ClassAccessor`, whose target is not visible here and which therefore resolves by name at runtime; the form taking a constructor reference is for an accessor that reads everything it needs in its constructor and touches no reflection. `GlmAccessorUtils.registerGlobalLootModifier` is the same pair of shapes for GLMs, and a class that is itself the listing — shim-owned, or an accessor implementing `VillagerTrades.ItemListing` — registers through the self form rather than a target/accessor pair.

`registry.register*` is called directly only where no wrapper fits: a hook `PluginUtils` does not cover, or a tooltip with no accessor at all, and then the body is a named static method and the registration a method reference to it (`registry.registerFunctionTooltip(<Target>.class, <Mod>Compat::<thing>Tooltip)`). A lambda survives only where a reference cannot express the call — `registerTrades`'s supplier and level function, for instance. `ali/common`'s `Plugin.java` is the measure: 177 method references, three lambdas, and each of those three captures something.

A `Lang` enum constant is `SCREAMING_SNAKE` over a `snake_case` key, with the prefix applied in the constructor and a trailing comma plus semicolon on their own line so adding the next constant touches no existing one. The English text ends in a colon when children follow (`"Match Tool:"`), carries `%s` when it is one value (`"Loot Table Id: %s"`), and ends in neither when it is a whole sentence (`"Killed by player"`). A key that already exists in `ali/common`'s `Lang` is imported, never redeclared.

Shims carry no comments. All of `ali/common`'s `plugin/server` holds one comment across two thousand lines; what needs explaining goes in the summary handed to the user, not in the source.

## Translations

A shim owns the tooltip keys it introduces — they live in its own source set, in a `<Mod>Lang implements ICompatTranslations`, under the `alicompat.` prefix, not in `ali/common`'s `Lang`.

They are published through **`ICompatTranslations`, a service of its own** — deliberately not a method on `IModCompat`. An `IModCompat` implementation names the target mod's classes in its method signatures, so merely constructing it (as `ServiceLoader` must, to hand it over) loads them and throws `NoClassDefFoundError` when the target mod is absent — which is exactly the situation datagen runs in, since target mods are compile-only. An `ICompatTranslations` implementation must therefore stay free of any reference to the target mod; it names its target only by id (`targetModId()`).

`ModCompatManager.collectTranslations(boolean loadedOnly)` is the single reader:

- `registerCommon` calls it with `true` and pushes the keys through `registry.registerTranslationKey`. ALI turns the collected keys into a string→int dictionary sent with the tooltip payload (`aci`'s `CoreCommonRegistry`), and the dictionary freezes at first use — a key registered late is logged and dropped, and its tooltip has no way to reach the client.
- Each loader's `datagen.LanguageProvider` calls it with `false` — datagen must emit the keys whether or not the target mod is in the run — and writes them to `<loader>/src/main/generated/assets/alicompat/lang/en_us.json`. Only the shims built into the jar contribute, so `compat_mods` decides the file's contents.

Other locales stay hand-written next to the shim (`src/compat/<slug>/resources/assets/alicompat/lang/zh_cn.json`).

Register nothing ALI already registers: node ids, widgets and data-node factories for ALI's own node kinds (`ModifiedNode` and friends) come from `ali/common`'s `Plugin.registerClient`.
