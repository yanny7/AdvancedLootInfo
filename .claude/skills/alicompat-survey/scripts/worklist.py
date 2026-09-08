#!/usr/bin/env python3
"""Seed a per-mod work tracker from a finished survey.

Writes an index (`--out`) plus one file per mod in a sibling `compatibility/` directory,
each merging that mod's survey findings with its work checklist. Generated once, then
maintained by hand — re-running it overwrites ticked boxes and any notes, so write to a
new directory if the tracker is already in use.

Usage: worklist.py --candidates c --coverage v --projects p --owners o --gaps g
                   --pack pack.json --out COMPATIBILITY.md [--dir compatibility]
"""
import argparse
import json
import os
import re
from collections import OrderedDict

import build_report as report


def load(path):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def priority(has_confirmed, has_trader, oldest):
    """P1 first: a confirmed gap on the oldest branch ports upward, so it is worth most."""
    if has_confirmed and oldest == "1.20.1":
        return "P1"
    if has_confirmed:
        return "P2"
    if has_trader:
        return "P3"
    return "P4"


PRIORITY_NOTE = OrderedDict([
    ("P1", "Confirmed gaps and a `1.20.1` file — write the shim here and port it upward."),
    ("P2", "Confirmed gaps but no `1.20.1` file — start on the oldest version the mod has."),
    ("P3", "A trading entity, which no log can ever report. Needs the offer source read by hand."),
    ("P4", "Only dormant types: nothing this run exercised, but a datapack can reach them."),
])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--gaps", required=True)
    parser.add_argument("--candidates", required=True)
    parser.add_argument("--coverage", required=True)
    parser.add_argument("--projects", required=True)
    parser.add_argument("--owners", required=True)
    parser.add_argument("--pack", required=True)
    parser.add_argument("--out", required=True,
                        help="index markdown; per-mod files land beside it in --dir")
    parser.add_argument("--dir", default=None,
                        help="per-mod directory (default: `compatibility` next to --out)")
    args = parser.parse_args()

    gaps, candidates = load(args.gaps), load(args.candidates)
    coverage, projects = load(args.coverage), load(args.projects)
    owners, pack = load(args.owners), load(args.pack)

    jars = projects["jars"]
    targets = [(t["version"], t["loaders"]) for t in projects["targets"]]
    versions = [v for v, _ in targets]
    mods = report.build_items(gaps, candidates, owners, coverage)
    foreign = mods.pop("", {"idOnly": OrderedDict()})
    traders = {row["class"]: row for row in report.trader_rows(mods, candidates, coverage)}

    def meta_of(jar):
        return jars.get(jar) or candidates.get("jars", {}).get(jar, {})

    def project_of(jar):
        cf = (jars.get(jar) or {}).get("curseforge")
        return projects["projects"].get(str(cf["projectId"])) if cf else None

    rows, quiet = [], []
    for jar, data in mods.items():
        items = [i for i in data["items"].values() if i["confidence"] in ("confirmed", "likely")]
        if not items and not data["idOnly"]:
            quiet.append(jar)
            continue
        project = project_of(jar) or {}
        available = [v for v in versions if project.get("versions", {}).get(v)]
        confirmed = [i for i in items if i["confidence"] == "confirmed"]
        trader_items = [i for i in items if "trader_entity" in i["hooks"]]
        rows.append({
            "jar": jar,
            "meta": meta_of(jar),
            "project": project,
            "available": available,
            "oldest": available[0] if available else "",
            "items": sorted(items, key=lambda i: (i["confidence"] != "confirmed", i["subject"])),
            "idOnly": data["idOnly"],
            "priority": priority(bool(confirmed), bool(trader_items), available[0] if available else ""),
            "confirmed": len(confirmed),
            "likely": len(items) - len(confirmed),
            "traders": trader_items,
        })

    rank = {"P1": 0, "P2": 1, "P3": 2, "P4": 3}
    rows.sort(key=lambda r: (rank[r["priority"]], -r["confirmed"], -r["likely"],
                             (r["meta"].get("modId") or r["jar"]).lower()))

    out_dir = args.dir or os.path.join(os.path.dirname(os.path.abspath(args.out)), "compatibility")
    os.makedirs(out_dir, exist_ok=True)
    link = os.path.relpath(out_dir, os.path.dirname(os.path.abspath(args.out)))

    def file_slug(mod_id, jar):
        return re.sub(r"[^a-z0-9_.-]", "_", (mod_id or jar).lower())

    # ---- one file per mod: survey findings and work checklist, merged ----
    for index, row in enumerate(rows, 1):
        meta, project = row["meta"], row["project"]
        name = meta.get("displayName") or meta.get("modId") or row["jar"]
        mod_id = meta.get("modId", "")
        slug = report.property_slug(mod_id)
        out = [f"# {name} (`{mod_id}`)", ""]
        add = out.append
        add(f"- Priority: **{row['priority']}** · confirmed {row['confirmed']} · likely {row['likely']}")
        add(f"- Versions: {' '.join(row['available']) or '—'}")
        add("- Done: [ ]")
        if project and project.get("url"):
            add(f"- CurseForge: [{project['slug']}]({project['url']})")
        add("- Files: " + " · ".join(
            f"`{v}`: " + ("+".join(sorted((project or {}).get('versions', {}).get(v, {}))) or "—")
            for v in versions))
        add("")

        if row["traders"]:
            add("## Trading entities")
            add("")
            add("`registerTrades` is a pull, so no log can report these. An id under *guessed* was")
            add("matched from the mod's `entity.<modid>.*` language keys and needs confirming against")
            add("its `EntityType` registration. The listings supplier is the part no scan can derive.")
            add("")
            add("| Entity class | Entity id | Guessed ids | State |")
            add("|---|---|---|---|")
            for item in row["traders"]:
                trader = traders.get(item["subject"], {})
                ids = ", ".join(f"`{i}`" for i in trader.get("ids", [])) or "—"
                guesses = ", ".join(f"`{g}`" for g in trader.get("guesses", [])[:4]) or "—"
                add(f"| `{item['subject']}` | {ids} | {guesses} | {trader.get('status', '')} |")
            add("")

        add("## Work")
        add("")
        if project:
            add(f"- CurseForge `{project.get('slug', '')}` project `{project.get('projectId', '')}`, "
                f"files on: {', '.join(f'`{v}`' for v in row['available']) or 'none of the targets'}")
            oldest = row["oldest"]
            if oldest:
                for loader, coordinate in sorted(project.get("maven", {}).get(oldest, {}).items()):
                    add(f"- `{oldest}` / {loader}: `{slug}_{loader}_dep={coordinate}`")
        else:
            add("- CurseForge: **not matched** — find the project id before anything else")
        add("- [ ] dependency property + `compat_mods` entry")
        add("- [ ] source set + `IModCompat` + `services` fragment")
        add("- [ ] tooltip keys + datagen (only if the shim introduces any)")
        add("- [ ] changelog entry")
        add("")
        if row["traders"]:
            add("Trading entities — the offer source has to be read by hand:")
            add("")
            for item in row["traders"]:
                trader = traders.get(item["subject"], {})
                ids = ", ".join(f"`{i}`" for i in trader.get("ids", [])) or "id unconfirmed"
                add(f"- [ ] `{item['subject']}` — {ids}")
            add("")
        others = [i for i in row["items"] if "trader_entity" not in i["hooks"]]
        lambdas = [i for i in others if "lambda" in i["flags"]]
        others = [i for i in others if "lambda" not in i["flags"]]
        if others:
            add("Findings:")
            add("")
            for item in others:
                hooks = ", ".join(sorted(item["hooks"])) or "—"
                flags = sorted(item["flags"])
                suffix = f" — {', '.join(flags)}" if flags else ""
                add(f"- [ ] `{item['subject']}` [{hooks}] ({item['confidence']}){suffix}")
            add("")
        if lambdas:
            enclosing = sorted({re.sub(r"\$\$Lambda.*$", "", i["subject"]) for i in lambdas})
            add(f"- [ ] {len(lambdas)} synthetic lambdas, no class to key on — one accessor over the "
                "enclosing type covers them: " + ", ".join(f"`{e}`" for e in enclosing))
            add("")
        if row["idOnly"]:
            for category, subjects in row["idOnly"].items():
                _, title = report.LOG_CATEGORIES.get(category, (None, category))
                add(f"{title}: " + ", ".join(f"`{s}`" for s in sorted(subjects)))
            add("")
        add("Notes:")
        add("")
        add("## Survey findings")
        add("")
        out.extend(report.mod_body(row["jar"], meta, project, mods[row["jar"]], versions))

        row["file"] = file_slug(mod_id, row["jar"]) + ".md"
        with open(os.path.join(out_dir, row["file"]), "w", encoding="utf-8") as handle:
            handle.write("\n".join(out).rstrip() + "\n")

    # ---- survey notes: everything that belongs to no single mod ----
    notes = [f"# Survey notes — {pack.get('name') or 'modpack'}", ""]
    add = notes.append
    add(f"- Pack: **{pack.get('name', '?')}** — Minecraft `{pack.get('minecraftVersion', '?')}`, "
        f"{pack.get('loader', '?')} `{pack.get('loaderVersion', '')}`, {pack.get('jarCount', '?')} jars")
    add(f"- Log: `{pack.get('log') or 'none — static scan only'}`")
    add(f"- Class hierarchy from: `{pack.get('minecraftJar') or 'MISSING — static results unreliable'}`")
    add(f"- Classes indexed: **{candidates.get('classesIndexed', 0)}** across "
        f"{candidates.get('jarsIndexed', 0)} jars")
    add(f"- Mods with something to do: **{len(rows)}**"
        + (f" (plus {len(quiet)} whose findings are all covered or informational)" if quiet else ""))
    add(f"- Findings queued: **{sum(len(r['items']) for r in rows)}** "
        f"({sum(r['confirmed'] for r in rows)} confirmed, {sum(r['likely'] for r in rows)} likely)")
    add("- CurseForge availability probed for: "
        + ", ".join(f"`{v}` ({'/'.join(l)})" for v, l in targets))
    add("")
    add("Two kinds of evidence. The **log** shows what this run of the pack actually handed ALI and")
    add("ALI could not render — precise, but only what the pack exercised. The **static scan** walks")
    add("every class in every jar and finds what a mod *can* hand ALI, including the pull-model gaps")
    add("no log will ever show: ALI asks for a trader's offers by entity id, so a trader nobody")
    add("registered produces no warning at all. Availability is the newest published CurseForge file")
    add("for that Minecraft version and loader; it does not promise the class still exists there.")
    add("")
    if foreign["idOnly"]:
        add("## Not ALICompat's business")
        add("")
        add("Vanilla, loader and library classes ALI met through some mod's data, plus ids belonging to")
        add("no jar in the pack. These belong in `ali/common` or a loader module, are ALI configuration,")
        add("or are noise (a JDK proxy, a captured lambda).")
        add("")
        for category, subjects in foreign["idOnly"].items():
            _, title = report.LOG_CATEGORIES.get(category, (None, category))
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
        for jar in sorted(quiet, key=lambda j: (meta_of(j).get("modId") or j).lower()):
            counts = OrderedDict()
            for item in mods[jar]["items"].values():
                counts[item["confidence"]] = counts.get(item["confidence"], 0) + 1
            summary = ", ".join(f"{v} {k}" for k, v in counts.items()) or "no findings"
            add(f"- `{meta_of(jar).get('modId', '')}` — {summary}")
        add("")
    add("## `gradle.properties` dependency block")
    add("")
    add("Suggested property names (mod id with separators stripped); a slug already in `compat_mods`")
    add("keeps its existing name. Pick the block for the branch you are on.")
    add("")
    notes.extend(report.dependency_blocks(targets, [r["jar"] for r in rows], meta_of, project_of))
    with open(os.path.join(out_dir, "survey-notes.md"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(notes).rstrip() + "\n")

    # ---- index ----
    lines = []
    add = lines.append
    add("# ALICompat compatibility tracker — " + (pack.get("name") or "modpack"))
    add("")
    add(f"Seeded from a survey of the pack (Minecraft `{pack.get('minecraftVersion', '?')}`, "
        f"{pack.get('loader', '?')}). Hand-maintained from here: tick boxes, add notes, record")
    add("gotchas. Regenerating overwrites it.")
    add("")
    add(f"- Mods queued: **{len(rows)}**")
    for key, note in PRIORITY_NOTE.items():
        count = sum(1 for r in rows if r["priority"] == key)
        if count:
            add(f"  - **{key}** ({count}) — {note}")
    add(f"- Findings queued: **{sum(len(r['items']) for r in rows)}** "
        f"({sum(r['confirmed'] for r in rows)} confirmed, {sum(r['likely'] for r in rows)} likely)")
    add("")
    add(f"One file per mod under [`{link}/`]({link}/), each carrying that mod's survey findings and its")
    add("work checklist. Pack metadata, the mods with nothing to do, the classes that are not")
    add(f"ALICompat's business and the full `gradle.properties` dependency blocks are in the")
    add(f"[survey notes]({link}/survey-notes.md).")
    add("")
    add("Per-mod steps follow `alicompat/CLAUDE.md`'s \"Adding a target mod\": the `<slug>_<loader>_dep`")
    add("property and `compat_mods` entry, the `src/compat/<slug>/` source set with the `IModCompat`")
    add("implementation and its `services` fragment, then the tooltip keys and datagen if the shim")
    add("introduces any.")
    add("")
    add("## Queue")
    add("")
    add("| # | Mod | Mod id | Prio | Confirmed | Likely | Versions | Done |")
    add("|---|---|---|---|---|---|---|---|")
    for index, row in enumerate(rows, 1):
        meta = row["meta"]
        name = meta.get("displayName") or meta.get("modId") or row["jar"]
        add(f"| {index} | [{name}]({link}/{row['file']}) | `{meta.get('modId', '')}` | "
            f"{row['priority']} | {row['confirmed']} | {row['likely']} | "
            f"{' '.join(row['available']) or '—'} | [ ] |")
    add("")
    add("## Gotchas")
    add("")
    add("Only what this campaign turns up. The standing rules — target mods staying compile-only and")
    add("off the run classpath, Fabric datagen constructing every mod's entrypoint, the `ServiceLoader`")
    add("iterator walk, `showEmpty()` on a branch that can come out empty — are in `alicompat/CLAUDE.md`")
    add("and are not repeated here.")
    add("")

    with open(args.out, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines).rstrip() + "\n")
    print(f"wrote {args.out} + {len(rows)} mod files and survey-notes.md in {out_dir}")


if __name__ == "__main__":
    main()
