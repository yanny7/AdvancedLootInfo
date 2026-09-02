## []

- Fixed Plugins discovered multiple times on Fabric
- Plugins can register enum translations under their own mod id (`registerEnumTranslation(type, modId, owner)`); the two-argument overload is deprecated

## [1.1.0]

- Added `tooltipColors` configuration to change the tooltip text, value, error and branch colors
- Added a published JSON schema for the configuration file (`awi_config.schema.json`)
- Enum values shown in tooltips are translatable
- Heightmap types describe what they mean instead of showing the vanilla constant name (`Solid Ground, Ignores Water` instead of `OCEAN_FLOOR`)

## [1.0.0]

- First release