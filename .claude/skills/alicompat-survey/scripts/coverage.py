#!/usr/bin/env python3
"""What this repo already registers, so a static candidate can be told from a real gap.

Reads the ALI / ALICompat sources rather than a log: every `register<Hook>(X.class, ...)`
call, every `registerTrades(<id>, ...)`, every mixin target, and the `compat_mods` list.

Usage: coverage.py --repo <repo root> [--out coverage.json]
"""
import argparse
import json
import os
import re

HOOK_OF_METHOD = {
    "registerFunctionTooltip": ["function"],
    "registerCountModifier": ["function"],
    "registerItemStackModifier": ["function"],
    "registerConditionTooltip": ["condition"],
    "registerChanceModifier": ["condition"],
    "registerEntry": ["entry"],
    "registerEntryTooltip": ["entry"],
    "registerItemCollector": ["function", "entry"],
    "registerNumberProvider": ["number_provider"],
    "registerIngredientTooltip": ["ingredient"],
    "registerItemSubPredicateTooltip": ["item_sub_predicate"],
    "registerItemListing": ["item_listing"],
    "registerGlobalLootModifier": ["global_loot_modifier"],
    "registerDataComponentType": ["data_component_type"],
}

CALL = re.compile(r"\b(" + "|".join(HOOK_OF_METHOD) + r")\s*\(([^;]{0,400}?)\)\s*;", re.S)
CLASS_LITERAL = re.compile(r"([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\.class")
IMPORT = re.compile(r"^import\s+(?:static\s+)?([\w.$]+);", re.M)
IMPORT_WILDCARD = re.compile(r"^import\s+(?:static\s+)?([\w.$]+)\.\*;", re.M)
MODID_CONST = re.compile(r'(?:MOD_ID|MODID)\s*=\s*"([^"]+)"')
PACKAGE = re.compile(r"^package\s+([\w.]+);", re.M)
TRADES = re.compile(r"registerTrades\s*\((.{0,300}?);", re.S)
DECLARATION = re.compile(r"^\s*[\w.<>\[\]]+\s+\w+\s*(?:,|$)")
RL_TWO = re.compile(r'ResourceLocation\s*(?:\.\s*fromNamespaceAndPath)?\s*\(\s*([A-Za-z_$][\w$.]*|"[^"]*")\s*,\s*"([^"]+)"')
RL_ONE = re.compile(r'ResourceLocation\s*(?:\.\s*parse)?\s*\(\s*"([^"]+)"\s*\)')
MIXIN = re.compile(r'@Mixin\s*\(\s*(?:value\s*=\s*)?\{?([^)]*?)\}?\s*\)', re.S)
SKIP_DIRS = {"build", ".git", ".gradle", "run", "generated", ".idea"}


def resolve(name, imports, wildcards, package):
    """A simple name in source back to binary names. A wildcard import makes this ambiguous,
    so every possibility is returned and the caller keeps them all."""
    head = name.split(".", 1)[0]
    if "." in name and name[0].islower():
        return [name]
    if head in imports:
        return [imports[head] + name[len(head):]]
    if name[0].isupper():
        out = [f"{prefix}.{name}" for prefix in wildcards]
        if package:
            out.append(f"{package}.{name}")
        return out or [name]
    return [name]


def scan_file(path, out):
    with open(path, encoding="utf-8", errors="replace") as handle:
        text = handle.read()
    package_match = PACKAGE.search(text)
    package = package_match.group(1) if package_match else ""
    wildcards = IMPORT_WILDCARD.findall(text)
    imports = {}
    for fqn in IMPORT.findall(text):
        if not fqn.endswith("*"):
            imports[fqn.rsplit(".", 1)[-1]] = fqn
    modid_match = MODID_CONST.search(text)
    modid = modid_match.group(1) if modid_match else ""

    for method, arguments in CALL.findall(text):
        literal = CLASS_LITERAL.search(arguments)
        if not literal:
            continue
        simple = literal.group(1).rsplit(".", 1)[-1]
        for hook in HOOK_OF_METHOD[method]:
            for binary in resolve(literal.group(1), imports, wildcards, package):
                out["byHook"].setdefault(hook, {})[binary] = method
            out["simpleNames"].setdefault(hook, {})[simple] = method

    for argument in TRADES.findall(text):
        if DECLARATION.match(argument):
            continue
        two = RL_TWO.search(argument)
        one = RL_ONE.search(argument)
        if two:
            namespace = two.group(1)
            if namespace.startswith('"'):
                namespace = namespace.strip('"')
            elif namespace.split(".")[-1] in ("MOD_ID", "MODID") and modid:
                namespace = modid
            else:
                namespace = namespace or "?"
            out["traders"][f"{namespace}:{two.group(2)}"] = os.path.basename(path)
        elif one:
            value = one.group(1)
            out["traders"][value if ":" in value else f"minecraft:{value}"] = os.path.basename(path)
        else:
            out["dynamicTraders"].setdefault(os.path.basename(path), []).append(argument.strip()[:80])

    for group in MIXIN.findall(text):
        for literal in CLASS_LITERAL.findall(group):
            for binary in resolve(literal, imports, wildcards, package):
                out["mixins"][binary] = os.path.basename(path)
        for target in re.findall(r'"([\w.$]+)"', group):
            if "." in target:
                out["mixins"][target] = os.path.basename(path)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", required=True)
    parser.add_argument("--out", default="-")
    args = parser.parse_args()

    out = {"byHook": {}, "simpleNames": {}, "traders": {}, "dynamicTraders": {},
           "mixins": {}, "compatMods": []}
    for root, dirs, files in os.walk(args.repo):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for name in files:
            if name.endswith(".java"):
                scan_file(os.path.join(root, name), out)

    properties = os.path.join(args.repo, "gradle.properties")
    if os.path.isfile(properties):
        with open(properties, encoding="utf-8") as handle:
            match = re.search(r"^compat_mods=(.*)$", handle.read(), re.M)
        if match:
            out["compatMods"] = [s for s in match.group(1).strip().split(",") if s]

    out["covered"] = sorted({c for hook in out["byHook"].values() for c in hook} | set(out["mixins"]))
    text = json.dumps(out, indent=2) + "\n"
    if args.out == "-":
        print(text, end="")
    else:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(text)
        print(f"covered classes={len(out['covered'])} traders={len(out['traders'])} "
              f"hooks={','.join(f'{k}:{len(v)}' for k, v in sorted(out['byHook'].items()))}")


if __name__ == "__main__":
    main()
