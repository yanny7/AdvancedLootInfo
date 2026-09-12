# Worked examples, by hook

Every shim already in this repo, indexed by what it registers. Open the one whose shape matches the
target before deriving anything from a decompile — the tooltip conventions, the accessor shape and
the registration form are all settled there.

Paths are `alicompat/<loader>/src/compat/<slug>/java/com/yanny/alicompat/compat/<slug>/`. Which
loaders a slug exists under differs per branch, and a slug that is dormant here (absent from
`compat_mods`) still has readable source — read whichever copy this checkout has.

Code shape itself — method signature, the single `return`, delegation through `getValueTooltip`,
null and `Optional`, key placement, `Lang` enums — is `alicompat/CLAUDE.md`'s **"Tooltip code style"**.
This file only says *where to look*.

## Functions

| Shape | Example |
|---|---|
| Non-public fields | `ironsspellbooks` → `RandomizeSpellFunctionAccessor` — `BaseAccessor<Target>` with `@FieldAccessor`, registered `PluginUtils.registerFunctionTooltip(registry, Target.class, Accessor.class)` |
| No own fields, only `predicates` | `farmersdelight` → `CopyMealFunctionAccessor` — extends `ConditionalFunction`, registered by **constructor reference** (`…, CopyStorageDataFunctionAccessor::new`), never by class: it needs no reflection |
| Replaces the stack | `twilightforest` → `ModItemSwapAccessor`, registered twice on the same accessor — `registerFunctionTooltip` and `registerItemStackModifier` |
| Modifies the count | `morered` → `MoreRedCompat` — `registerFunctionTooltip` + `registerCountModifier` on `WireCountLootFunction`, both as method references, no accessor |

## Conditions

`twilightforest` → `TwilightForestCompat.registerServer()` holds four shapes side by side:

| Shape | Example |
|---|---|
| Non-public fields | `GiantPickUsedCondition`, `ModExistsCondition` — `PluginUtils.registerConditionTooltip(registry, X.class, XAccessor.class)` |
| One value, no accessor | `IsMinionCondition` — named static method plus a reference to it; body is `TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, !cond.inverse())), …IS_MINION)` |
| No value at all | `UncraftingTableEnabledCondition` — `TooltipBuilder.array(TooltipBuilder::showEmpty, …KEY)`. **Not** `keyOnly` — that is a label inside a tree, never a registration's whole result |
| Modifies the chance | `artifacts` → `ConfigValueChanceAccessor`, registered as both `registerConditionTooltip` and `registerChanceModifier` |

## Global Loot Modifiers

| Shape | Example |
|---|---|
| Registration | `GlmAccessorUtils.registerGlobalLootModifier(registry, Modifier.class, ModifierAccessor.class)` — `twilightforest`, `farmersdelight` (five in a row) |
| Destination from a mod constant | `twilightforest` → `getGiantPickUsedDestination` returns `new Destination.Blocks(Set.copyOf(GiantToolGroupingModifier.CONVERSIONS.keySet()), false)`; no accessor, the method reads a static map |
| Destination from an instance field | `portinglib` → `LootTableIdConditionAccessor` implements `IConditionTooltip` **and** `IDestination`, registered twice through `PluginUtils` |

An "auto-GLM without a resolvable destination" is usually a missing `registerDestination` for the
mod's own condition, not a reason to hand-roll `ILootModifier`. See `alicompat/CLAUDE.md`.

## Entries, ingredients, values

| Shape | Example |
|---|---|
| Loot entry | `placebo` → `StackLootEntryAccessor`, registered through **both** `PluginUtils.registerEntry` and `registerEntryTooltip`; one accessor serves two target classes (`StackLootEntry`, `EnchantedLootEntry`) |
| Ingredient needing no special rendering | `sophisticatedstorage` → `registry.registerIngredientTooltip(X.class, IngredientTooltipUtils::getIngredientTooltip)` — delegate to ALI's own util rather than writing one |
| Value tooltip | `ironsspellbooks` → `SpellFilterAccessor` implements `IValueTooltip`; its `array` carries **no key** — the caller names it |
| Number provider | no shim registers one. Take the shape from `ali/common`'s `Plugin` (`registry.registerNumberProvider(...)`) |

## Villager trades

| Shape | Example |
|---|---|
| Target is an `ItemListing`, accessor reads its fields | `morejs` → `PluginUtils.registerItemListing(registry, SimpleTrade.class, SimpleTradeAccessor.class)`, five of them |
| Accessor *is* the listing | `ribbits` → `PluginUtils.registerSelfItemListing(registry, ItemsForAmethystsAccessor.class)` — the accessor implements `VillagerTrades.ItemListing` and `IItemListing` |
| Trader with a static `ItemListing[]` | `farlanders` → `registerTrades(id, () -> FarlanderTrades.FARLANDER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(2)))` |
| Trader configured by the mod | `goblintraders` → `getLevelInfo` reads `getMinValue()`/`getMaxValue()`/`includeChance()` into `TradeLevelInfo(RangeValue(min, max), chance)` |
| Trader with no `ItemListing[]` at all | `ironsspellbooks` → `WizardTrades` mirrors the target's `getOffers()` by hand with shim-owned listings; each RNG gate becomes its own level (`new TradeLevelInfo(new RangeValue(1), 0.25f)`) |
| Listing whose data is captured in a lambda | `ironsspellbooks` → `SimpleTradeAccessor` and the notes in `WanderingTrades`; uses `com.yanny.ali.plugin.common.ReflectionUtils.getCapturedInstances`, which matches **by type** |

## Whole shims worth reading end to end

- `farmersdelight` — two functions, five GLMs and one listing, in both loaders. The smallest shim
  that shows more than one hook kind.
- `twilightforest` — conditions, a function, an item-stack modifier, GLMs, a destination and entity
  variants in one file.
- `ironsspellbooks` — the trade-heavy end: custom traders, lambda captures, a value tooltip, a GLM.

## Language keys

`ribbits` → `RibbitsLang` is the smallest complete one (enum, prefix in the constructor,
`CoreLang.register` in a static block, plus hand-written entity names). `ironsspellbooks` →
`IronsSpellbooksLang` is the same thing with several enums.
