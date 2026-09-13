#!/usr/bin/env python3
"""Resolves the mods listed in scripts/supported_mods.json against CurseForge for this branch's
Minecraft version, reports what is outdated, newly available or gone, and can regenerate
the generated ALICompat block in gradle.properties together with missing shim skeletons."""

import argparse
import re
import sys
from pathlib import Path

import shimscan

from modpack import (
    MOD_LOADER_TYPE,
    PROJECT_DIR,
    RELEASE_TYPES,
    SUPPORTED_MODS_FILE,
    CurseForge,
    parse_project,
    pick_project,
    progress,
    progress_done,
    read_enabled_mods,
    read_env_secret,
    read_gradle_properties,
    read_pinned_deps,
    read_supported_mods,
)

BLOCK_START = "# --- generated from supported_mods.json by check_versions.py, do not edit by hand ---"
BLOCK_END = "# --- end of generated block ---"
LEGACY_BLOCK_START = "# ALICompat target mods"
CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods"
SERVICE_FILE = "com.yanny.alicompat.IModCompat"
REPORT_FILE = PROJECT_DIR / "build" / "compat_scan.txt"


def source_set(loader: str, key: str):
    return PROJECT_DIR / "alicompat" / loader / "src" / "compat" / key


def has_source_set(platforms: list, key: str):
    return any(source_set(loader, key).is_dir() for loader in platforms)


def describe(file: dict):
    return f"{file['id']} {RELEASE_TYPES.get(file['releaseType'], 'unknown'):7} {file['fileDate'][:10]}  {file['displayName']}"


def class_name(entry: dict):
    sibling = sorted(PROJECT_DIR.glob(f"alicompat/*/src/compat/{entry['key']}/java/**/*Compat.java"))

    if sibling:
        return sibling[0].stem[:-len("Compat")]

    name = re.sub(r"^The\s+", "", entry["name"])
    name = re.sub(r"[^0-9A-Za-z ]", " ", name)
    return "".join(word[0].upper() + word[1:] for word in name.split()) or entry["key"].capitalize()


def scaffold(loader: str, entry: dict):
    key = entry["key"]
    root = source_set(loader, key)
    package = f"com.yanny.alicompat.compat.{key}"
    java = root / "java" / Path(package.replace(".", "/"))
    compat = f"{class_name(entry)}Compat"

    java.mkdir(parents=True, exist_ok=True)
    (java / "package-info.java").write_text(
        f"@ParametersAreNonnullByDefault\npackage {package};\n\nimport javax.annotation.ParametersAreNonnullByDefault;\n",
        encoding="utf-8")
    (java / f"{compat}.java").write_text(
        f"package {package};\n\n"
        "import com.yanny.alicompat.IModCompat;\n"
        "import org.jetbrains.annotations.NotNull;\n\n"
        f"public class {compat} implements IModCompat {{\n"
        f'    static final String MOD_ID = "{entry["mod_ids"][0]}";\n\n'
        "    @NotNull\n"
        "    @Override\n"
        "    public String targetModId() {\n"
        "        return MOD_ID;\n"
        "    }\n"
        "}\n",
        encoding="utf-8")

    services = root / "services"
    services.mkdir(parents=True, exist_ok=True)
    (services / SERVICE_FILE).write_text(f"{package}.{compat}\n", encoding="utf-8")

    return root.relative_to(PROJECT_DIR)


def scanned_file(result: dict, update: bool):
    """The file the shim source set is measured against: what it is pinned to, or what
    --update has just repinned it to."""
    if update or "current" not in result:
        return result.get("latest")

    return result["current"]


def download(client: CurseForge, pin: dict, file: dict):
    jar = shimscan.cached_jar(pin)

    if jar or not file or not file.get("downloadUrl"):
        return jar

    return shimscan.download_jar(client.session, pin, file["downloadUrl"])


def shim_jars(args, clients, results, deps, pinned, rows):
    """The jar every shim in `rows` is measured against, downloaded once per file id, plus the
    (pinned, newest) pair of whatever has a newer file than the one it is pinned to."""
    resolved = {(result["key"], result["loader"]): result for result in results}
    jars = {}
    versions = {}

    for index, (key, loader) in enumerate(rows, start=1):
        progress(index, len(rows), f"{key} ({loader})")
        pin = deps.get((key, loader)) if args.update else pinned[loader].get(key) or deps.get((key, loader))
        result = resolved.get((key, loader))

        if pin:
            jars[(key, loader)] = download(clients[loader], pin, scanned_file(result, args.update) if result else None)

        if not result or result["state"] != "outdated" or key not in pinned[loader]:
            continue

        old_jar = download(clients[loader], pinned[loader][key], result.get("current"))
        new_jar = download(clients[loader], {"slug": result["slug"], "project_id": result["project_id"],
                                             "file_id": result["latest"]["id"]}, result["latest"])

        if old_jar and new_jar:
            versions[(key, loader)] = (old_jar, new_jar)

    progress_done()
    return jars, versions


def scan_shims(args, entries, enabled, checked_loaders, clients, results, deps, pinned, minecraft_version):
    base_types = shimscan.load_base_types()
    titles = {hook: spec["title"] for hook, spec in base_types["hooks"].items()}
    rows = [
        (key, loader) for key in sorted(entries) for loader in checked_loaders
        if source_set(loader, key).is_dir() and key in enabled
    ]
    dormant = [
        (key, loader) for key in sorted(entries) for loader in checked_loaders
        if source_set(loader, key).is_dir() and key not in enabled
    ]
    width = max((len(f"{key} ({loader})") for key, loader in rows + dormant), default=0)
    jars, versions = shim_jars(args, clients, results, deps, pinned, rows)
    renames = shimscan.intermediary_names(minecraft_version)
    lines = []
    counts = {"ok": 0, "changed": 0, "skipped": len(dormant)}

    for loader in checked_loaders:
        minecraft = shimscan.minecraft_jar(loader, minecraft_version)
        loaded = [jar for (_, owner), jar in jars.items() if owner == loader and jar]

        if not minecraft:
            lines.append(f"! no {loader} Minecraft jar under .gradle/loom-cache — run a gradle sync first, "
                         f"every hook reached through a vanilla class is invisible without it")

        index, owned = shimscan.build_index(loaded + ([minecraft] if minecraft else []))
        bases = shimscan.hook_map(base_types, index)
        cache = {}
        unreachable = [hook for hook in base_types["hooks"] if hook not in {h for hs in bases.values() for h in hs}]

        if unreachable:
            lines.append(f"! {loader}: no base type on the classpath for {', '.join(sorted(unreachable))} — "
                         f"those hooks are not checked here")

        for key, owner in rows:
            if owner != loader:
                continue

            label = f"{key} ({loader})".ljust(width)
            jar = jars.get((key, loader))

            if not jar:
                counts["skipped"] += 1
                lines.append(f"{label}  [NO JAR]   nothing pinned for this loader, or the file is not downloadable")
                continue

            found = shimscan.scan(loader, key, owned[jar], index, bases, cache, base_types, renames,
                                  versions.get((key, loader)))
            sections = [
                ("+", "not registered", found["new"]),
                ("-", "registered, gone from the jar", found["missing"]),
                ("!", "registered, no longer inherits the base type", found["detached"]),
            ]

            if not any(hooks for _, _, hooks in sections) and not found["changed"]:
                counts["ok"] += 1
                lines.append(f"{label}  [OK]")
                continue

            counts["changed"] += 1
            lines.append(f"{label}  [CHANGED]")

            for marker, note, hooks in sections:
                for hook, names in sorted(hooks.items()):
                    lines.append(f"  {marker} {titles.get(hook, hook)} ({note}):")
                    lines += [f"      {name}" for name in sorted(names)]

            for hook, classes in sorted(found["changed"].items()):
                lines.append(f"  ~ {titles.get(hook, hook)} (registered, fields differ in the newest file):")

                for name, (added, removed) in sorted(classes.items()):
                    lines.append(f"      {name}")
                    lines += [f"        + {field}" for field in added]
                    lines += [f"        - {field}" for field in removed]

    for key, loader in dormant:
        lines.append(f"{f'{key} ({loader})'.ljust(width)}  [DORMANT]  not in compat_mods, nothing compiles it")

    lines.append("")
    lines.append(f"{counts['ok']} clean, {counts['changed']} changed, {counts['skipped']} not scanned")
    return lines


def resolve(client: CurseForge, entry: dict, pinned: dict):
    result = {"key": entry["key"], "loader": client.loader, "entry": entry}
    matches = pick_project(client, entry)
    current = pinned.get(entry["key"])

    if not matches:
        result["state"] = "unavailable" if current is None else "gone"
        result["untagged"] = [
            slug for slug, project_id in map(parse_project, entry["curseforge"])
            if client.pick_untagged_file(project_id)
        ]
        return result

    if len(matches) > 1:
        result["ambiguous"] = [slug for slug, _, _ in matches]

    result["slug"], result["project_id"], result["latest"] = matches[0]

    if current is None:
        result["state"] = "new"
    elif current["file_id"] != result["latest"]["id"]:
        result["state"] = "outdated"
        result["current"] = client.get(f"/mods/{result['project_id']}/files/{current['file_id']}")
    else:
        result["state"] = "current"

    return result


def render_block(deps: dict, entries: dict):
    mods = sorted({key for key, _ in deps})
    lines = [BLOCK_START]

    if mods:
        lines.append("compat_mods=\\")
        lines += [f"  {mod},\\" for mod in mods[:-1]]
        lines.append(f"  {mods[-1]}")
    else:
        lines.append("compat_mods=")

    lines.append("")

    for key in mods:
        entry = entries[key]
        rendered = {}

        for loader in sorted(loader for mod, loader in deps if mod == key):
            dep = deps[(key, loader)]

            if dep["slug"] not in rendered:
                lines.append(f"# {entry['name']} {CURSEFORGE_URL}/{dep['slug']}")
                rendered[dep["slug"]] = True

            lines.append(f"{key}_{loader}_dep=curse.maven:{dep['slug']}-{dep['project_id']}:{dep['file_id']}")

    lines.append(BLOCK_END)
    return "\n".join(lines)


def write_block(block: str, init: bool):
    path = PROJECT_DIR / "gradle.properties"
    lines = path.read_text(encoding="utf-8").splitlines()
    anchors = [index for index, line in enumerate(lines) if line.startswith("alicompat_")]
    lead = []
    trail = []

    if BLOCK_START in lines:
        start, end = lines.index(BLOCK_START), lines.index(BLOCK_END)
    elif LEGACY_BLOCK_START in lines:
        start = lines.index(LEGACY_BLOCK_START)
        end = max(index for index, line in enumerate(lines) if re.match(r"\w+_\w+_dep=", line))
    elif init:
        start = (max(anchors) + 1) if anchors else len(lines)

        while start < len(lines) and not lines[start].strip():
            lines.pop(start)

        lines.insert(start, "")
        end = start
        lead = [""]
        trail = [""] if start + 1 < len(lines) else []
    else:
        print("Error: found neither the generated block nor the legacy one in gradle.properties, use --init to write a new one.")
        return False

    path.write_text("\n".join(lines[:start] + lead + block.splitlines() + trail + lines[end + 1:]) + "\n", encoding="utf-8")
    return True


def main():
    parser = argparse.ArgumentParser(description="Resolves the supported_mods.json registry against CurseForge.")
    parser.add_argument("--loader", choices=sorted(MOD_LOADER_TYPE), help="Mod loader to check (default: every enabled platform)")
    parser.add_argument("--update", action="store_true", help="Regenerate the block in gradle.properties and scaffold missing shims")
    parser.add_argument("--init", action="store_true", help="Pin and enable every registry mod, ignoring the current compat_mods; implies --update")
    parser.add_argument("--scaffold", metavar="MOD", help="Do all of that for one registry mod only: scaffold its missing source sets, pin it, enable it; implies --update")
    parser.add_argument("--no-scan", action="store_true", help="Skip the shim scan, only resolve versions")
    parser.add_argument("--curseforge-api-key", help="CurseForge API Key (default: $CURSEFORGE_API_KEY)")
    args = parser.parse_args()

    api_key = args.curseforge_api_key or read_env_secret("CURSEFORGE_API_KEY")

    if not api_key:
        return 1

    properties = read_gradle_properties()
    platforms = [platform.strip() for platform in properties.get("enabled_platforms", "").split(",") if platform.strip()]

    if args.loader and args.loader not in platforms:
        print(f"Error: loader '{args.loader}' is not in enabled_platforms ({properties.get('enabled_platforms')}).")
        return 1

    minecraft_version = properties["minecraft_version"]
    entries = {entry["key"]: entry for entry in read_supported_mods()}

    if args.scaffold and args.scaffold not in entries:
        print(f"Error: '{args.scaffold}' is not in {SUPPORTED_MODS_FILE}, add it there first.")
        return 1

    enabled = set(entries) if args.init else set(read_enabled_mods(properties)) | ({args.scaffold} if args.scaffold else set())
    activated = sorted(set(entries) - set(read_enabled_mods(properties))) if args.init else []
    args.update = args.update or args.init or bool(args.scaffold)
    checked_loaders = [args.loader] if args.loader else platforms
    pinned = {loader: read_pinned_deps(properties, loader) for loader in platforms}
    clients = {loader: CurseForge(api_key, minecraft_version, loader) for loader in checked_loaders}

    deps = {}
    dormant = []
    available = {}
    orphans = []
    results = []
    extendable = []
    scaffolds = []

    for loader in platforms:
        for key, dep in pinned[loader].items():
            if key not in entries:
                orphans.append((key, loader, dep))
            elif loader not in checked_loaders or (args.scaffold and key != args.scaffold) or (key not in enabled and has_source_set(platforms, key)):
                deps[(key, loader)] = dep

    pending = []

    for key, entry in sorted(entries.items()):
        if args.scaffold and key != args.scaffold:
            continue

        probe = key not in enabled and has_source_set(platforms, key)

        if probe:
            dormant.append(entry)

        pending += [(entry, loader, probe) for loader in checked_loaders]

    print(f"Resolving {len(pending)} mod/loader pairs for {minecraft_version} ({', '.join(checked_loaders)})")

    for index, (entry, loader, probe) in enumerate(pending, start=1):
        progress(index, len(pending), f"{entry['key']} ({loader})")
        result = resolve(clients[loader], entry, pinned[loader])

        if probe:
            if "latest" in result:
                available.setdefault(entry["key"], []).append(loader)

            continue

        results.append(result)

        if result["state"] not in ("new", "outdated", "current"):
            continue

        pin = {"slug": result["slug"], "project_id": result["project_id"], "file_id": result["latest"]["id"]}

        if source_set(loader, entry["key"]).is_dir():
            deps[(entry["key"], loader)] = pin
        elif has_source_set(platforms, entry["key"]) and args.scaffold != entry["key"]:
            result["extendable"] = True
            extendable.append(result)

            if entry["key"] in pinned[loader]:
                deps[(entry["key"], loader)] = pin
        else:
            deps[(entry["key"], loader)] = pin
            scaffolds.append(result)

    progress_done()

    for state, title in (("outdated", "Outdated, newer file on CurseForge:"), ("new", "Newly available, not pinned yet:"),
                         ("gone", "No release or beta file any more, the pin is dropped:")):
        selected = [result for result in results if result["state"] == state and not result.get("extendable")]

        if not selected:
            continue

        print(f"\n{title}")

        for result in selected:
            print(f"  {result['key']} ({result['loader']})")

            if "current" in result:
                print(f"    pinned {describe(result['current'])}")

            if "latest" in result:
                print(f"    latest {describe(result['latest'])}")

    if scaffolds:
        print(f"\nNo shim source set{' yet' if args.update else ', run with --update to scaffold'}:")

        for result in scaffolds:
            print(f"  {result['key']} ({result['loader']})")

    if extendable:
        print("\nAvailable on a loader this shim does not cover, left alone — write the source set by hand to take it:")

        for result in extendable:
            print(f"  {result['key']} ({result['loader']}) {result['latest']['displayName']}")

    ambiguous = [result for result in results if "ambiguous" in result]

    if ambiguous:
        print("\nSeveral projects have a file for the same loader, the newest one was taken:")

        for result in ambiguous:
            print(f"  {result['key']} ({result['loader']}): {', '.join(result['ambiguous'])}")

    untagged = [result for result in results if result.get("untagged")]

    if untagged:
        print("\nOnly files that name no loader at all, so nothing was assigned:")

        for result in untagged:
            print(f"  {result['key']} ({result['loader']}): {', '.join(result['untagged'])}")

    if dormant:
        width = max(len(entry["key"]) for entry in dormant)
        print("\nDormant, present in the tree but not in compat_mods:")

        for entry in dormant:
            loaders = available.get(entry["key"], [])

            if loaders:
                print(f"  {entry['key']:<{width}}  available on {', '.join(loaders)} — port it, then add it to compat_mods")
            else:
                print(f"  {entry['key']:<{width}}  no {minecraft_version} file on {', '.join(checked_loaders)} — nothing to do here")

    if activated:
        print("\nSwitched on by --init, each one has to compile against this Minecraft version:")

        for key in activated:
            print(f"  {key}")

    if orphans:
        print(f"\nPinned but missing from {SUPPORTED_MODS_FILE}, the pin is dropped:")

        for key, loader, dep in orphans:
            print(f"  {key} ({loader}) curse.maven:{dep['slug']}-{dep['project_id']}:{dep['file_id']}")

    if args.update:
        for result in scaffolds:
            print(f"\nScaffolded {scaffold(result['loader'], result['entry'])}")

        if not write_block(render_block(deps, entries), args.init):
            return 1

        print(f"\nWrote {len(deps)} dependency lines and {len({key for key, _ in deps})} compat_mods entries to gradle.properties")

    if not args.no_scan:
        print(f"\nScanning shim source sets against their {'newly pinned' if args.update else 'pinned'} jars")
        lines = scan_shims(args, entries, enabled, checked_loaders, clients, results, deps, pinned, minecraft_version)
        print("\n".join(lines))
        REPORT_FILE.parent.mkdir(parents=True, exist_ok=True)
        REPORT_FILE.write_text("\n".join(lines) + "\n", encoding="utf-8")
        print(f"\nReport written to {REPORT_FILE.relative_to(PROJECT_DIR)}")

    counts = {state: len([result for result in results if result["state"] == state]) for state in ("current", "outdated", "new", "gone", "unavailable")}
    print(f"\n{counts['current']} up to date, {counts['outdated']} outdated, {counts['new']} new, {counts['gone']} gone, "
          f"{counts['unavailable']} without a file on the loader asked, {len(dormant)} dormant ({len(available)} portable)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
