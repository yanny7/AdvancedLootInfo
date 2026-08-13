---
name: tooltip-coverage-audit
description: Use this skill when the user asks to "check for unused lang keys", "check unused translation keys", "audit Value/Branch lang coverage", "check if TooltipUtils covers all fields", "audit tooltip field completeness", or wants a repeat of the "unused LANG keys + missing tooltip fields" sweep across `ali/common` or `awi/common` `*TooltipUtils` classes — typically re-run once per Minecraft version branch after porting.
---

# Tooltip coverage audit (unused Lang keys + missing tooltip fields)

Both mods share the same `aci`-based tooltip tree system (see `aci/CLAUDE.md`) and the same `plugin/server` package of `*TooltipUtils` classes (see the `tooltip-test-writer` skill for the class/test-writing conventions this audit assumes). This skill is a **verification** sweep, not a feature — it has two independent halves that are usually run together but can be run separately if the user only asks for one:

- **Part A**: find `Lang.Value`/`Lang.Branch` (or whichever category the user names) keys that no test currently exercises, and extend tests to cover them (or delete genuinely dead ones).
- **Part B**: for every `*TooltipUtils` method, verify every field/record-component of the vanilla type it renders is actually referenced somewhere in the method body.

Both parts are read-heavy, cross-reference-heavy tasks against decompiled Minecraft sources — budget for it, don't rush the verification step.

## Scope check first

Ask (or infer from context) which mod(s) to audit — `ali/common`, `awi/common`, or both — and which Lang categories matter (the user may only care about `Value`/`Branch`, or may want everything). Don't silently expand scope beyond what's asked.

## Part A — unused Lang key sweep

### A1. How the tracking mechanism works

`<mod>/common/src/test/java/com/yanny/<mod>/test/TooltipTestSuite.java`'s `@BeforeSuite` calls `TestUtils.loadDefaultLanguage(resourceManager)`, which returns a `Pair<Language, Set<String>>`: the `Set<String>` (`notUsed`) starts as every key in `<Mod>Lang`'s `TRANSLATION_MAP` and a key is removed from it every time the injected `Language.getOrDefault(key, ...)` is actually called — i.e. every time some rendered `Component.translatable(key)` gets resolved during the test run. `@AfterSuite` logs `UNUSED` (sorted) via `LOGGER.info`. This means: **a key only counts as "used" if some test actually renders it**, not merely if some `*TooltipUtils` method references the `Lang` enum constant in source.

### A2. Get the actual unused-key list

Gradle caches the test task, so a plain `./gradlew :<mod>:common:test --tests "com.yanny.<mod>.test.TooltipTestSuite"` after a prior green run reports `UP-TO-DATE` and produces no fresh log. Force a real run:

```
./gradlew :<mod>:common:test --tests "com.yanny.<mod>.test.TooltipTestSuite" --rerun-tasks
```

Gradle does not print SLF4J test output to the console by default. Read it from the JUnit XML report instead:

```python
import re
data = open('<mod>/common/build/test-results/test/TEST-com.yanny.<mod>.test.TooltipTestSuite.xml').read()
print(re.findall(r'<system-out>(.*?)</system-out>', data, re.S)[0])
```

The `----- Unused translation keys (N) -----` block is the ground truth. Filter it to the categories the user cares about (e.g. lines matching `<mod>.property.value.` / `<mod>.property.branch.` if scoped to `Value`/`Branch`).

### A3. Classify every unused key before touching anything

For each unused key, find the owning `Lang` enum constant (grep the key's suffix in `<mod>/common/src/main/java/com/yanny/<mod>/language/Lang.java`), then grep every call site of that constant across `<mod>/common/src/main/java`. Classify:

1. **Genuine `*TooltipUtils` coverage gap** — the constant IS referenced inside a `*TooltipUtils` class, but no existing test exercises the code path that reaches it (empty list where a non-empty one was needed, `Optional.empty()` where `.of(...)` was needed, a rule/branch with zero entries, etc.). → extend an existing test or add a new one (see Part A5).
2. **Used only outside `*TooltipUtils` classes** — e.g. only in `plugin/common/nodes/*` (server-side tree-construction code, which has no unit-test harness — would need a real `ServerLevel`/registry scan) or `plugin/client/*` widget code. → out of scope for this audit; leave it alone, note it in the final report so the user knows it wasn't silently skipped.
3. **Structurally dead — zero call sites anywhere** (not even in node/client code) — confirm with a repo-wide grep across the *whole* module (`grep -rn "Branch.SOME_KEY\b" <mod>/`, not just `plugin/server`) before concluding this. Safe to delete the enum constant from `Lang.java` (datagen regenerates the lang json from it, so nothing else needs touching) — but still ask/flag rather than deleting silently if the user didn't explicitly say "delete unused keys".
4. **Structurally unreachable given current code** — a real call site exists, but the code shape makes the key permanently unreachable (see A4). → do NOT try to force a test into existence for this; report it as a design/code-smell question instead.

### A4. The merge/plural mechanism (read before writing any new assertion)

`TooltipBuilder.build()` (`aci/common/src/main/java/com/yanny/aci/tooltip/TooltipBuilder.java`) collapses a keyed wrapper with exactly one simple child (no key, no children of its own) into the wrapper's **singular** key form, using the child's own value directly (e.g. `"Can be Placed On: Stone"`). With ≥2 children, or a child that itself has a key/children, it renders the **plural** key form as a real header with nested children instead. Concretely:

- A `List<X>`/`HolderSet<X>` field linked to a `Lang.Branch.FOO(Lang.Value.FOO, ...)` pair needs **both** a 1-element test case (to hit the singular `Value` form) **and** a ≥2-element test case (to hit the plural `Branch` form) if both keys are unused — fixing one can silently un-cover the other if you only ever change the existing single test in place (widening 1→2 elements removes the only singular-merge trigger). Prefer adding a *second* assertion/variant alongside the original rather than replacing it, so both forms stay covered.
- A `*TooltipUtils` method that returns `TooltipBuilder.array(consumer, key)` for a single per-subtype value (the common per-`IntProvider`/per-`HeightProvider`/weighted-entry pattern) produces an **outer keyless `array` wrapper around an inner already-keyed array** — this outer/inner shape can *never* satisfy the merge condition (`isArray` must be false to merge), so a linked singular `Value` key reachable only through this exact call shape is permanently dead. This is category A3.4, not a test gap — don't spend time trying to construct a passing test for it.
- A field whose `Lang.Branch.FOO(...)` entry has identical singular/plural text (a 2-arg constructor, not a 3-arg linked one) doesn't have this problem — any non-empty size clears it.

### A5. Extending/adding tests

Follow the `tooltip-test-writer` skill's Step 4 loop exactly (write with an obviously-wrong expected list first, run the suite, transcribe the real output) — do not hand-derive nested indentation by reasoning about `TooltipBuilder`/`TooltipNode` alone; it is easy to get subtly wrong (this skill's author got one text string wrong on the first pass this way and only caught it via the run-and-transcribe loop). After each batch of edits, re-run the full suite and re-check the unused-key log — fixing one key's test input can accidentally un-cover a *different* key that used to be exercised only by the original input (see A4's first bullet for the concrete failure mode).

### A6. Register new test classes

If a `*TooltipUtils` class is completely untested (no existing `*TooltipTest` file), a fresh test class must still be added to `TooltipTestSuite`'s `@SelectClasses({...})` array or it silently never runs.

## Part B — missing tooltip field audit

### B1. What "complete" means

Every `get<X>Tooltip(IServerUtils utils, VanillaType value)` method in a `*TooltipUtils` class is expected to render **every field / record component** of `VanillaType` — via a bare field access, a same-named getter, *or* a differently-named getter/accessor (e.g. a boolean field surfaced as `isSomething()`), **including fields inherited from an abstract superclass constructor** (many vanilla families — `TrunkPlacer`, `FoliagePlacer`, `HeightProvider`, `IntProvider`, `RootPlacer`, `NoiseBasedStateProvider`, etc. — declare shared fields in the abstract base, and this codebase's convention is a small `addBase<X>Tooltip(...)` private helper called from every subtype method; check whether such a helper exists and whether every subtype method actually calls it — a subtype that builds its `array(...)` inline instead of delegating to the helper is the single most common miss pattern found in this audit).

### B2. Finding each vanilla type's real field list

**First, get the branch's actual MC version — don't skip this.** Read `minecraft_version` from the repo-root `gradle.properties`. When several branches are checked out side by side they share one Gradle cache — a version-agnostic search like `find ~/.gradle/caches/fabric-loom -iname "*-sources.jar"` will happily return a jar for a *different* branch's MC version, and it will look completely plausible (it decompiles fine, classes exist, fields exist) while being silently wrong for this branch. This has already caused wrong-but-plausible findings once, and it took an out-of-band re-verification against the correct version to catch it. Don't rely on subagents to catch this themselves — verify the version yourself before handing out any jar path.

Once you know the target version, locate the jar and confirm its name actually contains that version string before trusting it:

```
find ~/.gradle/caches/fabric-loom -iname "*-sources.jar" | grep -- "-<version-with-dots-and-underscores>-"
find <repo-root>/.gradle/loom-cache -iname "*-sources.jar"   # project-local cache, check here too
```

**Watch for loader-patch-only jars, not full merged sources.** A `forge-<mc>-...-sources.jar` or `neoforge-<mc>-...-sources.jar` in the loom cache is sometimes just that loader's own patch classes (a ~1MB jar containing only `net/minecraftforge/...` or `net/neoforged/...`, no `net/minecraft/...` at all) rather than the full decompiled merged Minecraft source tree — `unzip -l | grep -c "net/minecraft/"` (not `minecraftforge`/`neoforged`) before trusting it as a source of vanilla field lists. If no full decompiled tree is cached locally for the exact loader/version this branch builds (this can happen for Fabric-only mods like AWI, where loom may not have run a `genSources`/decompile step), a decompiled tree for the *same MC version* from a different loader (Forge/NeoForge/another sibling checkout on disk) is an acceptable substitute for vanilla (non-loader-patched) classes — the vanilla source is identical regardless of which loader's toolchain decompiled it, only the version has to match.

Extract the target class with:

```
unzip -o -q "<sources-jar-or-run>" "net/minecraft/path/to/SomeClass.java" -d /tmp/mcsrc
```

If the path is unknown, `unzip -l "<sources-jar>" | grep -i SomeClassName` first. Read the class's **`CODEC` definition** as the authoritative field list — a field *not* present in the codec (not serialized) is almost always a derived/cache value (e.g. a precomputed bounding box, a `NormalNoise` instance built from a `seed`) and is correctly *not* rendered; only fields that ARE in the codec are real user-facing configuration that must show up somewhere. Also check the immediate abstract superclass's own codec-builder helper (often a `protected static ... xxxParts(Instance<P> instance)` method) for inherited fields.

### B3. Ruling out false positives before reporting

A field can be legitimately absent from the tooltip for reasons that are NOT bugs:

- **Caller-handles-it split**: the field is rendered by the *caller* instead (e.g. used as a node's own key/label rather than part of the tooltip body). Grep how the method's return value / the value object is consumed elsewhere in `<mod>/common/src/main` before flagging.
- **Deliberate empty registration**: some types carry no useful end-user information and are intentionally registered to render nothing (see `aci/CLAUDE.md`'s "Missing-entry fallback and coverage reporting" section) — this is a legitimate pattern, not a gap, *only* when the type is genuinely fieldless/marker-like. A type with a real, non-trivial `CODEC` that renders nothing via `showEmpty`/`empty()` is a strong signal of a genuine miss, not this pattern — cross-check the codec before accepting "it's probably deliberate."

### B4. Execution approach — delegate, then personally verify the worst findings

This is a large, mechanical, file-by-file cross-referencing task across ~15-20 `*TooltipUtils` classes. Split the file list into 3-5 roughly line-count-balanced groups and dispatch one `Agent` (subagent_type `general-purpose`, `run_in_background: false` since you need all results before reporting) per group, each with: the exact file list, the sources-jar path you verified in B2 (state the MC version explicitly, e.g. "this branch targets 1.21.1"), the CODEC-as-ground-truth rule, and the three false-positive patterns from B3. Also tell every agent explicitly to spot-check that the jar you handed them actually matches the stated version (per B2) before trusting it, and to flag rather than silently proceed if it doesn't — one out of three agents in this skill's first real run caught a version mismatch in a jar path it was handed and self-corrected by finding a same-version jar elsewhere; the other two used the wrong-version jar without comment. Don't assume an agent will catch this on its own just because it's capable of it — say it explicitly. Do not use the `Workflow` tool for this unless the user has explicitly opted into multi-agent orchestration (see the `Workflow` tool's own gating rules) — plain parallel `Agent` calls in one message are sufficient and don't require that opt-in.

Agents can and do overstate confidence or misread a field list. Before reporting any finding to the user as a "definite bug," personally re-verify the highest-severity ones yourself: extract the same vanilla source, confirm the field really is in the `CODEC`, and re-read the flagged `*TooltipUtils` method directly. Only findings you've personally confirmed should be reported as "definite" — downgrade anything you haven't independently checked to "reported by audit, unverified."

### B5. Reporting — do not fix silently

Per this project's standing instructions: report findings, do not edit production code to fix them without asking first (this includes deleting a "structurally dead" Lang key from A3.3, or adding a missing field render in B3 — both are source-code changes, not test changes). Present a concise list (file, method, missing field(s), confidence, one-line reasoning) and ask whether to apply fixes, and whether to also add/extend tests for the fixed code paths (which loops back into Part A/`tooltip-test-writer`).

## Notes for repeating this on a new MC version branch

- The mechanism (`TooltipBuilder`, `TooltipTestSuite`, `Lang.java` shape, the `UNUSED` tracking) is identical across branches per `aci/CLAUDE.md` — only the sources-jar path, concrete vanilla field names, and which `*TooltipUtils` classes/methods exist will differ (new MC versions add/remove/rename vanilla subtypes).
- Re-run Part A fresh every time even if Part B found no new issues since the last branch's audit — a MC version bump can add new vanilla fields/subtypes to existing types (new `Lang` keys) independent of any code changes in this repo.
- Because sibling branches share `~/.gradle/caches`, a generic sources-jar search can return the *wrong* branch's MC version without any obvious signal that it's wrong (see B2) — always confirm the version against `gradle.properties` first, every time, even if last run's jar path is still sitting in your context from a previous session.
