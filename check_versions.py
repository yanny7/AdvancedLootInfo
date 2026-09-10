#!/usr/bin/env python3
"""Reports which of the ALICompat target mods pinned in gradle.properties are behind the
newest CurseForge file for this branch's Minecraft version, and can repin them."""

import argparse
import re
import sys

from modpack import MOD_LOADER_TYPE, PROJECT_DIR, RELEASE_TYPES, CurseForge, read_env_secret, read_gradle_properties


def read_pinned(properties: dict, loader: str):
    pinned = []

    for key, value in properties.items():
        match = re.fullmatch(rf"(\w+)_{loader}_dep", key)

        if not match:
            continue

        coordinates = re.fullmatch(r"curse\.maven:(.+)-(\d+):(\d+)", value)

        if not coordinates:
            print(f"Warning: cannot parse '{key}={value}', skipping.")
            continue

        pinned.append({
            "key": key,
            "mod": match.group(1),
            "loader": loader,
            "slug": coordinates.group(1),
            "project_id": int(coordinates.group(2)),
            "file_id": int(coordinates.group(3)),
        })

    return sorted(pinned, key=lambda entry: entry["mod"])


def describe(file: dict):
    return f"{file['id']} {RELEASE_TYPES.get(file['releaseType'], 'unknown'):7} {file['fileDate'][:10]}  {file['displayName']}"


def check(client: CurseForge, entry: dict):
    latest = client.pick_file(entry["project_id"])

    if latest is None:
        entry["problem"] = f"no {client.minecraft_version}/{client.loader} release or beta file"
        return entry

    entry["latest"] = latest

    if latest["id"] != entry["file_id"]:
        entry["current"] = client.get(f"/mods/{entry['project_id']}/files/{entry['file_id']}")

    return entry


def repin(outdated: list):
    path = PROJECT_DIR / "gradle.properties"
    text = path.read_text(encoding="utf-8")

    for entry in outdated:
        old = f"{entry['key']}=curse.maven:{entry['slug']}-{entry['project_id']}:{entry['file_id']}"
        new = f"{entry['key']}=curse.maven:{entry['slug']}-{entry['project_id']}:{entry['latest']['id']}"

        if old not in text:
            print(f"Warning: cannot find '{old}' in gradle.properties, left untouched.")
            continue

        text = text.replace(old, new)

    path.write_text(text, encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description="Reports outdated ALICompat target mod versions.")
    parser.add_argument("--loader", choices=sorted(MOD_LOADER_TYPE), help="Mod loader to check (default: every enabled platform)")
    parser.add_argument("--update", action="store_true", help="Repin the outdated file ids in gradle.properties")
    parser.add_argument("--curseforge-api-key", help="CurseForge API Key (default: $CURSEFORGE_API_KEY)")
    args = parser.parse_args()

    api_key = args.curseforge_api_key or read_env_secret("CURSEFORGE_API_KEY")

    if not api_key:
        return 1

    properties = read_gradle_properties()
    platforms = properties.get("enabled_platforms", "").split(",")

    if args.loader and args.loader not in platforms:
        print(f"Error: loader '{args.loader}' is not in enabled_platforms ({properties.get('enabled_platforms')}).")
        return 1

    minecraft_version = properties["minecraft_version"]
    checked = []

    for loader in [args.loader] if args.loader else platforms:
        pinned = read_pinned(properties, loader)

        if not pinned:
            print(f"Error: no <mod>_{loader}_dep entries in gradle.properties.")
            return 1

        print(f"Checking {len(pinned)} mods for {loader} {minecraft_version}...")
        client = CurseForge(api_key, minecraft_version, loader)
        checked += [check(client, entry) for entry in pinned]

    outdated = [entry for entry in checked if "current" in entry]
    problems = [entry for entry in checked if "problem" in entry]

    if outdated:
        print(f"\nOutdated, newer file on CurseForge:")

        for entry in outdated:
            print(f"  {entry['mod']} ({entry['loader']})")
            print(f"    pinned {describe(entry['current'])}")
            print(f"    latest {describe(entry['latest'])}")

    if problems:
        print(f"\nCannot be checked:")

        for entry in problems:
            print(f"  {entry['mod']} ({entry['loader']}): {entry['problem']}")

    if args.update and outdated:
        repin(outdated)
        print(f"\nRepinned {len(outdated)} file ids in gradle.properties")

    print(f"\n{len(checked) - len(outdated) - len(problems)}/{len(checked)} up to date, {len(outdated)} outdated, {len(problems)} unresolved")
    return 0


if __name__ == "__main__":
    sys.exit(main())
