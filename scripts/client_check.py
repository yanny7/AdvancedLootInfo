#!/usr/bin/env python3
"""Starts every enabled ALI/AWI client run (mod x loader x recipe viewer) one after another, joins the
"New World" save and checks that the recipe viewer received the mod's data."""

import argparse
import os
import queue
import re
import signal
import subprocess
import sys
import threading
import time
from collections import deque
from pathlib import Path

MODS = ["ali", "awi"]
VIEWERS = ["emi", "jei", "rei"]
WORLD = "New World"
SEPARATOR = "=" * 100

SUCCESS = "Data registration finished successfully."
FAILURES = [
    "Failed to finish registering data",
    "Registration thread interrupted!",
    "CRITICAL: Could not fetch",
    "---- Minecraft Crash Report ----",
    "#@!@# Game crashed!",
]
MOD_LOG = re.compile(r"\[(ali|awi|aci)] ")
GRACE_SECONDS = 3
TAIL_LINES = 60

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_DIR = SCRIPT_DIR.parent
LOG_DIR = PROJECT_DIR / "build" / "client_check"


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


def read_enabled_loaders(properties: dict):
    platforms = [platform.strip() for platform in properties.get("enabled_platforms", "").split(",") if platform.strip()]
    return [platform for platform in platforms if properties.get(f"{platform}_enabled") == "true"]


def read_combinations(properties: dict, mods: list, loaders: list, viewers: list):
    enabled_loaders = read_enabled_loaders(properties)
    combinations = []

    for mod in mods:
        for loader in loaders:
            if loader not in enabled_loaders:
                continue

            for viewer in viewers:
                if properties.get(f"{viewer}_enabled") == "true" and properties.get(f"{loader}_{viewer}_enabled") == "true":
                    combinations.append((mod, loader, viewer))

    return combinations


def find_game_pids(run_dir: Path):
    pids = []

    for entry in Path("/proc").iterdir():
        if not entry.name.isdigit():
            continue

        try:
            if Path(os.readlink(entry / "cwd")) == run_dir:
                pids.append(int(entry.name))
        except OSError:
            continue

    return pids


def stop_game(gradle: subprocess.Popen, run_dir: Path):
    for sig in (signal.SIGTERM, signal.SIGKILL):
        for pid in find_game_pids(run_dir):
            try:
                os.kill(pid, sig)
            except ProcessLookupError:
                pass

        try:
            gradle.wait(timeout=20)
            return
        except subprocess.TimeoutExpired:
            continue

    gradle.kill()
    gradle.wait()


def pump(stream, lines: queue.Queue):
    for line in stream:
        lines.put(line.rstrip("\n"))

    lines.put(None)


def run_combination(mod: str, loader: str, viewer: str, timeout: int):
    name = f"{mod}-{loader}-{viewer}"
    run_dir = PROJECT_DIR / mod / loader / "run"
    log_file = LOG_DIR / f"{name}.log"

    if not (run_dir / "saves" / WORLD).is_dir():
        return False, f"missing save {run_dir / 'saves' / WORLD}", []

    command = [
        str(PROJECT_DIR / "gradlew"), "--console=plain", f":{mod}:{loader}:runClient", f"-Precipe_viewer={viewer}",
        f"--args=--quickPlaySingleplayer '{WORLD}'",
    ]
    gradle = subprocess.Popen(command, cwd=PROJECT_DIR, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
                              encoding="utf-8", errors="replace")
    lines = queue.Queue()
    threading.Thread(target=pump, args=(gradle.stdout, lines), daemon=True).start()

    tail = deque(maxlen=TAIL_LINES)
    deadline = time.monotonic() + timeout
    result = None
    reason = f"timed out after {timeout}s"

    try:
        with log_file.open("w", encoding="utf-8") as log:
            while time.monotonic() < deadline:
                try:
                    line = lines.get(timeout=1)
                except queue.Empty:
                    continue

                if line is None:
                    if result is None:
                        result, reason = False, f"game exited (code {gradle.wait()}) before data registration"

                    break

                log.write(line + "\n")
                tail.append(line)

                if MOD_LOG.search(line):
                    print(line, flush=True)

                if result is not None:
                    continue

                if SUCCESS in line:
                    result, reason = True, SUCCESS
                    break

                failure = next((marker for marker in FAILURES if marker in line), None)

                if failure:
                    result, reason = False, failure
                    deadline = time.monotonic() + GRACE_SECONDS
    finally:
        stop_game(gradle, run_dir)

    return result is True, reason, list(tail)


def main():
    parser = argparse.ArgumentParser(description="Checks that every ALI/AWI client run loads its data into the recipe viewer.")
    parser.add_argument("--mod", action="append", choices=MODS, help="Mod to check, repeatable (default: all)")
    parser.add_argument("--loader", action="append", help="Loader to check, repeatable (default: every enabled one)")
    parser.add_argument("--viewer", action="append", choices=VIEWERS, help="Recipe viewer to check, repeatable (default: all)")
    parser.add_argument("--timeout", type=int, default=600, help="Seconds one run may take (default: 600)")
    args = parser.parse_args()

    properties = read_gradle_properties()
    loaders = args.loader or read_enabled_loaders(properties)
    combinations = read_combinations(properties, args.mod or MODS, loaders, args.viewer or VIEWERS)

    if not combinations:
        print("Error: no enabled combination matches the arguments.")
        return 1

    LOG_DIR.mkdir(parents=True, exist_ok=True)
    results = []

    for index, (mod, loader, viewer) in enumerate(combinations, 1):
        name = f"{mod}-{loader}-{viewer}"
        print(SEPARATOR)
        print(f"[{index}/{len(combinations)}] {name}", flush=True)
        print(SEPARATOR, flush=True)

        start = time.monotonic()
        ok, reason, tail = run_combination(mod, loader, viewer, args.timeout)
        elapsed = int(time.monotonic() - start)
        results.append((name, ok, reason, elapsed))

        if ok:
            print(f"[OK] {name} ({elapsed}s)", flush=True)
        else:
            print(f"[ERROR] {name} ({elapsed}s): {reason}")
            print(f"--- last {len(tail)} lines, full log: {LOG_DIR / (name + '.log')}")
            print("\n".join(tail), flush=True)

    print(SEPARATOR)

    for name, ok, reason, elapsed in results:
        print(f"{'[OK]   ' if ok else '[ERROR]'} {name:<20} {elapsed:>4}s  {'' if ok else reason}")

    failed = sum(1 for _, ok, _, _ in results if not ok)
    print(f"{len(results) - failed}/{len(results)} passed")
    return failed


if __name__ == "__main__":
    sys.exit(main())
