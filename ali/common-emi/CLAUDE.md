# ali/common-emi/CLAUDE.md

Guidance for `ali/common-emi` (`com.yanny.ali.emi`) — ALI's EMI integration. **This file is the canonical description of the recipe-viewer integration pattern shared by all six `<mod>/common-<viewer>` modules** (`ali/common-emi`, `ali/common-jei`, `ali/common-rei`, `awi/common-emi`, `awi/common-jei`, `awi/common-rei`): same package shape, same widget-adapter convention, same entry-point role, differing only in the host viewer's API and the domain type being displayed. `ali/common-jei/CLAUDE.md`, `ali/common-rei/CLAUDE.md`, and the `awi/common-<viewer>/CLAUDE.md` files reference this doc for the shared pattern and only describe what's specific to them — don't restate the pattern there.

Enabled independently per branch via `gradle.properties` (`emi_enabled` + `<platform>_emi_enabled`); see the repo-root `CLAUDE.md`. **On this `1.21.11` branch `emi_enabled=false`**, so `ali/common-emi` and `awi/common-emi` are excluded from the build (EMI has no 1.21.11 release yet — `emi_version` is still a `+1.21.1` build) and no `run*Emi*` task exists. This doc remains the canonical pattern reference regardless; only the EMI-specific parts below are currently untested against 1.21.11.

## Shared foundation (lives in `aci`, not duplicated per viewer)

Every viewer integration, in both mods, renders through `com.yanny.aci.api.IWidget` (`getRect()`, `getDirection()`, `render(GuiGraphics,...)`, `getTooltipLines(...)`) and `CoreListWidget` (lays out a tree of `IWidget`s and draws the connecting branch lines). This is the one genuinely shared rendering abstraction — see `aci/CLAUDE.md`. Nothing about tree layout or tooltip rendering is reimplemented per viewer; only a thin adapter layer differs (below).

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

`EmiWidgetWrapper`/`JeiWidgetWrapper`/`ReiWidgetWrapper` are thin (~30-65 line) adapters holding an `aci.api.IWidget` and forwarding `render`/tooltip calls into the viewer's native widget interface (`Widget`, `IRecipeWidget`, `WidgetWithBounds` respectively), translating `RelativeRect` ↔ the viewer's own `Bounds`/`Rect`/`Rectangle` type. No rendering logic is reimplemented per viewer — only the interop shim differs.

Every tooltip ALI or AWI hands a viewer — slot tooltips, widget tooltips, spawn tooltips — goes in as a single `aci.compatibility.ScrollableTooltip` wrapping all its `TooltipLine`s (text and chart rows, see `aci/CLAUDE.md`), never line by line, and nothing when there are no lines (an empty component still draws a tooltip box). The per-viewer entry points: EMI returns it in its `List<ClientTooltipComponent>` (or adds it in `addSlotTooltip`), JEI calls `ITooltipBuilder.add(TooltipComponent)`, REI uses `Tooltip.from(Tooltip.entry(...))` for widgets and `EntryStack.tooltipProcessor` for slots, since `EntryStack.tooltip` takes only text. See `aci/CLAUDE.md` for the mixin and Forge factory registration it depends on. `ScrollWidget`/`SlotWidget` classes repeat the same pattern: a viewer-native container widget delegating to the shared `IWidget` tree.

ALI's slots come in through `IWidgetUtils.addSlotWidget(List<IDataNode> options, rect)` — `ClientUtils` routes ACI's single-entry `addSlotWidget` into it as a list of one, so a viewer implements only the list form. Every option is an `IItemNode`; the slot cycles through all of them and shows the count and tooltip of the one it currently displays. The slot is tinted when any option has predicates. Each viewer finds the displayed option its own way: JEI by matching `getDisplayedItemStack()` against the options' stacks (`TooltipUtils.getDisplayedOption`, the first option on no match), REI by binding a `tooltipProcessor` to every `EntryStack` and mapping `Slot.getCurrentEntry()` back to its option, EMI by overriding `SlotWidget.getStack()` to return the option the clock currently selects.

## ali vs awi

Structurally identical — same package shape, same adapter classes, same entry-point role — differing only in package name (`com.yanny.ali.*` vs `com.yanny.awi.*`) and domain type: ALI's modules have four category families (Block/Entity/Gameplay/Trade, outputs typed `ItemStack`/`Either<ItemStack, TagKey<...>>`); AWI's modules have a single Biome family (outputs typed `Block`, with a `NumberExpr` instead of item counts). Diffing `EmiScrollWidget` line-for-line between `ali` and `awi` shows zero logic differences beyond import paths; diffing `EmiBaseLoot` shows the only real differences are the output type and the ingredient-construction branch (item lookup vs block-or-fluid-state lookup) — everything else (widget tree building, tooltip building, recipe-interface plumbing) is the same shape. See `awi/common-emi/CLAUDE.md`.
