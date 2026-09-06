#!/usr/bin/env python3
"""Static scan: every class in a pack that plugs into an ALI registry hook.

Finds what a mod *could* hand ALI, not only what this run of the pack did. That is the
only way to see the pull-model gaps at all: ALI asks for a trader's offers by id, so a
trader nobody registered produces no warning to read.

Two passes. The first parses the hierarchy header of every class in every jar (mods plus
the Minecraft and loader jars, which supply the vanilla half of each chain). The second
re-reads only the classes that turned out to be candidates, for field and constructor
shapes and string constants.

Usage: classindex.py --pack pack.json --base-types base_types.json [--out candidates.json]
                     [--cache cache.json]
"""
import argparse
import json
import os
import re
import sys
import zipfile

import classfile
from scan_jars import read_metadata

ENTITY_KEY = re.compile(r"^entity\.([a-z0-9_.-]+)\.([a-z0-9_/.-]+)$")


def camel_to_snake(name):
    name = re.sub(r"(.)([A-Z][a-z]+)", r"\1_\2", name)
    return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", name).lower()


def jar_paths(pack):
    mods = sorted(
        os.path.join(pack["modsDir"], n) for n in os.listdir(pack["modsDir"]) if n.endswith(".jar")
    )
    platform = [p for p in ([pack.get("minecraftJar")] + pack.get("loaderJars", [])) if p]
    return mods, platform


def index_jar(path, is_mod, hierarchy, jar_index, jars):
    try:
        archive = zipfile.ZipFile(path)
    except (zipfile.BadZipFile, OSError):
        return
    with archive:
        name = os.path.basename(path)
        modids, display, version = read_metadata(archive) if is_mod else ([], "", "")
        entity_keys = lang_entity_keys(archive, modids) if is_mod else {}
        jars[jar_index] = {
            "jar": name,
            "isMod": is_mod,
            "modId": modids[0] if modids else "",
            "modIds": modids,
            "displayName": display,
            "version": version,
            "entityKeys": entity_keys,
        }
        for entry in archive.infolist():
            if not entry.filename.endswith(".class") or entry.filename.startswith("META-INF/"):
                continue
            try:
                info = classfile.parse(archive.read(entry))
            except (classfile.ClassFileError, KeyError, OSError, struct_error()):
                continue
            if not info.name or info.name in hierarchy:
                continue
            hierarchy[info.name] = (info.supername, tuple(info.interfaces), info.access, jar_index)


def struct_error():
    import struct
    return struct.error


def lang_entity_keys(archive, modids):
    """entity.<modid>.<path> keys, the cheapest usable source of a mod's entity ids."""
    out = {}
    for modid in modids[:1] or []:
        name = f"assets/{modid}/lang/en_us.json"
        if name not in archive.NameToInfo:
            continue
        try:
            data = json.loads(archive.read(name).decode("utf-8", "replace"))
        except ValueError:
            continue
        for key, value in data.items():
            match = ENTITY_KEY.match(key)
            if match and match.group(1) == modid:
                out[match.group(2)] = value
    return out


def build_base_map(base_types, hierarchy):
    present = {}
    missing = {}
    for hook, spec in base_types["hooks"].items():
        found = [b for b in spec["bases"] if b in hierarchy]
        if found:
            present[hook] = found
        else:
            missing[hook] = spec["bases"]
    return present, missing


def hooks_of(name, hierarchy, base_to_hook, memo):
    """Every hook the class reaches through its supertype chain, memoized."""
    cached = memo.get(name)
    if cached is not None:
        return cached
    memo[name] = frozenset()  # guards a malformed cycle
    found = set()
    hook = base_to_hook.get(name)
    if hook:
        found.add(hook)
    node = hierarchy.get(name)
    if node:
        supername, interfaces, _, _ = node
        for parent in (supername,) + interfaces:
            if parent:
                found |= hooks_of(parent, hierarchy, base_to_hook, memo)
    result = frozenset(found)
    memo[name] = result
    return result


def detail(archive, internal_name):
    try:
        info = classfile.parse(archive.read(internal_name + ".class"), want_strings=True)
    except (classfile.ClassFileError, KeyError, OSError):
        return {}
    return {
        "fields": [{"name": n, "descriptor": d} for n, d in classfile.field_types(info)],
        "constructors": info.constructors(),
        "namespacedStrings": sorted({s for s in info.strings if re.fullmatch(r"[a-z0-9_.-]+:[a-z0-9_/.-]+", s)})[:20],
    }


def guess_entity_ids(simple_name, jar_meta):
    """Match a merchant class against the mod's own entity lang keys."""
    keys = jar_meta.get("entityKeys", {})
    if not keys:
        return [], []
    snake = re.sub(r"_(entity|mob|npc)$", "", camel_to_snake(simple_name))
    words = [w for w in snake.split("_") if w not in ("entity", "mob", "npc")]
    exact = [k for k in keys if k == snake]
    if exact:
        return exact, []
    partial = [k for k in keys if any(w and w in k.split("_") for w in words)]
    return [], sorted(partial)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pack", required=True)
    parser.add_argument("--base-types", required=True)
    parser.add_argument("--out", default="-")
    args = parser.parse_args()

    with open(args.pack, encoding="utf-8") as handle:
        pack = json.load(handle)
    with open(args.base_types, encoding="utf-8") as handle:
        base_types = json.load(handle)

    excluded_mods = set(base_types.get("excludedModIds", []))
    mods, platform = jar_paths(pack)
    if not platform:
        print("warning: no Minecraft jar — vanilla chains cannot be resolved, expect almost no hits",
              file=sys.stderr)

    hierarchy = {}
    jars = {}
    for index, path in enumerate(platform):
        index_jar(path, False, hierarchy, -1 - index, jars)
    for index, path in enumerate(mods):
        index_jar(path, True, hierarchy, index, jars)

    base_to_hook = {}
    present, missing = build_base_map(base_types, hierarchy)
    for hook, bases in present.items():
        for base in bases:
            base_to_hook[base] = hook

    excluded = tuple(base_types.get("excludedRoots", []))
    excluded_segments = set(base_types.get("excludedPackageSegments", []))
    memo = {}
    candidates = {}
    for name, (_, _, access, jar_index) in hierarchy.items():
        if jar_index < 0 or name.startswith(excluded):
            continue
        if jars[jar_index].get("modId") in excluded_mods:
            continue
        if excluded_segments & set(name.split("/")[:-1]):
            continue
        hooks = hooks_of(name, hierarchy, base_to_hook, memo)
        if not hooks:
            continue
        candidates.setdefault(jars[jar_index]["jar"], []).append({
            "class": name.replace("/", "."),
            "internalName": name,
            "hooks": sorted(hooks),
            "abstract": bool(access & classfile.ACC_ABSTRACT),
            "interface": bool(access & classfile.ACC_INTERFACE),
            "public": bool(access & classfile.ACC_PUBLIC),
            "enum": bool(access & classfile.ACC_ENUM),
        })

    for jar_name, entries in candidates.items():
        path = os.path.join(pack["modsDir"], jar_name)
        try:
            archive = zipfile.ZipFile(path)
        except (zipfile.BadZipFile, OSError):
            continue
        meta = next(j for j in jars.values() if j["jar"] == jar_name)
        with archive:
            for entry in entries:
                entry.update(detail(archive, entry["internalName"]))
                if "trader_entity" in entry["hooks"]:
                    simple = entry["class"].rsplit(".", 1)[-1].split("$")[-1]
                    exact, partial = guess_entity_ids(simple, meta)
                    modid = meta.get("modId", "")
                    entry["entityIdExact"] = [f"{modid}:{k}" for k in exact]
                    entry["entityIdCandidates"] = [f"{modid}:{k}" for k in partial]
        entries.sort(key=lambda e: e["class"])

    result = {
        "pack": {k: pack.get(k) for k in ("name", "minecraftVersion", "loader", "modsDir")},
        "classesIndexed": len(hierarchy),
        "jarsIndexed": len(jars),
        "basesFound": present,
        "basesMissing": missing,
        "jars": {j["jar"]: {k: v for k, v in j.items() if k != "entityKeys"} for j in jars.values() if j["isMod"]},
        "entityKeys": {j["jar"]: j["entityKeys"] for j in jars.values() if j.get("entityKeys")},
        "candidates": candidates,
    }
    text = json.dumps(result, indent=2) + "\n"
    if args.out == "-":
        print(text, end="")
    else:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(text)
        total = sum(len(v) for v in candidates.values())
        print(f"classes={len(hierarchy)} jars={len(jars)} candidates={total} "
              f"in {len(candidates)} mods; bases missing: {','.join(missing) or 'none'}")


if __name__ == "__main__":
    main()
