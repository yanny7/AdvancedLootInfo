# ali/common-lootjs/CLAUDE.md

Guidance for `ali/common-lootjs` (`com.yanny.ali.lootjs`) — ALI's optional LootJS compatibility module. See `ali/CLAUDE.md` for the mod's overall plugin/data-scan architecture this module plugs into.

## Build inclusion

Only included when `lootjs_enabled == "true"` in `gradle.properties` (`settings.gradle`: `if (settings.lootjs_enabled == "true") include("ali:common-lootjs")`). Built via Architectury's `common(enabled_platforms)` so it's mixin/refmap-compiled against whichever loaders are enabled; the root `build.gradle` adds it to a loader's `commonProjects` only when `<loader>_lootjs_enabled` is `true` as well.

On this branch the module is built for NeoForge only (`neoforge_lootjs_enabled=true`; `fabric_lootjs_enabled`/`forge_lootjs_enabled=false`): `ali/neoforge` declares `ali.lootjs.mixins.json` in `neoforge.mods.toml` and compiles against LootJS (`modCompileOnly` in `ali/neoforge/build.gradle`), while `ali/fabric`'s `fabric.mod.json` only lists `lootjs` under `suggests` and has no `LootJsPlugin` entrypoint. `LootJsPlugin` is found through its `@AliEntrypoint` annotation.

The LootJS file this module and `ali/neoforge` compile against and the `lootjs` `versionRange` in `neoforge.mods.toml` move together: LootJS changes its API within a Minecraft version, so the range's lower bound is the release the pinned file belongs to, and NeoForge refuses an older LootJS instead of the scan failing with `NoSuchMethodError`.

## What it does

Translates LootJS-authored loot modifications into ALI's own `ILootModifier`/`IDataNode` tree so they show up in ALI's tooltips like any other loot source, via:
- Two accessor mixins: `MixinLootModificationsAPI` (the registered `LootModifier` list) and `MixinCustomParamPredicate`. Everything else is read through LootJS's public records and accessors, or through `Utils.getCapturedInstances` for values LootJS keeps only in a lambda's captured fields.
- `LootJsPlugin.registerModifiers` builds one modifier per LootJS `LootModifier`, by its run predicate: `BlockLootModifier`, `EntityLootModifier`, or `TableLootModifier` (table- and type-filtered). They extend `AbstractLootModifier`, which turns each LootJS action into an ALI `IOperation` (see `ali/CLAUDE.md`'s `api`/`plugin/glm` sections), so a LootJS modification is applied the same way a GLM modifier is.
- Actions that only carry data for the drop itself become pseudo loot functions in `modifier` (`CustomPlayerFunction`, `ModifiedItemFunction`, `PreserveComponentsFunction`) so they render as modifiers on the affected item.
- Tooltip renderers for LootJS's own condition/action/filter types: `LootJsConditionTooltipUtils`, `LootJsFunctionTooltipUtils`, `LootJsGenericTooltipUtils` — same one-builder-method-per-type convention as `aci`'s dispatch tiers (see `aci/CLAUDE.md`).
- Nodes and widgets for items LootJS adds or replaces: `ItemStackNode`/`ItemStackWidget` and `ItemTagNode`/`ItemTagWidget`.
