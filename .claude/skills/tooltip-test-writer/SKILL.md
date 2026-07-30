---
name: tooltip-test-writer
description: This skill should be used when the user asks to "write tests for X tooltip", "implement tests for the Y tooltip", "add tooltip tests for", or wants JUnit coverage for a `*TooltipUtils` class in `ali/common` or `awi/common` (e.g. FunctionTooltipUtils, ConditionTooltipUtils, PlacementModifierTooltipUtils, IntProviderTooltipUtils, HeightProviderTooltipUtils, BlockPredicateTooltipUtils, FeatureConfigurationTooltipUtils), following the existing FunctionTooltipTest/FeatureConfigurationTooltipTest pattern.
---

# Writing tooltip-builder JUnit tests (ALI / AWI)

Both mods share the same `aci`-based tooltip tree system (see `aci/CLAUDE.md`). Both `ali/common` and `awi/common` have a `plugin/server` package full of `Xyz TooltipUtils` classes — one static `getXTooltip(IServerUtils utils, ConcreteVanillaType instance)` method per vanilla subtype, registered by exact class in `plugin/Plugin.java`. This skill covers writing a JUnit test class that exercises one of these `*TooltipUtils` classes end-to-end (construct a real vanilla instance → build its tooltip → assert the rendered lines).

## Step 1 — survey the target class and the existing pattern

1. Read the target `*TooltipUtils` class in full (`ali/common/src/main/java/com/yanny/ali/plugin/server/` or `awi/common/src/main/java/com/yanny/awi/plugin/server/`). List every public static method and which vanilla subtype it handles.
2. Read one sibling test that already exists in the same module's `src/test/java/com/yanny/<mod>/test/` package (e.g. `ali`'s `FunctionTooltipTest.java`, `awi`'s `FeatureConfigurationTooltipTest.java`). Copy its shape exactly: package, static imports (`TooltipTestSuite.UTILS`, `TestUtils.assertTooltip` / `assertUnorderedTooltip`), one `@Test` method per vanilla subtype, calling `TooltipUtils.getXTooltip(UTILS, instance).build()`.
3. Read the mod's `Lang.java` for the exact header/child translation strings (`Lang.<Category>.SOME_KEY` gives the literal English text, e.g. `"Rarity Filter:"`, `"Chance: %s"`) — these are the only reliable source for exact expected text, do not paraphrase them.

## Step 2 — construct real instances, not mocks

- Prefer the vanilla class's own public static factory (`RarityFilter.onAverageOnceEvery(20)`, `BlockPredicate.matchesBlocks(...)`, `CountPlacement.of(5)`, etc.) over `new`. Many concrete vanilla implementation classes are **package-private** (e.g. `MatchingBlocksPredicate`, `TrueBlockPredicate`, `NotPredicate`) — only their owning interface's static methods are public. Pass the factory's return value directly where an interface-typed field is expected (`BlockPredicate`, `IntProvider`, `HeightProvider`); a cast to the concrete subtype is only needed if the target method itself declares the concrete subtype as its parameter type (check the `*TooltipUtils` method signature — some vanilla factories, like most `PlacementModifier` ones, already return the concrete type directly and need no cast at all).
- Find exact factory names/signatures by grepping the mapped Minecraft sources under `~/.gradle/caches/ng_execute/*/output/net/minecraft/...` or the fabric-loom mapped sources jar (`~/.gradle/caches/fabric-loom/minecraftMaven/.../*-sources.jar`) for the target package — don't guess factory method names from memory, they vary across MC versions and overloads.

## Step 3 — know the two `TooltipBuilder` gotchas that WILL break naive expected-string guesses

Read `aci/common/src/main/java/com/yanny/aci/tooltip/TooltipBuilder.java` if unsure, but the two load-bearing facts are:

1. **Content-less nodes collapse to nothing, not to a bare header line.** `array(consumer, key)` builds an "outer" (keyless) wrapping an "inner" (keyed). If the consumer adds zero children/values/component, `inner.build()` returns `TooltipNode.empty()` **immediately** (this happens eagerly inside the factory call, not lazily) — so the whole thing renders as **0 lines**, not `["Header:"]`. This bites test-writers who pick a "trivial" input value expecting it to still show its own header (e.g. `BlockPredicate.alwaysTrue()` → `TrueBlockPredicate` has no fields to render → the entire surrounding tooltip, including the outer category, collapses to `List.of()`). Pick input values that actually populate at least one field when a non-empty result is expected.
2. **Depth counts only keyed array nodes.** A keyless wrapper node (the "outer" from `array(consumer, key)`, or a bare `array(consumer)` with no key) is fully transparent when rendering — it adds no line and does not increase indentation. Only a node that ends up with its own key contributes one `"  -> "` indent level to its children. When manually tracing expected indentation through nested `getValueTooltip(...)` calls, count only the keyed levels.

## Step 4 — do not hand-derive exact strings for anything non-trivial; probe it

Number formatting (`Double.toString`, `RangeValue.toIntString()`/`toFloatString()`), enum rendering (`.name()`, all-caps), and structural value types (e.g. `Vec3i` renders as `[0,0,0]`, not `"0, 0, 0"`) are easy to get subtly wrong from reading source alone. For any assertion where the exact string isn't already confirmed by an existing sibling test:

1. Write the assertion with an obviously-wrong or empty expected list (`List.of()` works well — every actual line then shows up as its own `"expected: <> but was: <...>"` failure).
2. Add the new test class to the module's `TooltipTestSuite.java` `@SelectClasses({...})` array (tests must run through the Suite class — it bootstraps `Bootstrap.bootStrap()`/language loading/`PluginManager` registration in `@BeforeSuite`; running the test class directly without the suite will NPE).
3. Run just that suite:
   ```
   ./gradlew :ali:common:test --tests "com.yanny.ali.test.TooltipTestSuite"
   ./gradlew :awi:common:test --tests "com.yanny.awi.test.TooltipTestSuite"
   ```
4. Read the exact actual strings from the failure output (or from `awi/common/build/test-results/test/TEST-com.yanny.awi.test.<YourClass>.xml` / the `ali` equivalent if terminal output is truncated — `grep -A5 'testMethodName' ...xml`).
5. Transcribe those exact strings into the final expected `List.of(...)`, then rerun to confirm green.

This loop is faster and more reliable than iterating on hand-written guesses — it was necessary even after fully reading the tooltip-builder source, so don't skip it to save time.

## Step 5 — wire it up and confirm

- Register the new test class in `TooltipTestSuite`'s `@SelectClasses`.
- Re-run the full suite once more after all assertions are finalized to confirm everything is green together (not just the individual tests in isolation).
- If a vanilla factory is `@Deprecated` (e.g. `SolidPredicate`/`BlockPredicate.solid()`), add a `//noinspection deprecation` comment above the call, matching the existing convention in `FunctionTooltipTest.testSetNbtTooltip`.