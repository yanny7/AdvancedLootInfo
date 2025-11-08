package com.yanny.ali.datagen;

import java.util.HashMap;
import java.util.Map;

public class LanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        TRANSLATION_MAP.put("emi.category.ali.block_loot", "Block Drops");
        TRANSLATION_MAP.put("emi.category.ali.plant_loot", "Plant Drops");
        TRANSLATION_MAP.put("emi.category.ali.entity_loot", "Entity Drops");
        TRANSLATION_MAP.put("emi.category.ali.chest_loot", "Chest Loot");
        TRANSLATION_MAP.put("emi.category.ali.fishing_loot", "Fishing Loot");
        TRANSLATION_MAP.put("emi.category.ali.archaeology_loot", "Archaeology Loot");
        TRANSLATION_MAP.put("emi.category.ali.hero_loot", "Hero of the Village Loot");
        TRANSLATION_MAP.put("emi.category.ali.gameplay_loot", "Gameplay Loot");
        TRANSLATION_MAP.put("emi.category.ali.trade_loot", "Trading Loot");

        TRANSLATION_MAP.put("ali.description.rolls", "Rolls: %s%s");
        TRANSLATION_MAP.put("ali.description.chance", "Chance: %s%s");
        TRANSLATION_MAP.put("ali.description.chance_bonus", "%s (%s %s)");
        TRANSLATION_MAP.put("ali.description.count", "Count: %s");
        TRANSLATION_MAP.put("ali.description.count_bonus", "%s (%s %s)");
        TRANSLATION_MAP.put("ali.description.quality", "Quality: %s");

        /*
         * PREDICATES
         */

        TRANSLATION_MAP.put("ali.type.condition.all_of", "All Of:");
        TRANSLATION_MAP.put("ali.type.condition.any_of", "Any of:");
        TRANSLATION_MAP.put("ali.type.condition.block_state_property", "Block State Property:");
        TRANSLATION_MAP.put("ali.type.condition.damage_source_properties", "Damage Source Properties:");
        TRANSLATION_MAP.put("ali.type.condition.entity_properties", "Entity Properties:");
        TRANSLATION_MAP.put("ali.type.condition.entity_scores", "Entity Scores:");
        TRANSLATION_MAP.put("ali.type.condition.inverted", "Inverted:");
        TRANSLATION_MAP.put("ali.type.condition.killed_by_player", "Killed by player");
        TRANSLATION_MAP.put("ali.type.condition.location_check", "Location Check:");
        TRANSLATION_MAP.put("ali.type.condition.match_tool", "Match Tool:");
        TRANSLATION_MAP.put("ali.type.condition.random_chance", "Random Chance:");
        TRANSLATION_MAP.put("ali.type.condition.random_chance_with_looting", "Random Chance With Looting:");
        TRANSLATION_MAP.put("ali.type.condition.reference", "Reference: %s");
        TRANSLATION_MAP.put("ali.type.condition.survives_explosion", "Survives Explosion");
        TRANSLATION_MAP.put("ali.type.condition.table_bonus", "Table Bonus:");
        TRANSLATION_MAP.put("ali.type.condition.time_check", "Time Check:");
        TRANSLATION_MAP.put("ali.type.condition.value_check", "Value Check:");
        TRANSLATION_MAP.put("ali.type.condition.weather_check", "Weather Check:");

        // Forge
        TRANSLATION_MAP.put("ali.type.condition.can_tool_perform_action", "Can Tool Perform Action: %s");
        TRANSLATION_MAP.put("ali.type.condition.loot_table_id", "Loot Table Id: %s");

        // LootJS
        TRANSLATION_MAP.put("ali.type.condition.and", "And:");
        TRANSLATION_MAP.put("ali.type.condition.any_biome", "Any Biome:");
        TRANSLATION_MAP.put("ali.type.condition.any_dimension", "Any Dimension:");
        TRANSLATION_MAP.put("ali.type.condition.any_structure", "Any Structure:");
        TRANSLATION_MAP.put("ali.type.condition.biome", "Biome:");
        TRANSLATION_MAP.put("ali.type.condition.block_predicate", "Block Predicate:");
        TRANSLATION_MAP.put("ali.type.condition.direct_killer_predicate", "Direct Killer Predicate:");
        TRANSLATION_MAP.put("ali.type.condition.distance_to_killer", "Distance To Killer:");
        TRANSLATION_MAP.put("ali.type.condition.entity_predicate", "Entity Predicate:");
        TRANSLATION_MAP.put("ali.type.condition.killer_predicate", "Killer Predicate:");
        TRANSLATION_MAP.put("ali.type.condition.light_level", "Light Level:");
        TRANSLATION_MAP.put("ali.type.condition.match_damage_source", "Match Damage Source:");
        TRANSLATION_MAP.put("ali.type.condition.match_equipment_slot", "Match Equipment Slot:");
        TRANSLATION_MAP.put("ali.type.condition.match_loot", "Match Loot:");
        TRANSLATION_MAP.put("ali.type.condition.match_mainhand", "Match Mainhand:");
        TRANSLATION_MAP.put("ali.type.condition.match_offhand", "Match Offhand:");
        TRANSLATION_MAP.put("ali.type.condition.match_player", "Match Player:");
        TRANSLATION_MAP.put("ali.type.condition.not", "Not:");
        TRANSLATION_MAP.put("ali.type.condition.or", "Or:");
        TRANSLATION_MAP.put("ali.type.condition.player_predicate", "Player Predicate:");
        TRANSLATION_MAP.put("ali.type.condition.random_chance_with_enchantment", "Random Chance With Enchantment:");

        // Aether
        TRANSLATION_MAP.put("ali.type.condition.config_enabled", "Must Be Enabled In Config");

        // The Bumblezone
        TRANSLATION_MAP.put("ali.type.condition.essence_only_spawn", "Essence Only Spawn");

        // Moonlight
        TRANSLATION_MAP.put("ali.type.condition.optional_property", "Optional Property");

        // Mantle
        TRANSLATION_MAP.put("ali.type.condition.block_tag", "Block Tag");
        TRANSLATION_MAP.put("ali.type.condition.contains_item", "Contains Item");
        TRANSLATION_MAP.put("ali.type.condition.empty", "Empty");

        /*
         * MODIFIERS
         */

        TRANSLATION_MAP.put("ali.type.function.apply_bonus", "Apply Bonus:");
        TRANSLATION_MAP.put("ali.type.function.copy_components", "Copy Components:");
        TRANSLATION_MAP.put("ali.type.function.copy_custom_data", "Copy Custom Data:");
        TRANSLATION_MAP.put("ali.type.function.copy_name", "Copy Name:");
        TRANSLATION_MAP.put("ali.type.function.copy_state", "Copy State:");
        TRANSLATION_MAP.put("ali.type.function.enchant_randomly", "Enchant Randomly:");
        TRANSLATION_MAP.put("ali.type.function.enchant_with_levels", "Enchant With Levels:");
        TRANSLATION_MAP.put("ali.type.function.exploration_map", "Exploration Map:");
        TRANSLATION_MAP.put("ali.type.function.explosion_decay", "Explosion Decay");
        TRANSLATION_MAP.put("ali.type.function.fill_player_head", "Fill Player Head:");
        TRANSLATION_MAP.put("ali.type.function.filtered", "Filtered:");
        TRANSLATION_MAP.put("ali.type.function.furnace_smelt", "Furnace Smelt");
        TRANSLATION_MAP.put("ali.type.function.limit_count", "Limit Count:");
        TRANSLATION_MAP.put("ali.type.function.looting_enchant", "Looting Enchant:");
        TRANSLATION_MAP.put("ali.type.function.modify_contents", "Modify Contents:");
        TRANSLATION_MAP.put("ali.type.function.reference", "Reference:");
        TRANSLATION_MAP.put("ali.type.function.sequence", "Sequence:");
        TRANSLATION_MAP.put("ali.type.function.set_attributes", "Set Attributes:");
        TRANSLATION_MAP.put("ali.type.function.set_banner_pattern", "Set Banner Pattern:");
        TRANSLATION_MAP.put("ali.type.function.set_book_cover", "Set Book Cover:");
        TRANSLATION_MAP.put("ali.type.function.set_components", "Set Components:");
        TRANSLATION_MAP.put("ali.type.function.set_contents", "Set Contents:");
        TRANSLATION_MAP.put("ali.type.function.set_count", "Set Count:");
        TRANSLATION_MAP.put("ali.type.function.set_custom_data", "Set Custom Data:");
        TRANSLATION_MAP.put("ali.type.function.set_custom_model_data", "Set Custom Model Data:");
        TRANSLATION_MAP.put("ali.type.function.set_damage", "Set Damage:");
        TRANSLATION_MAP.put("ali.type.function.set_enchantments", "Set Enchantments:");
        TRANSLATION_MAP.put("ali.type.function.set_firework_explosion", "Set Firework Explosion:");
        TRANSLATION_MAP.put("ali.type.function.set_fireworks", "Set Fireworks:");
        TRANSLATION_MAP.put("ali.type.function.set_instrument", "Set Instrument:");
        TRANSLATION_MAP.put("ali.type.function.set_item", "Set Item:");
        TRANSLATION_MAP.put("ali.type.function.set_loot_table", "Set Loot Table:");
        TRANSLATION_MAP.put("ali.type.function.set_lore", "Set Lore:");
        TRANSLATION_MAP.put("ali.type.function.set_name", "Set Name:");
        TRANSLATION_MAP.put("ali.type.function.set_ominous_bottle_amplifier", "Set Ominous Bottle Amplifier:");
        TRANSLATION_MAP.put("ali.type.function.set_potion", "Set Potion:");
        TRANSLATION_MAP.put("ali.type.function.set_stew_effect", "Set Stew Effect:");
        TRANSLATION_MAP.put("ali.type.function.set_writable_book_pages", "Set Writable Book Pages:");
        TRANSLATION_MAP.put("ali.type.function.set_written_book_pages", "Set Written Book Pages:");
        TRANSLATION_MAP.put("ali.type.function.toggle_tooltips", "Toggle Tooltips:");

        // LootJS
        TRANSLATION_MAP.put("ali.type.function.custom_player", "Custom Player Modifier:");
        TRANSLATION_MAP.put("ali.type.function.modified_item", "Modified dynamically!");

        // Aether
        TRANSLATION_MAP.put("ali.type.function.spawn_xp", "Spawn XP");
        TRANSLATION_MAP.put("ali.type.function.spawn_tnt", "Spawn TNT");
        TRANSLATION_MAP.put("ali.type.function.double_drops", "Double Drops");
        TRANSLATION_MAP.put("ali.type.function.whirlwind_spawn_entity", "Whirlwind Spawn Entity");

        // Farmer's delight
        TRANSLATION_MAP.put("ali.type.function.copy_meal", "Copy Meal");
        TRANSLATION_MAP.put("ali.type.function.copy_skillet", "Copy Skillet");

        // Supplementaries
        TRANSLATION_MAP.put("ali.type.function.random_arrow", "Random Arrow");
        TRANSLATION_MAP.put("ali.type.function.curse_loot", "Curse Loot");

        // The Bumblezone
        TRANSLATION_MAP.put("ali.type.function.drop_container_items", "Drop Container Items");
        TRANSLATION_MAP.put("ali.type.function.honey_compass_locate_structure", "Honey Compass Locate Structure");
        TRANSLATION_MAP.put("ali.type.function.tag_item_removals", "Tag Item Removals");
        TRANSLATION_MAP.put("ali.type.function.uniquify_if_has_items", "Uniquify If Has Items");

        // Deeper and Darker
        TRANSLATION_MAP.put("ali.type.function.set_painting_variant", "Set Painting Variant");

        // Immersive Engineering
        TRANSLATION_MAP.put("ali.type.function.bluprintz", "Bluprintz Easter Egg");
        TRANSLATION_MAP.put("ali.type.function.conveyor_cover", "Apply Conveyor Cover");
        TRANSLATION_MAP.put("ali.type.function.property_count", "Get Count From Property");
        TRANSLATION_MAP.put("ali.type.function.revolver_perk", "Revolver Perk");
        TRANSLATION_MAP.put("ali.type.function.windmill", "Set Windmill Sails");

        // Mantle
        TRANSLATION_MAP.put("ali.type.function.retexture_block_entity", "Retexture Block Entity");
        TRANSLATION_MAP.put("ali.type.function.set_fluid", "Set Fluid");

        // Trades
        TRANSLATION_MAP.put("ali.type.function.dyed_randomly", "Dyed Randomly");

        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_attribute_modifiers", "Attribute Modifiers:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_bundle", "Bundle:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_container", "Container:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_custom_data", "Custom Data:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_damage", "Damage:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_enchantments", "Enchantments:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_firework_explosion", "Firework Explosion:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_fireworks", "Fireworks:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_potions", "Potions:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_stored_enchantments", "Stored Enchantments:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_trim", "Trim:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_writable_book", "Writable Book:");
        TRANSLATION_MAP.put("ali.type.item_sub_predicate.item_written_book", "Written Book:");

        TRANSLATION_MAP.put("ali.type.entity_sub_predicate.fishing_hook", "Fishing Hook:");
        TRANSLATION_MAP.put("ali.type.entity_sub_predicate.lightning_bolt", "Lightning Bolt:");
        TRANSLATION_MAP.put("ali.type.entity_sub_predicate.player", "Player:");
        TRANSLATION_MAP.put("ali.type.entity_sub_predicate.raider", "Raider:");
        TRANSLATION_MAP.put("ali.type.entity_sub_predicate.slime", "Slime:");

        TRANSLATION_MAP.put("ali.enum.group_type.all", "Selects all entries");
        TRANSLATION_MAP.put("ali.enum.group_type.random", "Selects random entry");
        TRANSLATION_MAP.put("ali.enum.group_type.alternatives", "Selects only first successful entry");
        TRANSLATION_MAP.put("ali.enum.group_type.sequence", "Selects entries sequentially until first failed");
        TRANSLATION_MAP.put("ali.enum.group_type.dynamic", "Dynamic block-specific drops");
        TRANSLATION_MAP.put("ali.enum.group_type.empty", "Drops nothing");
        TRANSLATION_MAP.put("ali.enum.group_type.missing", "Not implemented");

        // Snow Real Magic
        TRANSLATION_MAP.put("ali.enum.group_type.normalize", "Drops block buried in snow");

        TRANSLATION_MAP.put("ali.property.branch.advancements", "Advancements:");
        TRANSLATION_MAP.put("ali.property.branch.all", "All:");
        TRANSLATION_MAP.put("ali.property.branch.any", "Any:");
        TRANSLATION_MAP.put("ali.property.branch.attribute_modifier", "Attribute Modifier:");
        TRANSLATION_MAP.put("ali.property.branch.attributes", "Attributes:");
        TRANSLATION_MAP.put("ali.property.branch.banner_patterns", "Banner Patterns:");
        TRANSLATION_MAP.put("ali.property.branch.bees", "Bees:");
        TRANSLATION_MAP.put("ali.property.branch.biomes", "Biomes:");
        TRANSLATION_MAP.put("ali.property.branch.block_predicate", "Block Predicate:");
        TRANSLATION_MAP.put("ali.property.branch.blocks", "Blocks:");
        TRANSLATION_MAP.put("ali.property.branch.chest", "Chest:");
        TRANSLATION_MAP.put("ali.property.branch.component_predicates", "Component Predicates:");
        TRANSLATION_MAP.put("ali.property.branch.components", "Components:");
        TRANSLATION_MAP.put("ali.property.branch.conditions", "Predicates:");
        TRANSLATION_MAP.put("ali.property.branch.contains", "Contains:");
        TRANSLATION_MAP.put("ali.property.branch.copy_operations", "Copy Operations:");
        TRANSLATION_MAP.put("ali.property.branch.counts", "Counts:");
        TRANSLATION_MAP.put("ali.property.branch.criterions", "Criterions:");
        TRANSLATION_MAP.put("ali.property.branch.decorations", "Decorations:");
        TRANSLATION_MAP.put("ali.property.branch.dimensions", "Dimensions:");
        TRANSLATION_MAP.put("ali.property.branch.direct_entity", "Direct Entity:");
        TRANSLATION_MAP.put("ali.property.branch.distance_to_player", "Distance to Player:");
        TRANSLATION_MAP.put("ali.property.branch.effect", "Effect:");
        TRANSLATION_MAP.put("ali.property.branch.effects", "Effects:");
        TRANSLATION_MAP.put("ali.property.branch.enchantments", "Enchantments:");
        TRANSLATION_MAP.put("ali.property.branch.entity_equipment", "Entity Equipment:");
        TRANSLATION_MAP.put("ali.property.branch.entity_flags", "Entity Flags:");
        TRANSLATION_MAP.put("ali.property.branch.entity_sub_predicate", "Entity Sub Predicate:");
        TRANSLATION_MAP.put("ali.property.branch.entity_types", "Entity Types:");
        TRANSLATION_MAP.put("ali.property.branch.equipment_slots", "Equipment Slots:");
        TRANSLATION_MAP.put("ali.property.branch.exclude", "Exclude:");
        TRANSLATION_MAP.put("ali.property.branch.explosion", "Explosion:");
        TRANSLATION_MAP.put("ali.property.branch.explosions", "Explosions:");
        TRANSLATION_MAP.put("ali.property.branch.feet", "Feet:");
        TRANSLATION_MAP.put("ali.property.branch.filter", "Filter:");
        TRANSLATION_MAP.put("ali.property.branch.fluid_predicate", "Fluid Predicate:");
        TRANSLATION_MAP.put("ali.property.branch.fluids", "Fluids:");
        TRANSLATION_MAP.put("ali.property.branch.global_pos", "Global Position:");
        TRANSLATION_MAP.put("ali.property.branch.head", "Head:");
        TRANSLATION_MAP.put("ali.property.branch.include", "Include:");
        TRANSLATION_MAP.put("ali.property.branch.item", "Item:");
        TRANSLATION_MAP.put("ali.property.branch.item_predicates", "Item Predicates:");
        TRANSLATION_MAP.put("ali.property.branch.items", "Items:");
        TRANSLATION_MAP.put("ali.property.branch.legs", "Legs:");
        TRANSLATION_MAP.put("ali.property.branch.lines", "Lines:");
        TRANSLATION_MAP.put("ali.property.branch.location", "Location:");
        TRANSLATION_MAP.put("ali.property.branch.looking_at", "Looking At:");
        TRANSLATION_MAP.put("ali.property.branch.lore", "Lore:");
        TRANSLATION_MAP.put("ali.property.branch.mainhand", "Main Hand:");
        TRANSLATION_MAP.put("ali.property.branch.materials", "Materials:");
        TRANSLATION_MAP.put("ali.property.branch.mob_effects", "Mob Effects:");
        TRANSLATION_MAP.put("ali.property.branch.modifier", "Modifier:");
        TRANSLATION_MAP.put("ali.property.branch.modifiers", "Modifiers:");
        TRANSLATION_MAP.put("ali.property.branch.occupant", "Occupant:");
        TRANSLATION_MAP.put("ali.property.branch.offhand", "Offhand:");
        TRANSLATION_MAP.put("ali.property.branch.operation", "Operation:");
        TRANSLATION_MAP.put("ali.property.branch.page", "Page:");
        TRANSLATION_MAP.put("ali.property.branch.pages", "Pages:");
        TRANSLATION_MAP.put("ali.property.branch.passenger", "Passenger:");
        TRANSLATION_MAP.put("ali.property.branch.patterns", "Patterns:");
        TRANSLATION_MAP.put("ali.property.branch.position", "Position:");
        TRANSLATION_MAP.put("ali.property.branch.predicate", "Predicate:");
        TRANSLATION_MAP.put("ali.property.branch.properties", "Properties:");
        TRANSLATION_MAP.put("ali.property.branch.recipes", "Recipes:");
        TRANSLATION_MAP.put("ali.property.branch.rule", "Rule:");
        TRANSLATION_MAP.put("ali.property.branch.rules", "Rules:");
        TRANSLATION_MAP.put("ali.property.branch.scores", "Scores:");
        TRANSLATION_MAP.put("ali.property.branch.source_entity", "Source Entity:");
        TRANSLATION_MAP.put("ali.property.branch.source_names", "Source Names:");
        TRANSLATION_MAP.put("ali.property.branch.stats", "Stats:");
        TRANSLATION_MAP.put("ali.property.branch.stepping_on_location", "Stepping on Location:");
        TRANSLATION_MAP.put("ali.property.branch.structures", "Structures:");
        TRANSLATION_MAP.put("ali.property.branch.stuck_entity", "Stuck Entity:");
        TRANSLATION_MAP.put("ali.property.branch.styled_lines", "Styled Lines:");
        TRANSLATION_MAP.put("ali.property.branch.tags", "Tags:");
        TRANSLATION_MAP.put("ali.property.branch.targeted_entity", "Targeted Entity:");
        TRANSLATION_MAP.put("ali.property.branch.title", "Title:");
        TRANSLATION_MAP.put("ali.property.branch.tooltip", "Tooltip:");
        TRANSLATION_MAP.put("ali.property.branch.values", "Values:");
        TRANSLATION_MAP.put("ali.property.branch.variants", "Variants:");
        TRANSLATION_MAP.put("ali.property.branch.vehicle", "Vehicle:");

        TRANSLATION_MAP.put("ali.property.value.absolute", "Absolute: %s");
        TRANSLATION_MAP.put("ali.property.value.add", "Add: %s");
        TRANSLATION_MAP.put("ali.property.value.allow_duplicate_loot", "Allow Duplicate Loot: %s");
        TRANSLATION_MAP.put("ali.property.value.ambient", "Ambient: %s");
        TRANSLATION_MAP.put("ali.property.value.amount", "Amount: %s");
        TRANSLATION_MAP.put("ali.property.value.amplifier", "Amplifier: %s");
        TRANSLATION_MAP.put("ali.property.value.append", "Append: %s");
        TRANSLATION_MAP.put("ali.property.value.attribute", "Attribute: %s");
        TRANSLATION_MAP.put("ali.property.value.author", "Author: %s");
        TRANSLATION_MAP.put("ali.property.value.back", "Back: %s");
        TRANSLATION_MAP.put("ali.property.value.banner_pattern", "Banner Pattern: %s");
        TRANSLATION_MAP.put("ali.property.value.biome", "Biome: %s");
        TRANSLATION_MAP.put("ali.property.value.block", "Block: %s");
        TRANSLATION_MAP.put("ali.property.value.block_entity_type", "Block Entity Type: %s");
        TRANSLATION_MAP.put("ali.property.value.blocks_on_fire", "Blocks On Fire: %s");
        TRANSLATION_MAP.put("ali.property.value.bonus_multiplier", "Bonus Multiplier: %s");
        TRANSLATION_MAP.put("ali.property.value.can_always_eat", "Can Always Eat: %s");
        TRANSLATION_MAP.put("ali.property.value.chance", "Chance: %s");
        TRANSLATION_MAP.put("ali.property.value.color", "Color: %s");
        TRANSLATION_MAP.put("ali.property.value.colors", "Colors: %s");
        TRANSLATION_MAP.put("ali.property.value.component", "Component: %s");
        TRANSLATION_MAP.put("ali.property.value.container", "Container: %s");
        TRANSLATION_MAP.put("ali.property.value.correct_for_drops", "Correct For Drops: %s");
        TRANSLATION_MAP.put("ali.property.value.count", "Count: %s");
        TRANSLATION_MAP.put("ali.property.value.custom_color", "Custom Color: %s");
        TRANSLATION_MAP.put("ali.property.value.custom_name", "Custom Name: %s");
        TRANSLATION_MAP.put("ali.property.value.damage", "Damage: %s");
        TRANSLATION_MAP.put("ali.property.value.damage_per_block", "Damage Per Block: %s");
        TRANSLATION_MAP.put("ali.property.value.decoration", "Decoration: %s");
        TRANSLATION_MAP.put("ali.property.value.default_mining_speed", "Default Mining Speed: %s");
        TRANSLATION_MAP.put("ali.property.value.destination", "Destination: %s");
        TRANSLATION_MAP.put("ali.property.value.detail_not_available", "Detail Not Available");
        TRANSLATION_MAP.put("ali.property.value.dimension", "Dimension: %s");
        TRANSLATION_MAP.put("ali.property.value.done", "Done: %s");
        TRANSLATION_MAP.put("ali.property.value.durability", "Durability: %s");
        TRANSLATION_MAP.put("ali.property.value.duration", "Duration: %s");
        TRANSLATION_MAP.put("ali.property.value.eat_seconds", "Eat Seconds: %s");
        TRANSLATION_MAP.put("ali.property.value.effect", "Effect: %s");
        TRANSLATION_MAP.put("ali.property.value.enchantment", "Enchantment: %s");
        TRANSLATION_MAP.put("ali.property.value.entity_data", "Entity Data: %s");
        TRANSLATION_MAP.put("ali.property.value.entity_type", "Entity Type: %s");
        TRANSLATION_MAP.put("ali.property.value.exact", "Exact: %s");
        TRANSLATION_MAP.put("ali.property.value.extra_rounds", "Extra Rounds: %s");
        TRANSLATION_MAP.put("ali.property.value.fade_colors", "Fade Colors: %s");
        TRANSLATION_MAP.put("ali.property.value.filtered", "Filtered: %s");
        TRANSLATION_MAP.put("ali.property.value.flight_duration", "Flight Duration: %s");
        TRANSLATION_MAP.put("ali.property.value.fluid", "Fluid: %s");
        TRANSLATION_MAP.put("ali.property.value.formula", "Formula: %s");
        TRANSLATION_MAP.put("ali.property.value.fraction", "Fraction: %s");
        TRANSLATION_MAP.put("ali.property.value.front", "Front: %s");
        TRANSLATION_MAP.put("ali.property.value.game_type", "Game Type: %s");
        TRANSLATION_MAP.put("ali.property.value.generation", "Generation: %s");
        TRANSLATION_MAP.put("ali.property.value.has_raid", "Has Raid: %s");
        TRANSLATION_MAP.put("ali.property.value.has_trail", "Has Trail: %s");
        TRANSLATION_MAP.put("ali.property.value.has_twinkle", "Has Twinkle: %s");
        TRANSLATION_MAP.put("ali.property.value.hidden_effect", "Hidden Effect: %s");
        TRANSLATION_MAP.put("ali.property.value.horizontal", "Horizontal: %s");
        TRANSLATION_MAP.put("ali.property.value.in_open_water", "Is In Open Water: %s");
        TRANSLATION_MAP.put("ali.property.value.is_ambient", "Is Ambient: %s");
        TRANSLATION_MAP.put("ali.property.value.is_baby", "Is Baby: %s");
        TRANSLATION_MAP.put("ali.property.value.is_captain", "Is Captain: %s");
        TRANSLATION_MAP.put("ali.property.value.is_crouching", "Is Crouching: %s");
        TRANSLATION_MAP.put("ali.property.value.is_on_fire", "Is On Fire: %s");
        TRANSLATION_MAP.put("ali.property.value.is_raining", "Is Raining: %s");
        TRANSLATION_MAP.put("ali.property.value.is_sprinting", "Is Sprinting: %s");
        TRANSLATION_MAP.put("ali.property.value.is_swimming", "Is Swimming: %s");
        TRANSLATION_MAP.put("ali.property.value.is_thundering", "Is Thundering: %s");
        TRANSLATION_MAP.put("ali.property.value.is_visible", "Is Visible: %s");
        TRANSLATION_MAP.put("ali.property.value.item", "Item: %s");
        TRANSLATION_MAP.put("ali.property.value.item_filter", "Item Filter: %s");
        TRANSLATION_MAP.put("ali.property.value.item_name", "Item Name: %s");
        TRANSLATION_MAP.put("ali.property.value.left", "Left: %s");
        TRANSLATION_MAP.put("ali.property.value.level", "Level: %s");
        TRANSLATION_MAP.put("ali.property.value.levels", "Levels: %s");
        TRANSLATION_MAP.put("ali.property.value.light", "Light: %s");
        TRANSLATION_MAP.put("ali.property.value.limit", "Limit: %s");
        TRANSLATION_MAP.put("ali.property.value.list_operation", "List Operation: %s");
        TRANSLATION_MAP.put("ali.property.value.loot_table", "Loot Table: %s");
        TRANSLATION_MAP.put("ali.property.value.map_decoration", "Map Decoration: %s");
        TRANSLATION_MAP.put("ali.property.value.material", "Material: %s");
        TRANSLATION_MAP.put("ali.property.value.merge_strategy", "Merge Strategy: %s");
        TRANSLATION_MAP.put("ali.property.value.min_ticks_in_hive", "Min Ticks In Hive: %s");
        TRANSLATION_MAP.put("ali.property.value.multiplier", "Multiplier: %s");
        TRANSLATION_MAP.put("ali.property.value.name", "Name: %s");
        TRANSLATION_MAP.put("ali.property.value.nbt", "Nbt: %s");
        TRANSLATION_MAP.put("ali.property.value.null", "%s");
        TRANSLATION_MAP.put("ali.property.value.nutrition", "Nutrition: %s");
        TRANSLATION_MAP.put("ali.property.value.offset", "Offset: %s");
        TRANSLATION_MAP.put("ali.property.value.operation", "Operation: %s");
        TRANSLATION_MAP.put("ali.property.value.options", "Options: %s");
        TRANSLATION_MAP.put("ali.property.value.page", "Page: %s");
        TRANSLATION_MAP.put("ali.property.value.pattern", "Pattern: %s");
        TRANSLATION_MAP.put("ali.property.value.percent", "Percent: %s");
        TRANSLATION_MAP.put("ali.property.value.period", "Period: %s");
        TRANSLATION_MAP.put("ali.property.value.potion", "Potion: %s");
        TRANSLATION_MAP.put("ali.property.value.price_multiplier", "Price Multiplier: %s");
        TRANSLATION_MAP.put("ali.property.value.probability", "Probability: %s");
        TRANSLATION_MAP.put("ali.property.value.property", "Property: %s");
        TRANSLATION_MAP.put("ali.property.value.provider", "Provider: %s");
        TRANSLATION_MAP.put("ali.property.value.range", "Range: %s");
        TRANSLATION_MAP.put("ali.property.value.ranged_property_any", "%s: any");
        TRANSLATION_MAP.put("ali.property.value.ranged_property_both", "%s: %s-%s");
        TRANSLATION_MAP.put("ali.property.value.ranged_property_gte", "%s: ≥%s");
        TRANSLATION_MAP.put("ali.property.value.ranged_property_lte", "%s: ≤%s");
        TRANSLATION_MAP.put("ali.property.value.rarity", "Rarity: %s");
        TRANSLATION_MAP.put("ali.property.value.raw", "Raw: %s");
        TRANSLATION_MAP.put("ali.property.value.replace", "Replace: %s");
        TRANSLATION_MAP.put("ali.property.value.resolution_context", "Resolution Context: %s");
        TRANSLATION_MAP.put("ali.property.value.resolved", "Resolved: %s");
        TRANSLATION_MAP.put("ali.property.value.rgb", "RGB: %s");
        TRANSLATION_MAP.put("ali.property.value.right", "Right: %s");
        TRANSLATION_MAP.put("ali.property.value.rotation", "Rotation: %s");
        TRANSLATION_MAP.put("ali.property.value.saturation", "Saturation: %s");
        TRANSLATION_MAP.put("ali.property.value.search_radius", "Search Radius: %s");
        TRANSLATION_MAP.put("ali.property.value.seed", "Seed: %s");
        TRANSLATION_MAP.put("ali.property.value.shape", "Shape: %s");
        TRANSLATION_MAP.put("ali.property.value.show_icon", "Show Icon: %s");
        TRANSLATION_MAP.put("ali.property.value.show_in_tooltip", "Show In Tooltip: %s");
        TRANSLATION_MAP.put("ali.property.value.signature", "Signature: %s");
        TRANSLATION_MAP.put("ali.property.value.size", "Size: %s");
        TRANSLATION_MAP.put("ali.property.value.skip_known_structures", "Skip Known Structures: %s");
        TRANSLATION_MAP.put("ali.property.value.slot", "Slot: %s");
        TRANSLATION_MAP.put("ali.property.value.smokey", "Smokey: %s");
        TRANSLATION_MAP.put("ali.property.value.source", "Source: %s");
        TRANSLATION_MAP.put("ali.property.value.source_path", "Source Path: %s");
        TRANSLATION_MAP.put("ali.property.value.speed", "Speed: %s");
        TRANSLATION_MAP.put("ali.property.value.structure", "Structure: %s");
        TRANSLATION_MAP.put("ali.property.value.tag", "Tag: %s");
        TRANSLATION_MAP.put("ali.property.value.target", "Target: %s");
        TRANSLATION_MAP.put("ali.property.value.target_path", "Target Path: %s");
        TRANSLATION_MAP.put("ali.property.value.team", "Team: %s");
        TRANSLATION_MAP.put("ali.property.value.ticks_in_hive", "Ticks In Hive: %s");
        TRANSLATION_MAP.put("ali.property.value.title", "Title: %s");
        TRANSLATION_MAP.put("ali.property.value.tracked", "Tracked: %s");
        TRANSLATION_MAP.put("ali.property.value.trail", "Trail: %s");
        TRANSLATION_MAP.put("ali.property.value.treasure", "Treasure: %s");
        TRANSLATION_MAP.put("ali.property.value.twinkle", "Twinkle: %s");
        TRANSLATION_MAP.put("ali.property.value.type", "Type: %s");
        TRANSLATION_MAP.put("ali.property.value.uses", "Uses: %s");
        TRANSLATION_MAP.put("ali.property.value.uuid", "UUID: %s");
        TRANSLATION_MAP.put("ali.property.value.value", "Value: %s");
        TRANSLATION_MAP.put("ali.property.value.values", "Values: %s");
        TRANSLATION_MAP.put("ali.property.value.variant", "Variant: %s");
        TRANSLATION_MAP.put("ali.property.value.villager_type", "Villager Type: %s");
        TRANSLATION_MAP.put("ali.property.value.villager_xp", "XP: %s");
        TRANSLATION_MAP.put("ali.property.value.x", "X: %s");
        TRANSLATION_MAP.put("ali.property.value.y", "Y: %s");
        TRANSLATION_MAP.put("ali.property.value.z", "Z: %s");
        TRANSLATION_MAP.put("ali.property.value.zoom", "Zoom: %s");

        TRANSLATION_MAP.put("ali.property.multi.offset", "Offset: [X: %s, Y: %s, Z: %s]");
        TRANSLATION_MAP.put("ali.property.multi.position", "Position: [X: %s, Y: %s, Z: %s]");

        TRANSLATION_MAP.put("ali.util.advanced_loot_info.missing", "Not implemented: %s");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.two_values", "%s%s");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.two_values_with_space", "%s %s");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.1", "  ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.2", "    ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.3", "      ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.4", "        ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.5", "          ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.6", "            ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.7", "              ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.8", "                ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.9", "                  ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.10", "                    ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.11", "                      ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.12", "                        ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.13", "                          ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.14", "                            ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.15", "                              ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.16", "                                ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.17", "                                  ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.18", "                                    ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.19", "                                      ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.pad.20", "                                        ->");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.key_value", "%s: %s");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.delimiter.functions", "----- Modifiers -----");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.delimiter.conditions", "----- Predicates -----");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.any", "ANY");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.removed", "REMOVED");

        TRANSLATION_MAP.put("ali/loot_table/chests/abandoned_mineshaft", "Abandoned Mineshaft Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/ancient_city", "Ancient City Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/ancient_city_ice_box", "Ancient City Ice Box Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_bridge", "Bastion Bridge Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_hoglin_stable", "Bastion Hoglin Stable Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_other", "Bastion Other Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_treasure", "Bastion Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/buried_treasure", "Buried Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/desert_pyramid", "Desert Pyramid Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/end_city_treasure", "End City Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/igloo_chest", "Igloo Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/jungle_temple", "Jungle Temple Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/jungle_temple_dispenser", "Jungle Temple Dispenser Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/nether_bridge", "Nether Bridge Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/pillager_outpost", "Pillager Outpost Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/ruined_portal", "Ruined Portal Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/shipwreck_map", "Shipwreck Map Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/shipwreck_supply", "Shipwreck Supply Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/shipwreck_treasure", "Shipwreck Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/simple_dungeon", "Simple Dungeon Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/spawn_bonus_chest", "Spawn Bonus Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/stronghold_corridor", "Stronghold Corridor Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/stronghold_crossing", "Stronghold Crossing Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/stronghold_library", "Stronghold Library Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/underwater_ruin_big", "Underwater Ruin Big Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/underwater_ruin_small", "Underwater Ruin Small Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_armorer", "Village Armorer Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_butcher", "Village Butcher Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_cartographer", "Village Cartographer Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_desert_house", "Village Desert House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_fisher", "Village Fisher Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_fletcher", "Village Fletcher Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_mason", "Village Mason Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_plains_house", "Village Plains House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_savanna_house", "Village Savanna House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_shepherd", "Village Shepherd Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_snowy_house", "Village Snowy House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_taiga_house", "Village Taiga House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_tannery", "Village Tannery Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_temple", "Village Temple Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_toolsmith", "Village Toolsmith Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_weaponsmith", "Village Weaponsmith Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/woodland_mansion", "Woodland Mansion Chest");

        TRANSLATION_MAP.put("ali/loot_table/archaeology/desert_pyramid", "Desert Pyramid");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/desert_well", "Desert Well");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/ocean_ruin_cold", "Ocean Ruin Cold");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/ocean_ruin_warm", "Ocean Ruins Warm");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/trail_ruins_common", "Trail Ruins Common");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/trail_ruins_rare", "Trail Ruins Rare");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/armorer_gift", "Armorer Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/butcher_gift", "Butcher Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/cartographer_gift", "Cartographer Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/cleric_gift", "Cleric Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/farmer_gift", "Farmer Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/fisherman_gift", "Fisherman Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/fletcher_gift", "Fletcher Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/leatherworker_gift", "Leatherworker Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/librarian_gift", "Librarian Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/mason_gift", "Mason Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/shepherd_gift", "Shepherd Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/toolsmith_gift", "Toolsmith Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/weaponsmith_gift", "Weaponsmith Gift");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing", "Fishing");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/junk", "Fishing: Junk");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/fish", "Fishing: Fish");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/treasure", "Fishing: Treasure");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/sniffer_digging", "Sniffer Digging");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/cat_morning_gift", "Cat Morning Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/piglin_bartering", "Piglin Bartering");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/panda_sneeze", "Panda Sneeze");
    }
}
