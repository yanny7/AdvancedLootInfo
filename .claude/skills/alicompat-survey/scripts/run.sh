#!/usr/bin/env bash
# Whole survey from one modpack directory:
#   pack layout -> log gaps -> owning jars -> static class scan -> repo coverage
#   -> CurseForge -> one markdown report.
#
# Usage: run.sh --pack-dir <instance dir> --out <report.md>
#               [--repo <repo root>] [--work <dir>] [--targets "1.20.1:fabric,forge" ...]
#               [--minecraft-jar <jar>] [--log <file>] [--libraries <dir>]
# Needs CURSEFORGE_API_KEY in the environment.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL="$(dirname "$HERE")"
PACK_DIR=""; OUT=""; REPO=""; WORK=""; MCJAR=""; LOG=""; LIBS=""; TARGETS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --pack-dir) PACK_DIR="$2"; shift 2 ;;
    --out) OUT="$2"; shift 2 ;;
    --repo) REPO="$2"; shift 2 ;;
    --work) WORK="$2"; shift 2 ;;
    --minecraft-jar) MCJAR="$2"; shift 2 ;;
    --log) LOG="$2"; shift 2 ;;
    --libraries) LIBS="$2"; shift 2 ;;
    --targets) shift; while [[ $# -gt 0 && "$1" != --* ]]; do TARGETS+=("$1"); shift; done ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

[[ -n "$PACK_DIR" && -n "$OUT" ]] || { echo "need --pack-dir and --out" >&2; exit 2; }
REPO="${REPO:-$(git -C "$HERE" rev-parse --show-toplevel 2>/dev/null || echo "$PWD")}"
WORK="${WORK:-$(mktemp -d)}"
mkdir -p "$WORK"

DETECT=("--pack-dir" "$PACK_DIR" "--out" "$WORK/pack.json")
[[ -n "$MCJAR" ]] && DETECT+=("--minecraft-jar" "$MCJAR")
[[ -n "$LOG" ]] && DETECT+=("--log" "$LOG")
[[ -n "$LIBS" ]] && DETECT+=("--libraries" "$LIBS")
python3 "$HERE/packdir.py" "${DETECT[@]}"

MODS="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["modsDir"])' "$WORK/pack.json")"
PACK_LOG="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["log"] or "")' "$WORK/pack.json")"

if [[ -n "$PACK_LOG" ]]; then
  python3 "$HERE/parse_log.py" "$PACK_LOG" > "$WORK/gaps.json"
else
  echo "no log found — static scan only"
  echo '{"categories": {}, "counts": {}}' > "$WORK/gaps.json"
fi

python3 "$HERE/scan_jars.py" --gaps "$WORK/gaps.json" --mods-dir "$MODS" --out "$WORK/owners.json"
python3 "$HERE/classindex.py" --pack "$WORK/pack.json" --base-types "$SKILL/base_types.json" \
        --out "$WORK/candidates.json"
python3 "$HERE/coverage.py" --repo "$REPO" --out "$WORK/coverage.json"

CF=("--owners" "$WORK/owners.json" "--candidates" "$WORK/candidates.json" "--mods-dir" "$MODS"
    "--out" "$WORK/projects.json")
[[ -f "$SKILL/overrides.json" ]] && CF+=("--overrides" "$SKILL/overrides.json")
[[ ${#TARGETS[@]} -gt 0 ]] && CF+=("--targets" "${TARGETS[@]}")
python3 "$HERE/cf_lookup.py" "${CF[@]}"

python3 "$HERE/build_report.py" --gaps "$WORK/gaps.json" --candidates "$WORK/candidates.json" \
        --coverage "$WORK/coverage.json" --projects "$WORK/projects.json" \
        --owners "$WORK/owners.json" --pack "$WORK/pack.json" --out "$OUT"

echo "intermediate json in $WORK"
