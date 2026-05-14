package com.yanny.ali.language;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class Lang {
    public enum Conditions implements ITooltipKey {
        ALL_OF("all_of", "All Of:"),
        ANY_OF("any_of", "Any Of:"),
        BLOCK_STATE_PROPERTY("block_state_property", "Block State Property:"),
        DAMAGE_SOURCE_PROPERTIES("damage_source_properties", "Damage Source Properties:"),
        ENTITY_PROPERTIES("entity_properties", "Entity Properties:"),
        ENTITY_SCORES("entity_scores", "Entity Scores:"),
        INVERTED("inverted", "Inverted:"),
        KILLED_BY_PLAYER("killed_by_player", "Killed by player"),
        LOCATION_CHECK("location_check", "Location Check:"),
        MATCH_TOOL("match_tool", "Match Tool:"),
        RANDOM_CHANCE("random_chance", "Random Chance:"),
        RANDOM_CHANCE_WITH_LOOTING("random_chance_with_looting", "Random Chance With Looting:"),
        REFERENCE("reference", "Reference: %s"),
        SURVIVES_EXPLOSION("survives_explosion", "Survives Explosion"),
        TABLE_BONUS("table_bonus", "Table Bonus:"),
        TIME_CHECK("time_check", "Time Check:"),
        VALUE_CHECK("value_check", "Value Check:"),
        WEATHER_CHECK("weather_check", "Weather Check:"),
        // Forge
        CAN_TOOL_PERFORM_ACTION("can_tool_perform_action", "Can Tool Perform Action: %s"),
        LOOT_TABLE_ID("loot_table_id", "Loot Table Id: %s"),
        // LootJS
        AND("and", "And:"),
        ANY_BIOME("any_biome", "Any Biome:"),
        ANY_DIMENSION("any_dimension", "Any Dimension:"),
        ANY_STRUCTURE("any_structure", "Any Structure:"),
        BIOME("biome", "Biome:"),
        BLOCK_PREDICATE("block_predicate", "Block Predicate:"),
        DIRECT_KILLER_PREDICATE("direct_killer_predicate", "Direct Killer Predicate:"),
        DISTANCE_TO_KILLER("distance_to_killer", "Distance To Killer:"),
        ENTITY_PREDICATE("entity_predicate", "Entity Predicate:"),
        KILLER_PREDICATE("killer_predicate", "Killer Predicate:"),
        LIGHT_LEVEL("light_level", "Light Level:"),
        MATCH_DAMAGE_SOURCE("match_damage_source", "Match Damage Source:"),
        MATCH_EQUIPMENT_SLOT("match_equipment_slot", "Match Equipment Slot:"),
        MATCH_LOOT("match_loot", "Match Loot:"),
        MATCH_MAINHAND("match_mainhand", "Match Mainhand:"),
        MATCH_OFFHAND("match_offhand", "Match Offhand:"),
        MATCH_PLAYER("match_player", "Match Player:"),
        NOT("not", "Not:"),
        OR("or", "Or:"),
        PLAYER_PREDICATE("player_predicate", "Player Predicate:"),
        RANDOM_CHANCE_WITH_ENCHANTMENT("random_chance_with_enchantment", "Random Chance With Enchantment:")
        ;

        private final Translation translation;

        Conditions(String k, String e) {
            translation = new Translation("ali.type.condition." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Functions implements ITooltipKey {
        APPLY_BONUS("apply_bonus", "Apply Bonus:"),
        COPY_NAME("copy_name", "Copy Name:"),
        COPY_NBT("copy_nbt", "Copy Nbt:"),
        COPY_STATE("copy_state", "Copy State:"),
        ENCHANT_RANDOMLY("enchant_randomly", "Enchant Randomly:"),
        ENCHANT_WITH_LEVELS("enchant_with_levels", "Enchant With Levels:"),
        EXPLORATION_MAP("exploration_map", "Exploration Map:"),
        EXPLOSION_DECAY("explosion_decay", "Explosion Decay"),
        FILL_PLAYER_HEAD("fill_player_head", "Fill Player Head:"),
        FURNACE_SMELT("furnace_smelt", "Furnace Smelt"),
        LIMIT_COUNT("limit_count", "Limit Count:"),
        LOOTING_ENCHANT("looting_enchant", "Looting Enchant:"),
        REFERENCE("reference", "Reference:"),
        SET_ATTRIBUTES("set_attributes", "Set Attributes:"),
        SET_BANNER_PATTERN("set_banner_pattern", "Set Banner Pattern:"),
        SET_CONTENTS("set_contents", "Set Contents:"),
        SET_COUNT("set_count", "Set Count:"),
        SET_DAMAGE("set_damage", "Set Damage:"),
        SET_ENCHANTMENTS("set_enchantments", "Set Enchantments:"),
        SET_INSTRUMENT("set_instrument", "Set Instrument:"),
        SET_LOOT_TABLE("set_loot_table", "Set Loot Table:"),
        SET_LORE("set_lore", "Set Lore:"),
        SET_NAME("set_name", "Set Name:"),
        SET_NBT("set_nbt", "Set Nbt:"),
        SET_POTION("set_potion", "Set Potion:"),
        SET_STEW_EFFECT("set_stew_effect", "Set Stew Effect:"),
        // LootJS
        CUSTOM_PLAYER("custom_player", "Custom Player Modifier:"),
        // Farmer's delight
        COPY_MEAL("copy_meal", "Copy Meal"),
        COPY_SKILLET("copy_skillet", "Copy Skillet"),
        // Trades
        DYED_RANDOMLY("dyed_randomly", "Dyed Randomly"),
        ;

        private final Translation translation;

        Functions(String k, String e) {
            this.translation = new Translation("ali.type.function." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        ABSOLUTE("absolute", "Absolute: %s"),
        ADD("add", "Add: %s"),
        ALLOW_DUPLICATE_LOOT("allow_duplicate_loot", "Allow Duplicate Loot: %s"),
        AMOUNT("amount", "Amount: %s"),
        AMPLIFIER("amplifier", "Amplifier: %s"),
        APPEND("append", "Append: %s"),
        ATTRIBUTE("attribute", "Attribute: %s"),
        BANNER_PATTERN("banner_pattern", "Banner Pattern: %s"),
        BIOME("biome", "Biome: %s"),
        BLOCK("block", "Block: %s"),
        BLOCK_ENTITY_TYPE("block_entity_type", "Block Entity Type: %s"),
        BLOCKS_ON_FIRE("blocks_on_fire", "Blocks On Fire: %s"),
        BONUS_MULTIPLIER("bonus_multiplier", "Bonus Multiplier: %s"),
        COLOR("color", "Color: %s"),
        COUNT("count", "Count: %s"),
        DAMAGE("damage", "Damage: %s"),
        DESTINATION("destination", "Destination: %s"),
        DIMENSION("dimension", "Dimension: %s"),
        DONE("done", "Done: %s"),
        DURABILITY("durability", "Durability: %s"),
        DURATION("duration", "Duration: %s"),
        EFFECT("effect", "Effect: %s"),
        ENCHANTMENT("enchantment", "Enchantment: %s"),
        ENTITY_TYPE("entity_type", "Entity Type: %s"),
        EQUIPMENT_SLOT("equipment_slot", "Equipment Slot: %s"),
        EXACT("exact", "Exact: %s"),
        EXTRA_ROUNDS("extra_rounds", "Extra Rounds: %s"),
        FLUID("fluid", "Fluid: %s"),
        FORMULA("formula", "Formula: %s"),
        GAME_TYPE("game_type", "Game Type: %s"),
        HORIZONTAL("horizontal", "Horizontal: %s"),
        ID("id", "Id: %s"),
        IN_OPEN_WATER("in_open_water", "Is In Open Water: %s"),
        IS_AMBIENT("is_ambient", "Is Ambient: %s"),
        IS_BABY("is_baby", "Is Baby: %s"),
        IS_CROUCHING("is_crouching", "Is Crouching: %s"),
        IS_ON_FIRE("is_on_fire", "Is On Fire: %s"),
        IS_RAINING("is_raining", "Is Raining: %s"),
        IS_SPRINTING("is_sprinting", "Is Sprinting: %s"),
        IS_SWIMMING("is_swimming", "Is Swimming: %s"),
        IS_THUNDERING("is_thundering", "Is Thundering: %s"),
        IS_VISIBLE("is_visible", "Is Visible: %s"),
        ITEM("item", "Item: %s"),
        ITEM_FILTER("item_filter", "Item Filter: %s"),
        LEVEL("level", "Level: %s"),
        LEVELS("levels", "Levels: %s"),
        LIGHT("light", "Light: %s"),
        LIMIT("limit", "Limit: %s"),
        LOOT_TABLE("loot_table", "Loot Table: %s"),
        LORE("lore", "Lore: %s"),
        MAP_DECORATION("map_decoration", "Map Decoration: %s"),
        MERGE_STRATEGY("merge_strategy", "Merge Strategy: %s"),
        MOB_EFFECT("mob_effect", "Mob Effect: %s"),
        MULTIPLIER("multiplier", "Multiplier: %s"),
        NAME("name", "Name: %s"),
        NBT("nbt", "Nbt: %s"),
        NBT_PROVIDER("nbt_provider", "Nbt Provider: %s"),
        OPERATION("operation", "Operation: %s"),
        OPTIONS("options", "Options: %s"),
        PERCENT("percent", "Percent: %s"),
        PERIOD("period", "Period: %s"),
        POTION("potion", "Potion: %s"),
        PRICE_MULTIPLIER("price_multiplier", "Price Multiplier: %s"),
        PROBABILITY("probability", "Probability: %s"),
        PROPERTY("property", "Property: %s"),
        PROVIDER("provider", "Provider: %s"),
        RANGE("range", "Range: %s"),
        RANGED_ANY("ranged_property_any", "%s: any"),
        RANGED_BOTH("ranged_property_both", "%s: %s-%s"),
        RANGED_GTE("ranged_property_gte", "%s: ≥%s"),
        RANGED_LTE("ranged_property_lte", "%s: ≤%s"),
        REPLACE("replace", "Replace: %s"),
        RESOLUTION_CONTEXT("resolution_context", "Resolution Context: %s"),
        SEARCH_RADIUS("search_radius", "Search Radius: %s"),
        SEED("seed", "Seed: %s"),
        SIZE("size", "Size: %s"),
        SKIP_KNOWN_STRUCTURES("skip_known_structures", "Skip Known Structures: %s"),
        SLOT("slot", "Slot: %s"),
        SMOKEY("smokey", "Smokey: %s"),
        SOURCE("source", "Source: %s"),
        STRUCTURE("structure", "Structure: %s"),
        TAG("tag", "Tag: %s"),
        TARGET("target", "Target: %s"),
        TEAM("team", "Team: %s"),
        TREASURE("treasure", "Treasure: %s"),
        USES("uses", "Uses: %s"),
        UUID("uuid", "UUID: %s"),
        VALUE("value", "Value: %s"),
        VALUES("values", "Values: %s"),
        VARIANT("variant", "Variant: %s"),
        VILLAGER_TYPE("villager_type", "Villager Type: %s"),
        VILLAGER_XP("villager_xp", "XP: %s"),
        X("x", "X: %s"),
        Y("y", "Y: %s"),
        Z("z", "Z: %s"),
        ZOOM("zoom", "Zoom: %s"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            this.translation = new Translation("ali.property.value." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        ADVANCEMENTS("advancements", "Advancements:"),
        ALL("all", "All:"),
        ANY("any", "Any:"),
        BANNER_PATTERNS("banner_patterns", "Banner Patterns:"),
        BIOMES("biomes", "Biomes:"),
        BLOCKS(Value.BLOCK, "blocks", "Blocks:"),
        BLOCK_PREDICATE("block_predicate", "Block Predicate:"),
        CHEST("chest", "Chest:"),
        CONDITION("condition", "Predicate:"),
        CONDITIONS("conditions", "Predicates:"),
        CRITERIONS("criterions", "Criterions:"),
        DIMENSIONS("dimensions", "Dimensions:"),
        DIRECT_ENTITY("direct_entity", "Direct Entity:"),
        DISTANCE_TO_PLAYER("distance_to_player", "Distance to Player:"),
        ENCHANTMENTS(Value.ENCHANTMENT, "enchantments", "Enchantments:"),
        ENTITY_EQUIPMENT("entity_equipment", "Entity Equipment:"),
        ENTITY_FLAGS("entity_flags", "Entity Flags:"),
        ENTITY_SUB_PREDICATE("entity_sub_predicate", "Entity Sub Predicate:"),
        EQUIPMENT_SLOTS(Value.EQUIPMENT_SLOT, "equipment_slots", "Equipment Slots:"),
        FEET("feet", "Feet:"),
        FLUID_PREDICATE("fluid_predicate", "Fluid Predicate:"),
        HEAD("head", "Head:"),
        ITEMS(Value.ITEM, "items", "Items:"),
        LEGS("legs", "Legs:"),
        LOCATION("location", "Location:"),
        LOOKING_AT("looking_at", "Looking At:"),
        LORE(Value.LORE, "lore", "Lore:"),
        MAINHAND("mainhand", "Main Hand:"),
        MOB_EFFECTS(Value.MOB_EFFECT, "mob_effects", "Mob Effects:"),
        MODIFIER("modifier", "Modifier:"),
        OFFHAND("offhand", "Offhand:"),
        OPERATION("operation", "Operation:"),
        OPERATIONS("operations", "Operations:"),
        PASSENGER("passenger", "Passenger:"),
        PREDICATE("predicate", "Predicate:"),
        PROPERTIES(Value.PROPERTY, "properties", "Properties:"),
        RECIPES("recipes", "Recipes:"),
        SCORES("scores", "Scores:"),
        SOURCE_ENTITY("source_entity", "Source Entity:"),
        SOURCE_NAMES("source_names", "Source Names:"),
        STATS("stats", "Stats:"),
        STEPPING_ON_LOCATION("stepping_on_location", "Stepping on Location:"),
        STORED_ENCHANTMENTS("stored_enchantments", "Stored Enchantments:"),
        STRUCTURES("structures", "Structures:"),
        STUCK_ENTITY("stuck_entity", "Stuck Entity:"),
        TAGS("tags", "Tags:"),
        TARGETED_ENTITY("targeted_entity", "Targeted Entity:"),
        VEHICLE("vehicle", "Vehicle:"),
        ;

        private final Translation translation;

        Branch(ITooltipKey s, String k, String e) {
            this.translation = new Translation(s.singular(), "ali.property.branch." + k, s.englishSingular(), e);
        }

        Branch(String k, String e) {
            this.translation = new Translation("ali.property.branch." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Description implements ITooltipKey {ROLLS("rolls", "Rolls: %s%s"),
        CHANCE("chance", "Chance: %s%s"),
        CHANCE_BONUS("chance_bonus", "%s (%s %s)"),
        COUNT("count", "Count: %s"),
        COUNT_BONUS("count_bonus", "%s (%s %s)"),
        QUALITY("quality", "Quality: %s"),
        ;

        private final Translation translation;

        Description(String k, String e) {
            this.translation = new Translation("ali.description." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Group implements ITooltipKey {
        ALL("all", "Selects all entries"),
        RANDOM("random", "Selects random entry"),
        ALTERNATIVES("alternatives", "Selects only first successful entry"),
        SEQUENCE("sequence", "Selects entries sequentially until first failed"),
        DYNAMIC("dynamic", "Dynamic block-specific drops"),
        EMPTY("empty", "Drops nothing"),
        MISSING("missing", "Not implemented"),
        ;

        private final Translation translation;

        Group(String k, String e) {
            this.translation = new Translation("ali.enum.group_type." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Error implements ITooltipKey {
        DETAIL_NOT_AVAILABLE("detail_not_available", "Detail Not Available"),
        MODIFIED_ITEM("modified_item", "Modified dynamically!"),
        ;

        private final Translation translation;

        Error(String k, String e) {
            this.translation = new Translation("ali.error." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }
}
