# ali/common-jei/CLAUDE.md

Guidance for `ali/common-jei` (`com.yanny.ali.jei`) — ALI's JEI integration. **The shared recipe-viewer integration pattern (package shape, `aci.IWidget` foundation, widget-wrapper adapter convention) is documented once in `ali/common-emi/CLAUDE.md` — read that first.** This file only covers what's specific to JEI.

Enabled independently per branch via `gradle.properties` (`jei_enabled` + `<platform>_jei_enabled`). On this branch `jei_enabled=true` but only for NeoForge (`neoforge_jei_enabled=true`, `fabric_jei_enabled=false`).

## Package contents

`JeiCompatibility` (entry point, `implements IModPlugin`, `@JeiPlugin`), `Jei{Base,Block,Entity,Gameplay,Trade}Loot` (implement `IRecipeCategory` directly — category and recipe rendering are not split into separate classes, unlike REI), `JeiWidgetWrapper implements IRecipeWidget`, `JeiScrollWidget`, `Jei{Block,Loot}SlotWidget`, `JeiBlockSlotWidget`, `JeiLootSlotWidget`, `RecipeHolder` (a thin recipe-holder wrapper JEI's API requires, which EMI's API doesn't need).

## Entry-point lifecycle

`JeiCompatibility` registers in three phases, unlike EMI's single pass:
1. `registerCategories` — builds the `RecipeType`/category maps for each loot family.
2. `registerRecipeCatalysts` — registers workstation items/blocks.
3. `registerRecipes` — wraps each parsed loot object in `RecipeHolder` and calls `addRecipes`.

Category and recipe registration are decoupled across these phases, but the recipe classes (`JeiBaseLoot` etc.) still directly implement the rendering category interface (`IRecipeCategory`) — JEI doesn't require the Category/Display split REI does (see `ali/common-rei/CLAUDE.md`).
