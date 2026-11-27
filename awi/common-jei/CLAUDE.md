# awi/common-jei/CLAUDE.md

Guidance for `awi/common-jei` (`com.yanny.awi.jei`) — AWI's JEI integration. **The shared recipe-viewer integration pattern, and JEI's three-phase entry-point lifecycle, are documented once in `ali/common-emi/CLAUDE.md` and `ali/common-jei/CLAUDE.md` — read those first.** This module is structurally identical to `ali/common-jei`; this file only notes the domain substitution.

Enabled independently per branch via `gradle.properties` (`jei_enabled` + `<platform>_jei_enabled`).

## Package contents

`JeiCompatibility` (entry point), `JeiBaseLoot`, `JeiBiomeLoot` (implement `IRecipeCategory` directly — one category per dimension with one recipe per biome, where ALI has four families: Block/Entity/Gameplay/Trade), `JeiBlockGenerationCategory` (the single `awi:block_generation` category, `IRecipeCategory<BlockGeneration.Page>` with one recipe per block family and every member as an output slot, rendered by `BlockGenerationWidget` — see `awi/CLAUDE.md`'s `compatibility` section; it reuses `JeiBaseLoot`'s width and height but not its node-tree layout), `JeiWidgetWrapper`, `JeiScrollWidget`, slot widgets, `RecipeHolder`.

## Domain substitution vs `ali/common-jei`

Same three-phase lifecycle (`registerCategories` → `registerRecipeCatalysts` → `registerRecipes`), same `RecipeHolder` wrapping. The only real difference is the output type: `Block` (a biome's surface/feature blocks) instead of ALI's `ItemStack`/item outputs, with the slot count taken from a `NumberExpr` through ACI's `NumberFormatter.slot`.
