---
name: alicompat-port
description: Carry ALICompat work from one Minecraft version branch to the next — merge the branch below, keep the target-mod block as ours, get ACI/ALI/AWI compiling, and afterwards activate the shims that arrived dormant by repinning their jars and re-deriving their class lists. Use when the user asks to merge a branch upward, port shims to a newer Minecraft version, asks why a shim present on a lower branch does nothing here, or asks which shims on this branch are still dormant.
---

# Porting ALICompat between version branches

Read `CLAUDE.md`'s **"Porting between branches"** first — it is the contract this skill executes.
The short of it: a merge moves code, not decisions, and it ends at a commit. Activating shims is a
separate pass that starts afterwards.

Never do both in one step. A build failure then has two possible causes — the merge, or a target
mod's API change between versions — and no commit to fall back to.

## Which branch merges from which

Branches follow Minecraft version order and the set changes over time, so **derive it, never assume
a list**:

```bash
grep -m1 '^minecraft_version=' gradle.properties            # this checkout
git branch -r                                               # what exists
```

Merge one step (`git merge origin/<the branch directly below>`). Reaching the newest branch from the
oldest is several runs of this skill, one per step, each with its own commit.

Every branch is its own checkout, a sibling directory one level above the repo root (`ls ..`); read
`minecraft_version` in each to find the lower one, never guess from the directory name. Work in the target
branch's checkout and read the lower branch from its sibling directory rather than from `git show`.

## Phase 1 — the merge

```bash
git fetch origin
git merge -X no-renames origin/<lower>
```

Always without rename detection. Shims are near-identical files repeated across loaders and mods, so git pairs
unrelated ones — a neoforge shim of one mod conflicting with a forge shim of another — and the result is
conflicts that are not real and edits applied to the wrong file. A modify/delete conflict on a file this branch
removed is resolved by keeping it removed (`git rm`).

**The generated target-mod block in `gradle.properties` is resolved as `ours`, silently.** That is
`compat_mods` and every `<mod>_<loader>_dep` line: each `_dep` pins a CurseForge file id or maven artifact
versions for one specific Minecraft version, so anything merged up from below names a file for the wrong version, and
`compat_mods` states what this branch has actually ported. Do not try to reconcile them and do not
ask about them. `scripts/supported_mods.json` is the opposite case — it holds no Minecraft-version-specific
data, so it merges like any other file and a new entry arriving from below is kept. The rest of the file — loader versions, viewer versions, enabled platforms —
conflicts like any other code and is resolved on its merits.

New shim source sets arrive as new files. That is expected and they stay: a slug whose files are
present but whose name is absent from `compat_mods` is **dormant**, compiled by nothing. See
`alicompat/CLAUDE.md`.

### Dormant shims are not phase 1's business

Phase 1 never reads, fixes or sweeps a dormant source set. Nothing compiles it, and phase 2 re-derives it
against this version's jar and the current ALI/ALICompat API anyway, so any work on it now is spent twice.

- A conflict inside a dormant source set takes the lower branch's side unread: `git checkout --theirs -- <path>`,
  or `git rm` when the lower branch deleted it. Check every such path against `$S/active_dirs.txt` (built below,
  before resolving any conflict), never against a list in memory: `--theirs` on an active source set silently throws
  away this branch's port, and the build will not catch it.
- A grep, sed or script that brings shims in line with a changed API — ALI's, ALICompat's or vanilla's (a
  constructor turned factory method) — runs over active source sets only, never over `alicompat` or
  `alicompat/*/src/compat/*` as a whole. Activity is per slug *and* loader: a source set is active when its
  `<slug>_<loader>_dep` line exists. Build the list first (`$S` is your scratchpad) and feed every sweep from it:

  ```bash
  for d in alicompat/*/src/compat/*/; do
    l=$(echo $d | cut -d/ -f2); s=$(basename $d)
    grep -q "^${s}_${l}_dep=" gradle.properties && echo $d
  done > "$S/active_dirs.txt"
  ```

  A repo-wide sweep (`grep -rl … aci ali awi alicompat`) excludes `alicompat/*/src/compat/` and runs a second
  time over that list.
- Unresolved symbols, stale key schemes or old lambda shapes in a dormant shim are not reported as merge findings.

### Other recurring conflicts

- **`alicompat/<loader>/src/main/generated/**`** is datagen output, so it is never merged by hand. Take either
  side, build, then regenerate with `./gradlew runAlicompat<Loader>Datagen` for every enabled loader. It merges
  silently too, typically with keys of a slug that is dormant here. Datagen rewrites a file only when its output hash
  differs from the one in `generated/.cache/`, not from the file on disk, so it leaves a merged file in place
  (`written: 0`). Compare the cache entry with the `HEAD` file (`git show HEAD:<path> | sha1sum`). If they match,
  restore the file with `git checkout HEAD -- <path>`. Otherwise delete the file and run datagen again.
- **Branch self-references in the `CLAUDE.md` tree**: sentences such as "on this `<version>` branch", "unported to
  `<version>`", "this one, `ali_<x>/`", or which modules and run tasks are built here. They usually auto-merge without
  a conflict and arrive naming the lower branch. After the merge, grep the docs the merge touched for the lower
  branch's version, and for any other version used as "this branch". Rewrite each such line to this branch's version
  and to the flags in this `gradle.properties` (`<loader>_enabled`, `<viewer>_enabled`, `lootjs_enabled`). Leave
  references that mean a real other version alone ("1.21.5 split `GatherDataEvent`").
- **`alicompat/CHANGELOG.md`**: keep this branch's `## []` entries, add the lower branch's entries that apply here
  (cross-cutting changes), and drop "Added X support" for a slug that is dormant here, because this branch does not ship it.
- **`Lang` enums and the lang JSONs** (generated `en_us` and the hand-kept translations): a key deleted below sits next
  to keys that exist only here, and a key this branch already deleted as unused still exists below. Never resolve by
  taking the union of both sides, because that silently brings back the keys this branch deleted. Start from `ours`
  and drop the keys the lower branch deleted, unless code here still uses them (`grep -rn "Lang\.<Enum>\.<KEY>\b"` over
  the built modules). Then diff the `Lang` constants against `HEAD`: the result should be `HEAD` minus those keys, plus
  whatever the lower branch added and this branch uses. Regenerate `en_us` and check that it gained nothing.
  Datagen repairs only `en_us`, so remove the same keys from every translation by hand.

Stage resolved files by exact path, never `git add -A` or a directory: that marks every file under it resolved,
conflict markers included, and they vanish from `git diff --name-only --diff-filter=U`. Before the build, check
nothing slipped through: `grep -rln '^<<<<<<< \|^>>>>>>> ' --exclude-dir=build --exclude-dir=.gradle .`

### Source sets that went missing

A source set is never deleted to express dormancy, so one that exists below and not here is either a
loader this branch does not have, a mod with no file for this version, or an accident. List them and
judge each:

```bash
for l in fabric forge neoforge; do
  [ -d "alicompat/$l/src/compat" ] || continue
  for s in $(ls ../<lower checkout>/alicompat/$l/src/compat 2>/dev/null); do
    [ -d "alicompat/$l/src/compat/$s" ] || echo "missing: $l/$s"
  done
done
```

Restore an accidental one from the lower branch together with its `services/` fragments, and leave
its slug out of `compat_mods`. Deleting is right only when the target mod genuinely has no file for
that loader on this version.

### Finish the merge

Build every loader this branch enables — read them from `settings.gradle` and the `<loader>_enabled`
properties, do not assume which exist:

```bash
./gradlew :aci:<loader>:build :ali:<loader>:build :awi:<loader>:build :alicompat:<loader>:build
```

A failure in `alicompat/common` is a merge problem and belongs here. A failure in a shim means its
slug should not have been in `compat_mods` on this branch yet — take it out and leave it for phase 2. Check
first whether the merge touched the failing file (`git diff --cached HEAD --stat -- <path>`): if it did not, the
failure predates the merge — report it rather than disabling the slug.

Code merged up from below is written against the lower Minecraft version's API, so the usual failures are
vanilla renames and changed signatures (a moved package, a renamed class, a return type that became
`Optional<Holder<…>>`). Fix them the way the code already on this branch does — find a sibling that calls the same
API here and copy its shape, across ALI and AWI alike — rather than inventing a new adapter.

A conflict in a golden test file (`src/test/resources/*.json` guarded by a `-D<mod>.<name>.regenerate` switch) is not
merged by hand: take `ours`, run the owning `common` module's whole `test` task with the switch (a single `--tests`
class skips the suite's bootstrap and fails), and check the resulting diff contains only what the merged change
explains.

A merged change that alters rendered output (a number format, a tooltip shape) also breaks tests that exist only on
this branch, because their expectations still hold the old output. Run the `common` modules' `test` tasks and take the
failing lines from `build/test-results/test/*.xml`. Update an expectation only when the new value is exactly what the
merged change produces in the lower branch's own tests. A regex over the failing files is fine for a pure format
change, but restrict it to the lines that failed: a line in the old format that still passes marks code the merged
change does not reach on the lower branch either. Leave it as it is and report it.

A test whose subject exists only on this branch (a Minecraft type the lower branch lacks) has no lower-branch test to
copy the new value from. Derive it, do not guess it: let the assertion record instead of fail (a temporary flag-file
check in `aci/common`'s `TestUtils.assertTooltip` that appends caller, expected and actual to a scratchpad file), run
the suite, read every expected→actual diff, and apply only the diffs the merged change explains, matching the expected
lines by content (an assertion may sit in a shared helper, so its line number points nowhere useful). Revert the hook
before the final run. A test that compared a removed type's `toString()` is rewritten to assert the rendered tooltip,
and each new value is checked by hand against vanilla's algorithm, not copied from the output.

Code that exists only on this branch and is written against an API the merge removed is ported in this phase, since
nothing compiles until it is: converters for this version's own vanilla types, tests of them, docs. Vanilla's behaviour
is checked against the decompiled jar (the loom `*-sources.jar` files can be empty; decompile the merged
`minecraft-merged-*.jar` from `.gradle/loom-cache` with vineflower into the scratchpad). A name, a signature or a
behaviour the lower branch does not settle for this version (two families where the lower branch has one, an erasure
clash with an inherited method) is the user's decision: ask before writing the code that depends on it.

A merged change that rewrites a pattern across every shim, such as a key scheme or a renamed helper, does not
reach code that exists only on this branch, and that code still compiles. After the merge, grep the active
slugs' source sets for the old pattern and bring the leftovers in line; the generated lang files show them as
keys without the new shape.

The same holds for a change to one shim. A loader the lower branch lacks keeps its own copy of that shim here
(forge below, neoforge here), and the merge never touches that copy. For every shim file the merge changed,
open the same slug under each other loader here and, if that slug is active, port the change to that copy.
List them from the lower branch's diff (a `'alicompat/*/src/compat/'` pathspec matches nothing, filter with `grep`):

```bash
git diff --name-only --diff-filter=M $(git merge-base HEAD origin/<lower>) origin/<lower> -- alicompat | grep /src/compat/
```

Do it as a 3-way merge rather than by hand: the lower branch's diff of the sibling loader's file is the patch, and
`git merge-file` applies it to this branch's copy, leaving conflict markers only where the copies genuinely differ:

```bash
b=$(git merge-base HEAD origin/<lower>)        # once the merge is committed: git merge-base HEAD^1 HEAD^2
git show $b:alicompat/forge/<rel> > "$S/base.java"
git show origin/<lower>:alicompat/forge/<rel> > "$S/theirs.java"
git merge-file -L ours -L base -L theirs alicompat/neoforge/<rel> "$S/base.java" "$S/theirs.java"
```

Exit code 0 is a clean apply, a positive one is the number of conflicts left to resolve by hand. Use `fabric` as the
base when the lower branch has no `forge` copy of that file.

`git merge-file` only reaches files the lower branch has. A shim file that exists only here (a slug or class the lower
branch never had, or a loader copy written for this version) gets an API change only through the compiler, and a change
that compiles either way never reaches it — most often a `Lang` text: a key turned into an array header (`"X: %s"` →
`"X:"`) below stays `"X: %s"` here and renders the placeholder literally. After the build, compare each active slug's
`Lang` keys across its loader copies and port every value that differs only because one copy missed a lower-branch
change:

```bash
for n in alicompat/<this loader>/src/compat/*/java/com/yanny/alicompat/compat/*/*Lang.java; do
  for o in <every other loader>; do
    f=${n/<this loader>/$o}; [ -f "$f" ] || continue
    diff <(grep -oE '^\s+[A-Z_0-9]+\("[^"]+", "[^"]*"\)' "$f" | sort) \
         <(grep -oE '^\s+[A-Z_0-9]+\("[^"]+", "[^"]*"\)' "$n" | sort) | grep '^[<>]'
  done
done
```

Only a key present on both sides with a different value is a finding; keys on one side only are version differences.
A value that differs on purpose (an empty `showEmpty` array has no colon) stays as it is.

Neither the merge nor `git merge-file` reaches code that implements the same thing differently here: a module
written against another major version of its target (LootJS 1 below, LootJS 3 here), a vanilla type that exists only
here (`TypeSpecificTrade`), or a shim class the lower branch never had. Such a file usually conflicts as a whole or not
at all, so take `ours` and port the merged change by its intent. Read each lower-branch commit's diff
(`git show <commit>`), name the pattern it replaced (a hardcoded string turned `Lang` key, a `ResourceLocation` id
turned `ResourceKey`, a `toString()` value, a call that gained an argument), and grep for the old pattern over
`aci`, `ali`, `awi`, `alicompat/common` and the active source sets (`$S/active_dirs.txt`), leaving out tests. Every hit
the merged change would have rewritten had the code existed below gets the same rewrite. A hit the lower branch left
alone in its own equivalent code stays as it is. A `Lang` enum the merge brings in for code that differs here is
reshaped to this branch's constants, not copied from below.

**Stop here.** Report what merged, what was restored, and how many slugs are dormant. The user
commits. Phase 2 does not begin until that commit exists.

## Phase 2 — activating the dormant shims

Run this only on a committed merge.

### Repin first

The pinned file ids and artifact versions came up from a lower branch and name files for the wrong Minecraft version, so
every later step would be reading the wrong jar:

```bash
python3 scripts/check_versions.py --loader <loader>
python3 scripts/check_versions.py --update
```

`--update` regenerates the block for the shims this branch already has active, and skips every
dormant one. It does not check that anything still compiles — build after it. **Never `--init` here**:
that switches every dormant shim on at once, which is the opposite of what phase 2 is for.

The same run then diffs every active shim against its newly pinned jar and writes
`build/compat_scan.txt`. That report is the re-derivation this phase would otherwise do by hand: `-`
and `!` rows are the classes this Minecraft version moved, renamed or re-parented out from under the
shim, `+` rows are what it gained. Work that list before building — the `-` rows are exactly the
compile errors the build is about to print, with the reason attached.

### What is dormant

```bash
python3 scripts/check_versions.py --loader <loader>
```

Its "Dormant, present in the tree but not in `compat_mods`" section is the list, and each row says
which loaders that target mod has a file on for this Minecraft version — the rows marked *port it*
are phase 2's worklist, the rest cannot be written here at all. The script leaves those entries
strictly alone — it neither pins nor scaffolds them — so a `--update` run after the merge cannot
switch on a shim nobody has ported.

"0 portable" can be real: mods skip Minecraft versions. Before reporting that, check it on CurseForge in one pass
over every project in `scripts/supported_mods.json` (`/v1/mods/<id>` → `latestFilesIndexes[]` whose `gameVersion` is
this one). Print each project's hits together with its `modLoader` and `releaseType`, and print an active mod
whether it has hits or not, as the control.
The scripts take only release and beta files, so a version listed there may still be "no file": check
`releaseType` (`/v1/mods/<id>/files?gameVersion=<mc>`, 3 is alpha). A mod that ships only alphas on CurseForge
often publishes releases on its own maven. Find that maven and report it, since adding a `maven` source to
`scripts/supported_mods.json` is the user's decision.

### Take them in groups, not in bulk

Group by the hooks a shim registers, so one pass needs one part of `alicompat-shim`'s knowledge
rather than all of it. Derive the grouping from the code — it stays current as shims are added:

```bash
for f in $(find alicompat -path '*src/compat*' -name '*Compat.java' -not -path '*/build/*' | sort -u); do
  slug=$(echo $f | sed 's|.*/compat/\([a-z0-9]*\)/.*|\1|')
  echo "$slug: $(grep -oE 'register[A-Za-z]+' $f | sed 's/register//' | sort -u | tr '\n' ' ')"
done
```

Sensible batches, cheapest first:

1. **One accessor, tooltip only** — a single `FunctionTooltip`, `ConditionTooltip`, `EntryTooltip`,
   `IngredientTooltip` or `ValueTooltip`. Start here: it rebuilds the wiring and shows what this
   Minecraft version changed, on the shims where a mistake costs least.
2. **Global loot modifiers** — `GlobalLootModifier`, often with a `PageResolver` or a `LootContextPreparer`.
3. **Trade listings and traders** — `ItemListing`, `SelfItemListing`, `Trades`.
4. **Mixed** — an accessor plus a GLM in one mod.
5. **The large ones** — five or more hook kinds. Each alone.

Two orderings hold regardless of version: a library shim goes before a mod that builds on it (its
classes may be needed on the compile classpath), and a simple trader before a complex one.

### Per shim

This is a full port, not a merge. It runs `alicompat-shim` from its Step 0:

- fetch the jar for **this** Minecraft version and loader, and diff the classes the shim uses against
  what the jar contains — classes move, get renamed and disappear between versions
- adjust the shim; only its shape ports, never its contents
- bring it up to the current ALI/ALICompat API too: phase 1 left it untouched, so every API change merged
  while it was dormant (key scheme, helper signatures, removed interfaces) lands here as a compile error
- add the slug to `compat_mods`, then `python3 scripts/check_versions.py --update` to pin its `_dep` lines
  and rewrite the block (the slug's order in the list is the script's business, not yours)
- if the mod has no file for this version or loader, write no shim, delete no source set, and say why
- if the mod has a file but nothing left to register — its loot and trade classes are gone, or ALI already renders
  what replaced them — reduce the source set to the `--scaffold` skeleton (an `IModCompat` returning only the mod id,
  no `Lang`, no accessors) and enable it anyway. Never leave it dormant and never delete it: an enabled shim keeps
  being repinned and scanned, so a later mod version that adds a class reaching an ALI hook shows up as `+` on the
  next run. Anything it deliberately leaves unregistered goes into `scan_ignore.json` with the reason

## Done when

- the merge is a commit of the user's making, with ACI/ALI/AWI/ALICompat building
- activated slugs are in `compat_mods` with file ids or artifact versions resolved for **this** version
- `META-INF/services/com.yanny.alicompat.IModCompat` in the built jars lists the new shims
- no source set was deleted to express dormancy

## Tell the user

Which step of the chain this was and whether another follows; what was restored and what was
deliberately left out; how many slugs are still dormant; and which port is least certain. Nothing
here is run in game.
