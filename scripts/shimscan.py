#!/usr/bin/env python3
"""Diffs every built ALICompat shim against the target-mod jar it is pinned to.

Four findings per shim: a class the jar has and the shim does not register, a class the
shim registers that the jar no longer has, a class the shim registers that no longer
inherits the hook's base type, and a registered class whose instance fields differ between
the pinned jar and the newest one - the last two still compile and silently render nothing
or too little.

The hooks and their base types come from base_types.json, the same file the alicompat-survey
skill scans modpacks with.
"""
import io
import json
import re
import struct
import zipfile
from pathlib import Path

import classfile

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_DIR = SCRIPT_DIR.parent
BASE_TYPES_FILE = SCRIPT_DIR / "base_types.json"
IGNORE_FILE = "scan_ignore.json"
JAR_CACHE = PROJECT_DIR / "build" / "compat_jars"
GRADLE_FILES = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"
GRADLE_CACHE = GRADLE_FILES / "curse.maven"
LOOM_CACHE = PROJECT_DIR / ".gradle" / "loom-cache" / "minecraftMaven" / "net" / "minecraft"
MAPPINGS_CACHE = Path.home() / ".gradle" / "caches" / "fabric-loom"

HOOK_OF_METHOD = {
    "registerFunctionTooltip": ["function"],
    "registerCountModifier": ["function"],
    "registerItemStackModifier": ["function"],
    "registerConditionTooltip": ["condition"],
    "registerChanceModifier": ["condition"],
    "registerEntry": ["entry"],
    "registerEntryTooltip": ["entry"],
    "registerNumberProvider": ["number_provider"],
    "registerIngredientTooltip": ["ingredient"],
    "registerItemListing": ["item_listing"],
    "registerGlobalLootModifier": ["global_loot_modifier"],
}

CALL = re.compile(r"\b(" + "|".join(HOOK_OF_METHOD) + r")\s*\(([^;]{0,400}?)\)\s*;", re.S)
CLASS_LITERAL = re.compile(r"([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\.class")
CLASS_ACCESSOR = re.compile(r'@ClassAccessor\s*\(\s*"([^"]+)"\s*\)[\s\S]{0,400}?\bclass\s+(\w+)')
IMPORT = re.compile(r"^import\s+(?:static\s+)?([\w.$]+);", re.M)
IMPORT_WILDCARD = re.compile(r"^import\s+(?:static\s+)?([\w.$]+)\.\*;", re.M)
PACKAGE = re.compile(r"^package\s+([\w.]+);", re.M)
DECLARATION = re.compile(r"\b(?:class|record|interface|enum)\s+([A-Z]\w*)")
TRADES = re.compile(r"\bregisterTrades\s*\(")
TRADES_PATH = re.compile(r'ResourceLocation\s*\([^,)]+,\s*"([^"]+)"\s*\)')
TRADER_ENTITY_HOOK = "trader_entity"


def load_base_types():
    return json.loads(BASE_TYPES_FILE.read_text(encoding="utf-8"))


def source_set(loader: str, key: str):
    return PROJECT_DIR / "alicompat" / loader / "src" / "compat" / key


def read_ignored(loader: str, key: str):
    path = source_set(loader, key) / IGNORE_FILE

    if not path.is_file():
        return {}

    return json.loads(path.read_text(encoding="utf-8"))


def _resolve(name: str, imports: dict, wildcards: list, package: str):
    head = name.split(".", 1)[0]

    if head in imports:
        return [imports[head] + name[len(head):]]

    if name[0].isupper():
        return [f"{prefix}.{name}" for prefix in wildcards] + ([f"{package}.{name}"] if package else [])

    return [name]


def read_registrations(loader: str, key: str):
    """{hook: [[every binary name one `X.class` literal could mean]]} for one shim source set.

    A wildcard import makes the spelling ambiguous, so the alternatives stay grouped: the class
    is missing from the jar only when none of them is in it."""
    hooks = {}
    accessors = {}

    for path in sorted(source_set(loader, key).rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        package_match = PACKAGE.search(text)
        package = package_match.group(1) if package_match else ""
        wildcards = IMPORT_WILDCARD.findall(text)
        imports = {fqn.rsplit(".", 1)[-1]: fqn for fqn in IMPORT.findall(text)}

        for method, arguments in CALL.findall(text):
            literal = CLASS_LITERAL.search(arguments)

            if not literal:
                continue

            alternatives = _resolve(literal.group(1), imports, wildcards, package)

            for hook in HOOK_OF_METHOD[method]:
                hooks.setdefault(hook, []).append(alternatives)

        for target, accessor in CLASS_ACCESSOR.findall(text):
            accessors[f"{package}.{accessor}" if package else accessor] = target

    for alternatives in [group for groups in hooks.values() for group in groups]:
        targets = [accessors[name] for name in alternatives if name in accessors]

        if targets:
            alternatives[:] = targets

    return hooks


def read_declared(loader: str, key: str):
    declared = set()

    for path in source_set(loader, key).rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="replace")
        package_match = PACKAGE.search(text)
        package = package_match.group(1) if package_match else ""
        declared |= {f"{package}.{name}" if package else name for name in DECLARATION.findall(text)}

    return declared


def _first_argument(text: str, start: int):
    """Everything up to the comma that separates the first argument, nested calls included."""
    depth = 0

    for offset in range(start, min(len(text), start + 400)):
        character = text[offset]

        if character in "([{":
            depth += 1
        elif character in ")]}":
            if depth == 0:
                return text[start:offset]

            depth -= 1
        elif character == "," and depth == 0:
            return text[start:offset]

    return text[start:start + 400]


def read_trades(loader: str, key: str):
    """The entity ids a shim hands `registerTrades`, and whether any of them is not a literal.

    Trading entities are the one hook keyed on an entity id rather than on a class, so a shim
    covering one never names its class anywhere."""
    paths = set()
    dynamic = False

    for path in sorted(source_set(loader, key).rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")

        for call in TRADES.finditer(text):
            literal = TRADES_PATH.search(_first_argument(text, call.end()))

            if literal:
                paths.add(literal.group(1))
            else:
                dynamic = True

    return paths, dynamic


def entity_path(name: str):
    """`com/legacy/farlanders/entity/ElderFarlanderEntity` -> `elder_farlander`, the registry
    path a mod almost always gives that class."""
    simple = name.rsplit("/", 1)[-1].rsplit("$", 1)[-1]

    if simple.endswith("Entity") and simple != "Entity":
        simple = simple[:-len("Entity")]

    return re.sub(r"(?<!^)(?=[A-Z])", "_", simple).lower()


def cached_jar(pin: dict):
    """The pinned file if gradle already downloaded it, or if an earlier scan cached it."""
    coordinate = f"{pin['slug']}-{pin['project_id']}"
    gradle = GRADLE_CACHE / coordinate / str(pin["file_id"])

    if gradle.is_dir():
        for jar in sorted(gradle.glob(f"*/{coordinate}-{pin['file_id']}.jar")):
            return jar

    jar = JAR_CACHE / f"{coordinate}-{pin['file_id']}.jar"
    return jar if jar.is_file() else None


def download_jar(session, pin: dict, url: str):
    jar = JAR_CACHE / f"{pin['slug']}-{pin['project_id']}-{pin['file_id']}.jar"
    JAR_CACHE.mkdir(parents=True, exist_ok=True)
    response = session.get(url, timeout=120)
    response.raise_for_status()
    jar.write_bytes(response.content)
    return jar


def maven_jar(session, repo: str, coordinate: str):
    """One `group:artifact:version` jar: from gradle's cache, from an earlier scan, or downloaded from `repo`."""
    group, artifact, version = coordinate.split(":")
    name = f"{artifact}-{version}.jar"

    for jar in sorted((GRADLE_FILES / group / artifact / version).glob(f"*/{name}")):
        return jar

    jar = JAR_CACHE / group / name

    if not jar.is_file():
        response = session.get(f"{repo.rstrip('/')}/{group.replace('.', '/')}/{artifact}/{version}/{name}", timeout=120)
        response.raise_for_status()
        jar.parent.mkdir(parents=True, exist_ok=True)
        jar.write_bytes(response.content)

    return jar


def minecraft_jar(loader: str, minecraft_version: str):
    """The loom-remapped Minecraft jar, which supplies the vanilla half of every hierarchy."""
    if not LOOM_CACHE.is_dir():
        return None

    prefix = "minecraft-merged-" if loader == "fabric" else f"{loader}-"
    candidates = [
        jar for directory in LOOM_CACHE.glob(f"{prefix}*")
        for version in directory.iterdir()
        if version.name == minecraft_version or version.name.startswith(f"{minecraft_version}-")
        for jar in version.glob("*.jar")
        if not jar.name.endswith("-sources.jar")
    ]

    if not candidates:
        return None

    return max(candidates, key=lambda jar: jar.stat().st_mtime)


def intermediary_names(minecraft_version: str):
    """intermediary -> Mojang class names.

    A Fabric jar on CurseForge names Minecraft classes in intermediary (`net/minecraft/class_2073`),
    so without this every supertype chain dies at the first vanilla class. Forge jars carry Mojang
    names already and get an empty map."""
    root = MAPPINGS_CACHE / minecraft_version

    for mappings in sorted(root.glob("loom.mappings.*-v2/mappings.tiny")):
        renames = {}

        with mappings.open(encoding="utf-8") as handle:
            for line in handle:
                columns = line.rstrip("\n").split("\t")

                if len(columns) >= 4 and columns[0] == "c":
                    renames[columns[2]] = columns[3]

        return renames

    return {}


def index_jar(path: Path, index: dict, owned: set = None):
    """name -> (supername, interfaces, access) for every class in one jar."""
    with zipfile.ZipFile(path) as archive:
        for entry in archive.namelist():
            # bundled libraries feed the hierarchy only; owning them would list their classes as the host mod's findings
            if entry.startswith("META-INF/jars/") and entry.endswith(".jar"):
                nested = {}

                try:
                    index_jar(io.BytesIO(archive.read(entry)), nested)
                except zipfile.BadZipFile:
                    continue

                for name, value in nested.items():
                    index.setdefault(name, value)

                continue

            if not entry.endswith(".class"):
                continue

            try:
                info = classfile.parse(archive.read(entry))
            except (classfile.ClassFileError, KeyError, OSError, struct.error):
                continue

            if not info.name:
                continue

            index[info.name] = (info.supername, info.interfaces, info.access)

            if owned is not None:
                owned.add(info.name)


def supertypes(name: str, index: dict, cache: dict, renames: dict = None):
    known = cache.get(name)

    if known is not None:
        return known

    cache[name] = frozenset()
    entry = index.get(name)
    closure = {name}

    if entry:
        supername, interfaces, _ = entry

        for parent in [supername, *interfaces]:
            if parent:
                closure |= supertypes((renames or {}).get(parent, parent), index, cache, renames)

    closure = frozenset(closure)
    cache[name] = closure
    return closure


def hook_map(base_types: dict, index: dict):
    """base type internal name -> the hooks it feeds, skipping names absent on this version."""
    mapping = {}

    for hook, spec in base_types["hooks"].items():
        for base in spec["bases"]:
            if base in index:
                mapping.setdefault(base, []).append(hook)

    return mapping


def _is_candidate(name: str, access: int, base_types: dict):
    if access & (classfile.ACC_INTERFACE | classfile.ACC_ABSTRACT | classfile.ACC_SYNTHETIC):
        return False

    if any(name.startswith(root) for root in base_types["excludedRoots"]):
        return False

    segments = set(name.split("/")[:-1])
    return not segments & set(base_types["excludedPackageSegments"])


def _binary_variants(name: str):
    """`a.b.Outer.Inner` in source is `a/b/Outer$Inner` in the jar, and which dot is the
    nesting one is not knowable from the source alone."""
    parts = name.split(".")

    for split in range(len(parts), 0, -1):
        yield "/".join(parts[:split - 1] + ["$".join(parts[split - 1:])]) if split < len(parts) \
            else "/".join(parts)


PRIMITIVES = {"B": "byte", "C": "char", "D": "double", "F": "float", "I": "int", "J": "long",
              "S": "short", "Z": "boolean", "V": "void"}


def type_name(descriptor: str):
    arrays = len(descriptor) - len(descriptor.lstrip("["))
    element = descriptor[arrays:]

    if element.startswith("L"):
        element = element[1:-1].rsplit("/", 1)[-1].replace("$", ".")
    else:
        element = PRIMITIVES.get(element, element)

    return element + "[]" * arrays


def _jar_classes(jar: Path):
    with zipfile.ZipFile(jar) as archive:
        classes = {entry[:-len(".class")] for entry in archive.namelist() if entry.endswith(".class")}

        for entry in archive.namelist():
            if entry.startswith("META-INF/jars/") and entry.endswith(".jar"):
                try:
                    classes |= _jar_classes(io.BytesIO(archive.read(entry)))
                except zipfile.BadZipFile:
                    continue

    return classes


def _read_fields(jar: Path, names: set):
    fields = {}

    with zipfile.ZipFile(jar) as archive:
        for name in names:
            try:
                fields[name] = classfile.field_types(classfile.parse(archive.read(f"{name}.class")))
            except (classfile.ClassFileError, KeyError, OSError, struct.error):
                continue

        for entry in archive.namelist():
            rest = names - fields.keys()

            if not rest:
                break

            if entry.startswith("META-INF/jars/") and entry.endswith(".jar"):
                try:
                    fields.update(_read_fields(io.BytesIO(archive.read(entry)), rest))
                except zipfile.BadZipFile:
                    continue

    return fields


def diff_fields(registered: dict, old_jars: list, new_jars: list):
    """{hook: {class: (added, removed)}} for registered classes whose instance fields moved.

    A class that keeps its name and its base type is invisible to the other three findings, so
    a field the target mod adds to one is a tooltip that silently stops being complete."""
    old_classes = set().union(*map(_jar_classes, old_jars))
    new_classes = set().union(*map(_jar_classes, new_jars))
    wanted = {}

    for hook, groups in registered.items():
        for alternatives in groups:
            found = next((variant for name in alternatives for variant in _binary_variants(name)
                          if variant in new_classes or variant in old_classes), None)

            if found:
                wanted.setdefault(hook, set()).add(found)

    names = {name for hooked in wanted.values() for name in hooked}
    before = {}
    after = {}

    for jar in old_jars:
        before.update(_read_fields(jar, names & old_classes - before.keys()))

    for jar in new_jars:
        after.update(_read_fields(jar, names & new_classes - after.keys()))

    changed = {}

    for hook, hooked in wanted.items():
        for name in hooked:
            if name not in before or name not in after:
                continue

            gone = set(before[name]) - set(after[name])
            fresh = set(after[name]) - set(before[name])

            if gone or fresh:
                changed.setdefault(hook, {})[name.replace("/", ".")] = (
                    sorted(f"{field} {type_name(descriptor)}" for field, descriptor in fresh),
                    sorted(f"{field} {type_name(descriptor)}" for field, descriptor in gone),
                )

    return changed


def build_index(jars: list):
    """One class index for a whole loader: every pinned target jar plus Minecraft.

    A shim's own supertype chain routinely leaves its jar - a sawmill trade listing implements a
    moonlight interface - so a jar indexed alone loses the hook.
    """
    index = {}
    owned = {}

    for jar in jars:
        names = set()
        index_jar(jar, index, names)
        owned[jar] = names

    return index, owned


def scan(loader: str, key: str, owned: set, index: dict, bases: dict, cache: dict, base_types: dict,
         renames: dict = None, versions: tuple = None):
    """Diffs one shim source set against the jars it is pinned to.

    `versions` is the (pinned jars, newest jars) pair, when the two differ."""
    roots = {name.split("/")[0] for name in owned}
    ignored = read_ignored(loader, key)
    registered = read_registrations(loader, key)
    declared = read_declared(loader, key)
    covered = {
        hook: {spelling for group in groups for name in group for spelling in (name, name.replace(".", "$"))}
        for hook, groups in registered.items()
    }

    new = {}
    missing = {}
    detached = {}

    traded, dynamic_trades = read_trades(loader, key)

    for name in sorted(owned):
        _, _, access = index[name]

        if not _is_candidate(name, access, base_types):
            continue

        if ignored.keys() & _dotted_variants(name):
            continue

        inherited = {spelling for parent in supertypes(name, index, cache, renames)
                     for spelling in _dotted_variants(parent)}

        for base in supertypes(name, index, cache, renames) & set(bases):
            for hook in bases[base]:
                if covered.get(hook, set()) & inherited:
                    continue

                if hook == TRADER_ENTITY_HOOK and (dynamic_trades or entity_path(name) in traded):
                    continue

                new.setdefault(hook, set()).add(name.replace("/", "."))

    for hook, groups in registered.items():
        wanted = {base for base, hooks in bases.items() if hook in hooks}

        for alternatives in groups:
            found = next((variant for name in alternatives
                          for variant in _binary_variants(name) if variant in index), None)

            if found is None:
                if not declared & set(alternatives) and any(name.split(".")[0] in roots for name in alternatives):
                    missing.setdefault(hook, set()).add(alternatives[0])

                continue

            if wanted and found in owned and not supertypes(found, index, cache, renames) & wanted:
                detached.setdefault(hook, set()).add(found.replace("/", "."))

    changed = diff_fields(registered, *versions) if versions else {}
    return {"new": new, "missing": missing, "detached": detached, "changed": changed}


def _dotted_variants(name: str):
    """Every spelling of a jar class a source file could have used to name it."""
    dotted = name.replace("/", ".")
    return {dotted, dotted.replace("$", ".")}
