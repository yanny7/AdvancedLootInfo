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
        ENCHANTMENT_ACTIVE_CHECK("enchantment_active_check", "Enchantment Active Check:"),
        ENTITY_PROPERTIES("entity_properties", "Entity Properties:"),
        ENTITY_SCORES("entity_scores", "Entity Scores:"),
        INVERTED("inverted", "Inverted:"),
        KILLED_BY_PLAYER("killed_by_player", "Killed by player"),
        LOCATION_CHECK("location_check", "Location Check:"),
        MATCH_TOOL("match_tool", "Match Tool:"),
        RANDOM_CHANCE("random_chance", "Random Chance:"),
        RANDOM_CHANCE_WITH_ENCHANTED_BONUS("random_chance_with_enchanted_bonus", "Random Chance With Enchanted Bonus:"),
        REFERENCE("reference", "Reference:"),
        SURVIVES_EXPLOSION("survives_explosion", "Survives Explosion"),
        TABLE_BONUS("table_bonus", "Table Bonus:"),
        TIME_CHECK("time_check", "Time Check:"),
        VALUE_CHECK("value_check", "Value Check:"),
        WEATHER_CHECK("weather_check", "Weather Check:"),
        // Forge
        CAN_ITEM_PERFORM_ABILITY("can_item_perform_ability", "Can Item Perform Ability: %s"),
        LOOT_TABLE_ID("loot_table_id", "Loot Table Id: %s"),
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
        COPY_COMPONENTS("copy_components", "Copy Components:"),
        COPY_CUSTOM_DATA("copy_custom_data", "Copy Custom Data:"),
        COPY_NAME("copy_name", "Copy Name:"),
        COPY_STATE("copy_state", "Copy State:"),
        ENCHANTED_COUNT_INCREASE("enchanted_count_increase", "Enchanted Count Increase:"),
        ENCHANT_RANDOMLY("enchant_randomly", "Enchant Randomly:"),
        ENCHANT_WITH_LEVELS("enchant_with_levels", "Enchant With Levels:"),
        EXPLORATION_MAP("exploration_map", "Exploration Map:"),
        EXPLOSION_DECAY("explosion_decay", "Explosion Decay"),
        FILL_PLAYER_HEAD("fill_player_head", "Fill Player Head:"),
        FILTERED("filtered", "Filtered:"),
        FURNACE_SMELT("furnace_smelt", "Furnace Smelt"),
        LIMIT_COUNT("limit_count", "Limit Count:"),
        MODIFY_CONTENTS("modify_contents", "Modify Contents:"),
        REFERENCE("reference", "Reference:"),
        SEQUENCE("sequence", "Sequence:"),
        SET_ATTRIBUTES("set_attributes", "Set Attributes:"),
        SET_BANNER_PATTERN("set_banner_pattern", "Set Banner Pattern:"),
        SET_BOOK_COVER("set_book_cover", "Set Book Cover:"),
        SET_COMPONENTS("set_components", "Set Components:"),
        SET_CONTENTS("set_contents", "Set Contents:"),
        SET_COUNT("set_count", "Set Count:"),
        SET_CUSTOM_DATA("set_custom_data", "Set Custom Data:"),
        SET_CUSTOM_MODEL_DATA("set_custom_model_data", "Set Custom Model Data:"),
        SET_DAMAGE("set_damage", "Set Damage:"),
        SET_ENCHANTMENTS("set_enchantments", "Set Enchantments:"),
        SET_FIREWORKS("set_fireworks", "Set Fireworks:"),
        SET_FIREWORK_EXPLOSION("set_firework_explosion", "Set Firework Explosion:"),
        SET_INSTRUMENT("set_instrument", "Set Instrument:"),
        SET_ITEM("set_item", "Set Item:"),
        SET_LOOT_TABLE("set_loot_table", "Set Loot Table:"),
        SET_LORE("set_lore", "Set Lore:"),
        SET_NAME("set_name", "Set Name:"),
        SET_OMINOUS_BOTTLE_AMPLIFIER("set_ominous_bottle_amplifier", "Set Ominous Bottle Amplifier:"),
        SET_POTION("set_potion", "Set Potion:"),
        SET_STEW_EFFECT("set_stew_effect", "Set Stew Effect:"),
        SET_WRITABLE_BOOK_PAGES("set_writable_book_pages", "Set Writable Book Pages:"),
        SET_WRITTEN_BOOK_PAGES("set_written_book_pages", "Set Written Book Pages:"),
        TOGGLE_TOOLTIPS("toggle_tooltips", "Toggle Tooltips:"),
        // Farmer's delight
        COPY_SKILLET("copy_skillet", "Copy Skillet"),
        SMOKER_COOK("smoker_cook", "Smoker Cook"),
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

    public enum EntitySubPredicates implements ITooltipKey {
        FISHING_HOOK("fishing_hook", "Fishing Hook:"),
        LIGHTNING_BOLT("lightning_bolt", "Lightning Bolt:"),
        PLAYER("player", "Player:"),
        RAIDER("raider", "Raider:"),
        SHEEP("sheep", "Sheep:"),
        SLIME("slime", "Slime:"),
        ;

        private final Translation translation;

        EntitySubPredicates(String k, String e) {
            this.translation = new Translation("ali.type.entity_sub_predicate." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum ConsumeEffects implements ITooltipKey {
        APPLY_EFFECTS("apply_effects", "Apply Effects:"),
        REMOVE_EFFECTS("remove_effects", "Remove Effects:"),
        CLEAR_ALL_EFFECTS("clear_all_effects", "Clear All Effects"),
        TELEPORT_RANDOMLY("teleport_randomly", "Teleport Randomly:"),
        PLAY_SOUND("play_sound", "Play Sound:"),
        ;

        private final Translation translation;

        ConsumeEffects(String k, String e) {
            this.translation = new Translation("ali.type.consume_effect." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        ABSOLUTE("absolute", "Absolute: %s"),
        ACTIVE("active", "Active: %s"),
        ADD("add", "Add: %s"),
        ADDED("added", "Added: %s"),
        AMBIENT("ambient", "Ambient: %s"),
        AMOUNT("amount", "Amount: %s"),
        AMPLIFIER("amplifier", "Amplifier: %s"),
        APPEND("append", "Append: %s"),
        ATTRIBUTE("attribute", "Attribute: %s"),
        AUTHOR("author", "Author: %s"),
        BACK("back", "Back: %s"),
        BANNER_PATTERN("banner_pattern", "Banner Pattern: %s"),
        BASE("base", "Base: %s"),
        BIOME("biome", "Biome: %s"),
        BLOCK("block", "Block: %s"),
        BLOCKS_ON_FIRE("blocks_on_fire", "Blocks On Fire: %s"),
        BLOCK_ENTITY_TYPE("block_entity_type", "Block Entity Type: %s"),
        BONUS_MULTIPLIER("bonus_multiplier", "Bonus Multiplier: %s"),
        CAN_ALWAYS_EAT("can_always_eat", "Can Always Eat: %s"),
        CAN_SEE_SKY("can_see_sky", "Can See Sky: %s"),
        CHANCE("chance", "Chance: %s"),
        COLOR("color", "Color: %s"),
        COLORS("colors", "Colors: %s"),
        COMPONENT("component", "Component: %s"),
        CONSTANT("constant", "Constant: %s"),
        CONTAINER("container", "Container: %s"),
        CORRECT_FOR_DROPS("correct_for_drops", "Correct For Drops: %s"),
        COUNT("count", "Count: %s"),
        CUSTOM_COLOR("custom_color", "Custom Color: %s"),
        CUSTOM_NAME("custom_name", "Custom Name: %s"),
        DAMAGE("damage", "Damage: %s"),
        DAMAGE_PER_BLOCK("damage_per_block", "Damage Per Block: %s"),
        DECORATION("decoration", "Decoration: %s"),
        DEFAULT_MINING_SPEED("default_mining_speed", "Default Mining Speed: %s"),
        DESTINATION("destination", "Destination: %s"),
        DIMENSION("dimension", "Dimension: %s"),
        DONE("done", "Done: %s"),
        DURABILITY("durability", "Durability: %s"),
        DURATION("duration", "Duration: %s"),
        EFFECT("effect", "Effect: %s"),
        ENCHANTMENT("enchantment", "Enchantment: %s"),
        ENTITY_DATA("entity_data", "Entity Data: %s"),
        ENTITY_TYPE("entity_type", "Entity Type: %s"),
        EQUIPMENT_SLOT("equipment_slot", "Equipment Slot: %s"),
        EXCLUDE("exclude", "Exclude: %s"),
        EXTRA_ROUNDS("extra_rounds", "Extra Rounds: %s"),
        FADE_COLORS("fade_colors", "Fade Colors: %s"),
        FALL_DISTANCE("fall_distance", "Fall Distance: %s"),
        FILTERED("filtered", "Filtered: %s"),
        FLIGHT_DURATION("flight_duration", "Flight Duration: %s"),
        FLUID("fluid", "Fluid: %s"),
        FORMULA("formula", "Formula: %s"),
        FRACTION("fraction", "Fraction: %s"),
        FRONT("front", "Front: %s"),
        GAME_TYPE("game_type", "Game Type: %s"),
        GENERATION("generation", "Generation: %s"),
        HAS_RAID("has_raid", "Has Raid: %s"),
        HAS_TRAIL("has_trail", "Has Trail: %s"),
        HAS_TWINKLE("has_twinkle", "Has Twinkle: %s"),
        HORIZONTAL("horizontal", "Horizontal: %s"),
        HORIZONTAL_SPEED("horizontal_speed", "Horizontal Speed: %s"),
        ID("id", "Id: %s"),
        INCLUDE("include", "Include: %s"),
        IN_OPEN_WATER("in_open_water", "Is In Open Water: %s"),
        IS_AMBIENT("is_ambient", "Is Ambient: %s"),
        IS_BABY("is_baby", "Is Baby: %s"),
        IS_CAPTAIN("is_captain", "Is Captain: %s"),
        IS_CROUCHING("is_crouching", "Is Crouching: %s"),
        IS_DIRECT("is_direct", "Is Direct: %s"),
        IS_FLYING("is_flying", "Is Flying: %s"),
        IS_ON_FIRE("is_on_fire", "Is On Fire: %s"),
        IS_ON_GROUND("is_on_ground", "Is On Ground: %s"),
        IS_RAINING("is_raining", "Is Raining: %s"),
        IS_SPRINTING("is_sprinting", "Is Sprinting: %s"),
        IS_SWIMMING("is_swimming", "Is Swimming: %s"),
        IS_THUNDERING("is_thundering", "Is Thundering: %s"),
        IS_VISIBLE("is_visible", "Is Visible: %s"),
        ITEM("item", "Item: %s"),
        ITEM_NAME("item_name", "Item Name: %s"),
        LEFT("left", "Left: %s"),
        LEVEL("level", "Level: %s"),
        LEVELS("levels", "Levels: %s"),
        LIGHT("light", "Light: %s"),
        LIMIT("limit", "Limit: %s"),
        LINE("line", "Line: %s"),
        LIST_OPERATION("list_operation", "List Operation: %s"),
        LOOT_TABLE("loot_table", "Loot Table: %s"),
        LORE("lore", "Lore: %s"),
        MAP_DECORATION("map_decoration", "Map Decoration: %s"),
        MATERIAL("material", "Material: %s"),
        MAX("max", "Max: %s"),
        MERGE_STRATEGY("merge_strategy", "Merge Strategy: %s"),
        MIN("min", "Min: %s"),
        MIN_TICKS_IN_HIVE("min_ticks_in_hive", "Min Ticks In Hive: %s"),
        NAME("name", "Name: %s"),
        NBT("nbt", "Nbt: %s"),
        NUTRITION("nutrition", "Nutrition: %s"),
        OFFSET("offset", "Offset: %s"),
        ONLY_COMPATIBLE("only_compatible", "Only Compatible: %s"),
        OPERATION("operation", "Operation: %s"),
        OPTIONS("options", "Options: %s"),
        PAGE("page", "Page: %s"),
        PATTERN("pattern", "Pattern: %s"),
        PERIOD("period", "Period: %s"),
        PERIODIC_TICK("periodic_tick", "Periodic Tick: %s"),
        PER_LEVEL("per_level", "Per Level: %s"),
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
        RARITY("rarity", "Rarity: %s"),
        RAW("raw", "Raw: %s"),
        RECIPE("recipe", "Recipe: %s"),
        REPLACE("replace", "Replace: %s"),
        RESOLUTION_CONTEXT("resolution_context", "Resolution Context: %s"),
        RESOLVED("resolved", "Resolved: %s"),
        RGB("rgb", "RGB: %s"),
        RIGHT("right", "Right: %s"),
        ROTATION("rotation", "Rotation: %s"),
        SATURATION("saturation", "Saturation: %s"),
        SEARCH_RADIUS("search_radius", "Search Radius: %s"),
        SEED("seed", "Seed: %s"),
        SHAPE("shape", "Shape: %s"),
        SHOW_ICON("show_icon", "Show Icon: %s"),
        SIGNATURE("signature", "Signature: %s"),
        SIZE("size", "Size: %s"),
        SKIP_KNOWN_STRUCTURES("skip_known_structures", "Skip Known Structures: %s"),
        SLOT("slot", "Slot: %s"),
        SMOKEY("smokey", "Smokey: %s"),
        SONG("song", "Song: %s"),
        SOURCE("source", "Source: %s"),
        SOURCE_PATH("source_path", "Source Path: %s"),
        SPEED("speed", "Speed: %s"),
        STRUCTURE("structure", "Structure: %s"),
        TAG("tag", "Tag: %s"),
        TARGET("target", "Target: %s"),
        TARGET_PATH("target_path", "Target Path: %s"),
        TEAM("team", "Team: %s"),
        TICKS_IN_HIVE("ticks_in_hive", "Ticks In Hive: %s"),
        TITLE("title", "Title: %s"),
        TOOLTIP("tooltip", "Tooltip: %s"),
        TRACKED("tracked", "Tracked: %s"),
        TRAIL("trail", "Trail: %s"),
        TREASURE("treasure", "Treasure: %s"),
        TWINKLE("twinkle", "Twinkle: %s"),
        TYPE("type", "Type: %s"),
        UNENCHANTED_CHANCE("unenchanted_chance", "Unenchanted Chance: %s"),
        USES("uses", "Uses: %s"),
        UUID("uuid", "UUID: %s"),
        VALUE("value", "Value: %s"),
        VALUES("values", "Values: %s"),
        VARIANT("variant", "Variant: %s"),
        VERTICAL_SPEED("vertical_speed", "Vertical Speed: %s"),
        VILLAGER_TYPE("villager_type", "Villager Type: %s"),
        X("x", "X: %s"),
        XP("villager_xp", "XP: %s"),
        Y("y", "Y: %s"),
        Z("z", "Z: %s"),
        ZOOM("zoom", "Zoom: %s"),
        FLOATS("floats", "Floats: %s"),
        FLAGS("flags", "Flags: %s"),
        STRINGS("strings", "Strings: %s"),
        HIDE_TOOLTIP("hide_tooltip", "Hide Tooltip: %s"),
        CONSUME_SECONDS("consume_seconds", "Consume Seconds: %s"),
        ANIMATION("animation", "Animation: %s"),
        SOUND("sound", "Sound: %s"),
        SECONDS("seconds", "Seconds: %s"),
        COOLDOWN_GROUP("cooldown_group", "Cooldown Group: %s"),
        HAS_CUSTOM_PARTICLES("has_custom_particles", "Has Custom Particles: %s"),
        CAN_DESTROY_BLOCKS_IN_CREATIVE("can_destroy_blocks_in_creative", "Can Destroy Blocks In Creative: %s"),
        ITEM_DAMAGE_PER_ATTACK("item_damage_per_attack", "Item Damage Per Attack: %s"),
        DISABLE_BLOCKING_FOR_SECONDS("disable_blocking_for_seconds", "Disable Blocking For Seconds: %s"),
        EQUIP_SOUND("equip_sound", "Equip Sound: %s"),
        ASSET_ID("asset_id", "Asset ID: %s"),
        CAMERA_OVERLAY("camera_overlay", "Camera Overlay: %s"),
        DISPENSABLE("dispensable", "Dispensable: %s"),
        SWAPPABLE("swappable", "Swappable: %s"),
        DAMAGE_ON_HURT("damage_on_hurt", "Damage On Hurt: %s"),
        EQUIP_ON_INTERACT("equip_on_interact", "Equip On Interact: %s"),
        BLOCK_DELAY_SECONDS("block_delay_seconds", "Block Delay Seconds: %s"),
        DISABLE_COOLDOWN_SCALE("disable_cooldown_scale", "Disable Cooldown Scale: %s"),
        BYPASSED_BY("bypassed_by", "Bypassed By: %s"),
        BLOCK_SOUND("block_sound", "Block Sound: %s"),
        DISABLE_SOUND("disable_sound", "Disable Sound: %s"),
        FORWARD("forward", "Forward: %s"),
        BACKWARD("backward", "Backward: %s"),
        JUMP("jump", "Jump: %s"),
        SNEAK("sneak", "Sneak: %s"),
        SPRINT("sprint", "Sprint: %s"),
        FACTOR("factor", "Factor: %s"),
        HORIZONTAL_BLOCKING_ANGLE("horizontal_blocking_angle", "Horizontal Blocking Angle: %s"),
        THRESHOLD("threshold", "Threshold: %s"),
        SHEARED("sheared",  "Sheared: %s"),
        DIAMETER("diameter", "Diameter: %s"),
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
        ALLOWED_ENTITIES("allowed_entities", "Allowed Entities:"),
        ENCHANTMENT_PREDICATE("enchantment_predicate", "Enchantment Predicate:"),
        POTIONS("potions", "Potions:"),
        DAMAGE_TYPES("damage_types", "Damage Types:"),
        ITEM_DAMAGE("item_damage", "Item Damage:"),
        INPUT("input", "Input:"),
        FLOATS("floats", "Floats:"),
        COLORS("colors", "Colors:"),
        FLAGS("flags", "Flags:"),
        STRINGS("strings", "Strings:"),
        DAMAGE_REDUCTION("damage_reduction", "Damage Reduction:"),
        DAMAGE_REDUCTIONS("damage_reductions", "Damage Reductions:"),
        DEATH_EFFECTS("death_effects", "Death Effects:"),
        ON_CONSUME_EFFECTS("on_consume_effects", "On Consume Effects:"),
        CONVERT_INTO("convert_into", "Convert Into:"),
        HIDDEN_COMPONENTS("hidden_components", "Hidden Components:"),
        ADVANCEMENTS("advancements", "Advancements:"),
        AFFECTS_MOVEMENT("affects_movement", "Affects Movement:"),
        ATTRIBUTE_MODIFIER("attribute_modifier", "Attribute Modifier:"),
        ATTRIBUTES(Value.ATTRIBUTE, "attributes", "Attributes:"),
        BANNER_PATTERNS("banner_patterns", "Banner Patterns:"),
        BEES("bees", "Bees:"),
        BIOMES(Value.BIOME, "biomes", "Biomes:"),
        BLOCKS(Value.BLOCK, "blocks", "Blocks:"),
        BLOCK_PREDICATE("block_predicate", "Block Predicate:"),
        BODY("body", "Body:"),
        CHEST("chest", "Chest:"),
        CLAMPED("clamped", "Clamped:"),
        COMPONENTS(Value.COMPONENT, "components", "Components:"),
        CONDITION("condition", "Predicate:"),
        CONDITIONS("conditions", "Predicates:"),
        CONTAINS("contains", "Contains:"),
        COPY_OPERATIONS("copy_operations", "Copy Operations:"),
        COUNTS("counts", "Counts:"),
        CRITERIONS("criterions", "Criterions:"),
        CUSTOM_EFFECTS("custom_effects", "Custom Effects:"),
        DECORATIONS("decorations", "Decorations:"),
        DENOMINATOR("denominator", "Denominator:"),
        DIRECT_ENTITY("direct_entity", "Direct Entity:"),
        DISTANCE_TO_PLAYER("distance_to_player", "Distance to Player:"),
        EFFECT("effect", "Effect:"),
        EFFECTS("effects", "Effects:"),
        ENCHANTED_CHANCE("enchanted_chance", "Enchanted Chance:"),
        ENCHANTMENT(Value.ENCHANTMENT, "enchantment", "Enchantment:"),
        ENCHANTMENTS(Value.ENCHANTMENT, "enchantments", "Enchantments:"),
        ENTITY_EQUIPMENT("entity_equipment", "Entity Equipment:"),
        ENTITY_FLAGS("entity_flags", "Entity Flags:"),
        ENTITY_SUB_PREDICATE("entity_sub_predicate", "Entity Sub Predicate:"),
        ENTITY_TYPES(Value.ENTITY_TYPE, "entity_types", "Entity Types:"),
        EQUIPMENT_SLOTS(Value.EQUIPMENT_SLOT, "equipment_slots", "Equipment Slots:"),
        EXCLUDE(Value.EXCLUDE, "exclude", "Exclude:"),
        EXPECTED_COMPONENTS("expected_components", "Expected Components:"),
        EXPLOSION("explosion", "Explosion:"),
        EXPLOSIONS("explosions", "Explosions:"),
        FALLBACK("fallback", "Fallback:"),
        FEET("feet", "Feet:"),
        FILTER("filter", "Filter:"),
        FLUID_PREDICATE("fluid_predicate", "Fluid Predicate:"),
        FLUIDS(Value.FLUID, "fluids", "Fluids:"),
        FRACTION("fraction", "Fraction:"),
        GAME_TYPES(Value.GAME_TYPE, "game_types", "Game Types:"),
        GLOBAL_POS("global_pos", "Global Position:"),
        HEAD("head", "Head:"),
        HIDDEN_EFFECT("hidden_effect", "Hidden Effect:"),
        INCLUDE(Value.INCLUDE, "include", "Include:"),
        ITEM("item", "Item:"),
        ITEMS(Value.ITEM, "items", "Items:"),
        LEGS("legs", "Legs:"),
        LEVEL_SQUARED("level_squared", "Squared Level:"),
        LINEAR("linear", "Linear:"),
        LINES(Value.LINE, "lines", "Lines:"),
        LOCATED("located", "Located:"),
        LOCATION("location", "Location:"),
        LOOKING_AT("looking_at", "Looking At:"),
        LOOKUP("lookup", "Lookup:"),
        LORE(Value.LORE, "lore", "Lore:"),
        MAINHAND("mainhand", "Main Hand:"),
        MATERIALS(Value.MATERIAL, "materials", "Materials:"),
        MOB_EFFECTS("mob_effects", "Mob Effects:"),
        MODIFIER("modifier", "Modifier:"),
        MODIFIERS("modifiers", "Modifiers:"),
        MOVEMENT("movement", "Movement:"),
        NUMERATOR("numerator", "Numerator:"),
        OCCUPANT("occupant", "Occupant:"),
        OFFHAND("offhand", "Offhand:"),
        OPERATION("operation", "Operation:"),
        OPTIONS(Value.OPTIONS, "options", "Options:"),
        PAGE("page", "Page:"),
        PAGES("pages", "Pages:"),
        PARTIAL_MATCHERS("partial_matchers", "Partial Matchers:"),
        PASSENGER("passenger", "Passenger:"),
        PATTERNS(Value.PATTERN, "patterns", "Patterns:"),
        POSITION("position", "Position:"),
        PREDICATE("predicate", "Predicate:"),
        PROPERTIES(Value.PROPERTY, "properties", "Properties:"),
        PROPERTY("property", "Property:"),
        RECIPES(Value.RECIPE, "recipes", "Recipes:"),
        RULE("rule", "Rule:"),
        RULES("rules", "Rules:"),
        SCORES("scores", "Scores:"),
        SLOTS("slots", "Slots:"),
        SONGS(Value.SONG, "songs", "Songs:"),
        SOURCE_ENTITY("source_entity", "Source Entity:"),
        STATS("stats", "Stats:"),
        STEPPING_ON_LOCATION("stepping_on_location", "Stepping on Location:"),
        STRUCTURES(Value.STRUCTURE, "structures", "Structures:"),
        STUCK_ENTITY("stuck_entity", "Stuck Entity:"),
        STYLED_LINES(Value.LINE, "styled_lines", "Styled Lines:"),
        TAGS("tags", "Tags:"),
        TARGETED_ENTITY("targeted_entity", "Targeted Entity:"),
        TITLE("title", "Title:"),
        VALUE("value", "Value:"),
        VALUES("values", "Values:"),
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
        ALTERNATIVES("alternatives", "Selects only first successful entry"),
        DYNAMIC("dynamic", "Dynamic block-specific drops"),
        EMPTY("empty", "Drops nothing"),
        MISSING("missing", "Not implemented"),
        RANDOM("random", "Selects random entry"),
        SEQUENCE("sequence", "Selects entries sequentially until first failed"),
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

    public enum Multi implements ITooltipKey {
        OFFSET("offset", "Offset: [X: %s, Y: %s, Z: %s]"),
        POSITON("position", "Position: [X: %s, Y: %s, Z: %s]"),
        ;

        private final Translation translation;

        Multi(String k, String e) {
            this.translation = new Translation("ali.property.multi." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }
}
