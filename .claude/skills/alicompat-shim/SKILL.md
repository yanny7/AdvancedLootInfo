---
name: alicompat-shim
description: Write one ALICompat compatibility shim for a target mod — fetch that mod's jar for the current branch's Minecraft version, decompile the loot/trade classes named in the survey, and produce the source set, accessors, services fragments, gradle.properties wiring, tooltip keys and changelog entry, then compile it. Use when the user names a mod to support ("start twilight forest", "add Apotheosis support", "write the shim for irons_spellbooks"), points at a row of the ALICompat tracker (COMPATIBILITY.md / compatibility/<mod id>.md), or asks to cover a specific loot function/condition/GLM/trading entity from another mod.
---

# Writing one ALICompat shim

Takes one target mod from plan to compiling code. The plan comes from the `alicompat-survey` skill
(`COMPATIBILITY.md` and its `compatibility/<mod id>.md` files); this skill is what runs per row of
that queue. Read only the target mod's file — the index is a link list, and the other 90-odd files
are irrelevant to the shim in hand. Read `alicompat/CLAUDE.md`
first — "Adding a target mod", "Writing a shim" and "Translations" are the contract, and this skill
only adds the parts that document does not: how to get the target's bytecode, what the survey gets
wrong, and the traps that cost a compile round-trip.

`alicompat/CLAUDE.md`'s **"Tooltip code style"** is the shape the code takes — method signature, the
single `return`, delegation through `getValueTooltip`, null and `Optional`, key placement, method
references over lambdas, the `Lang` enum, comments. Read that section before writing the first line
of Java; nothing about code shape is repeated here.

## Step 1 — the survey's class list is for a different Minecraft version

The survey scanned the jars **in the modpack**, which is one Minecraft version. The checkout you are
in is another (read `minecraft_version` from the repo-root `gradle.properties`). Loot classes move,
get added and get deleted between versions, so the finding list is a hypothesis, not an inventory.

Get the jar for *this* branch's version before writing anything. The tracker gives the CurseForge
project and file id per version; cursemaven serves it directly:

```bash
curl -sL -o /tmp/.../tf.jar "https://cursemaven.com/curse/maven/O-<projectId>/<fileId>/O-<projectId>-<fileId>.jar"
unzip -l tf.jar | grep -E "loot/|predicate|trade"
```

Diff that against the findings. On the Twilight Forest row, four of the ten listed classes did not
exist on `1.20.1` and one class that mattered (`ModItemSwap`) was not listed at all. Record both
directions in the tracker (`— not in the <version> jar`, `— <version> only, not in the survey`)
rather than silently covering a different set than the row claims.

Match findings by **simple class name**, not by the package the survey printed: a mod may rename its root package
between versions (Applied Cooking's `sebastrn.appliedcooking` is `dev.smolinacadena.appliedcooking` on `1.20.1`), and a
grep for the fully-qualified name reports a class that is right there as missing.

Read `targetModId()` out of the downloaded jar's `META-INF/mods.toml` / `fabric.mod.json`, never off the tracker's slug
or the mod's display name. An older branch's CurseForge file can be a different major version of the mod with a
different mod id and different code entirely — `bonsai-trees` on `1.20.1` is Bonsai Trees 3, mod id `bonsaitrees3`,
with no loot code at all — and a wrong id makes `isModLoaded` false, so the whole shim silently never registers.

The jar's own loot tables are the inventory the class list only approximates. Grep them for every non-vanilla type:

```bash
unzip -oq target.jar -d t 'data/<modid>/loot_tables/*'
grep -rhoE '"(condition|function|type)": *"[a-z_]+:[a-z_]*"' t/ | sort -u
```

Only `minecraft:` values means there is nothing to shim on this branch, whatever the survey lists, and each other value
is a hook to cover — including ones the survey missed. Do this whenever the class names do not line up: a mod whose
major version differs between branches keeps the mod id but renames everything (`refinedstorage` on `1.20.1` is RS 1.12,
`com.refinedmods.refinedstorage.loottable.StorageBlockLootFunction`, against RS 2.x's
`…refinedstorage.common.storage.storageblock.StorageBlockLootItemFunction`), so matching by name finds nothing and the
row looks empty when four functions need rendering.

Which loaders exist for this mod on this branch also comes from the tracker's version list — a
`1.20.1` Forge-only mod gets a source set in `alicompat/forge` and nothing in `alicompat/fabric`.

## Step 2 — decompile, do not guess semantics

A tooltip that lies is worse than a missing one, and field names alone do not tell you what a GLM
does. Vineflower ships in the Forge Gradle cache:

```bash
unzip -oq target.jar -d tfall 'twilightforest/**'
java -jar ~/.gradle/caches/forge_gradle/maven_downloader/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar \
  tfall/twilightforest/loot/ dec/
```

`javap -p` on the class is enough to decide accessor shape (visibility, record vs class, field
types); decompile when you need the behaviour — what a GLM's `doApply` actually returns, whether a
function's `run` replaces the stack or edits it, what a condition tests.

A production Forge jar is SRG-named for vanilla members, so the decompile is full of `Items.f_42616_`
and `random.m_216332_(2, 3)` and you cannot transcribe an offer without resolving them. Join Mojang's
proguard map with the SRG one once, then grep it — worth doing up front if the shim mirrors any
vanilla-item data:

```bash
python3 .claude/skills/alicompat-shim/scripts/srg_to_mojmap.py 1.20.1 > srg2moj.txt
grep -m1 "^f_42616_ " srg2moj.txt   # -> net.minecraft.world.item.Items.EMERALD
```

`m_216332_(a, b)` is `nextIntBetweenInclusive`, i.e. an inclusive `[a, b]` — read those as ranges when
turning a rolled offer into a `RangeValue`, and mind integer division (`5 * rarity / 2`) when the
target computes a price.

Read the mod's own data files too: `data/<modid>/loot_modifiers/*.json` says which conditions each
GLM is registered with, which is what decides Step 4. Also grep the jar for who populates a public
static map a GLM reads (`grep -rla CONVERSIONS`) — that map is usually filled in mod setup and is
readable from the shim at server-registry time.

## Step 3 — accessor shape follows field visibility

- Public field or public record component → use the target's API directly, no accessor class.
  A record condition is one lambda in `registerServer`.
- Non-public field → `BaseAccessor<Target>` with `@FieldAccessor` fields, built by
  `ReflectionUtils.copyClassData(Accessor.class, instance, Target.class)`.
- A `LootItemConditionalFunction` whose own fields you do **not** need → `ConditionalFunction`, which
  hands you `predicates` through ALI's access widener.

**The `@FieldAccessor` processor validates names against the accessor's type parameter.** Extending
`ConditionalFunction` types the accessor on `LootItemConditionalFunction`, and `SingletonContainer` on
`LootPoolSingletonContainer`, so a `@FieldAccessor` naming a field of the *target* fails the build with
`No field named x in LootItemConditionalFunction`.
When you need both the target's fields and its predicates, extend `BaseAccessor<Target>` and read
`parent.predicates` — the access widener opens it either way.

**`copyClassData` does not inherit fields, though everything around it does.** Reflection *reads*
through the target's superclasses (`getFieldsUpTo`) and the processor *validates* against them too,
but the copy loop walks `myClass.getDeclaredFields()` only. A shared base accessor for a target-side
base class therefore silently leaves its fields at `null`/`0`: when five targets extend one base,
each accessor redeclares that base's fields itself. Typing the base on a type variable
(`BaseAccessor<T>`) makes it worse — `resolveTarget` gives up and the processor skips validation with
a NOTE instead of an error.

**Only the value-tooltip registry resolves superclasses; every other one is keyed on the exact class.**
`valueTooltips` is a `ClassKeyedMap`, so a subclass reaches its base type's renderer — which is why an
`ItemPredicate` subclass carrying no vanilla fields renders *empty* rather than missing, and looks covered
when it is not. Entries, functions, conditions and ingredients are plain `HashMap`s: a subclass of a
registered type reaches nothing and falls to the missing tooltip, so an `ingredient` finding whose values
ALI could already render is fixed by registering the concrete class against ALI's own
`IngredientTooltipUtils::getIngredientTooltip` — no accessor and no new key.

**Only the target mod's own jar is on the compile classpath, never its libraries.** `modCompileOnly` adds the one
coordinate from `<slug>_<loader>_dep` and nothing it depends on, so an expression javac has to resolve through a
third-party type fails to compile — Ender IO's `EIOItems.BROKEN_SPAWNER` needs Registrate's `ItemEntry`, Artifacts'
`Artifacts.CONFIG.common` needs Cloth `autoconfig`'s `ConfigData`. Reach the same value another way instead of adding
the library: an item through `BuiltInRegistries.ITEM`, a config value through a reflective accessor over the field
that holds it. When neither works, drop that part of the tooltip and label what is left by the field it actually
reads (`Default Probability:`, not `Probability:`) rather than implying the applied value.
The one exception is a library that is itself a slug in `compat_mods`: every target's `modCompileOnly` lands on the
loader module's single compile classpath, so a shim may name that library's types directly (Supplementaries' listings
are Moonlight's `ModItemListing`) without reflection, and at runtime the target mod's own hard dependency guarantees
it is there.

An `item_sub_predicate` finding on a branch before `1.20.5` is an `ItemPredicate` subclass, not a sub-predicate:
there is no `registerItemSubPredicate`, and the hook is `registerValueTooltip` on that class.

**An entry that carries its own count reports `1` unless you seed the range yourself.**
`NodeUtils.getEnchantedCount` starts from `RangeValue(1)` and lets the entry's functions modify it, which is right
only for an entry whose count comes from a `SetItemCountFunction`. A `LootPoolSingletonContainer` holding its own
`min`/`max` (Placebo's `StackLootEntry`) must build `new EnchantedRanges(new RangeValue(min, max))` and run
`utils.applyCountModifier` over the functions itself, then hand that to both `ItemNode` and
`TooltipUtils.getTooltip`. `weight`, `quality`, `conditions` and `functions` are read off `parent` — the access
widener opens all four — and `IEntry` and `IEntryTooltip` sit on the one accessor.

One accessor may implement several hooks. A function that swaps the stack is worth registering three
times: `registerFunctionTooltip` (what it says), `registerItemStackModifier` (so the drop renders as
the swapped item) and `registerItemCollector` (so the recipe-viewer index finds it).

## Step 3b — trade item listings

Before writing anything, work out **which listing classes actually reach ALI**. ALI reads
`VillagerTrades.VILLAGER_TRADES` / `WANDERING_TRADER_TRADES` and whatever a `registerTrades` supplier
hands it — a listing class the target only instantiates inside its own entity's `getOffers()` to pull
one `MerchantOffer` out of never arrives, and registering it is dead code. The survey cannot tell the
difference and lists those too (usually as "likely"): grep the target for who *adds* the listing to a
trade list. On Forge, `VillagerTradingManager.postWandererEvent` writes the `WandererTradesEvent`
result back into `VillagerTrades.WANDERING_TRADER_TRADES`, so a mod's wandering-trader additions do
reach ALI and only a renderer is missing. Lookup is by **exact class**
(`tradeItemListings.get(entry.getClass())`), so every subclass needs its own `registerItemListing`.

An `item_listing` finding is registered by class like any other hook, and the shape depends on what
the target's trade already is:

- The target **is** a `VillagerTrades.ItemListing` (MoreJS's `SimpleTrade` and friends) → register it
  directly, `registerItemListing(SimpleTrade.class, …)`, and let the accessor implement only
  `IItemListing`.
- The target is the mod's **own** listing type (Ribbits) → the accessor implements
  `VillagerTrades.ItemListing` *and* `IItemListing`, and the shim hands the wrapped listing to
  `IServerUtils.getItemListing`. That is the only reason for the wrapper.

Measure the shim against what ALI already does, not against nothing: an unregistered listing falls
back to `entry.getOffer(null, null)` rendered through `TradeUtils.getNode`, and to a missing-listing
tooltip when that throws. A shim earns its place by reading the listing's fields — which gives count
*ranges* and survives a listing that needs a trader — not by re-deriving one rolled offer.

**A listing that keeps its data in a lambda** has no fields to read — a base class holding one
`BiFunction<Entity, RandomSource, MerchantOffer>` (Iron's Spellbooks' `AdditionalWanderingTrades`)
puts every constructor argument in the lambda's capture. Work down this list:

- Real fields on the subclass → ordinary `BaseAccessor`, nothing special. A stored field is not proof the offer
  uses it: read the `MerchantOffer` constructor call, not the record component beside it — Supplementaries'
  `RocketItemListing` keeps an `xp` field and then passes `ModItemListing.defaultXp(true, level)` instead.
  Beware a listing that mutates its own stacks per `getOffer` (`RandomScrollTrade` writes the rolled spell into `forSale`
  and the price into `price`): copy the item, ignore the stored count, and never sample it.
  A field holding a lazy, memoising resolver (Immersive Engineering's `Villages$LazyItemStack` over a
  `Function<Level, ItemStack>`) is never called either — the shim has no level to pass, and the first
  call caches whatever the `null` level yields, so the trade stays wrong for the rest of the session.
  Read the resolver's captures instead.
- Captured arguments → `com.yanny.ali.plugin.common.ReflectionUtils.getCapturedInstances(lambda,
  Class<T>)` pulls them **by type** off the lambda's synthetic fields. Type-keyed, so it survives a
  recompile as long as the capture is unique in its type; two captures of the same type come back in
  capture order and that ordering is the fragile part. A captured `null` yields no entry at all.
- Captures nothing but rolls a loot table → render *from that table* instead of rolling it: ALI's
  access widener opens `LootPool.entries` and `LootItem.item`, so a representative stack and the
  table id (`Lang.Value.LOOT_TABLE`) cost ten lines and stay correct when the table changes.
  `utils.getLootTable` / `getLootPools` / `convertNumber` give the rolls for a cost bound.
- Captures nothing and two instances are indistinguishable → one roll, but pass a real
  `RandomSource.create()`. ALI's own fallback fails on these only because it passes `null` for both
  arguments; most such lambdas never touch the trader, so supplying the random alone revives them.

Build the node with `ItemsToItemsNode`; `TradeUtils` in `ali/common` is the worked example for every
vanilla listing, including the tooltip conventions for a random result (`ENCHANT_RANDOMLY`, and
`Lang.Value.POTION` / `EFFECT` / `DURATION`). A field defaulting to a whole registry
(all enchantments, all potions) must not be listed line by line — render the label alone and list the
entries only once a script or config has narrowed them.

## Step 3c — trading entities with no `ItemListing[]`

`registerTrades` wants an `Int2ObjectMap<ItemListing[]>`, but a custom trader often has none: it fills
a `MerchantOffers` inline in `getOffers()`, behind `random.nextFloat() < 0.25` gates and private
static filler lists. Mirror that list with **shim-owned** listings — one class implementing
`VillagerTrades.ItemListing` *and* `IItemListing`, carrying `RangeValue` counts and an optional
result tooltip — rather than reusing the target's listing objects. You then know every constructor
argument, because you are the one passing it, and the same builders serve the wandering-trader path.

`TradeLevelInfo` carries one chance for a whole level, so **each RNG gate becomes its own level**:
`new TradeLevelInfo(new RangeValue(1), 0.25f)` reads as "selects 1 of these, 25% chance", and a
`RangeValue(3, 4)` over a filler pool reads as "selects 3-4 of these". Ten small levels on a trader
with no real levels is the honest encoding; one level loses every probability. A private static
`List<MerchantOffer>` of fillers is worth reading by plain reflection (survives target updates); a
private static `List<ItemListing>` of lambda-backed entries is not, since the entries are unreadable
anyway — hand-write those.

A trade whose result varies between two items (pay in emeralds *or* the mod's own currency) gets the
common item in the slot and the other under a shim `Branch.ALTERNATIVE` key with its own count —
`ItemsToItemsNode` has one stack per slot, and `requiresAllChildren()` means an empty result silently
drops the whole trade.

## Step 4 — Global Loot Modifiers: check the destination resolves

`GlobalLootModifierUtils.getLootModifier` returns a modifier only when the GLM's conditions contain
one of three shapes: a `LootItemEntityPropertyCondition` on `THIS` with a concrete entity type
(→ `IType.ENTITY`), a `LootItemBlockStatePropertyCondition` (→ `IType.BLOCK`), or a condition the
`ILootTableIdConditionPredicate` recognises (→ `IType.LOOT_TABLE`). Anything else yields
`Optional.empty()` and the Forge plugin logs `Unable to locate destination`.

Conditions like `match_tool`, or the target mod's own conditions, constrain *how* loot is obtained,
not *what* the loot belongs to, so they resolve to nothing. The survey reports these as "Auto-GLM
without a resolvable destination". `Optional.empty()` does not mean the modifier has to be written by
hand — it means nobody registered a destination resolver for that condition. Work down this list:

1. **`registerDestination` first.** When the unresolvable condition is the *target mod's own* and it
   names the destination, register a resolver for it and the auto-GLM path starts working by itself —
   `registry.registerDestination(GiantPickUsedCondition.class, …)` in `TwilightForestCompat`,
   `LootTableIdCondition` in `PortingLibLootCompat`. `Destination` has three shapes,
   `Blocks(Collection<Block>, fullyExplained)`, `Entities(EntityTypePredicate, fullyExplained)` and
   `Table(ResourceLocation, fullyExplained)`; `fullyExplained` is `false` when the condition only
   narrows the destination and its other parts still have to appear in the tooltip. Never register a
   resolver for a **vanilla** condition class from a shim — that answer would speak for every mod.
2. **Then the registration itself**, one line per modifier:
   `GlmAccessorUtils.registerGlobalLootModifier(registry, X.class, XAccessor.class)`.
3. **A destination on the modifier class itself when it has no conditions at all.** `collect` asks
   `utils.getDestination` about the modifier instance as well as about each condition, so a GLM shipped with
   `"conditions": []` still reaches the auto path once `registerDestination(XLootModifier.class, XAccessor.class)`
   answers for it — Apotheosis' four config-driven modifiers match the table id against a `List` in the mod's own
   config that way. The accessor implements `IDestination` beside `IGlobalLootModifierAccessor` and its
   `getOperations()` runs inside `TooltipContext.set(location)`, so the same list also yields the per-table chance.
4. **A hand-built `ILootModifier` only when no destination can carry the answer** — neither a condition nor the
   modifier class names where the loot belongs.

**Never hand-build the unbounded case.** `GlobalLootModifierUtils.getLootModifier` already ends in exactly that
anonymous class — `predicate` true, `IType.UNBOUNDED`, the operations you supplied — behind the user's
`showUnboundedGlobalLootModifiers`. Writing it out again in an accessor copies that branch verbatim and its only
effect is to ignore the setting whose whole job is deciding whether unbound modifiers appear. Item precision is not
a reason: it lives in the operation's `Predicate<ItemStack>`, which `getLootModifier` takes from you either way, and
`AbstractServer` requires an unbounded modifier to match an item before attaching it. Going through
`getLootModifier` also means a datapack that later adds a `loot_table_id` condition upgrades the modifier to a real
`IType.LOOT_TABLE` on its own.

`data/<modid>/loot_modifiers/*.json` in the jar says which conditions each GLM is registered with,
and that is what decides between the four. A **library** mod ships none — its GLMs are registered by the mods that
depend on it, so pull one dependent's jar and read its `loot_modifiers` instead; that is the only place the real
condition sets exist. Read them before concluding anything from the survey's "no resolvable destination" line: when the
condition that fails to resolve is a **vanilla** one ALI declines on purpose — an `entity_properties` whose predicate
carries no entity type, say — no shim can fix it. Registering a resolver for a vanilla class is out of bounds, and a
hand-built `ILootModifier` would have to invent a destination that exists only in the pack's data. Leave the auto path
alone, and say so in the tracker; the modifier belongs to `showUnboundedGlobalLootModifiers`, not to the shim.

When the mod's own code tells you the real destination, build the `ILootModifier<Block>` (or `<Entity>`, or `<ResourceLocation>`) by hand:

- A GLM keyed on a known set of blocks → `predicate` is that set's `containsKey`, and emit one
  operation per entry.
- A GLM that transforms whatever it is given → `predicate` returns `true` and the *operation's* item
  predicate does the filtering. That is safe: `AbstractServer.predicateItem` only attaches a modifier
  to a loot table when some operation predicate matches an item that table can produce.
- A GLM keyed on the loot table id (a path substring, a regex over the id, a stored table name) →
  `IType.LOOT_TABLE` and `predicate(ResourceLocation)`. `getOperations()` is handed no location, but
  it runs inside the `TooltipContext.set(location)` that `AbstractServer` brackets each table with, so
  `TooltipContext.get()` is how an operation whose count or chance differs per table reads the table
  it is being built for. Guard for `null` and emit nothing.
- A GLM that deletes loot → a `RemoveOperation` whose factory returns `null` when the modifier is
  unconditional and, when it is not, an `ItemNode` with `NOT(AllOf(<the GLM conditions>))` appended to
  the source node's conditions — the shape `ali/common-lootjs` uses for a conditional
  `RemoveLootAction`. Return nodes that are not plain `ItemNode`s unchanged.

Wrap a replacement in `ModifiedNode(utils, src, replacement)` whenever the modifier is conditional,
so the original drop stays visible as the alternative. Build the replacement's tooltip with
`TooltipUtils.getTooltip(utils, DEFAULT_QUALITY, chance, count, functions, conditions)` over the
concatenation of the modifier's conditions and the source node's, and take `chance` from
`node.getChance()` rather than `1` so the percentage stays honest.

`new ILootModifier<>() { … }` infers its type argument from the target type, which inside
`Optional<ILootModifier<?>>` is `Object` — so the diamond compiles only for an `IType.UNBOUNDED` modifier
(that is what `GlobalLootModifierUtils` itself writes). A `BLOCK`, `ENTITY` or `LOOT_TABLE` one must name the
argument, `new ILootModifier<ResourceLocation>() { … }`, or every override fails with "does not override or
implement a method from a supertype".

## Step 5 — tooltip keys

Keys live in the shim's own `<Mod>Lang implements ICompatTranslations`, under `alicompat.`, split into
the enums the shim needs (`Conditions` → `alicompat.type.condition.`, `Functions` →
`alicompat.type.function.`, `Value` → `alicompat.property.value.`), each registered with
`CoreLang.register` in a static block. That class must not name a single target-mod type — datagen
constructs it with the target mod absent.

`TooltipBuilder.key()` **overwrites**, it does not nest. A condition rendering one value takes a
single `"Name: %s"` key (`getValueTooltip(...).key(KEY)`), not a value key plus a condition key. A
condition with several values uses `TooltipBuilder.array((b) -> …, KEY)` with a `"Name:"` key and one
`build(Lang.Value.X)` per line. A condition with no value at all is
`TooltipBuilder.keyOnly(KEY)`. Reuse `Lang.Value` / `Lang.Branch` from `ali/common` wherever a key
already exists; only add to the shim's own `Value` enum for something genuinely new.

**A value the tooltip renders as its own object gets `registerValueTooltip`, not an inline helper.**
When a function, condition or listing has to print a nested object — a formula, a modifier, a filter,
a range type — register a renderer for that object's class and let `getValueTooltip` dispatch to it,
even when exactly one caller uses it. That is what `ali/common`'s `Plugin` does: fifty
`registerValueTooltip` calls, among them `ApplyBonusCount.Formula` and `SetAttributesFunction.Modifier`,
each read by a single function. Calling a private helper instead works only for the caller that knows
about it, and the same object rendered from a GLM, a trade or another shim silently falls back to a
bare `toString`. `forge`/`ironsspellbooks` registers `SpellFilter` this way.

Check whether the key already exists before adding one: translations from every shim merge into one
`HashMap`, so two shims may ship the same key with the same value (Ribbits and MoreJS both declare
`alicompat.type.function.random_potion`) and the generated JSON does not change at all. Each shim
still declares its own — that is what keeps a shim self-contained.

Do **not** launch the datagen run to regenerate
`alicompat/<loader>/src/main/generated/assets/alicompat/lang/en_us.json` — it is a Minecraft run and
can hang the session. Add the new keys to that JSON by hand (alphabetically sorted, two-space indent,
**no trailing newline** — the generator writes none), and tell the user to re-run the loader's
`Minecraft Data` configuration to confirm the file comes back byte-identical.

## Step 6 — wiring, and what the user must be told

Per `alicompat/CLAUDE.md`: `compat_mods` gains the slug, a `<slug>_<loader>_dep` line goes next to
the other active targets (the commented-out block below them is for mods with no shim yet), and the
source set carries `services/com.yanny.alicompat.IModCompat` plus, when there are keys,
`services/com.yanny.alicompat.ICompatTranslations` — one fully-qualified class per line. Copy a
`package-info.java` from a sibling shim.

`alicompat/CHANGELOG.md`: while ALICompat is unreleased — the top section is `## []` reading
`Initial release` — a new shim gets **no** entry; that section already covers every shim shipped in
it, and neither the Ribbits nor the Twilight Forest commit touched the file. Once a numbered version
has been published, the normal rule applies: append to `## []` if it exists, else open one above the
newest version; `alicompat_version` moves only when opening a new section. Say in the summary which
of the two applied, since the tracker row carries a `changelog entry` checkbox either way.

Verify with `./gradlew :alicompat:<loader>:build` and check the merged services files in the jar:

```bash
unzip -p alicompat/forge/build/libs/ALICompat-forge-*-[0-9].[0-9].[0-9].jar \
  META-INF/services/com.yanny.alicompat.IModCompat
```

Compiling is not playing. Close the task by telling the user, explicitly: that the shim was never
run in game and which part is least certain (GLM rendering, usually); that datagen still needs
re-running; that `alicompat/CLAUDE.md`'s "Current targets" list wants a line for the new mod, which
only they may edit; that the wiki's supported-mods page drifts; and what the row still leaves open —
an `entityLootTables` config entry is an `ali_config` change, not Java, and belongs to the user's
decision, not this shim.

## Step 7 — the tracker

Tick the per-finding checkboxes in `compatibility/<mod id>.md`, update its `- Done:` line, tick the
mod's `Done` cell in `COMPATIBILITY.md`, annotate each finding the branch's jar does not have, add
the ones it has that the survey missed, and put the version-specific reasoning (why a GLM was
hand-rolled, which loaders exist) in that file's `Notes:` block. The tracker is
hand-maintained — never re-run `worklist.py` over a file with ticked boxes.

Porting to another branch starts from Step 1 again: the same mod on `1.21.1` is a different class
list, and only the shape of the shim ports, not its contents.
