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
| Modifies the count | `morered` → `MoreRedCompat` — `registerFunctionTooltip` + `registerCountModifier` on `WireCountLootFunction`, both as method references, no accessor; `gtceu` → `RandomWeightLootFunctionAccessor` implements `ICountModifier` (`min(uniformInt(min, max), constant(maxStack))`) |

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
| Page resolver from a mod constant | `twilightforest` → `testGiantPickUsed` returns `GlobalLootModifierUtils.testBlocks(page, GiantToolGroupingModifier.CONVERSIONS::containsKey, false)`; no accessor, the method reads a static map |
| Page resolver from an instance field | `moonlight` → `OptionalPropertyConditionAccessor` implements `IConditionTooltip` **and** `IPageResolverAccessor`, registered twice through `PluginUtils` |
| Page resolver on the modifier class | `apotheosis` → `GemLootModifierAccessor` answers `testTable` from the mod's own config list, beside its `IGlobalLootModifierAccessor` half |
| Table id read from a mod carrier | `portinglib` → `registerLootContextPreparer` writes `page.tableId()` into Porting Lib's `LootContextExtensions`; its `LootTableIdCondition` then decides itself and needs no resolver |
| Replaced by a weighted loot-table roll | `hybridaquatic` (forge) → `HAGlobalLootModifierAccessor` — `ModifiedNode` over a one-roll `LootPoolNode` of `ReferenceNode`s, chance split by weight |

A GLM the plugin produced nothing for usually wants a page resolver for the mod's own condition — or
nothing at all, since conditions ALI can run decide themselves. See `alicompat/CLAUDE.md`.

## Entries, ingredients, values

| Shape | Example |
|---|---|
| Loot entry | `placebo` → `StackLootEntryAccessor`, registered through **both** `PluginUtils.registerEntry` and `registerEntryTooltip`; one accessor serves two target classes (`StackLootEntry`, `EnchantedLootEntry`) |
| Composite entry (own weight and children) | `spellengine` → `AffiliationGroupEntryAccessor` implements `IEntryWeight` (`NodeUtils.getTotalWeight` over its children) and `IEntryChildren` beside `IEntry`/`IEntryTooltip`, registered through `PluginUtils.registerEntryWeight` / `registerEntryChildren`; a `UniformContainerBase` subclass needs neither, it inherits the singleton rule |
| Entry holding a whole pool | `spellengine` → `InlinePoolEntryAccessor`: `ReferenceNode` over a `LootTableNode` over `NodeUtils.getLootPoolNode(…, entryChance.value(), entryChance.conditions(), …)`, so the pool renders as a random pick with its rolls |
| Custom ingredient | `sophisticatedstorage` → `registry.registerValueTooltip(BaseTierWoodenStorageIngredient.class, …)` — a value tooltip, never `registerIngredientTooltip`: NeoForge and Fabric hand ALI the unwrapped `ICustomIngredient`/`CustomIngredient`, not an `Ingredient` subclass |
| Value tooltip | `ironsspellbooks` → `SpellFilterAccessor` implements `IValueTooltip`; its `array` carries **no key** — the caller names it |
| Number provider | no shim registers one. Implement `INumberProvider` and register through `PluginUtils.registerContextIntProvider`/`registerContextFloatProvider`; the converter shapes are in `ali/common`'s `Plugin` and ACI's `CommonNumberProviders` |

## Villager trades

| Shape | Example |
|---|---|
| Target is an `ItemListing`, accessor reads its fields | `morejs` → `PluginUtils.registerItemListing(registry, SimpleTrade.class, SimpleTradeAccessor.class)`, five of them |
| Package-private subclasses of one listing | `grimoireofgaia` → `ItemsToItemsAccessor` over the base, the subclasses registered via `Class.forName` in `registerItemsToItems` and listed in `scan_ignore.json` |
| Accessor *is* the listing | `ribbits` → `PluginUtils.registerSelfItemListing(registry, ItemsForAmethystsAccessor.class)` — the accessor implements `VillagerTrades.ItemListing` and `IItemListing` |
| Trader with a static `ItemListing[]` | `farlanders` → `registerTrades(id, () -> FarlanderTrades.FARLANDER_TRADES, (level) -> new TradeLevelInfo(NumberExpr.constant(2)))` |
| Same, a different pick count per level | `grimoireofgaia` → `(level) -> new TradeLevelInfo(NumberExpr.constant(level == 1 ? 10 : 5))`, the counts taken from each entity's `updateTrades` |
| Trader configured by the mod | `goblintraders` → `getLevelInfo` reads `getMinValue()`/`getMaxValue()`/`includeChance()` into `TradeLevelInfo(NumberExpr.uniformInt(min, max), chance)` — the entity rolls `min + nextInt(max - min + 1)` |
| Trader with no `ItemListing[]` at all | `ironsspellbooks` → `WizardTrades` mirrors the target's `getOffers()` by hand with shim-owned listings; each RNG gate becomes its own level (`new TradeLevelInfo(NumberExpr.constant(1), 0.25f)`) |
| Listing whose data is captured in a lambda | `ironsspellbooks` → `SimpleTradeAccessor` and the notes in `WanderingTrades`; uses `com.yanny.ali.plugin.common.ReflectionUtils.getCapturedInstances`, which matches **by type** |

## Numbers (Step 2b)

| Shape | Example |
|---|---|
| Uniform roll | `charm` → every `GenericTradeOffers` accessor: `base + nextInt(extra + 1)` is `uniformInt(base, base + extra)` |
| Sum and product of rolls | `ironsspellbooks` → `WizardTrades.elixirBuy`: `add(constant(6), mul(constant(m), uniformInt(3, 6)))` |
| Alternatives with weights | `charm` → `EnchantedShearsForEmeraldsAccessor` (level 1/2/3 at 45/45/10 %, price `add(weighted(…), uniformInt(0, extra))`); `supplementaries` → `fireworkStars` (a do-while with a `0.42` continue chance) |
| Grouped over a registry or list | `ironsspellbooks` → `SpellScrollTrade` (`TreeMap` value → weight over every spell and level); `villagertradingplus` → `SellEnchantedBookTradeOfferAccessor` (`min(weighted(…), constant(64))` over every tradeable enchantment) |
| Clamp to a limit | `villagertradingplus` → `SellEnchantedToolTradeOfferAccessor`: `min(add(constant(count), uniformInt(5, 19)), constant(64))` |
| Depends on an enchantment level | `apotheosis` → `WardenLootModifierAccessor`: `add(constant(1), binomial(constant(1), 0.1 + 0.1 × TooltipUtils.level(MOB_LOOTING)))` |
| Mod's own weighting function | `cognition` → `AddSingleItemAccessor.getCount` mirrors `weightedRandInt`, falling back to `uniformInt` exactly where the target does |
| Part of it cannot be known | `immersiveengineering` → `RevolverPieceForEmeraldsAccessor`: `add(mul(constant(5), range(1, 5)), uniformInt(0, 4))`, the tier coming from luck-dependent perks |

## Whole shims worth reading end to end

- `farmersdelight` — two functions, five GLMs and one listing, in both loaders. The smallest shim
  that shows more than one hook kind.
- `twilightforest` — conditions, a function, an item-stack modifier, GLMs, a page resolver and entity
  variants in one file.
- `ironsspellbooks` — the trade-heavy end: custom traders, lambda captures, a value tooltip, a GLM.

## Language keys

`ribbits` → `RibbitsLang` is the smallest complete one (enum, prefix in the constructor,
`CoreLang.register` in a static block, plus hand-written entity names). `ironsspellbooks` →
`IronsSpellbooksLang` is the same thing with several enums.
