# ali/common-lootjs/CLAUDE.md

Guidance for `ali/common-lootjs` (`com.yanny.ali.lootjs`) — ALI's optional LootJS compatibility module. See `ali/CLAUDE.md` for the mod's overall plugin/data-scan architecture this module plugs into.

## Build inclusion

Only included when `lootjs_enabled == "true"` in `gradle.properties` (`settings.gradle`: `if (settings.lootjs_enabled == "true") include("ali:common-lootjs")`). Built via Architectury's `common(enabled_platforms)` so it's mixin/refmap-compiled against whichever loaders are enabled; the root `build.gradle` adds it to a loader's `commonProjects` only when `<loader>_lootjs_enabled` is `true` as well.

On this branch the module is built for Fabric and Forge (`fabric_lootjs_enabled`/`forge_lootjs_enabled=true`):
- Fabric: `fabric.mod.json` lists `com.yanny.ali.lootjs.LootJsPlugin` under the `ali` entrypoint and `ali.lootjs.mixins.json` under `mixins`.
- Forge: `mods.toml` declares `ali.lootjs.mixins.json`, `ali/forge/build.gradle` adds it as a `mixinConfig` for dev runs, and `LootJsPlugin` is found through its `@AliEntrypoint` annotation.

`lootjs` is an optional dependency with no version bound on either loader (`(0,)` in `mods.toml`, `*` under `suggests` in `fabric.mod.json`). The module compiles against the Fabric LootJS jar; `ali/fabric` and `ali/forge` pin their own LootJS files (`modCompileOnly`). All three pins name the same LootJS release.

## What it does

Translates LootJS-authored loot modifications into ALI's own `ILootModifier`/`IDataNode` tree so they show up in ALI's tooltips like any other loot source, via:
- 36 accessor mixins (`ali.lootjs.mixins.json`): LootJS 2.x keeps its modifications, actions and conditions in private fields, so `MixinLootModificationsAPI` exposes the registered actions, `MixinAbstractLootModification` and the `MixinLootModificationBy*` mixins expose each modification's scope, and one mixin per LootJS action/condition exposes its data. `MixinWeightedRandomList` is the one that targets a vanilla class.
- `LootJsPlugin.registerModifiers` builds one modifier per `AbstractLootModification`, by its scope: `BlockLootModifier`, `EntityLootModifier`, `TableLootModifier` or `TypeLootModifier`. They extend `LootModifier`, which turns each LootJS action into an ALI `IOperation` (see `ali/CLAUDE.md`'s `api`/`plugin/glm` sections), so a LootJS modification is applied the same way a GLM modifier is.
- Actions that produce no loot — player actions, experience drops, explosions, lightning strikes — and the dynamic `modifyLoot` become pseudo loot functions in `modifier` (`CustomPlayerFunction`, `DropExperienceFunction`, `ExplodeFunction`, `LightningStrikeFunction`, `ModifiedItemFunction`) so they render as modifiers on the affected item. `Utils.collectHandler` turns a handler into a condition or such a function, for a modification and for a `pool(...)` group alike; LootJS runs handlers in order, so a condition or action applies to the loot added after it.
- Tooltip renderers for LootJS's own condition/action/filter types: `LootJsConditionTooltipUtils`, `LootJsFunctionTooltipUtils`, `LootJsGenericTooltipUtils` — same one-builder-method-per-type convention as `aci`'s dispatch tiers (see `aci/CLAUDE.md`).
- Nodes and widgets for content LootJS adds or replaces: `ItemStackNode`/`ItemStackWidget`, `ItemTagNode`/`ItemTagWidget`, `AddLootNode`/`AddLootWidget`, `GroupLootNode`/`GroupedLootWidget` and `WeightedAddLootNode`/`WeightedAddLootWidget`.

## Global loot modifier order on Forge

LootJS's Forge `LootModifierManagerMixin` rebuilds Forge's map of global loot modifiers through a `HashMap` after every reload, so that scripts can remove modifiers. With LootJS installed the modifiers therefore run — and ALI lists them on a page — in hash order of their ids instead of Forge's layered order from `global_loot_modifiers.json`. ALI shows the order the game applies; it is not something to correct here.

## Testing

The module is covered end to end by the gametest run, which loads the real LootJS with a KubeJS script — see `ali/CLAUDE.md`'s gametest section.
