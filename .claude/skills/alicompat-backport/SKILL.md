---
name: alicompat-backport
description: Carry a bug fix or small feature that already shipped on a lower Minecraft-version branch's ALICompat shim onto this branch's copy of the same shim, when the two copies were never diffed by a merge (independently rewritten against different target-mod API shapes, or one side is Forge-only while this branch runs NeoForge). Use when the user names a changelog entry or fix from another branch and asks to port it here ("port the stray colon fix", "bring over the Ars Nouveau cycling fix", "did the Relics random-stats fix make it to this branch"), or after an `alicompat-port` merge surfaces changelog entries that named no file the merge actually touched.
---

# Backporting a shim fix across Minecraft versions

This is **not** `alicompat-port` (merging a whole branch) and **not** `alicompat-shim` (writing a shim from
scratch or bumping its target-mod version). It is the gap between them: a fix that already exists on a
lower branch's **active** shim, for a slug that is **also active** on this branch, but whose code never
collided in any merge — so nothing forced you to look at it. This typically surfaces by reading the lower
branch's `alicompat/CHANGELOG.md` `## []` section after a merge and finding entries that named no file the
merge's conflict list touched.

Read `alicompat/CLAUDE.md` first for the shim architecture this works inside of.

## Why this needs its own pass

A shim is compiled against one pinned file of its target mod per branch (`check_versions.py`'s per-version
pin). Two branches several Minecraft versions apart routinely pin mod versions whose internals have
diverged — a field renamed, a class split, an API rewritten from Forge's `RegistryObject` to NeoForge's
`DeferredHolder`, or a whole subsystem replaced. `git merge` never looks at this: it only resolves text
collisions, and two independently-written shims for the same slug collide as a whole file (`take ours`) or
not at all when one side is Forge-only and the other NeoForge-only. A real fix can therefore sit on the
lower branch indefinitely without ever being flagged as "needs attention" here — it compiled on both sides
before and after, so nothing broke.

## Step 0 — find the source commit

Work from the lower branch's sibling checkout (`../ali_<version>`, see the root `CLAUDE.md`'s "Sibling
checkouts" if unsure of the path), never by re-reading a changelog line alone:

```bash
cd ../ali_<lower>
git log --oneline --all -i --grep="<mod name>" --grep="<keyword from the changelog line>"
```

"Added game tests for `<mod>`, fixed findings" commits are the single densest source of this category —
the gametest pass that added golden files for a mod is routinely also the commit that fixed what the
findings turned up (hash-order output, a stray colon, NBT carried over verbatim). Read the full diff
(`git show <commit>`), not just the changelog line — the line is a one-sentence summary and the diff is
the actual fix.

## Step 1 — find every active copy here that needs it

A shim can have up to three loader copies (`fabric`/`forge`/`neoforge`). Only an **active** one matters —
check `<slug>_<loader>_dep` in `gradle.properties`, never the presence of the directory (a dormant copy is
not shipped, so it is phase 2 of `alicompat-port`'s problem, not this skill's). List every active copy of
the slug before writing anything:

```bash
grep "^<slug>_" gradle.properties | grep "_dep="
```

A fix commonly needs porting into more than one active copy (e.g. a mod active on both `fabric` and
`neoforge` here, with no shared base file since the lower branch's loader split differs from this one's).

## Step 2 — verify against the actual pinned jar, every time

Never copy the lower branch's code on the strength of "it used to work" — the single most common way this
goes wrong is assuming the target mod's API shape travelled unchanged across the version gap. Before
writing anything:

```bash
find ~/.gradle/caches -iname "*<mod-slug>*<file-id-from-the-dep-line>*"
mkdir -p /tmp/<scratch>/ext && cd /tmp/<scratch>
unzip -o -q <jar> "<package/path/ClassName.class>" -d ext
javap -p ext/<package/path/ClassName.class>          # -c too, for a method body / data-structure check
```

Check, in order:
1. **Does the class/field/method the fix touches still exist, under the same name?** A rename is usually
   harmless (adjust the name); a disappearance means the fix's premise is gone.
2. **Did the *shape* change?** A field becoming a different collection type, a method gaining a provider
   parameter (e.g. `getFoo()` → `getFoo(HolderLookup.Provider)`), a `RegistryObject` becoming a
   `DeferredHolder` — these usually port with a mechanical signature change, not a rewrite. Check the
   replacement type's own methods (`javap -p` it too) before assuming `.getId()`/`.get()` still exist.
3. **Does the bug the fix addresses still exist in this shape?** This is the step most worth slowing down
   for — a hash-order bug ported forward only because the *collection type* carried over
   (`javap -p -c` the method that builds it: a `HashMap`/`HashSet` still means unstable order; a
   `LinkedHashMap`/an array built from a registry's own iteration already means stable order, and the fix
   is now a no-op that would just be dead code). Decide this from the bytecode, not from the fact that the
   bug's *name* still sounds applicable.
4. **Is the mechanism the fix depends on still reachable at all?** A fix that reads a lambda's captured
   fields (the `Utils.getCapturedInstances`/`ReflectionUtils.getCapturedInstances` pattern — see
   `alicompat/CLAUDE.md`'s "Writing a shim") only works because some *other*, findable piece of code
   constructs that lambda in a shape whose captured data answers the question. If the mod rewrote that
   construction site into an opaque functional interface with no exposed state (checked via `javap -p` on
   the interface — zero fields, one abstract method, nothing else to capture), the mechanism is gone and
   the fix cannot be ported by signature-adjustment alone. Don't improvise a replacement by guessing at an
   unseen construction site; report the blocker instead (see "When it doesn't port" below).

## Step 3 — apply, mirroring the existing shim's conventions

Follow `alicompat/CLAUDE.md`'s "Writing a shim" and "Tooltip code style" sections for shape (one
`getTooltip` per accessor, `PluginUtils`/`GlmAccessorUtils` registration, `Lang` enum punctuation
conventions). Port the fix's *intent* into this branch's actual accessor/compat file, not a patched copy of
the lower branch's file — the two are usually structured differently enough (different field names,
different accessor granularity) that a line-level patch does not apply cleanly anyway.

Translations: hand-maintained non-English locale files (`src/compat/<slug>/lang/<locale>.json`) must track
any English punctuation change the fix makes (a dropped trailing colon, a changed `%s` position) — grep the
same key across every locale file next to the one you're editing, not just the English `Lang` enum.

Changelog: add one line to this branch's `alicompat/CHANGELOG.md` `## []` section once the fix is applied
and verified, naming only the mods it actually landed in here (drop a mod from the list if its shim is
dormant on this branch, or if the fix is a no-op per step 2.3).

## Step 4 — verify

Build the loader(s) touched (`./gradlew :alicompat:<loader>:build`); run that mod's unit tests if it has
any (`find alicompat -path "*compat/<slug>*test*" -name "*.java"` — note dormant-forge and active-fabric
copies often carry the only existing tests, while an active neoforge-only copy frequently has none, which
is a pre-existing gap this skill does not need to close). Where the mod has a gametest golden set and the
fix changes rendered output, regenerate it (`alicompat/CLAUDE.md`'s Gametests section) — this skill does
not run gametests on its own initiative, since they are expensive; say whether one is warranted and let the
user decide.

## When it doesn't port

Report it, don't force it. Three outcomes are all legitimate and should be told apart explicitly in the
summary:
- **Ported** — applied, built, (tests updated if any exist).
- **Not needed** — the bug's precondition no longer holds on this branch's pinned version (say which
  bytecode check proved it, e.g. "already iterates `BuiltInRegistries.ITEM`, which is insertion-ordered").
- **Blocked** — the mechanism the fix relied on doesn't exist in this branch's pinned API shape, and
  writing a replacement would mean reverse-engineering an unseen part of the mod rather than adjusting a
  signature. Name exactly what's missing (the class, the field, the vanished capability) so a future attempt
  knows where it was left off.

Never silently drop a changelog entry's mod from the list *and* say nothing — either it shipped here too
(name it), or it didn't and the summary says why.
