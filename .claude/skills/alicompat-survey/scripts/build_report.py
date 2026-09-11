#!/usr/bin/env python3
"""Join every stage into one markdown survey.

Inputs: gaps.json (what the run logged), candidates.json (what the jars could produce),
coverage.json (what this repo already registers), projects.json (CurseForge).

Usage: build_report.py --gaps g --candidates c --coverage v --projects p --owners o
                       --pack pack.json --out W/survey.md
"""
import argparse
import json
import re
from collections import OrderedDict

LAMBDA = re.compile(r"\$\$Lambda(/0x[0-9a-f]+)?$")
FOREIGN_PREFIXES = ("net.minecraft.", "net.neoforged.", "net.minecraftforge.", "net.fabricmc.",
                    "com.mojang.", "com.google.", "jdk.", "java.", "it.unimi.")

# log category -> (hook it corresponds to, human title). A hook of None has no static
# counterpart: the scan cannot enumerate those, only the log can name them.
LOG_CATEGORIES = OrderedDict([
    ("global_loot_modifier", ("global_loot_modifier", "Global Loot Modifiers")),
    ("auto_glm_unresolved", (None, "Auto-GLM without a resolvable destination")),
    ("entry_factory", ("entry", "Loot entry factories")),
    ("function_tooltip", ("function", "Loot function tooltips")),
    ("condition_tooltip", ("condition", "Loot condition tooltips")),
    ("number_provider", ("number_provider", "Number providers")),
    ("trade_item_listing", ("item_listing", "Trade item listings")),
    ("trade_listing_fallback", ("item_listing", "Trade listings on the MerchantOffer fallback")),
    ("item_sub_predicate_tooltip", ("item_sub_predicate", "Item sub-predicate tooltips")),
    ("value_tooltip", ("value", "Value tooltips")),
    ("data_component_type_tooltip", ("data_component", "Data component type tooltips")),
    ("unresolved_entity_loot_table", (None, "Entity loot tables needing an entityLootTables entry")),
    ("unexpected_loot_action", (None, "Unexpected loot actions")),
])

CONFIDENCE_ORDER = ["confirmed", "likely", "informational", "covered"]
CONFIDENCE_TITLE = {
    "confirmed": "Confirmed — this run fed it to ALI and nothing covered it",
    "likely": "Likely — a concrete type the mod can produce, never exercised by this run",
    "informational": "Informational — abstract, non-public, or already named in this repo",
    "covered": "Already registered here",
}


def load(path):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def is_foreign(subject):
    return subject.startswith(FOREIGN_PREFIXES)


def simple_name(subject):
    return LAMBDA.sub("", subject).rsplit(".", 1)[-1].split("$", 1)[0]


def coverage_of(subject, hooks, coverage):
    """'covered' on an exact match, 'maybe' when only the simple name is registered."""
    if subject in coverage.get("mixins", {}):
        return "covered", "mixin " + coverage["mixins"][subject]
    for hook in hooks or coverage.get("byHook", {}):
        entry = coverage.get("byHook", {}).get(hook, {})
        if subject in entry:
            return "covered", entry[subject]
    for hook in hooks or coverage.get("simpleNames", {}):
        entry = coverage.get("simpleNames", {}).get(hook, {})
        if simple_name(subject) in entry:
            return "maybe", entry[simple_name(subject)]
    return "gap", ""


def build_items(gaps, candidates, owners, coverage):
    """One dict per (jar, subject), merging log evidence with static evidence."""
    mods = OrderedDict()

    def slot(jar):
        return mods.setdefault(jar, {"items": OrderedDict(), "traders": [], "idOnly": OrderedDict()})

    for category, subjects in gaps["categories"].items():
        hook, _ = LOG_CATEGORIES.get(category, (None, category))
        for subject in subjects:
            if ":" in subject:
                jar = owners["namespaceOwners"].get(subject.split(":", 1)[0])
                if jar:
                    slot(jar)["idOnly"].setdefault(category, []).append(subject)
                else:
                    slot("")["idOnly"].setdefault(category, []).append(subject)
                continue
            jar = owners["owners"].get(subject)
            if jar is None:
                slot("")["idOnly"].setdefault(category, []).append(subject)
                continue
            item = slot(jar)["items"].setdefault(subject, {
                "subject": subject, "hooks": set(), "logCategories": [],
                "evidence": set(), "flags": set(), "detail": {},
            })
            item["evidence"].add("log")
            item["logCategories"].append(category)
            if hook:
                item["hooks"].add(hook)
            if LAMBDA.search(subject):
                item["flags"].add("lambda")

    for jar, entries in candidates.get("candidates", {}).items():
        for entry in entries:
            subject = entry["class"]
            item = slot(jar)["items"].setdefault(subject, {
                "subject": subject, "hooks": set(), "logCategories": [],
                "evidence": set(), "flags": set(), "detail": {},
            })
            item["evidence"].add("static")
            item["hooks"].update(entry["hooks"])
            for flag in ("abstract", "interface", "enum"):
                if entry.get(flag):
                    item["flags"].add(flag)
            if not entry.get("public"):
                item["flags"].add("non-public")
            item["detail"] = {"fields": entry.get("fields", []), "constructors": entry.get("constructors", [])}
            if "trader_entity" in entry["hooks"]:
                slot(jar)["traders"].append(entry)

    for jar, data in mods.items():
        for item in data["items"].values():
            status, where = coverage_of(item["subject"], item["hooks"], coverage)
            item["status"] = status
            item["coveredBy"] = where
            if status == "covered":
                item["confidence"] = "covered"
            elif "log" in item["evidence"]:
                item["confidence"] = "confirmed"
            elif item["flags"] & {"abstract", "interface", "non-public"} or status == "maybe":
                item["confidence"] = "informational"
            else:
                item["confidence"] = "likely"
    return mods


def trader_rows(mods, candidates, coverage):
    rows = []
    compat = set(coverage.get("compatModIds", []))
    registered = set(coverage.get("traders", {}))
    for jar, data in mods.items():
        modid = candidates.get("jars", {}).get(jar, {}).get("modId", "")
        for entry in data["traders"]:
            if entry.get("abstract") or entry.get("interface"):
                continue
            ids = entry.get("entityIdExact") or []
            guesses = entry.get("entityIdCandidates") or []
            if any(i in registered for i in ids):
                status = "registered"
            elif modid in compat:
                status = "shim exists — verify"
            else:
                status = "missing"
            rows.append({
                "jar": jar, "modId": modid, "class": entry["class"],
                "ids": ids, "guesses": guesses, "status": status,
            })
    order = {"missing": 0, "shim exists — verify": 1, "registered": 2}
    rows.sort(key=lambda r: (order[r["status"]], r["modId"], r["class"]))
    return rows


def property_slug(mod_id):
    return re.sub(r"[^a-z0-9]", "", (mod_id or "").lower())


def mod_body(jar, meta, project, data, versions):
    """The survey's per-mod findings, without its heading — shared with the work tracker."""
    lines = []
    add = lines.append
    add(f"- Jar in pack: `{jar}`" + (f" (version `{meta['version']}`)" if meta.get("version") else ""))
    if project:
        add(f"- CurseForge: `{project['slug']}` project `{project['projectId']}` — {project.get('url', '')}")
        for version, loaders in project.get("maven", {}).items():
            for loader, coordinate in sorted(loaders.items()):
                add(f"  - `{version}` / {loader}: `{coordinate}`")
        missing = [v for v in versions if not project.get("versions", {}).get(v)]
        if missing:
            add(f"  - no file for: {', '.join(f'`{v}`' for v in missing)}")
    else:
        add("- CurseForge: **not matched by fingerprint** — needs a manual project id")
    add("")

    grouped = OrderedDict((c, []) for c in CONFIDENCE_ORDER)
    for item in data["items"].values():
        grouped[item["confidence"]].append(item)
    for confidence, items in grouped.items():
        if not items:
            continue
        add(f"**{CONFIDENCE_TITLE[confidence]}**")
        add("")
        for item in sorted(items, key=lambda i: i["subject"]):
            hooks = ", ".join(sorted(item["hooks"])) or "—"
            notes = []
            if "lambda" in item["flags"]:
                notes.append("synthetic lambda, no class to register")
            for flag in ("abstract", "interface", "non-public", "enum"):
                if flag in item["flags"]:
                    notes.append(flag)
            if item["status"] == "maybe":
                notes.append(f"same simple name registered by {item['coveredBy']}")
            elif item["status"] == "covered":
                notes.append(f"registered by {item['coveredBy']}")
            if item["evidence"] == {"log"}:
                notes.append("log only")
            elif item["evidence"] == {"static"}:
                notes.append("static only")
            suffix = f" — {'; '.join(notes)}" if notes else ""
            add(f"- `{item['subject']}` [{hooks}]{suffix}")
        add("")

    for category, subjects in data["idOnly"].items():
        _, title = LOG_CATEGORIES.get(category, (None, category))
        add(f"**{title}** (from the log, by id)")
        add("")
        for subject in sorted(subjects):
            add(f"- `{subject}`")
        add("")
    return lines


def dependency_blocks(targets, order, meta_of, project_of):
    lines = []
    add = lines.append
    for version, loaders in targets:
        rows = []
        for jar in order:
            project = project_of(jar)
            if not project:
                continue
            found = project.get("maven", {}).get(version, {})
            slug = property_slug(meta_of(jar).get("modId"))
            for loader in loaders:
                if loader in found:
                    rows.append(f"{slug}_{loader}_dep={found[loader]}")
        add(f"### `{version}`")
        add("")
        if rows:
            add("```properties")
            lines.extend(sorted(rows))
            add("```")
        else:
            add("No mod in this survey has a file for this version.")
        add("")
    return lines


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--gaps", required=True)
    parser.add_argument("--candidates", required=True)
    parser.add_argument("--coverage", required=True)
    parser.add_argument("--projects", required=True)
    parser.add_argument("--owners", required=True)
    parser.add_argument("--pack")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    gaps = load(args.gaps)
    candidates = load(args.candidates)
    coverage = load(args.coverage)
    projects = load(args.projects)
    owners = load(args.owners)
    pack = load(args.pack) if args.pack else {}

    jars = projects["jars"]
    targets = [(t["version"], t["loaders"]) for t in projects["targets"]]
    versions = [v for v, _ in targets]
    mods = build_items(gaps, candidates, owners, coverage)
    foreign = mods.pop("", {"idOnly": OrderedDict()})

    def meta_of(jar):
        return jars.get(jar) or candidates.get("jars", {}).get(jar, {})

    def project_of(jar):
        cf = (jars.get(jar) or {}).get("curseforge")
        return projects["projects"].get(str(cf["projectId"])) if cf else None

    def actionable(jar):
        return any(i["confidence"] in ("confirmed", "likely") for i in mods[jar]["items"].values()) \
            or bool(mods[jar]["idOnly"])

    everything = sorted(mods, key=lambda j: (meta_of(j).get("modId") or j).lower())
    order = [j for j in everything if actionable(j)]
    quiet = [j for j in everything if not actionable(j)]
    traders = trader_rows(mods, candidates, coverage)
    tally = OrderedDict((c, 0) for c in CONFIDENCE_ORDER)
    for data in mods.values():
        for item in data["items"].values():
            tally[item["confidence"]] += 1

    lines = []
    add = lines.append
    add("# Modpack ALI gap survey")
    add("")
    if pack:
        add(f"- Pack: **{pack.get('name', '?')}** — Minecraft `{pack.get('minecraftVersion', '?')}`, "
            f"{pack.get('loader', '?')} `{pack.get('loaderVersion', '')}`, {pack.get('jarCount', '?')} jars")
        add(f"- Log: `{pack.get('log') or 'none — static scan only'}`")
        add(f"- Class hierarchy from: `{pack.get('minecraftJar') or 'MISSING — static results unreliable'}`")
    add(f"- Classes indexed: **{candidates.get('classesIndexed', 0)}** across "
        f"{candidates.get('jarsIndexed', 0)} jars")
    add(f"- Mods with something to do: **{len(order)}**"
        + (f" (plus {len(quiet)} whose findings are all covered or informational)" if quiet else ""))
    add("- Findings: " + ", ".join(f"**{v}** {k}" for k, v in tally.items() if v))
    add("- CurseForge availability probed for: " + ", ".join(f"`{v}` ({'/'.join(l)})" for v, l in targets))
    add("")
    add("Two kinds of evidence. The **log** shows what this run of the pack actually handed ALI and")
    add("ALI could not render — precise, but only what the pack exercised. The **static scan** walks")
    add("every class in every jar and finds what a mod *can* hand ALI, including the pull-model gaps")
    add("no log will ever show: ALI asks for a trader's offers by entity id, so a trader nobody")
    add("registered produces no warning at all. Availability is the newest published CurseForge file")
    add("for that Minecraft version and loader; it does not promise the class still exists there.")
    add("")

    add("## Trading entities")
    add("")
    if traders:
        add("`registerTrades` is a pull: ALI only asks about ids some plugin declared. Nothing here is")
        add("visible in a log, so this table is the whole picture. An id under *guessed* was matched")
        add("from the mod's `entity.<modid>.*` language keys and needs confirming against the mod's")
        add("`EntityType` registration.")
        add("")
        add("| Mod | Entity class | Entity id | Guessed ids | State |")
        add("|---|---|---|---|---|")
        for row in traders:
            ids = ", ".join(f"`{i}`" for i in row["ids"]) or "—"
            guesses = ", ".join(f"`{g}`" for g in row["guesses"][:4]) or "—"
            add(f"| `{row['modId']}` | `{row['class']}` | {ids} | {guesses} | {row['status']} |")
        add("")
        add("Writing one means a `registerTrades(id, listings, levelInfo)` call, and the listings")
        add("supplier is the part that cannot be derived: the three shapes already in this repo are a")
        add("static field (Farlanders), a trade manager plus config (Goblin Traders) and per-profession")
        add("maps (Ribbits). `TradeLevelInfo` — trades per level and per-level chance — is a judgement")
        add("call about the mod's own rules, not something a scan can read off.")
    else:
        add("No merchant entity found outside what is already registered.")
    add("")

    add("## Availability matrix")
    add("")
    add("| Mod | Mod id | CurseForge | " + " | ".join(f"`{v}`" for v in versions) + " | Confirmed | Likely |")
    add("|---|---|---|" + "---|" * (len(versions) + 2))
    for jar in order:
        meta = meta_of(jar)
        project = project_of(jar)
        name = meta.get("displayName") or meta.get("modId") or jar
        slug = f"[{project['slug']}]({project['url']})" if project and project.get("url") else "—"
        cells = []
        for version in versions:
            found = (project or {}).get("versions", {}).get(version, {})
            cells.append("+".join(sorted(found)) if found else "—")
        items = mods[jar]["items"].values()
        confirmed = sum(1 for i in items if i["confidence"] == "confirmed")
        likely = sum(1 for i in items if i["confidence"] == "likely")
        add(f"| {name} | `{meta.get('modId', '')}` | {slug} | " + " | ".join(cells)
            + f" | {confirmed} | {likely} |")
    add("")

    add("## Per-mod findings")
    add("")
    for jar in order:
        meta = meta_of(jar)
        project = project_of(jar)
        name = meta.get("displayName") or meta.get("modId") or jar
        add(f"### {name} (`{meta.get('modId', '')}`)")
        add("")
        lines.extend(mod_body(jar, meta_of(jar), project_of(jar), mods[jar], versions))

    if foreign["idOnly"]:
        add("## Not ALICompat's business")
        add("")
        add("Vanilla, loader and library classes ALI met through some mod's data, plus ids belonging to")
        add("no jar in the pack. These belong in `ali/common` or a loader module, are ALI configuration,")
        add("or are noise (a JDK proxy, a captured lambda).")
        add("")
        for category, subjects in foreign["idOnly"].items():
            _, title = LOG_CATEGORIES.get(category, (None, category))
            add(f"**{title}**")
            add("")
            for subject in sorted(subjects):
                add(f"- `{subject}`")
            add("")

    if quiet:
        add("## Nothing to do")
        add("")
        add("Every finding in these mods is already registered here, or is abstract / non-public and")
        add("cannot be a registration target on its own.")
        add("")
        for jar in quiet:
            meta = meta_of(jar)
            counts = OrderedDict()
            for item in mods[jar]["items"].values():
                counts[item["confidence"]] = counts.get(item["confidence"], 0) + 1
            summary = ", ".join(f"{v} {k}" for k, v in counts.items()) or "no findings"
            add(f"- `{meta.get('modId', '')}` — {summary}")
        add("")

    add("## `gradle.properties` dependency block")
    add("")
    add("Suggested property names (mod id with separators stripped); a slug already in `supported_mods.json`")
    add("keeps its existing name. Pick the block for the branch you are on.")
    add("")
    lines.extend(dependency_blocks(targets, order, meta_of, project_of))

    with open(args.out, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines).rstrip() + "\n")
    print(f"wrote {args.out} ({len(order)} mods, {len(lines)} lines, "
          + ", ".join(f"{v} {k}" for k, v in tally.items() if v) + ")")


if __name__ == "__main__":
    main()
