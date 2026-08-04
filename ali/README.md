# AdvancedLootInfo — Plugin API guide

**AdvancedLootInfo (ALI)** is an EMI/JEI/REI plugin that shows detailed information about loot tables and villager
trades. Everything it knows how to display is registered through a plugin API, so any mod can teach ALI about its own
loot conditions, loot functions, loot entries, number providers, ingredients, villager trades and global loot modifiers
— **without depending on EMI, JEI or REI at all**.

This document is for mod developers who want to implement `com.yanny.ali.api.IPlugin`.

> Version note: this branch targets **Minecraft 1.20.1** (Forge + Fabric). The API shape is the same on all supported
> Minecraft versions, but concrete vanilla classes (`LootItemCondition` subclasses, `ItemStack` handling, …) differ per
> version, so plugin code usually needs a small per-version port.

---

## Table of contents

1. [Why you need a plugin](#1-why-you-need-a-plugin)
2. [Setup and plugin discovery](#2-setup-and-plugin-discovery)
3. [Lifecycle: when your callbacks run](#3-lifecycle-when-your-callbacks-run)
4. [Registration reference](#4-registration-reference)
5. [How lookup works (two dispatch tiers)](#5-how-lookup-works-two-dispatch-tiers)
6. [Working with `TooltipBuilder`](#6-working-with-tooltipbuilder)
7. [Translation keys](#7-translation-keys)
8. [`IServerUtils` — what you get inside a callback](#8-iserverutils--what-you-get-inside-a-callback)
9. [Examples](#9-examples)
10. [Custom data nodes and widgets](#10-custom-data-nodes-and-widgets)
11. [Loot modifiers (and Forge GLM)](#11-loot-modifiers-and-forge-glm)
12. [Compatibility without a compile dependency](#12-compatibility-without-a-compile-dependency)
13. [Debugging and log output](#13-debugging-and-log-output)
14. [Rules of thumb](#14-rules-of-thumb)

---

## 1. Why you need a plugin

ALI walks every loot table on the server, converts each entry / function / condition into a tree of tooltip nodes, and
syncs that tree to the client where the recipe viewer renders it.

Vanilla types are all covered by ALI's own built-in plugin (`com.yanny.ali.plugin.Plugin`). Your custom types are not:

* an **unregistered** type still shows up, but it falls back to `Missing…TooltipUtils`, which re-serializes the object
  through its own `Codec` and dumps the raw JSON under an *"Auto-detected: …"* line. Useful, but ugly;
* a **registered** type gets a proper localized, structured, human-readable tooltip.

A plugin also lets you supply information ALI cannot derive on its own — which items an exotic loot entry can yield,
how your loot function changes the item count, or how your global loot modifier rewrites a table.

---

## 2. Setup and plugin discovery

### 2.1 Compile dependency

Add ALI as a compile-only mod dependency (ALI is not required at runtime for your mod to work — keep it optional):

```gradle
dependencies {
    // Fabric / Forge — pick the jar for your loader and MC version.
    modCompileOnly "curse.maven:advanced-loot-info-<projectId>:<fileId>"
    // or, from the Modrinth maven:
    // modCompileOnly "maven.modrinth:advancedlootinfo:<version>"
}
```

The API you compile against lives in these packages:

| Package | Contents |
|---|---|
| `com.yanny.ali.api` | The plugin contract: `IPlugin`, `@AliEntrypoint`, the three registries, the three utils interfaces, `IDataNode`, `IItemNode`, `ListNode`, `ListWidget`, `ILootModifier`, `IOperation` |
| `com.yanny.aci.api` | Shared primitives: `RangeValue`, `IWidget`, `RelativeRect`, `WidgetDirection` |
| `com.yanny.aci.tooltip` | `TooltipBuilder`, `TooltipNode`, `TooltipNodePalette` |
| `com.yanny.aci.language` | `ITooltipKey`, `IMultiKey`, `Translation`, `CoreLang` |
| `com.yanny.ali.plugin.server` | Reusable helpers (`GenericTooltipUtils`, `TooltipUtils`, `EnchantedRanges`, …) — *not* a stable API, but commonly used |
| `com.yanny.ali.plugin.glm` | Forge Global Loot Modifier integration |
| `com.yanny.ali.plugin.mods` | Reflection helpers for compat without a compile dependency |

### 2.2 Write the plugin class

```java
package com.example.mymod.ali;

import com.yanny.ali.api.*;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint                                  // required on Forge, harmless on Fabric
public class MyModAliPlugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "mymod";                         // must be a loaded mod id
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        // registrations go here
    }
}
```

Requirements:

* **public class with a public no-arg constructor** (Forge instantiates it reflectively);
* `getModId()` must return the id of an **installed** mod. Both loaders check `isModLoaded(plugin.getModId())` and
  silently skip the plugin when it returns `false`. That is the intended way to ship compat for a *third* mod: write a
  plugin whose `getModId()` is that other mod's id, and it activates only when that mod is present;
* all three `register*` methods are `default` — implement only the ones you need.

### 2.3 Declare the entrypoint

**Forge** — nothing else to do. `@AliEntrypoint` is found by scanning `ModFileScanData` for the annotation.

**Fabric** — additionally list the class under the `"ali"` entrypoint key in `fabric.mod.json`:

```json
{
  "entrypoints": {
    "ali": [
      "com.example.mymod.ali.MyModAliPlugin"
    ]
  }
}
```

One mod may register any number of plugins.

### 2.4 Failure behaviour

Plugin loading and every `register*` call are wrapped in `try/catch`. A plugin that throws is logged
(`Failed to load plugin with error: …` / `Failed to register <modid> server part with error: …`) and skipped — it never
crashes the game. Look for those lines first when your registrations do not seem to apply.

---

## 3. Lifecycle: when your callbacks run

`com.yanny.ali.manager.PluginManager` (built on ACI's `CorePluginManager`) drives three registries and calls your
plugin once per side:

| Callback | Registry | When |
|---|---|---|
| `registerCommon(ICommonRegistry)` | `AliCommonRegistry` | Once, during common setup, before everything else |
| `registerClient(IClientRegistry)` | `AliClientRegistry` | Once, during client setup |
| `registerServer(IServerRegistry)` | `AliServerRegistry` | **Every time a `ServerLevel` loads, and again on every datapack/tag reload** |

Consequences worth internalizing:

* `registerServer` is **re-run on reload** (`reloadServer()` clears all registry data first), so it must be
  idempotent — build nothing global, cache nothing static that depends on world state.
* The server registry is created per `ServerLevel`, so inside `registerServer` (and inside every callback you register
  there) `utils.getServerLevel()` and `utils.lookupProvider()` are valid — dynamic registries, tags and datapack data
  are all reachable.
* The server registry is destroyed on world unload; anything you cached from it must be dropped with it.
* `registerCommon`/`registerClient` run before any world exists — do not touch registries that require a level there.

---

## 4. Registration reference

### 4.1 `ICommonRegistry` (common side)

| Method | Purpose |
|---|---|
| `registerTranslationKey(String key)` | Adds a translation key to the shared dictionary so the network layer can send it as a compact index instead of the full string. Optional optimization — unregistered keys still work, they are just sent verbatim. |
| `registerEntityVariants(EntityType<T> type, Function<Level, List<Entity>> factory)` | Supplies the concrete entity variants ALI should show for an entity type (vanilla uses this for sheep colors). |

### 4.2 `IClientRegistry` (client side)

| Method | Purpose |
|---|---|
| `registerDataNode(ResourceLocation id, BiFunction<IClientUtils, FriendlyByteBuf, IDataNode> factory)` | The client-side decoder for a node id you encode on the server. |
| `registerWidget(ResourceLocation id, IWidgetFactory<IDataNode, IWidgetUtils> factory)` | The widget that renders that node id. Factory signature: `(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) -> IWidget`. |

Both are keyed by the **same** `ResourceLocation`. Register a node without its widget and the client will decode data it
cannot draw; register a widget without its node and decoding fails. Always do both.

### 4.3 `IServerRegistry` (server side — the important one)

Tooltips:

| Method | Use it for |
|---|---|
| `registerConditionTooltip(Class<T extends LootItemCondition>, BiFunction<IServerUtils, T, TooltipBuilder>)` | Your loot conditions |
| `registerFunctionTooltip(Class<T extends LootItemFunction>, BiFunction<IServerUtils, T, TooltipBuilder>)` | Your loot functions |
| `registerIngredientTooltip(Class<T extends Ingredient>, BiFunction<IServerUtils, T, TooltipBuilder>)` | Custom `Ingredient` implementations (used by trades) |
| `registerValueTooltip(Class<T>, BiFunction<IServerUtils, T, TooltipBuilder>)` | Any *value* type appearing inside another tooltip — your predicate records, enums, POJOs, … (inherited from `ICoreServerRegistry`) |

Structure — turning a loot entry into a displayable node:

| Method | Use it for |
|---|---|
| `registerEntry(Class<T extends LootPoolEntryContainer>, EntryFactory<T>)` | Building an `IDataNode` for your loot entry type. Factory: `(IServerUtils utils, T entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) -> IDataNode` |
| `registerItemCollector(Class<T extends LootPoolEntryContainer>, BiFunction<IServerUtils, T, List<Item>>)` | Which items your loot entry can produce (drives the recipe-viewer item index and category filtering) |
| `registerItemCollector(Class<T extends LootItemFunction>, TriFunction<IServerUtils, List<Item>, T, List<Item>>)` | A function that *changes* which items come out (e.g. smelting) — receives the items collected so far |
| `registerNumberProvider(Class<T extends NumberProvider>, BiFunction<IServerUtils, T, RangeValue>)` | Converting your number provider into a `RangeValue` (min/max range) |

Value modifiers — how your function/condition changes the displayed numbers:

| Method | Use it for |
|---|---|
| `registerCountModifier(Class<T extends LootItemFunction>, TriConsumer<IServerUtils, T, EnchantedRanges>)` | Mutate the drop count range (per enchantment level where relevant) |
| `registerChanceModifier(Class<T extends LootItemCondition>, TriConsumer<IServerUtils, T, EnchantedRanges>)` | Mutate the drop chance range |
| `registerItemStackModifier(Class<T extends LootItemFunction>, TriFunction<IServerUtils, T, ItemStack, ItemStack>)` | Return the item stack as it will actually drop (enchanted, named, NBT-tagged, …) |

Villager trades:

| Method | Use it for |
|---|---|
| `registerItemListing(Class<T extends VillagerTrades.ItemListing>, TriFunction<IServerUtils, T, TooltipNode, IDataNode>)` | Building the node for your `ItemListing` |
| `registerItemListingCollector(Class<T extends VillagerTrades.ItemListing>, BiFunction<IServerUtils, T, Pair<List<Item>, List<Item>>>)` | The (inputs, outputs) items of your listing |

Global loot modifiers:

| Method | Use it for |
|---|---|
| `registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>>)` | Contributing loot modifiers that ALI grafts onto matching block/entity/table nodes — see [§11](#11-loot-modifiers-and-forge-glm) |

> `ItemListing` has a built-in escape hatch: if no factory is registered, ALI tries `entry.getOffer(null, null)` and
> renders the resulting `MerchantOffer`. That works only for listings that ignore the entity/random params. Register
> explicitly if yours does not.

---

## 5. How lookup works (two dispatch tiers)

**Tier 1 — value tooltips (`registerValueTooltip` / `getValueTooltip`)** are stored in a `ClassKeyedMap`, which walks
superclasses and then interfaces to find the closest registered supertype. Registering one base type therefore covers
all its subtypes. ALI itself relies on this: `Collection`, `Optional`, `Enum`, `Holder`, `Boolean`, `Integer`,
`ResourceLocation`, `RangeValue`, … are registered once by `CommonValueTooltip` and work for every concrete subtype.
Arrays are handled automatically (each element is dispatched individually), and `null` yields
`TooltipBuilder.empty()` — you never need a null check before `getValueTooltip`.

**Tier 2 — everything else** (conditions, functions, ingredients, entries, number providers, modifiers, item listings)
is keyed by **exact class** in a plain `HashMap`. Registering a base class does *not* cover subclasses; register every
concrete class you ship.

Both tiers fall back to `Missing…TooltipUtils` (the `Codec`-based JSON dump) when nothing matches.

---

## 6. Working with `TooltipBuilder`

`com.yanny.aci.tooltip.TooltipBuilder` is the fluent API for the tooltip tree. You build a `TooltipBuilder`,
terminate it with `build(key)`, and get an immutable `TooltipNode`. Nodes are interned per server registry, so
identical subtrees are shared and sent over the network only once.

### 6.1 Factory methods

| Factory | Result |
|---|---|
| `value(Object... v)` | Value-only line, no key: `12`, or `1 2 3` for several values |
| `keyValue(String key, Object... v)` | Literal (untranslated) key + values: `mymod:thing: 12` |
| `keyOnly(String \| IMultiKey key)` | Key alone, and forces it visible even with no children |
| `component(Component c)` | A ready-made `Component` as the value (item names, custom formatting) |
| `error(String msg)` | Red error line |
| `branch(Consumer<TooltipBuilder> logic)` | Container: build children inside the consumer |
| `array(Consumer<TooltipBuilder> logic)` | Same, but marks the node as a list (forces the trailing `:` and disables single-child merging) |
| `array(Consumer<TooltipBuilder> logic, IMultiKey key)` | The usual shape for a condition/function tooltip: named list of properties |
| `empty()` | Explicit nothing — drop this tooltip entirely |
| `asElement(TooltipBuilder b, int size)` | Wrap one element of an iterated sequence; when the sequence has several elements and this one renders multiple keyless lines, it gets a generic `Entry:` header so neighbours stay visually separated |
| `translate(String key)` | Marks a **value** or a **raw key** string as "translate this client-side" |

### 6.2 Instance methods

| Method | Effect |
|---|---|
| `add(TooltipBuilder \| TooltipNode)` | Append a child. **Empty children are silently dropped**, so composing optional sections never leaves a stray header behind |
| `key(String \| IMultiKey)` | Set the translatable key |
| `rawKey(String)` | Set a literal key that is *not* run through the language file (unless wrapped in `translate(...)`) |
| `isAdvancedTooltip()` | Node is only rendered when advanced tooltips are on (F3+H) — use for probability math and other technical detail |
| `showEmpty()` | Render even with no value and no children |
| `hasKey()` | Whether a key was set |
| `build()` / `build(String)` / `build(IMultiKey)` | Produce the `TooltipNode` |

### 6.3 Emptiness

A builder with no values, no component, no children and no `showEmpty()`/`keyOnly(...)` builds to `TooltipNode.empty()`,
and `add(...)` throws such nodes away. This is the mechanism that keeps tooltips clean — write

```java
return TooltipBuilder.array((b) -> {
    b.add(utils.getValueTooltip(utils, cond.block).build(Lang.Value.BLOCK));
    b.add(utils.getValueTooltip(utils, cond.properties).build(Lang.Branch.PROPERTIES));   // vanishes if absent
}, Lang.Conditions.BLOCK_STATE_PROPERTY);
```

instead of guarding every optional field with an `if`.

If a type genuinely carries nothing useful for the user, register it as `TooltipBuilder.empty()` rather than leaving it
unregistered — an unregistered type falls through to the noisy JSON dump.

### 6.4 Singular / plural merging

A key can carry two forms (`Translation(sKey, pKey, sEng, pEng)`). When a node has *only one* simple child (a child with
no key and no children of its own), no own value, and is not an `array(...)`, the builder collapses parent and child into
a single line using the **singular** key; otherwise the **plural** key is used and the child stays indented. So:

```
Properties:              →      Property: waterlogged=false
  -> waterlogged=false
```

If merging were possible but the key has no distinct singular form, ALI logs
`Tooltip <key> could be merged if defined singular form in <context>` at INFO. That message is a hint to add a singular
form to your key, not an error.

### 6.5 Values, format arguments and styling

* Values are passed as translation arguments, so a translatable key may contain `%s` placeholders
  (`"Chance: %s%s"`). A key with placeholders and no values renders the raw `%s`.
* Keys render in gold, values in aqua, errors in red.
* A value string starting with the marker produced by `TooltipBuilder.translate(key)` is translated on the client
  instead of shown literally — that is how vanilla stat/item translation keys get embedded into a value.
* Indentation and the `-> ` branch prefix are produced by the renderer from tree depth; never bake them into strings.

### 6.6 Where you may call it

`build()` interns the node into the **server** registry's tooltip palette. Build tooltips only inside
`registerServer` callbacks (i.e. server side, while a level is loaded) — not in client code, not in static
initializers.

---

## 7. Translation keys

Keys are enum constants implementing `ITooltipKey`. The pattern used throughout ALI:

```java
package com.example.mymod.ali;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class MyLang {
    public enum Conditions implements ITooltipKey {
        MOON_PHASE("moon_phase", "Moon Phase:"),
        LUCK("luck", "Luck: %s"),
        ;

        private final Translation translation;

        Conditions(String key, String english) {
            this.translation = new Translation("mymod.condition." + key, english);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }
}
```

Use the four-argument `Translation(sKey, pKey, sEng, pEng)` constructor when you want the singular/plural merge from
§6.4.

Then, in `registerCommon`, register the keys into the shared dictionary so they travel over the network as indices:

```java
@Override
public void registerCommon(ICommonRegistry registry) {
    CoreLang.register(MyLang.Conditions.class);      // enum -> shared translation map (also feeds lang datagen)

    for (MyLang.Conditions key : MyLang.Conditions.values()) {
        registry.registerTranslationKey(key.singular());
        registry.registerTranslationKey(key.plural());
    }
}
```

Two things to know:

* the dictionary is **frozen** the first time it is used. Register keys in `registerCommon` and nowhere else —
  a later call only logs `Trying to register key … after registry freeze!` and is ignored;
* indices are assigned by sorting *all* registered keys, so client and server must register the same key set. That is
  automatic (`registerCommon` runs on both sides from the same plugin list), but it is why a client/server mod-version
  mismatch surfaces as `Unable to decode indexed key! Version mismatch!`.

You still have to ship the actual `en_us.json` entries for your mod — ALI's language file does not contain your keys.
`CoreLang.TRANSLATION_MAP` holds the English fallbacks you declared, which makes it easy to generate them from datagen.

`String`-based keys work too (`build("mymod.condition.moon_phase")`), they just miss the index compression and the
singular/plural merge.

---

## 8. `IServerUtils` — what you get inside a callback

Every server-side callback receives `IServerUtils`. Use it for recursion — never dispatch on `instanceof` yourself.

Tooltip dispatch:

```java
TooltipBuilder getValueTooltip(IServerUtils utils, @Nullable T value);        // tier 1, null-safe
TooltipBuilder getConditionTooltip(IServerUtils utils, T condition);
TooltipBuilder getFunctionTooltip(IServerUtils utils, T function);
TooltipBuilder getIngredientTooltip(IServerUtils utils, T ingredient);
```

Structure and numbers:

```java
IServerRegistry.EntryFactory<T> getEntryFactory(IServerUtils utils, T entry);
List<Item> collectItems(IServerUtils utils, T entry);                          // loot entry
List<Item> collectItems(IServerUtils utils, List<Item> items, T function);     // loot function
Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T itemListing);  // trade
RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider provider);
void applyCountModifier(IServerUtils utils, T function, EnchantedRanges count);
void applyChanceModifier(IServerUtils utils, T condition, EnchantedRanges chance);
ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack stack);
IDataNode getItemListing(IServerUtils utils, T listing, TooltipNode condition);
```

World and data access:

```java
ServerLevel getServerLevel();
HolderLookup.Provider lookupProvider();
LootTable getLootTable(ResourceLocation location);
List<LootPool> getLootPools(LootTable table);
LootContext getLootContext();                        // may be null
List<Entity> createEntities(EntityType<?> type, Level level);
AliConfig getConfiguration();
TooltipNodePalette getTooltipCache();
```

`RangeValue` (from `com.yanny.aci.api`) is the immutable min/max value type used for counts and chances. It supports
`add`, `multiply`, `multiplyMax`, `addMax`, `clamp`, and carries "has score" / "is unknown" flags that render as
`[+Score]` / `[+???]` — return an unknown range instead of a wrong number when a value cannot be determined statically.

`EnchantedRanges` (from `com.yanny.ali.plugin.server`) is a count/chance range **per enchantment level**: an
unenchanted base value plus optional per-enchantment level maps. Useful methods: `getUnenchantedValue`,
`modifyUnenchantedValue`, `modifyAllEntries`, `computeLevels(enchantment, (level, range) -> …)`.

---

## 9. Examples

### 9.1 A loot condition tooltip

```java
@NotNull
public static TooltipBuilder getMoonPhaseTooltip(IServerUtils utils, MoonPhaseCondition cond) {
    return TooltipBuilder.array((b) -> {
        b.add(utils.getValueTooltip(utils, cond.phase()).build(MyLang.Value.PHASE));
        b.add(utils.getValueTooltip(utils, cond.inverted()).build(MyLang.Value.INVERTED));
    }, MyLang.Conditions.MOON_PHASE);
}
```

```java
registry.registerConditionTooltip(MoonPhaseCondition.class, MyTooltipUtils::getMoonPhaseTooltip);
```

Renders as:

```
Moon Phase:
  -> Phase: FULL_MOON
  -> Inverted: false
```

A condition with no data at all (a marker condition) is written as:

```java
public static TooltipBuilder getBloodMoonTooltip(IServerUtils ignoredUtils, BloodMoonCondition ignoredCond) {
    return TooltipBuilder.array(TooltipBuilder::showEmpty, MyLang.Conditions.BLOOD_MOON);
}
```

### 9.2 A value tooltip for your own type

```java
@NotNull
public static TooltipBuilder getManaCostTooltip(IServerUtils utils, ManaCost cost) {
    return TooltipBuilder.branch((b) -> {
        b.add(utils.getValueTooltip(utils, cost.amount()).build(MyLang.Value.AMOUNT));
        b.add(utils.getValueTooltip(utils, cost.element()).build(MyLang.Value.ELEMENT));   // enum: covered by CommonValueTooltip
    });
}
```

```java
registry.registerValueTooltip(ManaCost.class, MyTooltipUtils::getManaCostTooltip);
```

Because this tier walks supertypes, registering a shared base class or interface once covers every subtype:

```java
registry.registerValueTooltip(AbstractSpellPredicate.class, MyTooltipUtils::getSpellPredicateTooltip);
```

### 9.3 A loot function that changes the count

```java
// Tooltip: what the function is.
public static TooltipBuilder getLuckyCountTooltip(IServerUtils utils, LuckyCountFunction fn) {
    return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, fn.bonus).build(MyLang.Value.BONUS)),
            MyLang.Functions.LUCKY_COUNT);
}

// Count modifier: how it changes the number ALI displays.
public static void applyLuckyCount(IServerUtils utils, LuckyCountFunction fn, EnchantedRanges count) {
    count.modifyAllEntries((range) -> range.addMax(fn.bonus));
}
```

```java
registry.registerFunctionTooltip(LuckyCountFunction.class, MyTooltipUtils::getLuckyCountTooltip);
registry.registerCountModifier(LuckyCountFunction.class, MyTooltipUtils::applyLuckyCount);
```

The two registrations are independent: the tooltip explains the function, the count modifier makes the displayed
`1-3` become `1-5`. Register both.

### 9.4 A chance modifier

```java
public static void applyMoonChance(IServerUtils utils, MoonPhaseCondition cond, EnchantedRanges chance) {
    chance.modifyAllEntries((range) -> range.multiply(0.25f));
}
```

```java
registry.registerChanceModifier(MoonPhaseCondition.class, MyTooltipUtils::applyMoonChance);
```

### 9.5 An item stack modifier

```java
public static ItemStack applySoulbound(IServerUtils utils, SoulboundFunction fn, ItemStack stack) {
    ItemStack copy = stack.copy();
    copy.getOrCreateTag().putBoolean("Soulbound", true);
    return copy;
}
```

```java
registry.registerItemStackModifier(SoulboundFunction.class, MyTooltipUtils::applySoulbound);
```

This is what makes the item shown in the viewer match what actually drops.

### 9.6 A number provider

```java
registry.registerNumberProvider(PlayerLuckValue.class,
        (utils, provider) -> new RangeValue(0, provider.maxLuck()));
```

Return `new RangeValue(false, true)` (unknown) when the value cannot be determined without a real loot context.

### 9.7 A custom loot entry

```java
registry.registerEntry(MyLootEntry.class, (utils, entry, chance, sumWeight, functions, conditions) ->
        new MyEntryNode(utils, entry, chance, functions, conditions));

registry.registerItemCollector(MyLootEntry.class,
        (utils, entry) -> List.of(entry.getResultItem()));
```

`chance` is the accumulated probability from the enclosing pool, `sumWeight` the pool's total weight — use them if you
compute your own probabilities. The item collector is what puts your drop into the viewer's searchable index and lets
ALI's category filters see it.

### 9.8 A custom `Ingredient`

```java
registry.registerIngredientTooltip(MyCustomIngredient.class,
        (utils, ingredient) -> TooltipBuilder.array(
                (b) -> b.add(utils.getValueTooltip(utils, ingredient.getMatchingItems())),
                MyLang.Branch.MATCHING_ITEMS));
```

### 9.9 A villager trade listing

```java
registry.registerItemListing(MyTrades.SpellForEmeralds.class, (utils, listing, condition) ->
        new ItemsToItemsNode(utils,
                Either.left(new ItemStack(Items.EMERALD)), new RangeValue(listing.price()),   // input + count
                Either.left(listing.result()),              new RangeValue(1),                // output + count
                listing.maxUses(), listing.xp(), listing.priceMultiplier(),
                condition));

registry.registerItemListingCollector(MyTrades.SpellForEmeralds.class, (utils, listing) ->
        new Pair<>(List.of(Items.EMERALD), List.of(listing.result().getItem())));
```

`ItemsToItemsNode` has overloads for one or two inputs, and a long form that additionally takes a per-slot
`TooltipNode` condition for each input/output — check the class before writing your own node.

---

## 10. Custom data nodes and widgets

You only need a custom node when no existing node shape fits. Reuse ALI's nodes where you can
(`com.yanny.ali.plugin.common.nodes.*`, `…common.trades.*`) — for example `ModifiedNode` + `ModifiedWidget` for
"this drop was altered by a modifier", or `ItemsToItemsNode` for trades.

A node is a server builder, a network codec and a client decoder in one class:

```java
public class MyEntryNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation("mymod", "my_entry");

    private final TooltipNode tooltip;
    private final ItemStack itemStack;
    private final RangeValue count;
    private final float chance;

    // --- server constructor: build the tooltip tree -------------------------
    public MyEntryNode(IServerUtils utils, MyLootEntry entry, float chance,
                       List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.chance = chance;
        this.itemStack = TooltipUtils.getItemStack(utils, entry.getResultItem(), functions);
        EnchantedRanges counts = new EnchantedRanges(1);
        functions.forEach((f) -> utils.applyCountModifier(utils, f, counts));
        this.count = counts.getUnenchantedValue();
        this.tooltip = EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY,
                NodeUtils.getEnchantedChance(utils, conditions, chance), counts, functions, conditions).build();
    }

    // --- client constructor: read fields back in the SAME order -------------
    public MyEntryNode(IClientUtils utils, FriendlyByteBuf buf) {
        itemStack = buf.readItem();
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        count = new RangeValue(buf);
        chance = buf.readFloat();
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));     // tooltips travel as palette ids
        count.encode(buf);
        buf.writeFloat(chance);
    }

    @NotNull @Override public TooltipNode getTooltip()  { return tooltip; }
    @NotNull @Override public ResourceLocation getId()  { return ID; }
    @Override          public float getChance()         { return chance; }

    // IItemNode — lets the recipe-viewer integration index this drop
    @NotNull @Override public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() { return Either.left(itemStack); }
    @NotNull @Override public List<LootItemCondition> getConditions() { return List.of(); }
    @NotNull @Override public List<LootItemFunction> getFunctions()   { return List.of(); }
    @NotNull @Override public RangeValue getCount()                   { return count; }
}
```

Contract:

* a `public static final ResourceLocation ID` — the shared key between `registerDataNode` and `registerWidget`;
* **encode and decode order must match exactly**;
* never write a `TooltipNode` or a `Component` directly — store the palette id
  (`getTooltipCache().getNodeId(node)` / `getNodeById(id)`). The whole palette is sent once per sync, before any node
  data;
* `getChance()` determines sibling ordering (nodes sort by descending chance);
* implement `IItemNode` if your node represents a concrete item drop — the EMI/JEI/REI integrations use it to index
  items and to build the item slot;
* for a node with children, extend `ListNode` instead: child encoding/decoding/sorting is handled for you, and you
  implement `encodeNode(...)` for your own payload. `ListNode.encode` is deliberately fault-tolerant — a child that
  throws while encoding is logged and skipped instead of corrupting the whole payload.

The widget side implements `IWidget` (or extends `ListWidget` for a tree node):

```java
public class MyEntryWidget implements IWidget {
    private final RelativeRect bounds;

    public MyEntryWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        MyEntryNode node = (MyEntryNode) entry;
        utils.addSlotWidget(node.getModifiedItem(), node, rect);   // real item slot in the host viewer
        bounds = rect;
        bounds.setDimensions(18, 18);
    }

    @NotNull @Override public RelativeRect getRect()        { return bounds; }
    @NotNull @Override public WidgetDirection getDirection() { return WidgetDirection.HORIZONTAL; }
}
```

`RelativeRect` is parent-chained and mutable: set your own size with `setDimensions(...)`, and read absolute positions
with `getX()`/`getY()` at render time. `getDirection()` tells the parent list whether you flow horizontally (item slots)
or vertically (rows). `addSlotWidget` is what makes the drop hoverable/clickable in EMI/JEI/REI — it works the same in
all three, you never touch a viewer API yourself.

Register both in `registerClient`:

```java
@Override
public void registerClient(IClientRegistry registry) {
    registry.registerDataNode(MyEntryNode.ID, MyEntryNode::new);
    registry.registerWidget(MyEntryNode.ID, MyEntryWidget::new);
}
```

---

## 11. Loot modifiers (and Forge GLM)

A loot modifier describes "something outside the loot table changes its result". ALI applies registered modifiers on top
of the matching block / entity / loot-table nodes.

```java
public interface ILootModifier<T> {
    boolean predicate(T value);            // does this modifier apply to this block/entity/table id?
    List<IOperation> getOperations();      // what it does
    IType<T> getType();                    // IType.BLOCK, IType.ENTITY or IType.LOOT_TABLE
}
```

`IOperation` is a sealed interface with three cases, all keyed by a `Predicate<ItemStack>` selecting the affected drops:

| Operation | Meaning |
|---|---|
| `AddOperation(predicate, node)` | Add an extra drop |
| `RemoveOperation(predicate, factory)` | Remove/replace matching drops with a single node |
| `ReplaceOperation(predicate, factory)` | Replace a matching drop with several nodes |

Register them as a supplier, because they are rebuilt per world load / reload:

```java
@Override
public void registerServer(IServerRegistry registry) {
    registry.registerLootModifiers(MyPlugin::collectModifiers);
}

private static List<ILootModifier<?>> collectModifiers(IServerUtils utils) {
    return MyModifierManager.all().stream()
            .map((m) -> (ILootModifier<?>) new MyBlockLootModifier(utils, m))
            .toList();
}
```

**Forge Global Loot Modifiers.** If your mod ships Forge GLMs, implement `IForgePlugin` (Forge-only) — it extends
`IPlugin` with an extra callback that receives ALI's GLM registry and the loot-table-id condition predicate:

```java
@AliEntrypoint
public class Plugin implements IForgePlugin {
    @NotNull @Override public String getModId() { return "mymod"; }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry,
                                           ILootTableIdConditionPredicate predicate) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, MyAddItemModifier.class, predicate);
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(ModifiedNode.ID, ModifiedWidget::new);   // reuse ALI's "modified" node
        registry.registerDataNode(ModifiedNode.ID, ModifiedNode::new);
    }
}
```

`GlobalLootModifierUtils.registerGlobalLootModifier` handles the common shape (read the modifier's conditions, match
them against blocks/entities/table ids, build add/replace operations). Use the lower-level
`IGlobalLootModifierPlugin.IRegistry.registerGlobalLootModifier(Class<T>, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>>)`
when your modifier needs custom handling.

---

## 12. Compatibility without a compile dependency

To support a mod you cannot (or do not want to) compile against, `com.yanny.ali.plugin.mods` offers a reflection-based
route: declare an accessor class shaped like the foreign class, annotate it with `@ClassAccessor("fully.qualified.Name")`,
implement the matching marker interface (`IConditionTooltip`, `IFunctionTooltip`, `IEntry`, `ICountModifier`,
`IChanceModifier`, `IItemStackModifier`, `IIngredientTooltip`, `INumberProvider`, `IItemListing`,
`IEntryItemCollector`, `IFunctionItemCollector`), and register it via `PluginUtils`:

```java
@Override
public void registerServer(IServerRegistry registry) {
    PluginUtils.registerFunctionTooltip(registry, CopyMealFunction.class);     // accessor class, not the real one
    PluginUtils.registerConditionTooltip(registry, SomeForeignCondition.class);
}
```

`PluginUtils` resolves the target class by name, copies field data into your accessor at runtime, and logs a warning
(instead of throwing) if the foreign class is absent or has changed. `PluginUtils` also offers helpers plugin authors
often want directly: `getItems(utils, TagKey)`, `getItems(utils, Either<ItemStack, TagKey>)` and
`getCapturedInstances(object, type)` (pulling captured values out of a lambda's synthetic fields).

This is exactly how ALI's own built-in third-party compat (Farmer's Delight, LootJS, Porting Lib, …) is implemented —
`ali/fabric/src/main/java/com/yanny/ali/fabric/plugin/mods/` and `ali/common-lootjs/` are working references.

---

## 13. Debugging and log output

ALI logs enough to verify a plugin is doing its job:

| Log line | Meaning |
|---|---|
| `Registered ALI plugin [<modid>] <class>` | Your plugin was discovered and accepted |
| `Found N plugin(s)` | Total discovered |
| `Failed to load plugin with error: …` | Discovery/instantiation threw — check the class is public with a no-arg constructor |
| `Failed to register <modid> <common\|client\|server> part with error: …` | Your `register*` threw; nothing from that call was applied |
| `Registered 18/22 condition tooltips` | Coverage per category against the backing vanilla registry — printed after every registration pass |
| `Missing condition tooltips for <class>` | Printed after data collection: this type fell back to the JSON dump. **These lines are your to-do list.** |
| `Tooltip <key> could be merged if defined singular form in <context>` | Add a singular form to that key (§6.4) |
| `Failed to write node in <context>` | A node's `encode` threw; that node was skipped |
| `Unable to decode indexed key! Version mismatch!` | Client and server disagree on the translation dictionary — usually mismatched mod versions |

In game, in-progress work is easiest to check with advanced tooltips on (F3+H), which reveals `isAdvancedTooltip()`
nodes.

---

## 14. Rules of thumb

1. **Recurse through `utils`, never `instanceof`.** `utils.getValueTooltip(utils, x)` handles null, arrays, collections,
   `Optional`, `Holder`, enums and every registered type. Manual type switches break the moment another mod registers
   something.
2. **`registerServer` runs repeatedly.** Keep it idempotent and free of static caches tied to world state.
3. **Register modifiers alongside tooltips.** A tooltip explains a function; a count/chance/item-stack modifier makes the
   *numbers and items* ALI displays correct. They are separate registrations and both matter.
4. **Register item collectors** for anything that yields items, or your drop will not be findable in the viewer and
   ALI's category filters will not see it.
5. **Register a widget for every data node**, with the same `ResourceLocation`.
6. **Prefer `TooltipBuilder.empty()` over silence** for deliberately uninteresting types; leaving them unregistered
   produces a JSON dump instead.
7. **Say "unknown" rather than guess.** `new RangeValue(false, true)` renders `[+???]` — better than a confidently wrong
   number.
8. **Keep encode/decode symmetric**, and route tooltips through palette ids.
9. **Never touch EMI/JEI/REI APIs.** Node + widget is the whole contract; ALI adapts it to all three viewers.
10. **Gate on `getModId()`**, so a compat plugin for another mod only activates when that mod is installed.
