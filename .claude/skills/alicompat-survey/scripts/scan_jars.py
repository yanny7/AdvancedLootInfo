#!/usr/bin/env python3
"""Resolve the class names / namespaced ids of a gaps.json to the mod jars that own them.

One pass over a mods directory: reads each jar's loader metadata, notes which of the
wanted classes it contains, and computes the CurseForge murmur2 fingerprint so the jar
can be turned into a project/file id by cf_lookup.py.

Usage: scan_jars.py --gaps gaps.json --mods-dir <dir> [--out owners.json]
"""
import argparse
import json
import os
import re
import zipfile

LAMBDA = re.compile(r"\$\$Lambda(/0x[0-9a-f]+)?$")


def normalized_bytes(data):
    return bytes(b for b in data if b not in (9, 10, 13, 32))


def murmur2(data, seed=1):
    m, r = 0x5BD1E995, 24
    length = len(data)
    h = (seed ^ length) & 0xFFFFFFFF
    for i in range(0, length - length % 4, 4):
        k = int.from_bytes(data[i:i + 4], "little")
        k = (k * m) & 0xFFFFFFFF
        k ^= k >> r
        k = (k * m) & 0xFFFFFFFF
        h = (h * m) & 0xFFFFFFFF
        h ^= k
    tail = length % 4
    if tail:
        rest = data[length - tail:]
        if tail == 3:
            h ^= rest[2] << 16
        if tail >= 2:
            h ^= rest[1] << 8
        h ^= rest[0]
        h = (h * m) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * m) & 0xFFFFFFFF
    h ^= h >> 15
    return h


def fingerprint(path):
    with open(path, "rb") as handle:
        return murmur2(normalized_bytes(handle.read()))


def read_metadata(archive):
    """Return (modids, display_name, version) from whatever loader metadata the jar carries."""
    names = set(archive.namelist())
    for toml_name in ("META-INF/neoforge.mods.toml", "META-INF/mods.toml"):
        if toml_name not in names:
            continue
        text = archive.read(toml_name).decode("utf-8", "replace")
        ids = re.findall(r'^\s*modId\s*=\s*"([^"]+)"', text, re.M)
        display = re.findall(r'^\s*displayName\s*=\s*"([^"]+)"', text, re.M)
        version = re.findall(r'^\s*version\s*=\s*"([^"]+)"', text, re.M)
        if ids:
            return ids, (display[0] if display else ids[0]), (version[0] if version else "")
    if "fabric.mod.json" in names:
        try:
            data = json.loads(archive.read("fabric.mod.json").decode("utf-8", "replace"))
        except ValueError:
            data = {}
        if data.get("id"):
            return [data["id"]], data.get("name", data["id"]), str(data.get("version", ""))
    return [], "", ""


def wanted_class_paths(gaps):
    """class name -> zip entry path, for every subject that looks like a Java class."""
    out = {}
    for subjects in gaps["categories"].values():
        for subject in subjects:
            if ":" in subject or "." not in subject:
                continue
            base = LAMBDA.sub("", subject)
            outer = base.split("$", 1)[0]
            out[subject] = outer.replace(".", "/") + ".class"
    return out


def scan(mods_dir, by_path, namespaces):
    """One metadata pass over every jar, then fingerprint only the jars that matter."""
    jars = {}
    owners = {}
    namespace_owners = {}
    for entry in sorted(os.listdir(mods_dir)):
        if not entry.endswith(".jar"):
            continue
        full = os.path.join(mods_dir, entry)
        try:
            with zipfile.ZipFile(full) as archive:
                hits = [p for p in by_path if p in archive.NameToInfo]
                modids, display, version = read_metadata(archive)
        except (zipfile.BadZipFile, OSError):
            continue
        primary = modids[0] if modids else ""
        matched = [n for n in namespaces if n == primary or n in modids[:1]]
        if not hits and not matched:
            continue
        jars[entry] = {
            "modId": primary,
            "modIds": modids,
            "displayName": display,
            "version": version,
            "fingerprint": fingerprint(full),
        }
        for path in hits:
            for subject in by_path[path]:
                owners[subject] = entry
        for namespace in matched:
            namespace_owners[namespace] = entry
    return jars, owners, namespace_owners


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--gaps", required=True)
    parser.add_argument("--mods-dir", required=True)
    parser.add_argument("--out", default="-")
    args = parser.parse_args()

    with open(args.gaps, encoding="utf-8") as handle:
        gaps = json.load(handle)
    wanted = wanted_class_paths(gaps)
    by_path = {}
    for subject, path in wanted.items():
        by_path.setdefault(path, []).append(subject)
    namespaces = sorted({s.split(":", 1)[0] for c in gaps["categories"].values() for s in c if ":" in s})

    jars, owners, namespace_owners = scan(args.mods_dir, by_path, namespaces)
    json_out = {
        "jars": jars,
        "owners": owners,
        "namespaceOwners": namespace_owners,
        "unresolved": sorted(set(wanted) - set(owners)),
        "unresolvedNamespaces": sorted(set(namespaces) - set(namespace_owners)),
    }
    text = json.dumps(json_out, indent=2) + "\n"
    if args.out == "-":
        print(text, end="")
    else:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(text)
        print(
            f"jars={len(jars)} classes={len(owners)}/{len(wanted)} "
            f"namespaces={len(namespace_owners)}/{len(namespaces)}"
        )


if __name__ == "__main__":
    main()
