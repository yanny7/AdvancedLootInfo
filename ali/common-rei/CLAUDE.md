# ali/common-rei/CLAUDE.md

Guidance for `ali/common-rei` (`com.yanny.ali.rei`) — ALI's REI integration. **The shared recipe-viewer integration pattern (package shape, `aci.IWidget` foundation, widget-wrapper adapter convention) is documented once in `ali/common-emi/CLAUDE.md` — read that first.** This file only covers what's specific to REI, which is the one viewer whose registration shape genuinely diverges from EMI/JEI.

Enabled independently per branch via `gradle.properties` (`rei_enabled` + `<platform>_rei_enabled`). On this branch REI is the only viewer enabled on *both* live loaders (`fabric_rei_enabled`/`neoforge_rei_enabled=true`), which makes it the default choice for reproducing a viewer-side issue.

## Package contents

`ReiCompatibility` (entry point, `implements REIClientPlugin`), a **Category** family (`ReiBaseCategory`, `Rei{Block,Entity,Gameplay,Trade}Category`, implementing `DisplayCategory`) and a separate **Display** family (`ReiBaseDisplay`, `Rei{Block,Entity,Gameplay,Trade}Display`, implementing `Display`), `ReiWidgetWrapper extends WidgetWithBounds`, `ReiScrollWidget`, `RecipeHolder`. There is no separate `SlotWidget` class — a `Display` holds its ingredient/output `EntryIngredient`s directly rather than delegating slot rendering to a dedicated widget.

## Why the Category/Display split exists

Unlike EMI (`EmiRecipe` is one class doing both) and JEI (`IRecipeCategory` implemented directly by the recipe class), REI's API requires two distinct class families: a `DisplayCategory` describing *how a category renders* and a `Display` describing *one recipe instance*. This is a genuine structural divergence, not just naming — don't try to collapse it to match EMI/JEI's shape.

## Entry-point lifecycle

`ReiCompatibility` also registers in two phases, but with an REI-specific indirection the other two viewers don't need:
1. `registerCategories` — builds the `Category` plus a `Holder(identifier, category, filler)` record mapping raw loot data to `Display` factories.
2. `registerDisplays` — calls `registry.registerFiller(predicate, filler)` (keyed on `CategoryIdentifier`, matching raw loot objects to the right filler function) and `registry.add(...)` for each display.

This filler/predicate pattern is REI-specific; EMI/JEI both use direct `addRecipe`/`addRecipes` calls with no predicate matching step. Keep this in mind when adding a new loot category to ALI — the REI side needs a new filler/predicate pair, not just a new recipe class.
