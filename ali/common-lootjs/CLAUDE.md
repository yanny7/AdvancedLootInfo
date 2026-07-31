# ali/common-lootjs/CLAUDE.md

Guidance for `ali/common-lootjs` (`com.yanny.ali.lootjs`) — ALI's optional LootJS compatibility module. See `ali/CLAUDE.md` for the mod's overall plugin/data-scan architecture this module plugs into.

## Build inclusion

Only included when `lootjs_enabled == "true"` in `gradle.properties` (`settings.gradle`: `if (settings.lootjs_enabled == "true") include("ali:common-lootjs")`). Built via Architectury's `common(enabled_platforms)` so it's mixin/refmap-compiled against whichever loaders are enabled — but on this branch it is **only actually wired up on Fabric**: `ali/fabric/src/main/resources/fabric.mod.json` references `com.yanny.ali.lootjs.LootJsPlugin` as an entrypoint plus `ali.lootjs.mixins.json`; the Forge `mods.toml`/resources reference neither. Don't assume Forge parity for LootJS features without checking the Forge resources first.

## What it does

Translates LootJS-authored loot modifications into ALI's own `ILootModifier`/`IDataNode` tree so they show up in ALI's tooltips like any other loot source, via:
- ~30 mixins into LootJS's condition/action classes (`MixinLootModificationsAPI`, `MixinAbstractLootModification`, and per-condition/per-action mixins) to intercept LootJS's `AbstractLootModification` API.
- Modifier classes per loot scope: `BlockLootModifier`, `EntityLootModifier`, `TableLootModifier`, `TypeLootModifier` — these implement ALI's `ILootModifier` (see `ali/CLAUDE.md`'s `api`/`plugin/glm` sections) so a LootJS modification is applied the same way a GLM modifier or built-in loot function would be.
- Tooltip renderers for LootJS's own condition/action types: `LootJsConditionTooltipUtils`, `LootJsFunctionTooltipUtils` — same one-builder-method-per-type convention as `aci`'s dispatch tiers (see `aci/CLAUDE.md`).
- Client widgets for LootJS-added content, e.g. `AddLootWidget`.
