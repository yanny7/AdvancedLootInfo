#!/usr/bin/env python3
"""Builds a CurseForge-format modpack zip (importable by Prism Launcher) out of the
ALICompat target mods declared in gradle.properties."""

import argparse
import html
import json
import os
import re
import sys
import zipfile
from pathlib import Path

import requests

API_URL = "https://api.curseforge.com/v1"
MOD_LOADER_TYPE = {"forge": 1, "fabric": 4, "neoforge": 6, "quilt": 5}
RELEASE_TYPES = {1: "release", 2: "beta"}
REQUIRED_RELATION_TYPE = 3
VIEWER_SLUGS = {"rei": "roughly-enough-items", "jei": "jei", "emi": "emi"}
OWN_MODS = ["aci", "ali", "alicompat"]

PROJECT_DIR = Path(__file__).resolve().parent


def read_env_secret(env_name: str):
    value = os.environ.get(env_name)

    if not value:
        print(f"Error: neither the matching argument nor the environment variable '{env_name}' is set.")
        return None

    return value


def read_gradle_properties():
    text = (PROJECT_DIR / "gradle.properties").read_text(encoding="utf-8")
    text = re.sub(r"\\\r?\n\s*", "", text)
    properties = {}

    for line in text.splitlines():
        line = line.strip()

        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        properties[key.strip()] = value.strip()

    return properties


def read_compat_mods(properties: dict, loader: str):
    mods = {}

    for key, value in properties.items():
        match = re.fullmatch(rf"(\w+)_{loader}_dep", key)

        if not match:
            continue

        coordinates = re.fullmatch(r"curse\.maven:.+-(\d+):(\d+)", value)

        if not coordinates:
            print(f"Warning: cannot parse '{key}={value}', skipping.")
            continue

        mods[match.group(1)] = int(coordinates.group(1))

    return mods


class CurseForge:
    def __init__(self, api_key: str, minecraft_version: str, loader: str):
        self.session = requests.Session()
        self.session.headers.update({"x-api-key": api_key, "Accept": "application/json"})
        self.minecraft_version = minecraft_version
        self.loader = loader
        self.file_cache = {}
        self.mod_cache = {}

    def get(self, path: str, params=None):
        response = self.session.get(f"{API_URL}{path}", params=params, timeout=60)
        response.raise_for_status()
        return response.json()["data"]

    def post(self, path: str, body: dict):
        response = self.session.post(f"{API_URL}{path}", json=body, timeout=60)
        response.raise_for_status()
        return response.json()["data"]

    def find_project_id(self, slug: str):
        results = self.get("/mods/search", {"gameId": 432, "slug": slug, "pageSize": 50})

        for result in results:
            if result["slug"] == slug:
                self.mod_cache[result["id"]] = result
                return result["id"]

        return None

    def get_mods(self, project_ids):
        missing = [project_id for project_id in project_ids if project_id not in self.mod_cache]

        for index in range(0, len(missing), 100):
            for mod in self.post("/mods", {"modIds": missing[index:index + 100]}):
                self.mod_cache[mod["id"]] = mod

        return [self.mod_cache[project_id] for project_id in project_ids if project_id in self.mod_cache]

    def pick_file(self, project_id: int):
        if project_id in self.file_cache:
            return self.file_cache[project_id]

        params = {
            "gameVersion": self.minecraft_version,
            "modLoaderType": MOD_LOADER_TYPE[self.loader],
            "pageSize": 50,
        }
        candidates = self.get(f"/mods/{project_id}/files", params)

        if not candidates:
            candidates = [
                candidate for candidate in self.get(f"/mods/{project_id}/files", {"gameVersion": self.minecraft_version, "pageSize": 50})
                if not self.tagged_with_other_loader(candidate)
            ]

        candidates = [candidate for candidate in candidates if candidate["releaseType"] in RELEASE_TYPES]
        candidates.sort(key=lambda candidate: candidate["fileDate"], reverse=True)
        chosen = candidates[0] if candidates else None
        self.file_cache[project_id] = chosen
        return chosen

    def tagged_with_other_loader(self, file: dict):
        tagged = {version.lower() for version in file["gameVersions"]} & set(MOD_LOADER_TYPE)
        return bool(tagged) and self.loader not in tagged


def resolve(client: CurseForge, pinned: dict):
    resolved = {}
    missing = []
    pending = [(slug, project_id) for slug, project_id in sorted(pinned.items())]
    seen = set()

    while pending:
        slug, project_id = pending.pop(0)

        if project_id in seen:
            continue

        seen.add(project_id)
        file = client.pick_file(project_id)

        if file is None:
            missing.append((slug, project_id))
            continue

        resolved[project_id] = file

        for dependency in file.get("dependencies", []):
            if dependency["relationType"] == REQUIRED_RELATION_TYPE and dependency["modId"] not in seen:
                pending.append((f"{slug} -> dependency", dependency["modId"]))

    return resolved, missing


def collect_own_jars(properties: dict, loader: str):
    jars = []
    problems = []

    for mod_id in OWN_MODS:
        name = f"{properties[f'{mod_id}_mod_name']}-{loader}-{properties['minecraft_version']}-{properties[f'{mod_id}_version']}.jar"
        jar = PROJECT_DIR / mod_id / loader / "build" / "libs" / name

        if jar.is_file():
            jars.append(jar)
        else:
            problems.append(jar.relative_to(PROJECT_DIR))

    return jars, problems


def build_modlist(mods):
    rows = "\n".join(
        f'<li><a href="{html.escape(mod.get("links", {}).get("websiteUrl") or "")}">{html.escape(mod["name"])}</a></li>'
        for mod in mods
    )
    return f"<ul>\n{rows}\n</ul>\n"


def build_manifest(properties: dict, loader: str, files):
    loader_version = {
        "forge": properties.get("forge_version"),
        "fabric": properties.get("fabric_loader_version"),
        "neoforge": properties.get("neoforge_version"),
    }[loader]

    return {
        "minecraft": {
            "version": properties["minecraft_version"],
            "modLoaders": [{"id": f"{loader}-{loader_version}", "primary": True}],
        },
        "manifestType": "minecraftModpack",
        "manifestVersion": 1,
        "name": f"{properties['alicompat_mod_name']} {loader} {properties['minecraft_version']}",
        "version": properties["alicompat_version"],
        "author": properties["mod_author"],
        "files": [
            {"projectID": file["modId"], "fileID": file["id"], "required": True}
            for file in sorted(files, key=lambda file: file["modId"])
        ],
    }


def write_zip(output: Path, manifest: dict, modlist: str, jars):
    output.parent.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("manifest.json", json.dumps(manifest, indent=2) + "\n")
        archive.writestr("modlist.html", modlist)

        for jar in jars:
            archive.write(jar, f"overrides/mods/{jar.name}")


def main():
    parser = argparse.ArgumentParser(description="Builds a Prism-importable modpack of the ALICompat target mods.")
    parser.add_argument("--loader", required=True, choices=sorted(MOD_LOADER_TYPE), help="Mod loader to build the pack for")
    parser.add_argument("--viewer", default="rei", choices=sorted(VIEWER_SLUGS), help="Recipe viewer to include (default: rei)")
    parser.add_argument("--output", help="Output zip path (default: build/modpack/<name>-<loader>-<mc>.zip)")
    parser.add_argument("--curseforge-api-key", help="CurseForge API Key (default: $CURSEFORGE_API_KEY)")
    args = parser.parse_args()

    api_key = args.curseforge_api_key or read_env_secret("CURSEFORGE_API_KEY")

    if not api_key:
        return 1

    properties = read_gradle_properties()
    loader = args.loader

    if loader not in properties.get("enabled_platforms", "").split(","):
        print(f"Error: loader '{loader}' is not in enabled_platforms ({properties.get('enabled_platforms')}).")
        return 1

    jars, jar_problems = collect_own_jars(properties, loader)

    if jar_problems:
        print("Error: build the mods first (./gradlew build), these jars are missing:")

        for problem in jar_problems:
            print(f"  {problem}")

        return 1

    client = CurseForge(api_key, properties["minecraft_version"], loader)
    pinned = read_compat_mods(properties, loader)

    if not pinned:
        print(f"Error: no <mod>_{loader}_dep entries in gradle.properties.")
        return 1

    viewer_id = client.find_project_id(VIEWER_SLUGS[args.viewer])

    if viewer_id is None:
        print(f"Error: cannot find '{VIEWER_SLUGS[args.viewer]}' on CurseForge.")
        return 1

    pinned[args.viewer] = viewer_id

    if loader == "fabric":
        fabric_api_id = client.find_project_id("fabric-api")

        if fabric_api_id is None:
            print("Error: cannot find 'fabric-api' on CurseForge.")
            return 1

        pinned["fabric-api"] = fabric_api_id

    print(f"Resolving {len(pinned)} mods for {loader} {properties['minecraft_version']}...")
    resolved, missing = resolve(client, pinned)
    mods = client.get_mods(sorted(resolved))
    blocked = [mod for mod in mods if resolved[mod["id"]].get("downloadUrl") is None]

    output = Path(args.output) if args.output else PROJECT_DIR / "build" / "modpack" / f"{properties['alicompat_mod_name']}-{loader}-{properties['minecraft_version']}.zip"
    write_zip(output, build_manifest(properties, loader, resolved.values()), build_modlist(mods), jars)

    for mod in mods:
        file = resolved[mod["id"]]
        print(f"  {mod['name']}: {file['displayName']} ({RELEASE_TYPES[file['releaseType']]})")

    if missing:
        print(f"\nNo {properties['minecraft_version']}/{loader} release or beta file, left out of the pack:")

        for slug, project_id in missing:
            print(f"  {slug} (project {project_id})")

    if blocked:
        print("\nThird-party downloads disabled by the author, Prism will ask you to download these by hand:")

        for mod in blocked:
            print(f"  {mod['name']}: {mod.get('links', {}).get('websiteUrl')}")

    print(f"\nWrote {output} ({len(resolved)} mods, {len(jars)} bundled jars)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
