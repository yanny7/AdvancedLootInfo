## []

- Tooltips wider than the window wrap their lines

## [1.3.0]

- Added `spawnInfo` configuration to hide biome mob spawns
- Height converters and placement contributions carry the condition tooltips of their conditional numbers
- Tooltips taller than the screen can be scrolled with the mouse wheel
- Values show most likely value, charts (`showCharts`) and formulas (F3+H)
- Fixed running out of memory during the worldgen scan with mods that cache compiled surface rules
- With `showInGameNames`, IDs (biomes, dimensions, structures, …) and tags are shown translated when a translation exists, in spawn info too
- Spawn category is shown translated (`Monster` instead of `monster`)

## [1.2.0]

- Added `dimensions` configuration to hide dimensions
- Added `dimensionIcons` configuration to set recipe viewer icon of each dimension
- Biome shows spawn eggs of mobs that spawn in it
- Fixed missing biomes with more feature steps than vanilla defines
- Faster worldgen scan of modded features
- Lower client memory use with JEI and EMI
- Updated Chinese translation (ZetaY)

## [1.1.1]

- Fixed Plugins discovered multiple times on Fabric
- `/reload` no longer rebuilds worldgen data

## [1.1.0]

- Added `tooltipColors` configuration to change the tooltip text, value, error and branch colors
- Added a published JSON schema for the configuration file (`awi_config.schema.json`)
- Enum values shown in tooltips are translatable
- Heightmap types describe what they mean instead of showing the vanilla constant name (`Solid Ground, Ignores Water` instead of `OCEAN_FLOOR`)

## [1.0.0]

- First release