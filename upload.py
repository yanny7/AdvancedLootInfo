import argparse
import hashlib
import json
import os.path
import re

import requests


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

def read_properties(file_path="gradle.properties", keys_to_find=None):
    properties = {}

    if not os.path.exists(file_path):
        print(f"Error: File '{file_path}' doesn't exists.")
        return properties

    try:
        with open(file_path, 'r') as f:
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
        print(f"Error while reading from file '{file_path}': {e}")
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

def upload_to_modrinth(api_token, project_id, version_number, mod_file_path, loaders, game_versions, changelog, dependencies, version_type, version_name):
    if not os.path.exists(mod_file_path):
        print(f"Error: File '{mod_file_path}' was not found!")
        return

    file_hash_sha512 = calculate_sha512(mod_file_path)
    print(f"File SHA512 hash '{os.path.basename(mod_file_path)}': {file_hash_sha512}")

    url = "https://api.modrinth.com/v2/version"
    headers = {
        "Authorization": api_token,
        "User-Agent": "Modrinth-Uploader-Script/1.0 (Yanny/AdvancedLootInfo)"
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
        "version_type": version_type,
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

    print(f"Uploading file '{mod_file_path}'")
    print(f"Metadata: {json.dumps(metadata, indent=2)}")

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

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Upload mod to Modrinth.")
    parser.add_argument("--api-token", required=True, help="Modrinth API Token")
    parser.add_argument("--version-type", required=True, help="Mod version type (release|beta|alpha)")

    args = parser.parse_args()
    props = read_properties(keys_to_find=["version", "minecraft_version", "mod_name"])
    version_changelog = read_changelog()
    project = "PEPVViac"

    mod_loaders = [["forge", "neoforge"], ["fabric"]]
    mod_dependencies = list(map(prepare_dependency, ["fRiHVvU7", "u6dRKJwZ", "nfn13YXA"])) # EMI JEI REI

    for loader in mod_loaders:
        print (f"processing {loader} launcher")
        path = f"./{loader[0]}/build/libs"
        version = f"{props['minecraft_version']}-{props['version']}"
        file_name = f"{props['mod_name']}-{loader[0]}-{version}.jar"
        name = f"{re.sub(r'(?<!^)(?=[A-Z])', ' ', props['mod_name'])} {version}"

        upload_to_modrinth(
            api_token=args.api_token,
            project_id=project,
            mod_file_path=f"{path}/{file_name}",
            version_number=version,
            loaders=loader,
            game_versions=[props['minecraft_version']],
            changelog="\n".join(version_changelog),
            dependencies=mod_dependencies,
            version_type=args.version_type,
            version_name=name
        )
        print()
