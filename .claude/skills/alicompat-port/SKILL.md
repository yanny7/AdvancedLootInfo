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

Branches are usually separate checkouts, one per version. Work in the target branch's checkout.

## Phase 1 — the merge

```bash
git fetch origin
git merge origin/<lower>
```

**The target-mod block in `gradle.properties` is resolved as `ours`, silently.** That is
`compat_mods` and every `<mod>_<loader>_dep` line: each `_dep` pins a CurseForge file id for one
specific Minecraft version, so anything merged up from below names a file for the wrong version, and
`compat_mods` states what this branch has actually ported. Do not try to reconcile them and do not
ask about them. The rest of the file — loader versions, viewer versions, enabled platforms —
conflicts like any other code and is resolved on its merits.

New shim source sets arrive as new files. That is expected and they stay: a slug whose files are
present but whose name is absent from `compat_mods` is **dormant**, compiled by nothing. See
`alicompat/CLAUDE.md`.

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
slug should not have been in `compat_mods` on this branch yet — take it out and leave it for phase 2.

**Stop here.** Report what merged, what was restored, and how many slugs are dormant. The user
commits. Phase 2 does not begin until that commit exists.

## Phase 2 — activating the dormant shims

Run this only on a committed merge.

### Repin first

The pinned file ids came up from a lower branch and name files for the wrong Minecraft version, so
every later step would be reading the wrong jar:

```bash
python3 check_versions.py --loader <loader>
python3 check_versions.py --update
```

`--update` swaps file ids and nothing else — it does not check that anything still compiles. Build
after it.

### What is dormant

```bash
act=$(sed -n '/^compat_mods=/,/[^\\]$/p' gradle.properties | tr -d '\\ \n' | sed 's/^compat_mods=//')
ls alicompat/*/src/compat 2>/dev/null | grep -v ':' | grep -v '^$' | sort -u | while read s; do
  echo "$act" | tr ',' '\n' | grep -qx "$s" || echo "dormant: $s"
done
```

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
2. **Global loot modifiers** — `GlobalLootModifier`, usually with `Destination`.
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
- add the slug to `compat_mods` (one per line, alphabetical)
- if the mod has no file for this version or loader, write no shim, delete no source set, and say why

## Done when

- the merge is a commit of the user's making, with ACI/ALI/AWI/ALICompat building
- activated slugs are in `compat_mods` with file ids resolved for **this** version
- `META-INF/services/com.yanny.alicompat.IModCompat` in the built jars lists the new shims
- no source set was deleted to express dormancy

## Tell the user

Which step of the chain this was and whether another follows; what was restored and what was
deliberately left out; how many slugs are still dormant; and which port is least certain. Nothing
here is run in game.
