#!/usr/bin/env python3
"""Resolves the mods listed in supported_mods.json against CurseForge for this branch's
Minecraft version, reports what is outdated, newly available or gone, and can regenerate
the generated ALICompat block in gradle.properties together with missing shim skeletons."""

import argparse
import re
import sys
from pathlib import Path

from modpack import (
    MOD_LOADER_TYPE,
    PROJECT_DIR,
    RELEASE_TYPES,
    SUPPORTED_MODS_FILE,
    CurseForge,
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


def resolve(client: CurseForge, entry: dict, loader: str, pinned: dict):
    project = entry["curseforge"][loader]
    result = {"key": entry["key"], "loader": loader, "entry": entry, "slug": project["slug"], "project_id": project["project_id"]}
    latest = client.pick_file(project["project_id"])
    current = pinned.get(entry["key"])

    if latest is None:
        result["state"] = "unavailable" if current is None else "gone"
        return result

    result["latest"] = latest

    if current is None:
        result["state"] = "new"
    elif current["file_id"] != latest["id"]:
        result["state"] = "outdated"
        result["current"] = client.get(f"/mods/{project['project_id']}/files/{current['file_id']}")
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

        pending += [(entry, loader, probe) for loader in checked_loaders if loader in entry["curseforge"]]

    print(f"Resolving {len(pending)} mod/loader pairs for {minecraft_version} ({', '.join(checked_loaders)})")

    for index, (entry, loader, probe) in enumerate(pending, start=1):
        progress(index, len(pending), f"{entry['key']} ({loader})")
        result = resolve(clients[loader], entry, loader, pinned[loader])

        if probe:
            if "latest" in result:
                available.setdefault(entry["key"], []).append(loader)

            continue

        results.append(result)

        if result["state"] in ("new", "outdated", "current"):
            deps[(entry["key"], loader)] = {"slug": result["slug"], "project_id": result["project_id"], "file_id": result["latest"]["id"]}

    progress_done()
    missing_source = [result for result in results if result["state"] in ("new", "outdated", "current") and not source_set(result["loader"], result["key"]).is_dir()]

    for state, title in (("outdated", "Outdated, newer file on CurseForge:"), ("new", "Newly available, not pinned yet:"),
                         ("gone", "No release or beta file any more, the pin is dropped:"), ("unavailable", "Not available for this Minecraft version:")):
        selected = [result for result in results if result["state"] == state]

        if not selected:
            continue

        print(f"\n{title}")

        for result in selected:
            print(f"  {result['key']} ({result['loader']})")

            if "current" in result:
                print(f"    pinned {describe(result['current'])}")

            if "latest" in result:
                print(f"    latest {describe(result['latest'])}")

    if missing_source:
        print(f"\nNo shim source set{' yet' if args.update else ', run with --update to scaffold'}:")

        for result in missing_source:
            print(f"  {result['key']} ({result['loader']})")

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
        for result in missing_source:
            print(f"\nScaffolded {scaffold(result['loader'], result['entry'])}")

        if not write_block(render_block(deps, entries), args.init):
            return 1

        print(f"\nWrote {len(deps)} dependency lines and {len({key for key, _ in deps})} compat_mods entries to gradle.properties")

    counts = {state: len([result for result in results if result["state"] == state]) for state in ("current", "outdated", "new", "gone", "unavailable")}
    print(f"\n{counts['current']}/{len(results)} up to date, {counts['outdated']} outdated, {counts['new']} new, "
          f"{counts['gone'] + counts['unavailable']} unresolved, {len(dormant)} dormant ({len(available)} portable)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
