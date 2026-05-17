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
        DISCARD_ITEM("discard_item", "Discard Item:"),
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
        CLEAR_ALL_EFFECTS("clear_all_effects", "Clear All Effects"),
        PLAY_SOUND("play_sound", "Play Sound:"),
        REMOVE_EFFECTS("remove_effects", "Remove Effects:"),
        TELEPORT_RANDOMLY("teleport_randomly", "Teleport Randomly:"),
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

    public enum SlotSource implements ITooltipKey {
        CONTENTS("contents", "Contents:"),
        EMPTY("empty", "Empty Slot"),
        FILTERED("filtered", "Filtered:"),
        GROUP("group", "Group:"),
        LIMIT_SLOTS("limit_slots", "Limit Slots:"),
        SLOT_RANGE("slot_range", "Slot Range:"),
        ;

        private final Translation translation;

        SlotSource(String k, String e) {
            this.translation = new Translation("ali.type.slot_source." + k, e);
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
        ANIMATION("animation", "Animation: %s"),
        APPEND("append", "Append: %s"),
        ASSET_ID("asset_id", "Asset ID: %s"),
        ATTRIBUTE("attribute", "Attribute: %s"),
        AUTHOR("author", "Author: %s"),
        BACK("back", "Back: %s"),
        BACKWARD("backward", "Backward: %s"),
        BANNER_PATTERN("banner_pattern", "Banner Pattern: %s"),
        BASE("base", "Base: %s"),
        BIOME("biome", "Biome: %s"),
        BLOCK("block", "Block: %s"),
        BLOCKS_ON_FIRE("blocks_on_fire", "Blocks On Fire: %s"),
        BLOCK_DELAY_SECONDS("block_delay_seconds", "Block Delay Seconds: %s"),
        BLOCK_ENTITY_TYPE("block_entity_type", "Block Entity Type: %s"),
        BLOCK_SOUND("block_sound", "Block Sound: %s"),
        BONUS_MULTIPLIER("bonus_multiplier", "Bonus Multiplier: %s"),
        BYPASSED_BY("bypassed_by", "Bypassed By: %s"),
        CAMERA_OVERLAY("camera_overlay", "Camera Overlay: %s"),
        CAN_ALWAYS_EAT("can_always_eat", "Can Always Eat: %s"),
        CAN_DESTROY_BLOCKS_IN_CREATIVE("can_destroy_blocks_in_creative", "Can Destroy Blocks In Creative: %s"),
        CAN_SEE_SKY("can_see_sky", "Can See Sky: %s"),
        CAN_SPRINT("can_sprint", "Can Sprint: %s"),
        CHANCE("chance", "Chance: %s"),
        COLOR("color", "Color: %s"),
        COLORS("colors", "Colors: %s"),
        COMPONENT("component", "Component: %s"),
        CONSTANT("constant", "Constant: %s"),
        CONSUME_SECONDS("consume_seconds", "Consume Seconds: %s"),
        CONTACT_COOLDOWN_TICKS("contact_cooldown_ticks", "Contact Cooldown Ticks: %s"),
        CONTAINER("container", "Container: %s"),
        COOLDOWN_GROUP("cooldown_group", "Cooldown Group: %s"),
        CORRECT_FOR_DROPS("correct_for_drops", "Correct For Drops: %s"),
        COUNT("count", "Count: %s"),
        CUSTOM_COLOR("custom_color", "Custom Color: %s"),
        CUSTOM_NAME("custom_name", "Custom Name: %s"),
        DAMAGE("damage", "Damage: %s"),
        DAMAGE_MULTIPLIER("damage_multiplier", "Damage Multiplier: %s"),
        DAMAGE_ON_HURT("damage_on_hurt", "Damage On Hurt: %s"),
        DAMAGE_PER_BLOCK("damage_per_block", "Damage Per Block: %s"),
        DEALS_KNOCKBACK("deals_knockback", "Deals Knockback: %s"),
        DECORATION("decoration", "Decoration: %s"),
        DEFAULT_MINING_SPEED("default_mining_speed", "Default Mining Speed: %s"),
        DELAY_TICKS("delay_ticks", "Delay Ticks: %s"),
        DESTINATION("destination", "Destination: %s"),
        DIAMETER("diameter", "Diameter: %s"),
        DIMENSION("dimension", "Dimension: %s"),
        DISABLE_BLOCKING_FOR_SECONDS("disable_blocking_for_seconds", "Disable Blocking For Seconds: %s"),
        DISABLE_COOLDOWN_SCALE("disable_cooldown_scale", "Disable Cooldown Scale: %s"),
        DISABLE_SOUND("disable_sound", "Disable Sound: %s"),
        DISMOUNTS("dismounts", "Dismounts: %s"),
        DISPENSABLE("dispensable", "Dispensable: %s"),
        DONE("done", "Done: %s"),
        DURABILITY("durability", "Durability: %s"),
        DURATION("duration", "Duration: %s"),
        EFFECT("effect", "Effect: %s"),
        ENCHANTMENT("enchantment", "Enchantment: %s"),
        ENTITY_DATA("entity_data", "Entity Data: %s"),
        ENTITY_TYPE("entity_type", "Entity Type: %s"),
        EQUIPMENT_SLOT("equipment_slot", "Equipment Slot: %s"),
        EQUIP_ON_INTERACT("equip_on_interact", "Equip On Interact: %s"),
        EQUIP_SOUND("equip_sound", "Equip Sound: %s"),
        EXCLUDE("exclude", "Exclude: %s"),
        EXTRA_ROUNDS("extra_rounds", "Extra Rounds: %s"),
        FACTOR("factor", "Factor: %s"),
        FADE_COLORS("fade_colors", "Fade Colors: %s"),
        FALL_DISTANCE("fall_distance", "Fall Distance: %s"),
        FILTERED("filtered", "Filtered: %s"),
        FLAGS("flags", "Flags: %s"),
        FLIGHT_DURATION("flight_duration", "Flight Duration: %s"),
        FLOATS("floats", "Floats: %s"),
        FLUID("fluid", "Fluid: %s"),
        FORMULA("formula", "Formula: %s"),
        FORWARD("forward", "Forward: %s"),
        FORWARD_MOVEMENT("forward_movement", "Forward Movement: %s"),
        FRACTION("fraction", "Fraction: %s"),
        FRONT("front", "Front: %s"),
        GAME_TYPE("game_type", "Game Type: %s"),
        GENERATION("generation", "Generation: %s"),
        HAS_CUSTOM_PARTICLES("has_custom_particles", "Has Custom Particles: %s"),
        HAS_RAID("has_raid", "Has Raid: %s"),
        HAS_TRAIL("has_trail", "Has Trail: %s"),
        HAS_TWINKLE("has_twinkle", "Has Twinkle: %s"),
        HIDE_TOOLTIP("hide_tooltip", "Hide Tooltip: %s"),
        HITBOX_MARGIN("hitbox_margin", "Hitbox Margin: %s"),
        HIT_SOUND("hit_sound", "Hit Sound: %s"),
        HORIZONTAL("horizontal", "Horizontal: %s"),
        HORIZONTAL_BLOCKING_ANGLE("horizontal_blocking_angle", "Horizontal Blocking Angle: %s"),
        HORIZONTAL_SPEED("horizontal_speed", "Horizontal Speed: %s"),
        ID("id", "Id: %s"),
        INCLUDE("include", "Include: %s"),
        INTERACT_VIBRATIONS("interact_vibrations", "Interact Vibrations: %s"),
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
        ITEM_DAMAGE_PER_ATTACK("item_damage_per_attack", "Item Damage Per Attack: %s"),
        ITEM_NAME("item_name", "Item Name: %s"),
        JUMP("jump", "Jump: %s"),
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
        MAX_CREATIVE_RANGE("max_creative_range", "Max Creative Range: %s"),
        MAX_DURATION_TICKS("max_duration_ticks", "Max Duration Ticks: %s"),
        MAX_RANGE("max_range", "Max Range: %s"),
        MERGE_STRATEGY("merge_strategy", "Merge Strategy: %s"),
        MIN("min", "Min: %s"),
        MIN_CREATIVE_RANGE("min_creative_range", "Min Creative Range: %s"),
        MIN_RANGE("min_range", "Min Range: %s"),
        MIN_RELATIVE_SPEED("min_relative_speed", "Min Relative Speed: %s"),
        MIN_SPEED("min_speed", "Min Speed: %s"),
        MIN_TICKS_IN_HIVE("min_ticks_in_hive", "Min Ticks In Hive: %s"),
        MOB_FACTOR("mob_factor", "Mob Factor: %s"),
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
        SECONDS("seconds", "Seconds: %s"),
        SEED("seed", "Seed: %s"),
        SHAPE("shape", "Shape: %s"),
        SHEARED("sheared",  "Sheared: %s"),
        SHOW_ICON("show_icon", "Show Icon: %s"),
        SIGNATURE("signature", "Signature: %s"),
        SIZE("size", "Size: %s"),
        SKIP_KNOWN_STRUCTURES("skip_known_structures", "Skip Known Structures: %s"),
        SLOT("slot", "Slot: %s"),
        SLOT_RANGE("slot_range", "Slot Range: %s"),
        SMOKEY("smokey", "Smokey: %s"),
        SNEAK("sneak", "Sneak: %s"),
        SONG("song", "Song: %s"),
        SOUND("sound", "Sound: %s"),
        SOURCE("source", "Source: %s"),
        SOURCE_PATH("source_path", "Source Path: %s"),
        SPEED("speed", "Speed: %s"),
        SPEED_MULTIPLIER("speed_multiplier", "Speed Multiplier: %s"),
        SPRINT("sprint", "Sprint: %s"),
        STRINGS("strings", "Strings: %s"),
        STRUCTURE("structure", "Structure: %s"),
        SWAPPABLE("swappable", "Swappable: %s"),
        TAG("tag", "Tag: %s"),
        TARGET("target", "Target: %s"),
        TARGET_PATH("target_path", "Target Path: %s"),
        TEAM("team", "Team: %s"),
        THRESHOLD("threshold", "Threshold: %s"),
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
        AFFECTS_MOVEMENT("affects_movement", "Affects Movement:"),
        ALLOWED_ENTITIES("allowed_entities", "Allowed Entities:"),
        ATTRIBUTES(Value.ATTRIBUTE, "attributes", "Attributes:"),
        ATTRIBUTE_MODIFIER("attribute_modifier", "Attribute Modifier:"),
        BANNER_PATTERNS("banner_patterns", "Banner Patterns:"),
        BEES("bees", "Bees:"),
        BIOMES(Value.BIOME, "biomes", "Biomes:"),
        BLOCKS(Value.BLOCK, "blocks", "Blocks:"),
        BLOCK_PREDICATE("block_predicate", "Block Predicate:"),
        BODY("body", "Body:"),
        CHEST("chest", "Chest:"),
        CLAMPED("clamped", "Clamped:"),
        COLORS("colors", "Colors:"),
        COMPONENTS(Value.COMPONENT, "components", "Components:"),
        CONDITION("condition", "Predicate:"),
        CONDITIONS("conditions", "Predicates:"),
        CONTAINS("contains", "Contains:"),
        CONVERT_INTO("convert_into", "Convert Into:"),
        COPY_OPERATIONS("copy_operations", "Copy Operations:"),
        COUNTS("counts", "Counts:"),
        CRITERIONS("criterions", "Criterions:"),
        CUSTOM_EFFECTS("custom_effects", "Custom Effects:"),
        DAMAGE_CONDITION("damage_condition", "Damage Condition:"),
        DAMAGE_REDUCTION("damage_reduction", "Damage Reduction:"),
        DAMAGE_REDUCTIONS("damage_reductions", "Damage Reductions:"),
        DAMAGE_TYPES("damage_types", "Damage Types:"),
        DEATH_EFFECTS("death_effects", "Death Effects:"),
        DECORATIONS("decorations", "Decorations:"),
        DENOMINATOR("denominator", "Denominator:"),
        DIRECT_ENTITY("direct_entity", "Direct Entity:"),
        DISMOUNT_CONDITION("dismount_condition", "Dismount Condition:"),
        DISTANCE_TO_PLAYER("distance_to_player", "Distance to Player:"),
        EFFECT("effect", "Effect:"),
        EFFECTS("effects", "Effects:"),
        ENCHANTED_CHANCE("enchanted_chance", "Enchanted Chance:"),
        ENCHANTMENT(Value.ENCHANTMENT, "enchantment", "Enchantment:"),
        ENCHANTMENTS(Value.ENCHANTMENT, "enchantments", "Enchantments:"),
        ENCHANTMENT_PREDICATE("enchantment_predicate", "Enchantment Predicate:"),
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
        FLAGS("flags", "Flags:"),
        FLOATS("floats", "Floats:"),
        FLUIDS(Value.FLUID, "fluids", "Fluids:"),
        FLUID_PREDICATE("fluid_predicate", "Fluid Predicate:"),
        FRACTION("fraction", "Fraction:"),
        GAME_TYPES(Value.GAME_TYPE, "game_types", "Game Types:"),
        GLOBAL_POS("global_pos", "Global Position:"),
        HEAD("head", "Head:"),
        HIDDEN_COMPONENTS("hidden_components", "Hidden Components:"),
        HIDDEN_EFFECT("hidden_effect", "Hidden Effect:"),
        INCLUDE(Value.INCLUDE, "include", "Include:"),
        INPUT("input", "Input:"),
        ITEM("item", "Item:"),
        ITEMS(Value.ITEM, "items", "Items:"),
        ITEM_DAMAGE("item_damage", "Item Damage:"),
        KNOCKBACK_CONDITION("knockback_condition", "Knockback Condition:"),
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
        ON_CONSUME_EFFECTS("on_consume_effects", "On Consume Effects:"),
        ON_FAIL("on_fail", "On Fail:"),
        ON_PASS("on_pass", "On Pass:"),
        OPERATION("operation", "Operation:"),
        OPTIONS(Value.OPTIONS, "options", "Options:"),
        PAGE("page", "Page:"),
        PAGES("pages", "Pages:"),
        PARTIAL_MATCHERS("partial_matchers", "Partial Matchers:"),
        PASSENGER("passenger", "Passenger:"),
        PATTERNS(Value.PATTERN, "patterns", "Patterns:"),
        POSITION("position", "Position:"),
        POTIONS("potions", "Potions:"),
        PREDICATE("predicate", "Predicate:"),
        PROPERTIES(Value.PROPERTY, "properties", "Properties:"),
        PROPERTY("property", "Property:"),
        RECIPES(Value.RECIPE, "recipes", "Recipes:"),
        RULE("rule", "Rule:"),
        RULES("rules", "Rules:"),
        SCORES("scores", "Scores:"),
        SLOTS("slots", "Slots:"),
        SLOT_SOURCE("slot_source", "Slot Source:"),
        SONGS(Value.SONG, "songs", "Songs:"),
        SOURCE_ENTITY("source_entity", "Source Entity:"),
        STATS("stats", "Stats:"),
        STEPPING_ON_LOCATION("stepping_on_location", "Stepping on Location:"),
        STRINGS("strings", "Strings:"),
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
