#!/usr/bin/env python3
"""Turn the jar fingerprints of an owners.json into CurseForge projects and, per
Minecraft version / loader, the newest published file id.

Needs CURSEFORGE_API_KEY in the environment (the same key upload.py uses).

Usage: cf_lookup.py --owners owners.json [--out projects.json]
                    [--targets 1.20.1:fabric,forge 1.21.1:fabric,forge,neoforge ...]
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

API = "https://api.curseforge.com"
LOADER_TYPE = {"forge": 1, "fabric": 4, "quilt": 5, "neoforge": 6}
DEFAULT_TARGETS = [
    ("1.20.1", ["fabric", "forge"]),
    ("1.21.1", ["fabric", "forge", "neoforge"]),
    ("1.21.11", ["fabric", "neoforge"]),
    ("26.1.2", ["fabric", "neoforge"]),
    ("26.2", ["fabric", "neoforge"]),
]


def request(path, key, body=None, params=None):
    url = API + path
    if params:
        url += "?" + urllib.parse.urlencode(params)
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Accept": "application/json", "x-api-key": key}
    if data:
        headers["Content-Type"] = "application/json"
    for attempt in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, data=data, headers=headers), timeout=60) as response:
                return json.loads(response.read())
        except urllib.error.HTTPError as error:
            if error.code in (429, 500, 502, 503, 504) and attempt < 3:
                time.sleep(2 ** attempt)
                continue
            print(f"CurseForge {error.code} on {url}", file=sys.stderr)
            return None
        except urllib.error.URLError as error:
            if attempt < 3:
                time.sleep(2 ** attempt)
                continue
            print(f"CurseForge unreachable: {error}", file=sys.stderr)
            return None
    return None


def match_fingerprints(fingerprints, key):
    out = {}
    for start in range(0, len(fingerprints), 100):
        chunk = fingerprints[start:start + 100]
        response = request("/v1/fingerprints", key, body={"fingerprints": chunk})
        if not response:
            continue
        for match in response.get("data", {}).get("exactMatches", []):
            out[match["file"]["fileFingerprint"]] = {
                "projectId": match["id"],
                "fileId": match["file"]["id"],
                "fileName": match["file"]["fileName"],
            }
    return out


def project_metadata(project_ids, key):
    out = {}
    for start in range(0, len(project_ids), 100):
        chunk = project_ids[start:start + 100]
        response = request("/v1/mods", key, body={"modIds": chunk})
        if not response:
            continue
        for mod in response.get("data", []):
            out[mod["id"]] = {
                "slug": mod.get("slug", ""),
                "name": mod.get("name", ""),
                "url": mod.get("links", {}).get("websiteUrl", ""),
            }
    return out


def latest_files(project_id, targets, key):
    out = {}
    for version, loaders in targets:
        for loader in loaders:
            params = {
                "gameVersion": version,
                "modLoaderType": LOADER_TYPE[loader],
                "pageSize": 1,
                "index": 0,
            }
            response = request(f"/v1/mods/{project_id}/files", key, params=params)
            files = (response or {}).get("data", [])
            if not files:
                continue
            newest = max(files, key=lambda f: f.get("fileDate", ""))
            out.setdefault(version, {})[loader] = {
                "fileId": newest["id"],
                "fileName": newest.get("fileName", ""),
                "fileDate": newest.get("fileDate", ""),
                "displayName": newest.get("displayName", ""),
            }
    return out


def parse_targets(values):
    if not values:
        return DEFAULT_TARGETS
    out = []
    for value in values:
        version, _, loaders = value.partition(":")
        out.append((version, [l for l in loaders.split(",") if l]))
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--owners", required=True)
    parser.add_argument("--candidates", help="candidates.json from classindex.py; its jars are "
                                             "surveyed too, not only the ones the log named")
    parser.add_argument("--mods-dir", help="needed with --candidates, to fingerprint the extra jars")
    parser.add_argument("--out", default="-")
    parser.add_argument("--targets", nargs="*")
    parser.add_argument("--overrides", help="JSON file mapping a mod id to the CurseForge project id "
                                            "to use instead of the fingerprint match")
    args = parser.parse_args()

    key = os.environ.get("CURSEFORGE_API_KEY")
    if not key:
        sys.exit("CURSEFORGE_API_KEY is not set")
    targets = parse_targets(args.targets)

    overrides = {}
    if args.overrides:
        with open(args.overrides, encoding="utf-8") as handle:
            overrides = {k: int(v) for k, v in json.load(handle).items()}

    with open(args.owners, encoding="utf-8") as handle:
        owners = json.load(handle)
    jars = owners["jars"]
    if args.candidates:
        if not args.mods_dir:
            sys.exit("--candidates needs --mods-dir")
        sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        from scan_jars import fingerprint
        with open(args.candidates, encoding="utf-8") as handle:
            extra = json.load(handle)
        for jar_name, meta in extra.get("jars", {}).items():
            if jar_name in jars or jar_name not in extra.get("candidates", {}):
                continue
            path = os.path.join(args.mods_dir, jar_name)
            if not os.path.isfile(path):
                continue
            jars[jar_name] = {
                "modId": meta.get("modId", ""),
                "modIds": meta.get("modIds", []),
                "displayName": meta.get("displayName", ""),
                "version": meta.get("version", ""),
                "fingerprint": fingerprint(path),
            }
    matches = match_fingerprints([j["fingerprint"] for j in jars.values()], key)

    projects = {}
    for jar_name, jar in jars.items():
        match = matches.get(jar["fingerprint"])
        override = overrides.get(jar.get("modId"))
        if override:
            match = {"projectId": override, "fileId": (match or {}).get("fileId"),
                     "fileName": (match or {}).get("fileName"), "overridden": True}
        if not match:
            projects.setdefault("_unmatched", []).append(jar_name)
            continue
        jar["curseforge"] = match
    project_ids = sorted({j["curseforge"]["projectId"] for j in jars.values() if "curseforge" in j})
    metadata = project_metadata(project_ids, key)

    for project_id in project_ids:
        info = dict(metadata.get(project_id, {}))
        info["projectId"] = project_id
        info["versions"] = latest_files(project_id, targets, key)
        info["maven"] = {
            version: {
                loader: f"curse.maven:{info.get('slug', 'mod')}-{project_id}:{data['fileId']}"
                for loader, data in loaders.items()
            }
            for version, loaders in info["versions"].items()
        }
        projects[str(project_id)] = info

    result = {
        "targets": [{"version": v, "loaders": l} for v, l in targets],
        "jars": jars,
        "projects": projects,
    }
    text = json.dumps(result, indent=2) + "\n"
    if args.out == "-":
        print(text, end="")
    else:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(text)
        print(f"projects={len(project_ids)} unmatched={len(projects.get('_unmatched', []))}")


if __name__ == "__main__":
    main()
