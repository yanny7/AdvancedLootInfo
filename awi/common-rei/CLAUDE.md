# awi/common-rei/CLAUDE.md

Guidance for `awi/common-rei` (`com.yanny.awi.rei`) — AWI's REI integration. **The shared recipe-viewer integration pattern, and the reason for REI's Category/Display split and filler/predicate registration, are documented once in `ali/common-emi/CLAUDE.md` and `ali/common-rei/CLAUDE.md` — read those first.** This module is structurally identical to `ali/common-rei`; this file only notes the domain substitution.

Enabled independently per branch via `gradle.properties` (`rei_enabled` + `<platform>_rei_enabled`).

## Package contents

`ReiCompatibility` (entry point), `ReiBaseCategory`/`ReiBiomeCategory` (implement `DisplayCategory` — AWI has a single Biome family, where ALI has four: Block/Entity/Gameplay/Trade), `ReiBaseDisplay`/`ReiBiomeDisplay` (implement `Display`), `ReiWidgetWrapper`, `ReiScrollWidget`, `RecipeHolder`.

## Domain substitution vs `ali/common-rei`

Same two-phase lifecycle (`registerCategories` building a `Holder(identifier, category, filler)`, then `registerDisplays` calling `registerFiller`/`registry.add`). The only real difference is the output type: `Block` instead of ALI's `ItemStack`/tag outputs, using `RangeValue` for chance/count display.
