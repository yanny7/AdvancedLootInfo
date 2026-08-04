# ali/common-emi/CLAUDE.md

Guidance for `ali/common-emi` (`com.yanny.ali.emi`) — ALI's EMI integration. **This file is the canonical description of the recipe-viewer integration pattern shared by all six `<mod>/common-<viewer>` modules** (`ali/common-emi`, `ali/common-jei`, `ali/common-rei`, `awi/common-emi`, `awi/common-jei`, `awi/common-rei`): same package shape, same widget-adapter convention, same entry-point role, differing only in the host viewer's API and the domain type being displayed. `ali/common-jei/CLAUDE.md`, `ali/common-rei/CLAUDE.md`, and the `awi/common-<viewer>/CLAUDE.md` files reference this doc for the shared pattern and only describe what's specific to them — don't restate the pattern there.

Enabled independently per branch via `gradle.properties` (`emi_enabled` + `<platform>_emi_enabled`); see the repo-root `CLAUDE.md`. **On this `1.21.5` branch `emi_enabled=false`**, so `ali/common-emi` and `awi/common-emi` are excluded from the build (EMI has no 1.21.5 release yet — `emi_version` is still a `+1.21.1` build) and no `run*Emi*` task exists. This doc remains the canonical pattern reference regardless; only the EMI-specific parts below are currently untested against 1.21.5.

## Shared foundation (lives in `aci`, not duplicated per viewer)

Every viewer integration, in both mods, renders through `com.yanny.aci.api.IWidget` (`getRect()`, `getDirection()`, `render(GuiGraphics,...)`, `getTooltipComponents(...)`) and `CoreListWidget` (lays out a tree of `IWidget`s and draws the connecting branch lines). This is the one genuinely shared rendering abstraction — see `aci/CLAUDE.md`. Nothing about tree layout or tooltip rendering is reimplemented per viewer; only a thin adapter layer differs (below).

## Package layout (same shape in all six modules)

`<mod>.<viewer>.compatibility.<Viewer>Compatibility` (entry point) + `<mod>.<viewer>.compatibility.<viewer>.*` (viewer-specific adapters). EMI additionally has a `mixin` package.

- **EMI** (this module): `EmiCompatibility` (entry point), `Emi{Base,Block,Entity,Gameplay,Trade}Loot` (extend `EmiRecipe`, one per ALI loot-category family — see `ali/CLAUDE.md`'s `compatibility/common` section for the underlying `IType` DTOs), `EmiWidgetWrapper extends Widget`, `EmiScrollWidget`, `Emi{Block,Loot}SlotWidget`, `IMouseEvents` (shared mixin hook interface), `mixin.MixinRecipeScreen`.
- **JEI** (`ali/common-jei`): `Jei{Base,Block,Entity,Gameplay,Trade}Loot` implement `IRecipeCategory` directly (category and recipe rendering aren't split into separate classes), `JeiWidgetWrapper implements IRecipeWidget`, `JeiScrollWidget`, `Jei{Block,Loot}SlotWidget`, `RecipeHolder` (thin recipe-holder wrapper JEI's API requires, which EMI doesn't need).
- **REI** (`ali/common-rei`): split into **Category** classes (`ReiBaseCategory`, `Rei{Block,Entity,Gameplay,Trade}Category`, implementing `DisplayCategory`) and **Display** classes (`ReiBaseDisplay`, `Rei{Block,Entity,Gameplay,Trade}Display`, implementing `Display`), plus `ReiWidgetWrapper extends WidgetWithBounds`, `ReiScrollWidget`, `RecipeHolder`. No separate `SlotWidget` — REI's `Display` holds ingredient/output `EntryIngredient`s directly. See `ali/common-rei/CLAUDE.md` for why this split exists.

## The `<Viewer>Compatibility` entry point

Each viewer's plugin lifecycle differs, but the role is the same: translate ALI's parsed loot data (`compatibility/common` DTOs — `IType`, `BlockLootType`, `EntityLootType`, `GameplayLootType`, `TradeLootType`) into the host viewer's recipe/category/display registration calls.

- **EMI** (`EmiCompatibility implements EmiPlugin`, `@EmiEntrypoint`): single `register(EmiRegistry)` callback does category *and* recipe registration in one pass — `registry.addCategory(...)` then `registry.addRecipe(new EmiXxxLoot(...))`, plus `addWorkstation` for catalysts.
- **JEI**: three-phase (`registerCategories` → `registerRecipeCatalysts` → `registerRecipes`) — see `ali/common-jei/CLAUDE.md`.
- **REI**: two-phase with a predicate/filler indirection — see `ali/common-rei/CLAUDE.md`.

## Widget wrapper adapters

`EmiWidgetWrapper`/`JeiWidgetWrapper`/`ReiWidgetWrapper` are thin (~30-65 line) adapters holding an `aci.api.IWidget` and forwarding `render`/tooltip calls into the viewer's native widget interface (`Widget`, `IRecipeWidget`, `WidgetWithBounds` respectively), translating `RelativeRect` ↔ the viewer's own `Bounds`/`Rect`/`Rectangle` type. No rendering logic is reimplemented per viewer — only the interop shim differs. `ScrollWidget`/`SlotWidget` classes repeat the same pattern: a viewer-native container widget delegating to the shared `IWidget` tree.

## ali vs awi

Structurally identical — same package shape, same adapter classes, same entry-point role — differing only in package name (`com.yanny.ali.*` vs `com.yanny.awi.*`) and domain type: ALI's modules have four category families (Block/Entity/Gameplay/Trade, outputs typed `ItemStack`/`Either<ItemStack, TagKey<...>>`); AWI's modules have a single Biome family (outputs typed `Block`, using `RangeValue`/`Block` instead of item counts). Diffing `EmiScrollWidget` line-for-line between `ali` and `awi` shows zero logic differences beyond import paths; diffing `EmiBaseLoot` shows the only real differences are the output type and the ingredient-construction branch (item lookup vs block-or-fluid-state lookup) — everything else (widget tree building, tooltip building, recipe-interface plumbing) is the same shape. See `awi/common-emi/CLAUDE.md`.
