#!/usr/bin/env python3
"""Print a `<srg field> <mojmap owner>.<name>` table for one Minecraft version.

A production Forge mod jar is SRG-named for vanilla members, so a decompile reads
`Items.f_42616_` where the shim has to write `Items.EMERALD`. Mojang's proguard map is
obf->mojmap and Forge's tsrg is obf->srg; joining them on the obf name gives srg->mojmap.

    python3 srg_to_mojmap.py 1.20.1 > srg2moj.txt
    grep -m1 '^f_42616_ ' srg2moj.txt

Both inputs are already in the Forge Gradle cache after any build of this repo. The tsrg
directory carries a build suffix (`1.20.1-20230612.114412`), so the version argument is
matched as a prefix.
"""

import re
import sys
from pathlib import Path

CACHE = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions"


def find_inputs(version):
    proguard = CACHE / version / "client_mappings.txt"

    if not proguard.exists():
        sys.exit(f"no client_mappings.txt for {version} under {CACHE}")

    for directory in sorted(CACHE.glob(f"{version}*")):
        tsrg = directory / "mcp_mappings.tsrg"

        if tsrg.exists():
            return proguard, tsrg

    sys.exit(f"no mcp_mappings.tsrg for {version} under {CACHE}")


def read_proguard(path):
    """obf class -> (mojmap class, {obf field: mojmap field})."""
    classes = {}
    current = None

    for line in path.open():
        if line.startswith("#"):
            continue

        if not line.startswith(" "):
            match = re.match(r"(\S+) -> (\S+):", line.strip())

            if match:
                current = match.group(2)
                classes[current] = (match.group(1), {})
        elif current is not None:
            match = re.match(r"\s+(?:\d+:\d+:)?(\S+) (\S+) -> (\S+)", line.rstrip())

            # a method has parentheses in its name column; fields do not
            if match and "(" not in match.group(2):
                classes[current][1][match.group(3)] = match.group(2)

    return classes


def main():
    if len(sys.argv) != 2:
        sys.exit(f"usage: {sys.argv[0]} <minecraft version>")

    proguard_path, tsrg_path = find_inputs(sys.argv[1])
    classes = read_proguard(proguard_path)
    obf_class = None

    for line in tsrg_path.open():
        if line.startswith("tsrg2"):
            continue

        if not line.startswith("\t"):
            obf_class = line.split()[0]
        elif not line.startswith("\t\t"):
            parts = line.split()
            entry = classes.get(obf_class)

            # a field line is `obf srg id`; a method line carries a descriptor too
            if len(parts) == 3 and entry is not None:
                mojmap = entry[1].get(parts[0])

                if mojmap:
                    print(f"{parts[1]} {entry[0]}.{mojmap}")


if __name__ == "__main__":
    main()
