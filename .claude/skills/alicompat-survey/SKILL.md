---
name: alicompat-survey
description: Survey a modpack for everything ALI cannot render — from its log and from a static scan of every jar — resolve each finding to the owning mod and its CurseForge maven coordinates per Minecraft version, subtract what this repo already registers, and write a per-mod compatibility tracker that plans ALICompat work. Use when the user supplies a modpack directory or a pack log ("StoneBlock", "SB4.txt", "make support for all mods in this pack"), asks which mods need ALICompat shims, asks which trading entities are unsupported, or asks for the curse.maven dep lines for a set of mods.
---

# ALICompat modpack survey

Turns one modpack directory into a work plan: which mods produce something ALI cannot render, how
confident that finding is, and the `curse.maven` coordinate needed to compile a shim against that mod
on every supported Minecraft version.

Everything is scripted. Run `run.sh`; do not re-derive its output by reading logs or jars into
context. A 414-jar pack costs one invocation — about two minutes, nearly all of it CurseForge API
round-trips; the 100k-class static scan is fifteen seconds.

## Run it

```bash
.claude/skills/alicompat-survey/scripts/run.sh \
  --pack-dir "$HOME/.local/share/PrismLauncher/instances/<pack>" \
  --work W --out W/survey.md
```

The survey markdown is an intermediate; the deliverable is what `worklist.py` writes from the same
JSON (see below), so keep both in the working directory `--work` names.

One path is enough. `packdir.py` reads the launcher's own metadata for the rest: `mods/`, the
Minecraft version and loader (`flame/manifest.json`, else Prism's `mmc-pack.json`), the log
(`logs/debug.log`, else `logs/latest.log`), the shared `libraries/` tree, and from it the Minecraft
and loader jars. `--minecraft-jar`, `--log`, `--libraries` override any of those; `--repo` points the
coverage stage at a different checkout (default: the git root of the skill).

`--targets` overrides the Minecraft version / loader matrix. The default matches what the ALI
branches build:

```
1.20.1:fabric,forge   1.21.1:fabric,forge,neoforge   1.21.11:fabric,neoforge
26.1.2:fabric,neoforge   26.2:fabric,neoforge
```

Needs `CURSEFORGE_API_KEY` (the key `upload.py` uses). The pack works without a log — the static
scan is the larger half.

## The two kinds of evidence

**Log** — what this run of the pack actually handed ALI and ALI could not render. Precise, but only
covers what the pack exercised, and only ALI's *push* paths.

**Static scan** — what a mod *can* hand ALI, found by walking the supertype chain of every class in
every jar. It catches two things the log structurally cannot:

- **Pull-model gaps.** `registerTrades` is a pull: ALI asks for a trader's offers by entity id, so a
  trading entity nobody declared produces **no warning at all**, ever. A static scan is the only way
  to see it.
- **Dormant types.** A loot function or condition the pack never triggered but a datapack can.

The report merges them into one confidence ladder:

| confidence | meaning |
| --- | --- |
| `confirmed` | logged this run **and** nothing in the repo covers it |
| `likely` | concrete public type reaching an ALI hook, not logged, not covered |
| `informational` | abstract / interface / non-public, or only a same-simple-name registration exists |
| `covered` | exact class already registered here (or a mixin targets it) |

## Stages

`run.sh` chains six scripts; run one directly when only part needs redoing. All intermediate JSON
stays in the work directory (`--work`, else a temp dir) — that is the interface between stages.

1. `packdir.py --pack-dir` → `pack.json`. Layout, versions, log, Minecraft and loader jars. A
   `missing` list names anything it could not find.
2. `parse_log.py <log>` → `gaps.json`. Regex-matches ALI's and ACI's warning shapes into
   `{category: [subject]}`. Adding a new warning is one row in `PATTERNS` (and, for a
   `[ali] Missing <label> for <x>` line, one in `CATEGORY_SLUG`); an unknown label still lands in the
   report under a slugified name rather than being dropped.
3. `scan_jars.py --gaps --mods-dir` → `owners.json`. Attributes each logged class to the jar that
   contains it and each namespaced id to the jar owning that mod id, and fingerprints those jars.
   Lambda and nested-class suffixes are stripped first, so a lambda is attributed to the mod
   declaring its enclosing class.
4. `classindex.py --pack --base-types` → `candidates.json`. The static scan. Parses the hierarchy
   header of every class in every jar — mods plus the Minecraft and loader jars, which supply the
   vanilla half of each chain — then keeps mod classes whose supertype closure reaches a base type in
   `base_types.json`. A second pass re-reads only the candidates for field descriptors, constructor
   descriptors and namespaced string constants. **Those shapes are written to the JSON and
   deliberately kept out of the report**: they are what a shim generator needs, not what a human
   reading the plan needs.
5. `coverage.py --repo` → `coverage.json`. Every `register<Hook>(X.class, …)` call, every
   `registerTrades(<id>, …)`, every `@Mixin` target and `compat_mods`, read from this repo's sources.
   Wildcard imports make a simple name ambiguous, so both the resolved binary names and the bare
   simple names are kept — an exact match is `covered`, a simple-name-only match is downgraded to
   `informational` rather than claimed as covered. A shim whose target is not on the compile
   classpath registers its accessor and names the target only in that accessor's
   `@ClassAccessor("<binary name>")`, in a different file from the `register` call — those
   annotations are collected into `classAccessors` and folded into the hooks before `covered` is
   built, so a reflective shim counts as coverage of its target rather than reading as a gap.
6. `cf_lookup.py --owners --candidates --mods-dir [--overrides]` → `projects.json`.
   `POST /v1/fingerprints` maps jar → project + file id, `POST /v1/mods` fetches slug and url, and one
   `GET /v1/mods/{id}/files?gameVersion=&modLoaderType=` per version/loader picks the newest file by
   `fileDate`. `modLoaderType` is `1` Forge, `4` Fabric, `5` Quilt, `6` NeoForge.
7. `build_report.py` → one markdown survey: trading-entity table, availability matrix, per-mod
   findings grouped by confidence, a "not ALICompat's business" section, and a paste-ready
   `gradle.properties` block per Minecraft version. It is an intermediate — keep it in the working
   directory and hand the user the tracker `worklist.py` writes from the same JSON.

`worklist.py` (not part of `run.sh`) turns the same JSON into the deliverable — a hand-maintained
tracker split one file per mod, so reading one mod costs one small file instead of a 130 KB report:

```bash
python3 scripts/worklist.py --gaps W/gaps.json --candidates W/candidates.json \
  --coverage W/coverage.json --projects W/projects.json --owners W/owners.json \
  --pack W/pack.json --out ../COMPATIBILITY.md
```

It writes, relative to `--out` (override the directory with `--dir`):

- `COMPATIBILITY.md` — the index: priority-ordered queue, one linked row per mod, and Gotchas.
  Keep it a link list; per-mod detail belongs in the mod's own file.
- `compatibility/<mod id>.md` — one per mod: priority/availability header, trading-entity table,
  the work checklist, and that mod's survey findings under `## Survey findings`.
- `compatibility/survey-notes.md` — what belongs to no single mod: pack metadata, "not ALICompat's
  business", the mods with nothing to do, and the `gradle.properties` blocks per Minecraft version.

Both scripts render per-mod findings and the dependency blocks through `build_report.mod_body` /
`dependency_blocks`, so the tracker and the survey never drift apart.

Priority is P1 confirmed gaps with a `1.20.1` file (port upward from there), P2 confirmed without one,
P3 trading entities, P4 dormant types only. Run it **once**: it is a seed, and re-running overwrites
ticked boxes and notes.

## `base_types.json`

Maps each ALI registry hook to the vanilla or loader base types a candidate must inherit from. Every
listed name that exists in the scanned jars is used and the rest are skipped, so one file covers all
branches — add a name when a version renames or moves a type rather than forking the file.
`excludedPackageSegments` drops `mixin`/`mixins`/`asm` packages: a mixin sits in its target's
hierarchy by construction, so every mixin on a vanilla loot or merchant class would otherwise be
reported as a candidate.

Two hook kinds have no static counterpart and come only from the log: value tooltips (any object can
appear as a trade value) and data component types (mods create `DataComponentType` *instances*, not
subclasses, so there is no hierarchy to walk).

## `overrides.json`

Maps a mod id to the CurseForge project id to use instead of the fingerprint match. Needed when a
pack ships a fork or a reupload — StoneBlock 4's CC: Tweaked jar is an unofficial NeoForge fork whose
project publishes nothing for the target versions, while the shim must compile against `cc-tweaked`
(282001). Add a line whenever a fingerprint resolves to a project that is not the upstream one.

## Reading the result

- **Availability decides feasibility, not correctness.** The matrix says a file exists for that
  Minecraft version; whether the class named in a finding survives in that build is known once the
  shim compiles.
- **Start on the oldest branch.** A shim written on `1.20.1` ports upward; one written on `26.x`
  usually cannot port down. A mod with no `1.20.1` file starts on the oldest version it does have.
- **Trading entities need a judgement call.** The scan gives the entity class and, matched from the
  mod's `entity.<modid>.*` language keys, its probable id — confirm that against the mod's
  `EntityType` registration. What it cannot give is where the offers live or how many roll per level;
  the three shapes already in this repo are a static field (Farlanders), a trade manager plus config
  (Goblin Traders) and per-profession maps (Ribbits), and `TradeLevelInfo` is a reading of the mod's
  own rules.
- **Gap kind sets the hook.** `global_loot_modifier` means an `IGlmModCompat` and a
  `GlmAccessorUtils.registerGlobalLootModifier` call; `function` / `condition` / `entry` /
  `item_listing` / `number_provider` / `ingredient` mean the matching `IServerRegistry` hook. See
  `alicompat/CLAUDE.md` for which registry each one lives in.
- **Synthetic lambdas cannot be registered.** A `$$Lambda` subject has no stable class to key on; it
  needs an accessor over the enclosing type or an upstream change, and the report flags each one.
- **`unresolved_entity_loot_table` is not a shim.** Those are `entityLootTables` entries in ALI's
  datapack configuration (`ali_config.schema.json`), a config change, not Java.

Then follow `alicompat/CLAUDE.md`'s "Adding a target mod" for each mod picked up: `compat_mods`, the
`<slug>_<loader>_dep` property from the report, the source set, the `IModCompat` implementation and
its `services` fragment.

## Extending this skill

Planned next stage: generate an ALICompat shim skeleton per finding from the field and constructor
descriptors `classindex.py` already records, so the accessor boilerplate is emitted rather than read
out of a decompiler. It belongs here as one more script consuming `candidates.json`; keep the rule
the existing stages follow — the script produces the data, the model only decides what to do with it.

A second candidate: reading `data/<modid>/loot_modifiers/` and datapack JSON for `type` ids, which
covers the id-based side (a registered type nobody's class-level scan sees) without any bytecode.
