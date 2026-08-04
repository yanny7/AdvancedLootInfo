# ali/common-lootjs/CLAUDE.md

Guidance for `ali/common-lootjs` (`com.yanny.ali.lootjs`) — ALI's optional LootJS compatibility module. See `ali/CLAUDE.md` for the mod's overall plugin/data-scan architecture this module plugs into.

## Build inclusion

Only included when `lootjs_enabled == "true"` in `gradle.properties` (`settings.gradle`: `if (settings.lootjs_enabled == "true") include("ali:common-lootjs")`). Built via Architectury's `common(enabled_platforms)` so it's mixin/refmap-compiled against whichever loaders are enabled.

⚠️ On this `1.21.5` branch `lootjs_enabled=false`, so this module is **not in the build at all** — and neither loader references it: `ali/fabric`'s `fabric.mod.json` lists no `LootJsPlugin` entrypoint and no `ali.lootjs.mixins.json` (`lootjs` appears only under `suggests`), and `ali/neoforge`'s `neoforge.mods.toml` has no `ali.lootjs.mixins.json` `[[mixins]]` block either. Reviving it means flipping `lootjs_enabled` plus the per-loader `<platform>_lootjs_enabled` flag *and* re-adding those resource references; the code below has not been compiled against 1.21.5.

## What it does

Translates LootJS-authored loot modifications into ALI's own `ILootModifier`/`IDataNode` tree so they show up in ALI's tooltips like any other loot source, via:
- ~30 mixins into LootJS's condition/action classes (`MixinLootModificationsAPI`, `MixinAbstractLootModification`, and per-condition/per-action mixins) to intercept LootJS's `AbstractLootModification` API.
- Modifier classes per loot scope: `BlockLootModifier`, `EntityLootModifier`, `TableLootModifier`, `TypeLootModifier` — these implement ALI's `ILootModifier` (see `ali/CLAUDE.md`'s `api`/`plugin/glm` sections) so a LootJS modification is applied the same way a GLM modifier or built-in loot function would be.
- Tooltip renderers for LootJS's own condition/action types: `LootJsConditionTooltipUtils`, `LootJsFunctionTooltipUtils` — same one-builder-method-per-type convention as `aci`'s dispatch tiers (see `aci/CLAUDE.md`).
- Client widgets for LootJS-added content, e.g. `AddLootWidget`.
