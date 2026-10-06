## [1.4.0]

- Added `SpawnInfoFilter` configuration block
- Variables with a finite domain containing 0 (luck) get a row per value, like enchantment levels
- Number converters receive the list of condition tooltips of their conditional values
- Grouping structure spawn info
- Scrollable tooltip by mouse wheel when taller than screen
- Number model `NumberExpr` replaces `RangeValue`, showing most likely value, level rows and charts
- Added `TooltipBuilder.intervals`, a list of ranges formatted in the client's locale
- Structure spawn overrides without mob category no longer break the entity's spawn tooltip
- Spawn biomes are listed as exclusions (`-`) only when more than 10 biomes would be listed otherwise
- IDs of any registry and tags are shown translated under `showInGameNames`, using the key forms of vanilla, Fabric API, NeoForge and EMI

## [1.3.0]

- Unbound Holder is rendered as its registry key instead of throwing
- Added collecting natural mob spawns per dimension, biome and structure
- Faster building of tooltip trees
- Updated Chinese translation (ZetaY)

## [1.2.0]

- Added `ManagedRegistry.entries`, so a registry's whole content can be read back
- Added `ManagedRegistry.classKeyName`, reporting a lambda class under its host class instead of its runtime address
- Missing class-keyed entries are reported once per class name, so lambda implementations no longer produce one line each
- Reporting mergeable tooltip key once per scan instead of once per occurrence

## [1.1.0]

- Added `TooltipStyle` and `TooltipColors`, so tooltip text, value, error and branch colors can be supplied by the calling mod
- Enum values are no longer rendered by `CommonValueTooltip` - a mod has to register its own handler for `Enum.class`, otherwise an enum falls through to the JSON dump fallback

## [1.0.0]

- First release
