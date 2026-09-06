#!/usr/bin/env python3
"""Work out everything needed from one modpack directory.

Reads the launcher's own metadata, so a survey needs a single path. Output pack.json:
mods dir, Minecraft version, loader, the log to parse and the jars that carry the
vanilla / loader class hierarchy.

Usage: packdir.py --pack-dir <instance dir> [--out pack.json]
                  [--minecraft-jar ...] [--log ...] [--libraries ...]
"""
import argparse
import glob
import json
import os
import re
import sys

LOG_NAMES = ("logs/debug.log", "logs/latest.log")
MODS_NAMES = ("minecraft/mods", "mods", ".minecraft/mods")
LOADERS = {"net.neoforged": "neoforge", "net.minecraftforge": "forge", "net.fabricmc.fabric-loader": "fabric"}


def first_existing(root, candidates):
    for candidate in candidates:
        path = os.path.join(root, candidate)
        if os.path.exists(path):
            return path
    return None


def read_flame(root):
    """CurseForge pack manifest: authoritative Minecraft version and loader."""
    for candidate in ("flame/manifest.json", "manifest.json", "minecraft/manifest.json"):
        path = os.path.join(root, candidate)
        if not os.path.isfile(path):
            continue
        try:
            with open(path, encoding="utf-8") as handle:
                data = json.load(handle)
        except ValueError:
            continue
        minecraft = data.get("minecraft", {})
        loaders = minecraft.get("modLoaders", [])
        primary = next((l for l in loaders if l.get("primary")), loaders[0] if loaders else {})
        loader_id = primary.get("id", "")
        return {
            "name": data.get("name", ""),
            "packVersion": data.get("version", ""),
            "minecraftVersion": minecraft.get("version", ""),
            "loader": loader_id.split("-", 1)[0] or "",
            "loaderVersion": loader_id.partition("-")[2],
            "manifest": path,
        }
    return {}


def read_mmc(root):
    """Prism / MultiMC component list: the fallback when there is no CurseForge manifest."""
    path = os.path.join(root, "mmc-pack.json")
    if not os.path.isfile(path):
        return {}
    try:
        with open(path, encoding="utf-8") as handle:
            components = json.load(handle).get("components", [])
    except ValueError:
        return {}
    out = {"manifest": path}
    for component in components:
        uid = component.get("uid", "")
        if uid == "net.minecraft":
            out["minecraftVersion"] = component.get("version", "")
        elif uid in LOADERS:
            out["loader"] = LOADERS[uid]
            out["loaderVersion"] = component.get("version", "")
    return out


def find_libraries(root, override):
    if override:
        return override
    # Prism keeps one shared libraries tree next to instances/<pack>.
    probe = os.path.abspath(root)
    for _ in range(4):
        probe = os.path.dirname(probe)
        candidate = os.path.join(probe, "libraries")
        if os.path.isdir(candidate):
            return candidate
    return None


def find_minecraft_jar(libraries, version):
    """Prefer the SRG-remapped client jar: obfuscated official jars carry no readable
    class names, and the class hierarchy is the whole point of loading it."""
    if not libraries or not version:
        return None
    patterns = [
        f"net/minecraft/client/{version}-*/client-{version}-*-srg.jar",
        f"net/minecraft/client/{version}/client-{version}-srg.jar",
        f"net/minecraft/client/{version}-*/client-{version}-*-official.jar",
        f"com/mojang/minecraft/{version}/minecraft-{version}-client.jar",
    ]
    for pattern in patterns:
        hits = sorted(glob.glob(os.path.join(libraries, pattern)))
        if hits:
            return hits[-1]
    return None


def find_loader_jars(libraries, loader, version):
    if not libraries or not loader:
        return []
    patterns = {
        "neoforge": [f"net/neoforged/neoforge/{version}/neoforge-{version}-universal.jar",
                     "net/neoforged/neoforge/*/neoforge-*-universal.jar"],
        "forge": [f"net/minecraftforge/forge/*{version}*/forge-*-universal.jar",
                  "net/minecraftforge/forge/*/forge-*-universal.jar"],
        "fabric": ["net/fabricmc/fabric-loader/*/fabric-loader-*.jar"],
    }.get(loader, [])
    for pattern in patterns:
        hits = sorted(glob.glob(os.path.join(libraries, pattern)))
        if hits:
            return [hits[-1]]
    return []


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pack-dir", required=True)
    parser.add_argument("--minecraft-jar")
    parser.add_argument("--log")
    parser.add_argument("--libraries")
    parser.add_argument("--out", default="-")
    args = parser.parse_args()

    root = os.path.abspath(args.pack_dir)
    if not os.path.isdir(root):
        sys.exit(f"not a directory: {root}")

    pack = {"packDir": root}
    pack.update(read_mmc(root))
    pack.update({k: v for k, v in read_flame(root).items() if v})

    mods = first_existing(root, MODS_NAMES)
    if not mods:
        sys.exit(f"no mods directory under {root}")
    pack["modsDir"] = mods
    pack["jarCount"] = len(glob.glob(os.path.join(mods, "*.jar")))

    game = os.path.dirname(mods)
    pack["log"] = args.log or first_existing(game, LOG_NAMES) or first_existing(root, LOG_NAMES)

    libraries = find_libraries(root, args.libraries)
    pack["libraries"] = libraries
    version = pack.get("minecraftVersion", "")
    pack["minecraftJar"] = args.minecraft_jar or find_minecraft_jar(libraries, version)
    pack["loaderJars"] = find_loader_jars(libraries, pack.get("loader", ""), pack.get("loaderVersion", ""))

    missing = [k for k in ("minecraftVersion", "loader", "log", "minecraftJar") if not pack.get(k)]
    pack["missing"] = missing

    text = json.dumps(pack, indent=2) + "\n"
    if args.out == "-":
        print(text, end="")
    else:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(text)
        print(f"pack={pack.get('name') or os.path.basename(root)} mc={version} "
              f"loader={pack.get('loader')} jars={pack['jarCount']} "
              f"log={'yes' if pack['log'] else 'no'} mcJar={'yes' if pack['minecraftJar'] else 'no'}"
              + (f" missing={','.join(missing)}" if missing else ""))


if __name__ == "__main__":
    main()
