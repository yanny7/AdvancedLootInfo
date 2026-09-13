import argparse
import functools
import hashlib
import json
import os.path
import re

import requests

MODRINTH_USER_AGENT = "Modrinth-Uploader-Script/1.0 (Yanny/AdvancedLootInfo)"
MODRINTH_VIEWER_PROJECT_IDS = ["fRiHVvU7", "u6dRKJwZ", "nfn13YXA"] # EMI JEI REI
MODRINTH_LOOTJS_PROJECT_ID = "fJFETWDN"
MODRINTH_ACI_PROJECT_ID = "BaR4ijFC"
MODRINTH_ENVIRONMENT = "client_and_server"
CURSEFORGE_USER_AGENT = "CurseForge-Uploader-Script/1.0 (Yanny/AdvancedLootInfo)"

def calculate_sha512(file_path: str):
    sha512_hash = hashlib.sha512()

    with open(file_path, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha512_hash.update(byte_block)

    return sha512_hash.hexdigest()

def print_response_body(response):
    if response is None or not response.content:
        return

    try:
        print(f"API Error: {response.json()}")
    except json.JSONDecodeError:
        print(f"API Error (invalid JSON): {response.text}")

def read_env_secret(env_name: str):
    value = os.environ.get(env_name)

    if not value:
        print(f"Error: neither the matching argument nor the environment variable '{env_name}' is set.")
        return None

    return value

def prepare_dependency(dep_id, dependency_type="optional"):
    return {
        "project_id": dep_id,
        "dependency_type": dependency_type
    }

@functools.lru_cache(maxsize=1)
def get_curseforge_game_version_types_mapping(api_key: str):
    print("CurseForge: Loading minecraft version mappings...")
    url = "https://api.curseforge.com/v1/games/432/version-types/"
    headers = {
        "x-api-key": api_key,
        "User-Agent": CURSEFORGE_USER_AGENT
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        versions_data = response.json()["data"]

        version_map = {item['name']: item['id'] for item in versions_data}
        print(f"CurseForge: Loaded mapping for {len(version_map)} game versions.")
        return version_map
    except requests.exceptions.RequestException as e:
        print(f"CurseForge Error while reading game version types: {e}")
        print_response_body(e.response)
        return {}

@functools.lru_cache(maxsize=1)
def get_curseforge_game_versions_mapping(api_token: str):
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

        print(f"CurseForge: Loaded mapping for {len(versions_data)} game versions.")
        return list(versions_data)
    except requests.exceptions.RequestException as e:
        print(f"CurseForge Error while reading game versions: {e}")
        print_response_body(e.response)
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

def map_loader(loader_name: str):
    if loader_name == "forge":
        return 1
    elif loader_name == "fabric":
        return 4
    elif loader_name == "neoforge":
        return 6
    else:
        return None

def read_changelog(filename: str):
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

def upload_to_modrinth(api_token: str, project_id: str, version_number: str, mod_file_path: str, loaders: list, game_versions: list, changelog: str, dependencies: list, release_type: str, version_name: str, environment: str):
    if not os.path.exists(mod_file_path):
        print(f"Error: File '{mod_file_path}' was not found!")
        return False

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
        "environment": environment,
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
    # print("Do you want to proceed?")
    # yes_no = input()
    #
    # if not yes_no.startswith("y"):
    #     print("Skipping upload...")
    #     return False

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
        return True

    except requests.exceptions.HTTPError as e:
        print(f"HTTP Error: {e}")
        print_response_body(e.response)
    except requests.exceptions.RequestException as e:
        print(f"Request Error: {e}")
    except Exception as e:
        print(f"Other Error: {e}")

    return False

def upload_to_curseforge(api_token: str, api_key: str, project_id: str, version_number: str, mod_file_path: str, loaders: list, game_versions: list, release_type: str, changelog: str, version_name: str):
    if not os.path.exists(mod_file_path):
        print(f"Error: File '{mod_file_path}' was not found!")
        return False

    url = f"https://minecraft.curseforge.com/api/projects/{project_id}/upload-file"
    headers = {
        "X-Api-Token": api_token,
        "User-Agent": CURSEFORGE_USER_AGENT
    }

    cf_game_version_ids = []
    mapping = get_curseforge_game_versions_mapping(api_token)

    # get loader versions
    for loader in loaders:
        gv_id = list(filter(lambda x: x["slug"] == loader, mapping))
        if gv_id:
            cf_game_version_ids.append(gv_id[0]["id"])
        else:
            print(f"CurseForge warning: Missing mapping for '{loader}'.")
            return False

    # get minecraft version
    type_mapping = get_curseforge_game_version_types_mapping(api_key)
    mc_group = re.search(r'\d+\.\d+', game_versions[0]).group()
    tv_id = type_mapping.get(f"Minecraft {mc_group}")
    if tv_id:
        gv_id = list(filter(lambda x: x["name"] == game_versions[0] and x["gameVersionTypeID"] == tv_id, mapping))

        if gv_id:
            cf_game_version_ids.append(gv_id[0]["id"])
        else:
            print(f"CurseForge warning: Missing game version for '{mc_group}'.")
            return False
    else:
        print(f"CurseForge warning: Missing mapping for '{mc_group}'.")
        return False

    # CurseForge rejects Minecraft Java uploads without an explicit Client/Server environment
    env_type_id = type_mapping.get("Environment")
    if not env_type_id:
        print("CurseForge warning: Missing mapping for 'Environment'.")
        return False

    for environment in ["client", "server"]:
        gv_id = list(filter(lambda x: x["slug"] == environment and x["gameVersionTypeID"] == env_type_id, mapping))

        if gv_id:
            cf_game_version_ids.append(gv_id[0]["id"])
        else:
            print(f"CurseForge warning: Missing environment mapping for '{environment}'.")
            return False

    metadata = {
        "changelog": changelog,
        "changelogType": "markdown",
        "displayName": os.path.basename(mod_file_path),
        "fileName": os.path.basename(mod_file_path),
        "gameVersions": cf_game_version_ids,
        "modLoaders": loaders,
        "releaseType": release_type, # 'release', 'beta', 'alpha'
    }

    print(f"\n--- Uploading to CurseForge ---")
    print(f"File: {mod_file_path}")
    # print(f"Metadata: {json.dumps(metadata, indent=2)}")
    # print("Do you want to proceed?")
    # yes_no = input()
    #
    # if not yes_no.startswith("y"):
    #     print("Skipping upload...")
    #     return False

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
        return True

    except requests.exceptions.HTTPError as e:
        print(f"CurseForge HTTP Error: {e}")
        print_response_body(e.response)
    except requests.exceptions.RequestException as e:
        print(f"CurseForge: Request error: {e}")
    except Exception as e:
        print(f"CurseForge: Unexpected error: {e}")

    return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Upload mod to Modrinth.")
    parser.add_argument("--modrinth-api-token", help="Modrinth API Token (default: $MODRINTH_API_TOKEN)")
    parser.add_argument("--curseforge-api-token", help="CurseForge API Token (default: $CURSEFORGE_API_TOKEN)")
    parser.add_argument("--curseforge-api-key", help="CurseForge API Key (default: $CURSEFORGE_API_KEY)")
    parser.add_argument("--release-type", required=True, help="Mod release type (release|beta|alpha)")
    parser.add_argument("--mod-id", required=True, help="Mod ID (ali|awi|aci)")
    parser.add_argument("--modrinth-project-id", required=True, help="Modrinth project ID")
    parser.add_argument("--curseforge-project-id", required=True, help="CurseForge project ID")
    parser.add_argument("--target", default="all", choices=["all", "modrinth", "curseforge"], help="Which platform to upload to (default: all)")

    args = parser.parse_args()
    upload_modrinth = args.target in ("all", "modrinth")
    upload_curseforge = args.target in ("all", "curseforge")
    modrinth_api_token = None
    curseforge_api_token = None
    curseforge_api_key = None

    if upload_modrinth:
        modrinth_api_token = args.modrinth_api_token or read_env_secret("MODRINTH_API_TOKEN")

        if not modrinth_api_token:
            raise SystemExit(1)

    if upload_curseforge:
        curseforge_api_token = args.curseforge_api_token or read_env_secret("CURSEFORGE_API_TOKEN")
        curseforge_api_key = args.curseforge_api_key or read_env_secret("CURSEFORGE_API_KEY")

        if not curseforge_api_token or not curseforge_api_key:
            raise SystemExit(1)

    props = read_properties(keys_to_find=["ali_version", "awi_version", "aci_version", "minecraft_version", "ali_mod_name", "awi_mod_name", "aci_mod_name", "enabled_platforms"])
    version_changelog = read_changelog(filename=f"{args.mod_id}/CHANGELOG.md")

    mod_loaders = [[platform] for platform in props["enabled_platforms"].split(",")]

    # branches without a neoforge module ship one Forge jar that NeoForge loads too
    if ["forge"] in mod_loaders and ["neoforge"] not in mod_loaders:
        mod_loaders[mod_loaders.index(["forge"])] = ["forge", "neoforge"]

    if args.mod_id == "aci":
        mod_dependencies = []
    else:
        mod_dependencies = list(map(prepare_dependency, MODRINTH_VIEWER_PROJECT_IDS))
        mod_dependencies.append(prepare_dependency(MODRINTH_ACI_PROJECT_ID, "required"))

        if args.mod_id == "ali":
            mod_dependencies.append(prepare_dependency(MODRINTH_LOOTJS_PROJECT_ID))

    failed = False

    for mod_loader in mod_loaders:
        print (f"processing {mod_loader} launcher")
        path = f"./{args.mod_id}/{mod_loader[0]}/build/libs"
        version = f"{props['minecraft_version']}-{props[f"{args.mod_id}_version"]}"
        file_name = f"{props[f"{args.mod_id}_mod_name"]}-{mod_loader[0]}-{version}.jar"
        file_path = f"{path}/{file_name}"
        name = f"{re.sub(r'(?<!^)(?=[A-Z])', ' ', props[f"{args.mod_id}_mod_name"])} {version}"

        modrinth_uploaded = not upload_modrinth or upload_to_modrinth(
            api_token=modrinth_api_token,
            project_id=args.modrinth_project_id,
            mod_file_path=file_path,
            version_number=version,
            loaders=mod_loader,
            game_versions=[props['minecraft_version']],
            changelog="\n".join(version_changelog),
            dependencies=mod_dependencies,
            release_type=args.release_type,
            version_name=name,
            environment=MODRINTH_ENVIRONMENT,
        )

        curseforge_uploaded = not upload_curseforge or upload_to_curseforge(
            api_token=curseforge_api_token,
            api_key=curseforge_api_key,
            project_id=args.curseforge_project_id,
            version_number=version,
            mod_file_path=file_path,
            loaders=mod_loader,
            game_versions=[props['minecraft_version']],
            release_type=args.release_type,
            changelog="\n".join(version_changelog),
            version_name=name,
        )

        if not modrinth_uploaded or not curseforge_uploaded:
            failed = True

        print()

    if failed:
        raise SystemExit(1)
