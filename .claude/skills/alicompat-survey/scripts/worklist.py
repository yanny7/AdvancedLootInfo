#!/usr/bin/env python3
"""Seed a per-mod work tracker from a finished survey.

Generated once, then maintained by hand — re-running it overwrites ticked boxes and any
notes, so write to a new path if the tracker is already in use.

Usage: worklist.py --candidates c --coverage v --projects p --owners o --gaps g
                   --pack pack.json --out SB4_WRK.md [--report SB4.md]
"""
import argparse
import json
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
    parser.add_argument("--report", default="SB4.md")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    gaps, candidates = load(args.gaps), load(args.candidates)
    coverage, projects = load(args.coverage), load(args.projects)
    owners, pack = load(args.owners), load(args.pack)

    jars = projects["jars"]
    targets = [(t["version"], t["loaders"]) for t in projects["targets"]]
    versions = [v for v, _ in targets]
    mods = report.build_items(gaps, candidates, owners, coverage)
    mods.pop("", None)
    traders = {row["class"]: row for row in report.trader_rows(mods, candidates, coverage)}

    def meta_of(jar):
        return jars.get(jar) or candidates.get("jars", {}).get(jar, {})

    def project_of(jar):
        cf = (jars.get(jar) or {}).get("curseforge")
        return projects["projects"].get(str(cf["projectId"])) if cf else None

    rows = []
    for jar, data in mods.items():
        items = [i for i in data["items"].values() if i["confidence"] in ("confirmed", "likely")]
        if not items and not data["idOnly"]:
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

    lines = []
    add = lines.append
    add("# ALICompat work tracker — " + (pack.get("name") or "modpack"))
    add("")
    add(f"Seeded from `{args.report}` (Minecraft `{pack.get('minecraftVersion', '?')}`, "
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
    add("Per-mod steps follow `alicompat/CLAUDE.md`'s \"Adding a target mod\": the `<slug>_<loader>_dep`")
    add("property and `compat_mods` entry, the `src/compat/<slug>/` source set with the `IModCompat`")
    add("implementation and its `services` fragment, then the tooltip keys and datagen if the shim")
    add("introduces any. Maven coordinates for every version are in the survey, not repeated here.")
    add("")

    add("## Queue")
    add("")
    add("| # | Mod | Mod id | Prio | Confirmed | Likely | Versions | Done |")
    add("|---|---|---|---|---|---|---|---|")
    for index, row in enumerate(rows, 1):
        meta = row["meta"]
        name = meta.get("displayName") or meta.get("modId") or row["jar"]
        add(f"| {index} | {name} | `{meta.get('modId', '')}` | {row['priority']} | "
            f"{row['confirmed']} | {row['likely']} | {' '.join(row['available']) or '—'} | [ ] |")
    add("")

    add("## Mods")
    add("")
    for index, row in enumerate(rows, 1):
        meta, project = row["meta"], row["project"]
        name = meta.get("displayName") or meta.get("modId") or row["jar"]
        slug = re.sub(r"[^a-z0-9]", "", (meta.get("modId") or "").lower())
        add(f"### {index}. {name} (`{meta.get('modId', '')}`) — {row['priority']}")
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
            add("Trading entities — `registerTrades`, and the offer source has to be read by hand:")
            add("")
            for item in row["traders"]:
                trader = traders.get(item["subject"], {})
                ids = ", ".join(f"`{i}`" for i in trader.get("ids", [])) or "id unconfirmed"
                guesses = trader.get("guesses", [])
                suffix = f" (guesses: {', '.join(f'`{g}`' for g in guesses[:3])})" if guesses else ""
                add(f"- [ ] `{item['subject']}` — {ids}{suffix}")
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

    add("## Gotchas")
    add("")
    add("Only what this campaign turns up. The standing rules — target mods staying compile-only and")
    add("off the run classpath, Fabric datagen constructing every mod's entrypoint, the `ServiceLoader`")
    add("iterator walk, `showEmpty()` on a branch that can come out empty — are in `alicompat/CLAUDE.md`")
    add("and are not repeated here.")
    add("")
    add("- CC: Tweaked in this pack is an unofficial NeoForge fork; the shim compiles against upstream")
    add("  `cc-tweaked` (project `282001`), which is why `overrides.json` in the survey skill pins it.")
    add("")

    with open(args.out, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines).rstrip() + "\n")
    print(f"wrote {args.out} ({len(rows)} mods, {len(lines)} lines)")


if __name__ == "__main__":
    main()
