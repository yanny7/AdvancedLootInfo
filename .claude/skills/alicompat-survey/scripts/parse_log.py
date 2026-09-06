#!/usr/bin/env python3
"""Parse an ALI/ACI server log (or a hand-trimmed excerpt) into structured gaps.

Output: JSON {"categories": {<category>: [<subject>, ...]}, "counts": {...}}
A subject is either a fully qualified Java class name or a namespaced id.
"""
import json
import re
import sys
from collections import OrderedDict

PATTERNS = [
    (re.compile(r"Unable to locate destination for auto GLM (\S+)"), "auto_glm_unresolved"),
    (re.compile(r"Missing GLM for (\S+)"), "global_loot_modifier"),
    (re.compile(r"Skipping unexpected loot action (\S+)"), "unexpected_loot_action"),
    (re.compile(r"Using MerchantOffer fallback for trade item listing (\S+?),"), "trade_listing_fallback"),
    (re.compile(r"Loot table (\S+) belongs to no entity"), "unresolved_entity_loot_table"),
    (re.compile(r"\[ali\] Missing (.+?) for (\S+)"), None),
]

CATEGORY_SLUG = {
    "entry factories": "entry_factory",
    "number converters": "number_provider",
    "trade item listings": "trade_item_listing",
    "function tooltips": "function_tooltip",
    "condition tooltips": "condition_tooltip",
    "value tooltips": "value_tooltip",
    "item sub predicate tooltips": "item_sub_predicate_tooltip",
    "data component type tooltips": "data_component_type_tooltip",
}


def parse(lines):
    out = OrderedDict()
    for line in lines:
        for pattern, category in PATTERNS:
            match = pattern.search(line)
            if not match:
                continue
            if category is None:
                label, subject = match.group(1), match.group(2)
                category = CATEGORY_SLUG.get(label, re.sub(r"[^a-z0-9]+", "_", label.lower()).strip("_"))
            else:
                subject = match.group(1)
            out.setdefault(category, [])
            if subject not in out[category]:
                out[category].append(subject)
            break
    return out


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else "-"
    stream = sys.stdin if path == "-" else open(path, encoding="utf-8", errors="replace")
    with stream:
        categories = parse(stream)
    json.dump(
        {"categories": categories, "counts": {k: len(v) for k, v in categories.items()}},
        sys.stdout,
        indent=2,
    )
    sys.stdout.write("\n")


if __name__ == "__main__":
    main()
