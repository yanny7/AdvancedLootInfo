# [1.6.2]

- Fixed freeze with JEI on world rejoin
- Improved error handling and recovery for data processing and synchronization
- Fixed chance value when used multiple probabilities in LootJS

# [1.6.1]

- Fixed suboptimal layout in some cases
- Fixed entity GLM condition list not complete
- Added support for GLM on Fabric implemented by PortingLib Loot library
- Resolved a client freeze when connecting to a LAN world
- Fixed reload not updating loot data
- Removed empty branches in loot table
- Fixed crash on second world login

# [1.6.0]

- Added GLM support (Global Loot Modifiers)
- Fixed missing spawn eggs for modded mobs on (Neo)Forge
- Hiding injected loot tables
- Improved loot info for unknown entries/modifiers/predicates
- Displaying loot table name in gameplay tooltip
- Fixed occasional no loot information
- Added support for REI on 1.21.10

## [1.5.2]

- Fixed missing Wandering Trader trade info
- Decreased packet size when syncing to avoid timeout in some cases

## [1.5.1]

- Optimized data synchronization
- Fixed LootJS missing modifiers and predicates in tooltip
- Displaying properties of auto-detected modifiers/predicates that doesn't have compatibility

## [1.5.0]

- Registrable value type
- Decreased memory consumption
- Fixed not correctly displayed item count for LootJS loot table
- Fixed entities not showing loot tables added by LootJS
- Configurable list of entities that will be excluded from creation (causing crash)

## [1.4.0]

- Sort loot by chance, descending (highest chance first).
- Configurable custom categories using JSON configuration file (client and server side)
- Removed resource pack configuration
- Improved loot data synchronization and logging

## [1.3.9]

- Fixed crash on failed mixin

## [1.3.8]

- Fixed deadlock while receiving loot data on Fabric
- Fixed subsequent /reload doesn't block registration thread (Hope that this is final fix for /reload bug)

## [1.3.7]

- Finally fixed bug with /reload

## [1.3.6]

- Fixed memory leak of ClientLevel
- Fixed reload caused missing loot entries
- Added compatibility for Mantle

## [1.3.5]

- Fixed crash on Forge 47.4.0+

## [1.3.4]

- Added compatibility for Supplementaries
- Added compatibility for Moonlight
- Added compatibility for The Bumblezone
- Added compatibility for Villagers Plus
- Added compatibility for Cultural Delights
- Added compatibility for Sawmill
- Added compatibility for Hybrid Aquatic
- Added compatibility for Snow! Real Magic!
- Added compatibility for Deeper And Darker
- Added compatibility for Repurposed Structures
- Added compatibility for Immersive Engineering
- Fixed crash when registry is unavailable
- Fixed unwanted modification of components
- Fixed missing item stack modifications for LootJS items

## [1.3.3]

- Display merchant offer if it's possible to determine it's content even without mod support
- Added zh_cn translation (ZetaY)

## [1.3.2]

- Fixed crash when used LootJS with Table/Type Modifier

## [1.3.1]

- Display duplicit information only when Advanced tooltip is enabled
- Fixed crash when NULL loot table

## [1.3.0]

- Added Trade info support for vanilla
- Fixed memory leak in JEI plugin
- Fixed crash when used with c2me

## [1.2.5]

- Fixed no LootJS info displayed when adding to empty loot table

## [1.2.4]

- Fixed no tooltip in part of right panel in REI
- Fixed REI doesn't show entity loot table when right-clicked on Spawn Egg
- Fixed rare tooltip bug printing nonsense
- Supported reload
- Fixed Ingredient tooltip
- Make uppercased chest name from loot table name if missing translation
- Added es_ar translation (Texaliuz)

## [1.2.3]

- Fixed crash on failed mixin

## [1.2.2]

- Fixed crash when empty itemStack

## [1.2.1]

- Fixed duplicating slots
- Added missing LootJS AnyStructure condition tooltip
- Extended LootJS ItemStack tooltip
- Fixed sporadic missing loot info
- Fixed sporadic crash on startup
- Fixed show recipe outputs doesn't follow modifier changes
- Changed modifiers and predicate names to match official names
- Hiding 100% chance text

## [1.2.0]

- Changed API
- Added API support for loot modifiers
- Displaying loot changes made by LootJS

## [1.1.8]

- Fixed view usages in EMI not working
- Used black text in REI loot name
- Ellipsis loot name when too long and added tooltip

## [1.1.7]

- Fixed crash when decoding loot table

## [1.1.6]

- Changed mod load ordering to prevent random crash
- Fixed crash when used with EMI
- Fixed crash when encoding loot table

## [1.1.5]

- Fixed loot overflow in REI
- Fixed loot overflow in JEI
- Fixed loot overflow in EMI
- Fixed crash when starting server

## [1.1.4]

- Fixed crash when used on standalone server

## [1.1.3]

- Fixed crash when used on standalone server
- Fixed crash when failed to encode loot table

## [1.1.2]

- Used compression for transferring loot tables
- Removed forced fabric-loader version
- Added missing tooltips
- Added components tooltip

## [1.1.1]

- Removed entity tooltip in REI
- Fixed enchantment level coloring
- Fixed double tooltip rendering in JEI

## [1.1.0]

- Fixed gameplay sub-categories not showing on multiplayer
- Apply item modifiers (like potion/name/lore...) to displayed item
- Fixed bonus count calculation
- Fixed missing tooltip in REI right panel when displayed ALI category
- Added missing group tooltips
- Performance improvements

## [1.0.5]

- Changed displaying of tooltips
- Fixed crash while converting loot function

## [1.0.4]

- Fixed crash when creating entity
- Added missing tooltips
- Added delimiters to tooltips

## [1.0.3]

- Fixed crash on Fabric
- Fixed missing conditions

## [1.0.2]

- Fixed crash with mixins in ApplyBonusCount

## [1.0.1]

- Fixed crash when referring to tag with no items