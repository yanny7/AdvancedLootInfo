# awi/common-emi/CLAUDE.md

Guidance for `awi/common-emi` (`com.yanny.awi.emi`) — AWI's EMI integration. **The shared recipe-viewer integration pattern is documented once in `ali/common-emi/CLAUDE.md` — read that first.** This module is structurally identical to `ali/common-emi` (same package shape, same `aci.IWidget`/`CoreListWidget` foundation, same `Compatibility`/`WidgetWrapper`/`ScrollWidget` adapter convention); this file only notes the domain substitution.

Enabled independently per branch via `gradle.properties` (`emi_enabled` + `<platform>_emi_enabled`).

## Package contents

`EmiCompatibility` (entry point), `EmiBaseLoot`, `EmiBiomeLoot` (extend `EmiRecipe` — AWI has a single Biome recipe family, where ALI has four: Block/Entity/Gameplay/Trade), `EmiWidgetWrapper`, `EmiScrollWidget`, slot widgets, `IMouseEvents`, `mixin.MixinRecipeScreen`.

## Domain substitution vs `ali/common-emi`

AWI's output type is `Block` (a biome's surface/feature blocks, per `awi/CLAUDE.md`'s data-scan section), using `RangeValue` for chance/count display instead of ALI's `ItemStack`/item-count outputs. The ingredient-construction branch looks up a block-or-fluid-state instead of an item. Everything else — widget tree building, tooltip building, recipe-interface plumbing — is the same shape as `ali/common-emi`; a line-for-line diff of `EmiScrollWidget` between the two mods shows zero logic differences beyond import paths.
