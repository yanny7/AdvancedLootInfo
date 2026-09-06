---
name: alicompat-shim
description: Write one ALICompat compatibility shim for a target mod — fetch that mod's jar for the current branch's Minecraft version, decompile the loot/trade classes named in the survey, and produce the source set, accessors, services fragments, gradle.properties wiring, tooltip keys and changelog entry, then compile it. Use when the user names a mod to support ("start twilight forest", "add Apotheosis support", "write the shim for irons_spellbooks"), points at a row of an ALICompat work tracker (SB4_WRK.md), or asks to cover a specific loot function/condition/GLM/trading entity from another mod.
---

# Writing one ALICompat shim

Takes one target mod from plan to compiling code. The plan comes from the `alicompat-survey` skill
(`SB4.md` + `SB4_WRK.md`); this skill is what runs per row of that queue. Read `alicompat/CLAUDE.md`
first — "Adding a target mod", "Writing a shim" and "Translations" are the contract, and this skill
only adds the parts that document does not: how to get the target's bytecode, what the survey gets
wrong, and the traps that cost a compile round-trip.

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
`ConditionalFunction` types the accessor on `LootItemConditionalFunction`, so a `@FieldAccessor`
naming a field of the *target* fails the build with `No field named x in LootItemConditionalFunction`.
When you need both the target's fields and its predicates, extend `BaseAccessor<Target>` and read
`parent.predicates` — the access widener opens it either way.

**`copyClassData` does not inherit fields, though everything around it does.** Reflection *reads*
through the target's superclasses (`getFieldsUpTo`) and the processor *validates* against them too,
but the copy loop walks `myClass.getDeclaredFields()` only. A shared base accessor for a target-side
base class therefore silently leaves its fields at `null`/`0`: when five targets extend one base,
each accessor redeclares that base's fields itself. Typing the base on a type variable
(`BaseAccessor<T>`) makes it worse — `resolveTarget` gives up and the processor skips validation with
a NOTE instead of an error.

One accessor may implement several hooks. A function that swaps the stack is worth registering three
times: `registerFunctionTooltip` (what it says), `registerItemStackModifier` (so the drop renders as
the swapped item) and `registerItemCollector` (so the recipe-viewer index finds it).

## Step 3b — trade item listings

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

Build the node with `ItemsToItemsNode`; `TradeUtils` in `ali/common` is the worked example for every
vanilla listing, including the tooltip conventions for a random result (`ENCHANT_RANDOMLY`, and
`Lang.Value.POTION` / `EFFECT` / `DURATION`). A field defaulting to a whole registry
(all enchantments, all potions) must not be listed line by line — render the label alone and list the
entries only once a script or config has narrowed them.

## Step 4 — Global Loot Modifiers: check the destination resolves

`GlobalLootModifierUtils.getLootModifier` returns a modifier only when the GLM's conditions contain
one of three shapes: a `LootItemEntityPropertyCondition` on `THIS` with a concrete entity type
(→ `IType.ENTITY`), a `LootItemBlockStatePropertyCondition` (→ `IType.BLOCK`), or a condition the
`ILootTableIdConditionPredicate` recognises (→ `IType.LOOT_TABLE`). Anything else yields
`Optional.empty()` and the Forge plugin logs `Unable to locate destination`.

Conditions like `match_tool`, or the target mod's own conditions, constrain *how* loot is obtained,
not *what* the loot belongs to, so they resolve to nothing. The survey reports these as "Auto-GLM
without a resolvable destination". When the mod's own code tells you the real destination, build the
`ILootModifier<Block>` (or `<Entity>`) by hand instead of calling `getLootModifier`:

- A GLM keyed on a known set of blocks → `predicate` is that set's `containsKey`, and emit one
  operation per entry.
- A GLM that transforms whatever it is given → `predicate` returns `true` and the *operation's* item
  predicate does the filtering. That is safe: `AbstractServer.predicateItem` only attaches a modifier
  to a loot table when some operation predicate matches an item that table can produce.

Wrap a replacement in `ModifiedNode(utils, src, replacement)` whenever the modifier is conditional,
so the original drop stays visible as the alternative. Build the replacement's tooltip with
`TooltipUtils.getTooltip(utils, DEFAULT_QUALITY, chance, count, functions, conditions)` over the
concatenation of the modifier's conditions and the source node's, and take `chance` from
`node.getChance()` rather than `1` so the percentage stays honest.

`new ILootModifier<>() { … }` does not compile — an anonymous class needs the explicit type argument.

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

Tick the row and its per-finding checkboxes in `SB4_WRK.md`, annotate each finding the branch's jar
does not have, add the ones it has that the survey missed, and put the version-specific reasoning
(why a GLM was hand-rolled, which loaders exist) in that row's `Notes:` block. The tracker is
hand-maintained — never re-run `worklist.py` over a file with ticked boxes.

Porting to another branch starts from Step 1 again: the same mod on `1.21.1` is a different class
list, and only the shape of the shim ports, not its contents.
