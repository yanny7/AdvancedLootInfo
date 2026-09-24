# Advanced Loot Info

EMI / JEI / REI plugins that show what loot tables, villager trades and world generation actually contain — in the recipe viewer you already use, with no commands and no wiki digging.

[![License](https://img.shields.io/github/license/yanny7/AdvancedLootInfo)](LICENSE)
[![ALI on Modrinth](https://img.shields.io/modrinth/dt/PEPVViac?label=ALI&logo=modrinth)](https://modrinth.com/mod/advanced-loot-info)
[![ALI on CurseForge](https://img.shields.io/curseforge/dt/1205426?label=ALI&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/advanced-loot-info)
[![AWI on Modrinth](https://img.shields.io/modrinth/dt/Ru0LLQdN?label=AWI&logo=modrinth)](https://modrinth.com/mod/advanced-worldgen-info)
[![AWI on CurseForge](https://img.shields.io/curseforge/dt/1661937?label=AWI&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/advanced-worldgen-info)
[![ALICompat on Modrinth](https://img.shields.io/modrinth/dt/qCne5RZz?label=ALICompat&logo=modrinth)](https://modrinth.com/mod/ali-compat)
[![ALICompat on CurseForge](https://img.shields.io/curseforge/dt/1700110?label=ALICompat&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/ali-compat)
[![Wiki](https://img.shields.io/badge/docs-wiki-blue)](https://github.com/yanny7/AdvancedLootInfo/wiki)

| | |
|---|---|
| <img src="https://cdn.modrinth.com/data/PEPVViac/images/14fca00909ecd699b1566461c3bd94972fb38320.png" width="420"> | <img src="https://cdn.modrinth.com/data/PEPVViac/images/7bde0c7d5728dec2576f80aa6bd29d280a902db0.png" width="420"> |
| Chest loot, pool by pool | Drop chances, per item |
| <img src="https://cdn.modrinth.com/data/PEPVViac/images/7d07b860ab07aeb35eba389b8b34e77aa78bea98.png" width="420"> | <img src="https://cdn.modrinth.com/data/Ru0LLQdN/images/ac230da99b3d2dfb69d80cf999037df8083e73a2.png" width="420"> |
| Conditions and functions in the tooltip | Worldgen, one category per dimension |
| <img src="https://cdn.modrinth.com/data/Ru0LLQdN/images/720a85129f36b0df5b3ccb3540eb39b8b54aaf1f.png" width="420"> | <img src="https://cdn.modrinth.com/data/Ru0LLQdN/images/ac0696c85b40119682b9361e0043a7eef8ef3de7.png" width="420"> |
| Where a feature can generate | ...and how often, and how deep |

## What is in this repository

Four separately versioned mods:

| Mod | Required? | What it does | Download |
|---|---|---|---|
| **ACI** — Advanced Core Info | required by ALI and AWI | Shared library: plugin discovery, tooltip trees, server → client data transfer. Does nothing on its own. | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/advanced-core-info) · [Modrinth](https://modrinth.com/mod/advanced-core-info) |
| **ALI** — Advanced Loot Info | — | Loot tables and villager trades in EMI/JEI/REI, with the conditions, functions and chances behind every drop. | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/advanced-loot-info) · [Modrinth](https://modrinth.com/mod/advanced-loot-info) |
| **AWI** — Advanced Worldgen Info | — | World generation: which features and blocks a biome places, where, how often and at which height. | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/advanced-worldgen-info) · [Modrinth](https://modrinth.com/mod/advanced-worldgen-info) |
| **ALICompat** | optional | ALI support for third-party mods that ship no ALI plugin of their own — see the [supported mods](https://github.com/yanny7/AdvancedLootInfo/wiki/Users-Loot-Info-ALI-Compat). | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/ali-compat) · [Modrinth](https://modrinth.com/mod/ali-compat) |

ALI and AWI are independent of each other — install either one, or both.

## Installing

1. Install a recipe viewer: [EMI](https://modrinth.com/mod/emi), [JEI](https://modrinth.com/mod/jei) or [REI](https://modrinth.com/mod/rei).
2. Install **ACI** — both mods need it, and the loader refuses a mismatched pair rather than failing later.
3. Install **ALI**, **AWI**, or both.
4. Optionally add **ALICompat** if your pack contains mods it covers.

On a dedicated server the mods belong on both sides: the data is collected on the server and sent to each client.

LootJS is supported out of the box; see the [LootJS wiki page](https://github.com/yanny7/AdvancedLootInfo/wiki/Users-Loot-Info-LootJS).

## Supported versions

Fabric, Forge and NeoForge, across a range of Minecraft versions — which loaders exist for which Minecraft version differs, so check the file list on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/advanced-loot-info/files) or [Modrinth](https://modrinth.com/mod/advanced-loot-info/versions) for the combination you need.

Development happens **one git branch per Minecraft version** (`1.20.1`, `1.21.1`, …), with `master` tracking the newest one and `archive/<version>` branches kept for versions no longer updated. Changes travel upwards one branch at a time, so a fix usually lands on several of them. See the [branch list](https://github.com/yanny7/AdvancedLootInfo/branches).

## Building from source

Pick the branch for the Minecraft version you want to build, then:

```
./gradlew build
```

Jars land in `<mod>/<loader>/build/libs/`, e.g. `ali/fabric/build/libs/AdvancedLootInfo-fabric-<mc>-<version>.jar`. Ignore the `-dev-shadow` and `-sources` ones.

The required JDK is in `java_version` in `gradle.properties`; which loaders and viewers are built is controlled by the `*_enabled` flags in the same file.

Single module:

```
./gradlew :aci:common:build
./gradlew :ali:fabric:build
```

Run the game from the dev environment — one task per mod / loader / viewer combination:

```
./gradlew runAliFabricEmiClient
./gradlew runAliForgeJeiClient
./gradlew runAwiFabricReiClient
```

Tests live in the `common` modules:

```
./gradlew :aci:common:test
./gradlew :ali:common:test
./gradlew :awi:common:test
```

## Documentation

The [wiki](https://github.com/yanny7/AdvancedLootInfo/wiki) covers every supported Minecraft version in one place:

- [Users](https://github.com/yanny7/AdvancedLootInfo/wiki/Users-Getting-Started) — the interface, configuration, loot categories, fake loot, global loot modifiers, troubleshooting.
- [Developers](https://github.com/yanny7/AdvancedLootInfo/wiki/Developers-Getting-Started) — the plugin API, for adding loot or worldgen support from your own mod without touching a recipe viewer.

Configuration files are described by [`ali_config.schema.json`](ali_config.schema.json) and [`awi_config.schema.json`](awi_config.schema.json). Each mod keeps its own changelog: [ACI](aci/CHANGELOG.md), [ALI](ali/CHANGELOG.md), [AWI](awi/CHANGELOG.md), [ALICompat](alicompat/CHANGELOG.md).

## Contributing

Bug reports and feature requests: [issues](https://github.com/yanny7/AdvancedLootInfo/issues). When reporting, say which Minecraft version, loader and recipe viewer you use, and attach the log.

Pull requests should target the branch of the Minecraft version they were written against — porting to the other branches is done afterwards, upwards, one branch at a time.

## License

[MIT](LICENSE)
