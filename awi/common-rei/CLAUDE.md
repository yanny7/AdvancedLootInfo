# awi/common-rei/CLAUDE.md

Guidance for `awi/common-rei` (`com.yanny.awi.rei`) — AWI's REI integration. **The shared recipe-viewer integration pattern, and the reason for REI's Category/Display split and filler/predicate registration, are documented once in `ali/common-emi/CLAUDE.md` and `ali/common-rei/CLAUDE.md` — read those first.** This module is structurally identical to `ali/common-rei`; this file only notes the domain substitution.

Enabled independently per branch via `gradle.properties` (`rei_enabled` + `<platform>_rei_enabled`). On this branch it is the only viewer enabled on both live loaders (Fabric and NeoForge).

## Package contents

`ReiCompatibility` (entry point), `ReiBaseCategory`/`ReiBiomeCategory` (implement `DisplayCategory` — one category per dimension with one display per biome, where ALI has four families: Block/Entity/Gameplay/Trade), `ReiBaseDisplay`/`ReiBiomeDisplay` (implement `Display`), `ReiBlockGenerationCategory`/`ReiBlockGenerationDisplay` (the single `awi:block_generation` category, one display per block family with every member as an output, rendered by `BlockGenerationWidget` — see `awi/CLAUDE.md`'s `compatibility` section; the category implements `DisplayCategory` directly and reuses `ReiBaseCategory`'s width, height, stack and block-model helpers, its filler is registered by class rather than by predicate), `ReiWidgetWrapper`, `ReiScrollWidget`, `RecipeHolder`.

## Domain substitution vs `ali/common-rei`

Same two-phase lifecycle (`registerCategories` building a `Holder(identifier, category, filler)`, then `registerDisplays` calling `registerFiller`/`registry.add`). The only real difference is the output type: `Block` instead of ALI's `ItemStack`/tag outputs, with the slot count taken from a `NumberExpr` through ACI's `NumberFormatter.slot`.
