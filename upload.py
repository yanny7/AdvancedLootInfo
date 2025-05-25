import argparse
import functools
import hashlib
import json
import os.path
import re

import requests

MODRINTH_USER_AGENT = "Modrinth-Uploader-Script/1.0 (Yanny/AdvancedLootInfo)"
CURSEFORGE_USER_AGENT = "CurseForge-Uploader-Script/1.0 (Yanny/AdvancedLootInfo)"

def calculate_sha512(file_path):
    sha512_hash = hashlib.sha512()

    with open(file_path, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha512_hash.update(byte_block)

    return sha512_hash.hexdigest()

def prepare_dependency(dep_id):
    return {
        "project_id": dep_id,
        "dependency_type": "optional"
    }

@functools.lru_cache(maxsize=1)
def get_curseforge_game_versions_mapping(api_token):
    print("CurseForge: Loading minecraft version mappings...")
    url = "https://minecraft.curseforge.com/api/game/versions"
    headers = {
        "X-Api-Token": api_token,
        "User-Agent": CURSEFORGE_USER_AGENT
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        versions_data = response.json()

        # version_map = {item['name']: item['id'] for item in versions_data}
        print(f"CurseForge: Loaded mapping for {len(versions_data)} game versions.")
        return list(versions_data)
    except requests.exceptions.RequestException as e:
        print(f"CurseForge Error while reading game versions: {e}")
        return {}

def read_properties(properties_file_path="gradle.properties", keys_to_find=None):
    properties = {}

    if not os.path.exists(properties_file_path):
        print(f"Error: File '{properties_file_path}' doesn't exists.")
        return properties

    try:
        with open(properties_file_path, 'r') as f:
            for line in f:
                stripped_line = line.strip()

                if not stripped_line or stripped_line.startswith('#'):
                    continue

                if '=' in stripped_line:
                    key, value = stripped_line.split('=', 1)
                    key = key.strip()
                    value = value.strip()

                    if keys_to_find is None or key in keys_to_find:
                        properties[key] = value

        return properties
    except Exception as e:
        print(f"Error while reading from file '{properties_file_path}': {e}")
        return None

def map_loader(loader_name):
    if loader_name == "forge":
        return 1
    elif loader_name == "fabric":
        return 4
    elif loader_name == "neoforge":
        return 6
    else:
        return None

def read_changelog(filename="CHANGELOG.md"):
    changelog_content = []
    found_first_version_header = False

    try:
        with open(filename, 'r', encoding='utf-8') as f:
            for line in f:
                stripped_line = line.strip()

                if stripped_line.startswith("## [") and stripped_line.endswith("]"):
                    if not found_first_version_header:
                        found_first_version_header = True
                        continue
                    else:
                        break

                if found_first_version_header and len(stripped_line) > 0:
                    changelog_content.append(line.strip())

    except FileNotFoundError:
        print(f"Error: File '{filename}' not found.")
        return []
    except Exception as e:
        print(f"Error while reading file: {e}")
        return []

    return changelog_content

def upload_to_modrinth(api_token, project_id, version_number, mod_file_path, loaders, game_versions, changelog, dependencies, release_type, version_name):
    if not os.path.exists(mod_file_path):
        print(f"Error: File '{mod_file_path}' was not found!")
        return

    url = "https://api.modrinth.com/v2/version"
    file_hash_sha512 = calculate_sha512(mod_file_path)
    headers = {
        "Authorization": api_token,
        "User-Agent": MODRINTH_USER_AGENT
    }

    metadata = {
        "name": version_name,
        "version_number": version_number,
        "project_id": project_id,
        "loaders": loaders,
        "game_versions": game_versions,
        "featured": False,
        "changelog": changelog,
        "dependencies": dependencies,
        "file_parts": [os.path.basename(mod_file_path)],
        "version_type": release_type,
        "files": [
            {
                "hashes": {
                    "sha512": file_hash_sha512
                },
                "name": os.path.basename(mod_file_path),
                "primary": True
            }
        ]
    }

    print(f"\n--- Uploading to Modrinth ---")
    print(f"Uploading file '{mod_file_path}'")
    # print(f"Metadata: {json.dumps(metadata, indent=2)}")
    print("Do you want to proceed?")
    yes_no = input()

    if not yes_no.startswith("y"):
        print("Skipping upload...")
        return

    try:
        with open(mod_file_path, 'rb') as f:
            files = {
                'data': (None, json.dumps(metadata), 'application/json'),
                'file': (os.path.basename(mod_file_path), f, 'application/octet-stream')
            }
            response = requests.post(url, headers=headers, files=files)

        response.raise_for_status()

        print("File was successfully uploaded to Modrinth!")
        print(f"API Response: {response.json()}")

    except requests.exceptions.HTTPError as e:
        print(f"HTTP Error: {e}")
        if response.content:
            try:
                error_response = response.json()
                print(f"API Error: {error_response}")
            except json.JSONDecodeError:
                print(f"API Error (invalid JSON): {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Request Error: {e}")
    except Exception as e:
        print(f"Other Error: {e}")

def upload_to_curseforge(api_token, project_id, version_number, mod_file_path, loaders, game_versions, release_type, changelog, version_name):
    if not os.path.exists(mod_file_path):
        print(f"Error: File '{mod_file_path}' was not found!")
        return

    url = f"https://minecraft.curseforge.com/api/projects/{project_id}/upload-file"
    headers = {
        "X-Api-Token": api_token,
        "User-Agent": CURSEFORGE_USER_AGENT
    }

    cf_game_version_ids = []
    for loader in loaders:
        mapping = get_curseforge_game_versions_mapping(api_token)

        gv_id = list(filter(lambda x: x["name"] == game_versions[0], mapping))
        if gv_id:
            cf_game_version_ids.append(gv_id)
        else:
            print(f"CurseForge warning: Missing mapping for '{loader}'.")
            return

    metadata = {
        "changelog": changelog,
        "changelogType": "markdown",
        "displayName": version_name,
        "fileName": os.path.basename(mod_file_path),
        "gameVersions": cf_game_version_ids,
        "modLoaders": loaders,
        "releaseType": release_type, # 'release', 'beta', 'alpha'
    }

    print(f"\n--- Uploading to CurseForge ---")
    print(f"File: {mod_file_path}")
    # print(f"Metadata: {json.dumps(metadata, indent=2)}")
    print("Do you want to proceed?")
    yes_no = input()

    if not yes_no.startswith("y"):
        print("Skipping upload...")
        return

    try:
        with open(mod_file_path, 'rb') as f:
            files = {
                'file': (os.path.basename(mod_file_path), f, 'application/octet-stream'),
                'metadata': (None, json.dumps(metadata), 'application/json')
            }
            response = requests.post(url, headers=headers, files=files)

        response.raise_for_status()

        print("Mod was successfully uploaded to CurseForge!")
        print(f"API Response: {response.json()}")
        return

    except requests.exceptions.HTTPError as e:
        print(f"CurseForge HTTP Error: {e}")
        if response.content:
            try:
                error_response = response.json()
                print(f"CurseForge Response Error from API: {error_response}")
            except json.JSONDecodeError:
                print(f"CurseForge Response Error from API (invalid JSON): {response.text}")
        return
    except requests.exceptions.RequestException as e:
        print(f"CurseForge: Request error: {e}")
        return
    except Exception as e:
        print(f"CurseForge: Unexpected error: {e}")
        return

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Upload mod to Modrinth.")
    parser.add_argument("--modrinth-api-token", required=True, help="Modrinth API Token")
    parser.add_argument("--curseforge-api-token", required=True, help="CurseForge API Token")
    parser.add_argument("--release-type", required=True, help="Mod release type (release|beta|alpha)")

    args = parser.parse_args()
    props = read_properties(keys_to_find=["version", "minecraft_version", "mod_name"])
    version_changelog = read_changelog()

    mod_loaders = [["forge", "neoforge"], ["fabric"]]
    mod_dependencies = list(map(prepare_dependency, ["fRiHVvU7", "u6dRKJwZ", "nfn13YXA"])) # EMI JEI REI

    for mod_loader in mod_loaders:
        print (f"processing {mod_loader} launcher")
        path = f"./{mod_loader[0]}/build/libs"
        version = f"{props['minecraft_version']}-{props['version']}"
        file_name = f"{props['mod_name']}-{mod_loader[0]}-{version}.jar"
        file_path = f"{path}/{file_name}"
        name = f"{re.sub(r'(?<!^)(?=[A-Z])', ' ', props['mod_name'])} {version}"

        upload_to_modrinth(
            api_token=args.modrinth_api_token,
            project_id="PEPVViac",
            mod_file_path=file_path,
            version_number=version,
            loaders=mod_loader,
            game_versions=[props['minecraft_version']],
            changelog="\n".join(version_changelog),
            dependencies=mod_dependencies,
            release_type=args.release_type,
            version_name=name,
        )

        upload_to_curseforge(
            api_token=args.curseforge_api_token,
            project_id="1205426",
            version_number=version,
            mod_file_path=file_path,
            loaders=mod_loader,
            game_versions=[props['minecraft_version']],
            release_type=args.release_type,
            changelog="\n".join(version_changelog),
            version_name=name,
        )
        print()
