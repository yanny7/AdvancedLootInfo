# awi/common-jei/CLAUDE.md

Guidance for `awi/common-jei` (`com.yanny.awi.jei`) — AWI's JEI integration. **The shared recipe-viewer integration pattern, and JEI's three-phase entry-point lifecycle, are documented once in `ali/common-emi/CLAUDE.md` and `ali/common-jei/CLAUDE.md` — read those first.** This module is structurally identical to `ali/common-jei`; this file only notes the domain substitution.

Enabled independently per branch via `gradle.properties` (`jei_enabled` + `<platform>_jei_enabled`).

## Package contents

`JeiCompatibility` (entry point), `JeiBaseLoot`, `JeiBiomeLoot` (implement `IRecipeCategory` directly — AWI has a single Biome recipe family, where ALI has four: Block/Entity/Gameplay/Trade), `JeiWidgetWrapper`, `JeiScrollWidget`, slot widgets, `RecipeHolder`.

## Domain substitution vs `ali/common-jei`

Same three-phase lifecycle (`registerCategories` → `registerRecipeCatalysts` → `registerRecipes`), same `RecipeHolder` wrapping. The only real difference is the output type: `Block` (a biome's surface/feature blocks) instead of ALI's `ItemStack`/item outputs, using `RangeValue` for chance/count display.
