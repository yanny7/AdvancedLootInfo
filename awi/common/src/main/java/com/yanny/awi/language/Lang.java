package com.yanny.awi.language;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class Lang {
    public enum FeatureConfiguration implements ITooltipKey {
        BLOCK_BLOB("block_blob", "Block Blob:"),
        BLOCK_COLUMN("block_column", "Block Column:"),
        BLOCK_PILE("block_pile", "Block Pile:"),
        BLOCK_STATE("block_state", "Block State:"),
        COLUMN_FEATURE("column_feature", "Column Feature:"),
        COMPOSITE_FEATURE("composite_feature", "Composite Feature:"),
        COUNT("count", "Count:"),
        END_SPIKE("end_spike", "End Spike:"),
        DELTA_FEATURE("delta_feature", "Delta Feature:"),
        DISK("disk", "Disk:"),
        END_GATEWAY("end_gateway", "End Gateway:"),
        FALLEN_TREE("fallen_tree", "Fallen Tree:"),
        FOSSIL_FEATURE("fossil_feature", "Fossil Feature:"),
        GEODE("geode", "Geode:"),
        HUGE_FUNGUS("huge_fungus", "Huge Fungus:"),
        HUGE_MUSHROOM_FEATURE("huge_mushroom_feature", "Huge Mushroom Feature:"),
        LAKE("lake", "Lake:"),
        LARGE_DRIPSTONE("large_dripstone", "Large Dripstone:"),
        LAYERED("layered", "Layered:"),
        MULTIFACE_GROWTH("multiface_growth", "Multiface Growth:"),
        NETHER_FOREST_VEGETATION("nether_forest_vegetation", "Nether Forest Vegetation:"),
        NONE_FEATURE("none_feature", "None Feature:"),
        ORE("ore", "Ore:"),
        PROBABILITY_FEATURE("probability_feature", "Probability Feature:"),
        RANDOM_BOOLEAN_FEATURE("random_boolean_feature", "Random Boolean Feature:"),
        RANDOM_FEATURE("random_feature", "Random Feature:"),
        REPLACEABLE_BLOCK("replaceable_block", "Replaceable Block:"),
        REPLACEABLE_SPHERE("replaceable_sphere", "Replaceable Sphere:"),
        ROOT_SYSTEM("root_system", "Root System:"),
        SCULK_PATCH("sculk_patch", "Sculk Patch:"),
        SIMPLE_BLOCK("simple_block", "Simple Block:"),
        SPELEOTHEM_CLUSTER("speleothem_cluster", "Speleothem Cluster:"),
        SPIKE("spike", "Spike:"),
        SPRING("spring", "Spring:"),
        TEMPLATE("template", "Template:"),
        TREE("tree", "Tree:"),
        TWISTING_VINES("twisting_vines", "Twisting Vines:"),
        UNDERWATER_MAGMA("underwater_magma", "Underwater Magma:"),
        VEGETATION_PATCH("vegetation_patch", "Vegetation Patch:"),
        WEIGHTED_RANDOM("weighted_random", "Weighted Random:"),
        ;

        private final Translation translation;

        FeatureConfiguration(String k, String e) {
            this.translation = new Translation("awi.type.feature_configuration." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum RuleTest implements ITooltipKey {
        ALWAYS_TRUE("always_true", "Always True"),
        BLOCK_MATCH("block_match", "Block Match:"),
        BLOCK_STATE_MATCH("block_state_match", "Block State Match:"),
        RANDOM_BLOCK_MATCH("random_block_match", "Random Block Match:"),
        RANDOM_BLOCK_STATE_MATCH("random_block_state_match", "Random Block State Match:"),
        TAG_MATCH("tag_match", "Tag Match:"),
        ;

        private final Translation translation;

        RuleTest(String k, String e) {
            this.translation = new Translation("awi.type.rule_test." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum StructureProcessor implements ITooltipKey {
        BLACKSTONE_REPLACE("blackstone_replace", "Blackstone Replace"),
        BLOCK_AGE("block_age", "Block Age:"),
        BLOCK_IGNORE("block_ignore", "Block Ignore:"),
        BLOCK_ROT("block_rot", "Block Rot:"),
        CAPPED("capped", "Capped:"),
        GRAVITY("gravity", "Gravity:"),
        JIGSAW_REPLACEMENT("jigsaw_replacement", "Jigsaw Replacement"),
        LAVA_SUBMERGED_BLOCK("lava_submerged_block", "Lava Submerged Block"),
        NOP("nop", "Nop"),
        PROTECTED_BLOCKS("protected_blocks", "Protected Blocks:"),
        RULE("rule", "Rule:"),
        ;

        private final Translation translation;

        StructureProcessor(String k, String e) {
            this.translation = new Translation("awi.type.structure_processor." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        ABOVE_BOTTOM("above_bottom", "Above Bottom: %s"),
        ABOVE_NOISE("above_noise", "Above Noise: %s"),
        ABSOLUTE_Y("absolute_y", "Absolute Y: %s"),
        ALLOWED_VERTICAL_WATER_FOR_TREE("allowed_vertical_water_for_tree", "Allowed Vertical Water For Tree: %s"),
        ALTERNATE_LAYER_CHANCE("alternate_layer_chance", "Alternate Layer Chance: %s"),
        AMOUNT_PER_CHARGE("amount_per_charge", "Amount Per Charge: %s"),
        ATTEMPTS_PER_CHUNK("attempts_per_chunk", "Attempts Per Chunk: %s"),
        ATTEMPTS_PER_CHUNK_DIST("attempts_per_chunk_dist", "Attempts Per Chunk: %s (%s)"),
        BASE_CRACK_SIZE("base_crack_size", "Base Crack Size: %s"),
        BASE_HEIGHT("base_height", "Base Height: %s"),
        BELOW_NOISE("below_noise", "Below Noise: %s"),
        BELOW_TOP("below_top", "Below Top: %s"),
        BLOCK("block", "Block: %s"),
        BLOCK_ENTITY_MODIFIER("block_entity_modifier", "Block Entity Modifier: %s"),
        BRANCH_PER_LOG_CHANCE("branch_per_log_chance", "Branch Per-Log Chance: %s"),
        CAN_BE_PLACED_ON("can_be_placed_on", "Can be Placed On: %s"),
        CAN_GROW_THROUGH("can_grow_through", "Can Grow Through: %s"),
        CAN_PLACE_ON_CEILING("can_place_on_ceiling", "Can Place On Ceiling: %s"),
        CAN_PLACE_ON_FLOOR("can_place_on_floor", "Can Place On Floor: %s"),
        CAN_PLACE_ON_WALL("can_place_on_wall", "Can Place On Wall: %s"),
        CATALYST_CHANCE("catalyst_chance", "Catalyst Chance: %s"),
        CENTER_X("center_x", "Center X: %s"),
        CENTER_Z("center_z", "Center Z: %s"),
        CHANCE("chance", "Chance: %s"),
        CHANCE_OF_DIRECTIONAL_SPREAD("chance_of_directional_spread", "Chance Of Directional Spread: %s"),
        CHANCE_OF_SPELEOTHEM_AT_MAX_DISTANCE_FROM_CENTER("chance_at_max_center_distance", "Chance At Max Center Distance: %s"),
        CHANCE_OF_SPREADING("chance_of_spreading", "Chance Of Spreading: %s"),
        CHANCE_OF_SPREAD_RADIUS_2("chance_of_spread_radius_2", "Chance Of Spread Radius 2: %s"),
        CHANCE_OF_SPREAD_RADIUS_3("chance_of_spread_radius_3", "Chance Of Spread Radius 3: %s"),
        CHANCE_OF_TALLER_GENERATION("chance_of_taller_generation", "Chance Of Taller Generation: %s"),
        CHARGE_COUNT("charge_count", "Charge Count: %s"),
        CORNER_HOLE_CHANCE("corner_hole_chance", "Corner Hole Chance: %s"),
        CRACK_POINT_OFFSET("crack_point_offset", "Crack Point Offset: %s"),
        CRYSTAL_BEAM_TARGET("crystal_beam_target", "Crystal Beam Target: %s"),
        DEFAULT_BLOCK("default_block", "Default Block: %s"),
        DEFAULT_FLUID("default_fluid", "Default Fluid: %s"),
        DEPTH_BELOW_SURFACE("depth_below_surface", "Depth Below Surface: %s"),
        DEVIATION("deviation", "Deviation: %s"),
        DIRECTION("direction", "Direction: %s"),
        DIRECTION_OF_SEARCH("direction_of_search", "Direction Of Search: %s"),
        DISCARD_CHANCE_ON_AIR_EXPOSURE("discard_chance_on_air_exposure", "Discard Chance On Air Exposure: %s"),
        EXACT("exact", "Exact: %s"),
        EXCLUSION_RADIUS_XZ("exclusion_radius_xz", "Exclusion Radius XZ: %s"),
        EXCLUSION_RADIUS_Y("exclusion_radius_y", "Exclusion Radius Y: %s"),
        EXIT("exit", "Exit: %s"),
        EXTRA_BOTTOM_BLOCK_CHANCE("extra_bottom_block_chance", "Extra Bottom Block Chance: %s"),
        EXTRA_EDGE_COLUMN_CHANCE("extra_edge_column_chance", "Extra Edge Column Chance: %s"),
        FEATURE("feature", "Feature: %s"),
        FILLING("filling", "Filling: %s"),
        FLOOR_RANGE_SEARCH("floor_range_search", "Floor Range Search: %s"),
        FLOOR_TO_CEILING_SEARCH_RANGE("floor_to_ceiling_search_range", "Floor-Ceiling Search Range: %s"),
        FLUID("fluid",  "Fluid: %s"),
        FOLIAGE_RADIUS("foliage_radius", "Foliage Radius: %s"),
        FOSSIL_STRUCTURE("fossil_structure", "Fossil Structure: %s"),
        GENERATE_CRACK_CHANCE("generate_crack_chance", "Generate Crack Chance: %s"),
        GENERATION_STEP("generation_step", "Generation Step: %s"),
        GROUND_PROBABILITY("ground_probability", "Ground Probability: %s"),
        GROWTH_ROUNDS("growth_rounds", "Growth Rounds: %s"),
        HALF_HEIGHT("half_height", "Half Height: %s"),
        HANGING_LEAVES_CHANCE("hanging_leaves_chance", "Hanging Leaves Chance: %s"),
        HANGING_LEAVES_EXTENSION_CHANCE("hanging_leaves_extension_chance", "Hanging Leaves Extension Chance: %s"),
        HANGING_ROOT_PLACEMENT_ATTEMPTS("hanging_root_placement_attempts", "Hanging Root Placement Attempts: %s"),
        HANGING_ROOT_RADIUS("hanging_root_radius", "Hanging Root Radius: %s"),
        HANGING_ROOT_VERTICAL_SPAN("hanging_root_vertical_span", "Hanging Root Vertical Span: %s"),
        HEIGHT("height", "Height: %s"),
        HEIGHT_DIST("height_dist", "Height: %s (%s)"),
        HEIGHT_DIST_BAND("height_dist_band", "Height: %s (%s), most likely %s"),
        HEIGHTMAP("heightmap", "Heightmap: %s"),
        HEIGHT_DEVIATION("height_deviation", "Height Deviation: %s"),
        HEIGHT_RAND_A("height_rand_a", "Height Rand A: %s"),
        HEIGHT_RAND_B("height_rand_b", "Height Rand B: %s"),
        HIGH_CHANCE("high_chance", "High Chance: %s"),
        HOLE_COUNT("hole_count", "Hole Count: %s"),
        IGNORE_VINES("ignore_vines", "Ignore Vines: %s"),
        INNER("inner", "Inner: %s"),
        INNER_LAYER("inner_layer", "Inner Layer: %s"),
        INTEGRITY("integrity", "Integrity: %s"),
        INVALID_BLOCKS_THRESHOLD("invalid_blocks_threshold", "Invalid Blocks Threshold: %s"),
        IS_CRYSTAL_INVULNERABLE("is_crystal_invulnerable", "Is Crystal Invulnerable: %s"),
        IS_GUARDED("is_guarded", "Is Guarded: %s"),
        LAYER_AT_Y("layer_at_y", "Layer At Y: %s"),
        LEAF_PLACEMENT_ATTEMPTS("leaf_placement_attempts", "Leaf Placement Attempts: %s"),
        LEAVES_PROBABILITY("leaves_probability", "Leaves Probability: %s"),
        LEVEL_TEST_DISTANCE("level_test_distance", "Level Test Distance: %s"),
        LIMIT("limit", "Limit: %s"),
        LOWER_SIZE("lower_size", "Lower Size: %s"),
        MAX_DISTANCE_FROM_CENTER_AFFECTING_HEIGHT_BIAS("max_center_distance_for_height_bias", "Max Center Distance For Height Bias: %s"),
        MAX_DISTANCE_FROM_EDGE_AFFECTING_CHANCE_OF_SPELEOTHEM("max_edge_distance_for_chance", "Max Edge Distance For Chance: %s"),
        MAX_EMPTY_CORNERS_ALLOWED("max_empty_corners_allowed", "Max Empty Corners Allowed: %s"),
        MAX_GEN_OFFSET("max_gen_offset", "Max Gen Offset: %s"),
        MAX_HEIGHT("max_height", "Max Height: %s"),
        MAX_LEVEL_DEVIATION("max_level_deviation", "Max Level Deviation: %s"),
        MAX_ROOT_LENGTH("max_root_length", "Max Root Length: %s"),
        MAX_ROOT_WIDTH("max_root_width", "Max Root Width: %s"),
        MAX_STALAGMITE_STALACTITE_HEIGHT_DIFF("max_stalagmite_stalactite_height_diff", "Max Stalagmite/Stalactite Diff: %s"),
        MAX_STEPS("max_steps", "Max Steps: %s"),
        MAX_WATER_DEPTH("max_water_depth", "Max Water Depth: %s"),
        MEAN("mean", "Mean: %s"),
        MIDDLE_LAYER("middle_layer", "Middle Layer: %s"),
        MIDDLE_SIZE("middle_size", "Middle Size: %s"),
        MIN_BLUNTNESS_FOR_WIND("min_bluntness_for_wind", "Min Bluntness For Wind: %s"),
        MIN_CLIPPED_HEIGHT("min_clipped_height", "Min Clipped Height: %s"),
        MIN_GEN_OFFSET("min_gen_offset", "Min Gen Offset: %s"),
        MIN_HEIGHT_FOR_LEAVES("min_height_for_leaves", "Min Height For Leaves: %s"),
        MIN_RADIUS_FOR_WIND("min_radius_for_wind", "Min Radius For Wind: %s"),
        MOSSINESS("mossiness", "Mossiness: %s"),
        MUDDY_ROOTS_IN("muddy_roots_in", "Muddy Roots In: %s"),
        NOISE_FACTOR("noise_factor", "Noise Factor: %s"),
        NOISE_LEVEL("noise_level", "Noise Level: %s"),
        NOISE_MULTIPLIER("noise_multiplier", "Noise Multiplier: %s"),
        NOISE_OFFSET("noise_offset", "Noise Offset: %s"),
        NOISE_TO_COUNT_RATIO("noise_to_count_ratio", "Noise To Count Ratio: %s"),
        OFFSET("offset", "Offset: %s"),
        OUTER_LAYER("outer_layer", "Outer Layer: %s"),
        OVERLAY_STRUCTURE("overlay_structure", "Overlay Structure: %s"),
        PLACEMENT("placement", "Placement: %s"),
        PLACEMENT_CHANCE("placement_chance", "Placement Chance: %s"),
        PLACEMENT_RADIUS_AROUND_FLOOR("placement_radius_around_floor", "Placement Radius Around Floor: %s"),
        PLACE_BLOCK("place_block", "Place Block: %s"),
        PLANTED("planted", "Planted: %s"),
        PLATEAU("plateau", "Plateau: %s"),
        POSITION("position", "Position: %s"),
        POSITION_PREDICATE("position_predicate", "Position Predicate: %s"),
        POTENTIAL_PLACEMENT_CHANCE("potential_placement_chance", "Potential Placement Chance: %s"),
        PRIORITIZE_TIP("prioritize_tip", "Prioritize Tip: %s"),
        PROBABILITY("probability", "Probability: %s"),
        PROBABILITY_PER_POSITION("probability_per_position", "Probability Per Position: %s"),
        PROPERTY_NAME("property_name", "Property Name: %s"),
        RADIUS("radius", "Radius: %s"),
        RADIUS_TO_HEIGHT_RATIO("radius_to_height_ratio", "Radius To Height Ratio: %s"),
        RANDOM_SKEW_CHANCE("random_skew_chance", "Random Skew Chance: %s"),
        RANGE("range", "Range: %s"),
        REQUIRED_EMPTY_BLOCKS("requred_empty_blocks", "Required Empty Blocks: %s"),
        REQUIRED_VERTICAL_SPACE_FOR_TREE("required_vertical_space_for_tree", "Required Vertical Space For Tree: %s"),
        REQUIRES_BLOCK_BELOW("requires_block_below", "Requires Block Below: %s"),
        REQUIRE_ALTERNATE_LAYER("require_alternate_layer", "Require Alternate Layer: %s"),
        ROCK_COUNT("rock_count", "Rock Count: %s"),
        ROOT_COLUMN_MAX_HEIGHT("root_column_max_height", "Root Column Max Height: %s"),
        ROOT_PLACEMENT_ATTEMPTS("root_placement_attempts", "Root Placement Attempts: %s"),
        ROOT_RADIUS("root_radius", "Root Radius: %s"),
        ROTATION("rotation",  "Rotation: %s"),
        ROTTABLE_BLOCK("rottable_block", "Rottable Block: %s"),
        SCHEDULE_TICK("schedule_tick", "Schedule Tick: %s"),
        SEARCH_RANGE("search_range", "Search Range: %s"),
        SEA_LEVEL("sea_level", "Sea Level: %s"),
        SIZE("size", "Size: %s"),
        SPREAD_ATTEMPTS("spread_attempts", "Spread Attempts: %s"),
        SPREAD_HEIGHT("spread_height", "Spread Height: %s"),
        SPREAD_ROUNDS("spread_rounds", "Spread Rounds: %s"),
        SPREAD_WIDTH("spread_width", "Spread Width: %s"),
        SURFACE("surface", "Surface: %s"),
        TAG("tag", "Tag: %s"),
        TEMPLATE("template", "Template: %s"),
        THRESHOLD("threshold", "Threshold: %s"),
        TO_IGNORE("to_ignore", "To Ignore: %s"),
        TOTAL_WEIGHT("total_weight", "Total Weight: %s"),
        TRIES("tries", "Tries: %s"),
        TRUNK_PROBABILITY("trunk_probability", "Trunk Probability: %s"),
        UPPER_LIMIT("upper_limit", "Upper Limit: %s"),
        UPPER_SIZE("upper_size", "Upper Size: %s"),
        VALID_BLOCK("valid_block", "Valid Block: %s"),
        VALUE("value", "Value: %s"),
        VEGETATION_CHANCE("vegetation_chance", "Vegetation Chance: %s"),
        VERTICAL_RANGE("vertical_range", "Vertical Range: %s"),
        WIDE_BOTTOM_LAYER_HOLE_CHANCE("wide_bottom_layer_hole_chance", "Wide Bottom Layer Hole Chance: %s"),
        WEIGHT("weight", "Weight: %s"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            this.translation = new Translation("awi.property.value." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        ABOVE_ROOT_PLACEMENT("above_root_placement", "Above Root Placement:"),
        ABOVE_ROOT_PROVIDER("above_root_provider", "Above Root Provider:"),
        ABSOLUTE_Y(Value.ABSOLUTE_Y, "absolute_y", "Absolute Y:"),
        ALLOWED_PLACEMENT("allowed_placement", "Allowed Placement:"),
        ALLOWED_SEARCH_CONDITION("allowed_search_condition", "Allowed Search Condition:"),
        ALLOWED_TREE_POSITION("allowed_tree_position", "Allowed Tree Position:"),
        ALTERNATE_INNER_LAYER_PROVIDER("alternate_inner_layer_provider", "Alternate Inner Layer Provider:"),
        BARRIER("barrier", "Barrier:"),
        BASE_BLOCK("base_block", "Base Block:"),
        BELOW_TRUNK_PROVIDER("below_trunk_provider", "Below Trunk Provider:"),
        BEND_LENGTH("bend_length", "Bend Length:"),
        BIOMES("biomes", "Biomes:"),
        BLOCK_PROVIDER("block_provider", "Block Provider:"),
        BLOCKS(Value.BLOCK, "blocks", "Blocks:"),
        BRANCH_COUNT("branch_count", "Branch Count:"),
        BRANCH_END_OFFSET_FROM_TOP("branch_end_offset_from_top", "Branch End Offset From Top:"),
        BRANCH_HORIZONTAL_LENGTH("branch_horizontal_length", "Branch Horizontal Length:"),
        BRANCH_START_OFFSET_FROM_TOP("branch_start_offset_from_top", "Branch Start Offset From Top:"),
        CAN_BE_PLACED_ON(Value.CAN_BE_PLACED_ON, "can_be_placed_on", "Can Be Placed On:"),
        CAN_GROW_THROUGH(Value.CAN_GROW_THROUGH, "can_grow_through", "Can Grow Through:"),
        CAN_PLACE_FEATURE("can_place_feature", "Can Place Feature:"),
        CAN_PLACE_ON("can_place_on", "Can Place On:"),
        CAP_PROVIDER("cap_provider", "Cap Provider:"),
        CAN_REPLACE("can_replace",  "Can Replace:"),
        CAN_REPLACE_WITH_AIR_OR_FLUID("can_replace_with_air_or_fluid", "Can Replace With Air Or Fluid:"),
        CAN_REPLACE_WITH_BARRIER("can_replace_with_barrier", "Can Replace With Barrier:"),
        CANNOT_REPLACE("cannot_replace", "Cannot Replace:"),
        COLUMN_RADIUS("column_radius", "Column Radius:"),
        CONFIG("config", "Config:"),
        CONFIGURED_FEATURE("configured_feature", "Configured Feature:"),
        CONTENTS("contents", "Contents:"),
        COUNT("count", "Count:"),
        CROWN_HEIGHT("crown_height", "Crown Height:"),
        DECORATORS("decorators", "Decorators:"),
        DECOR_STATE("decor_state", "Decor State:"),
        DEFAULT_FEATURE("default_feature", "Default Feature:"),
        DEFAULT_STATE("default_state", "Default State:"),
        DELEGATE("delegate", "Delegate:"),
        DENSITY("density", "Density:"),
        DEPTH("depth", "Depth:"),
        DIRECTIONS(Value.DIRECTION, "directions", "Directions:"),
        DISTRIBUTION("distribution", "Distribution:"),
        DISTRIBUTION_POINTS("distribution_points", "Distribution Points:"),
        EXTRA_BRANCH_LENGTH("extra_branch_length", "Extra Branch Length:"),
        EXTRA_BRANCH_STEPS("extra_branch_steps", "Extra Branch Steps:"),
        EXTRA_RARE_GROWTHS("extra_rare_growths", "Extra Rare Growths:"),
        FALLBACK("fallback", "Fallback:"),
        FEATURE("feature", "Feature:"),
        FEATURES("features", "Features:"),
        FEATURE_FALSE("feature_false", "Feature False:"),
        FEATURE_TRUE("feature_true", "Feature True:"),
        FILLING_PROVIDER("filling_provider", "Filling Provider:"),
        FLUID("fluid", "Fluid:"),
        FLUIDS(Value.FLUID, "fluids", "Fluids:"),
        FOLIAGE_HEIGHT("foliage_height", "Foliage Height:"),
        FOLIAGE_PLACER("foliage_placer", "Foliage Placer:"),
        FOLIAGE_PROVIDER("foliage_provider", "Foliage Provider:"),
        FOSSIL_PROCESSORS("fossil_processors", "Fossil Processors:"),
        FOSSIL_STRUCTURES(Value.FOSSIL_STRUCTURE, "fossil_structures", "Fossil Structures:"),
        GEODE_BLOCK_SETTINGS("geode_block_settings", "Geode Block Settings:"),
        GEODE_CRACK_SETTINGS("geode_crack_settings", "Geode Crack Settings:"),
        GEODE_LAYER_SETTINGS("geode_layer_settings", "Geode Layer Settings:"),
        GROUND_STATE("ground_state", "Ground State:"),
        HANGING_ROOT_STATE_PROVIDER("hanging_root_state_provider", "Hanging Root State Provider:"),
        HAT_STATE("hat_state", "Hat State:"),
        HEIGHT(Value.HEIGHT, "height", "Height:"),
        HEIGHT_SCALE("height_scale", "Height Scale:"),
        HIGH_STATES("high_states", "High States:"),
        IF_TRUE("if_true", "If True:"),
        INNER_LAYER_PROVIDER("inner_layer_provider", "Inner Layer Provider:"),
        INNER_PLACEMENTS("inner_placements", "Inner Placements:"),
        INPUT_PREDICATE("input_predicate", "Input Predicate:"),
        INVALID_BLOCKS("invalid_blocks", "Invalid Blocks:"),
        ITEMS("items", "Items:"),
        LAYERS("layers", "Layers:"),
        LAYERS_AT_Y(Value.LAYER_AT_Y, "layers_at_y", "Layers At Y:"),
        LIMIT(Value.LIMIT, "limit", "Limit:"),
        LOCATION_PREDICATE("location_predicate", "Location Predicate:"),
        LOG_DECORATORS("log_decorators", "Log Decorators:"),
        LOG_LENGTH("log_length", "Log Length:"),
        LOW_STATES("low_states", "Low States:"),
        MANGROVE_ROOT_PLACEMENT("mangrove_root_placement", "Mangrove Root Placement:"),
        MAX("max", "Max:"),
        MIDDLE_LAYER_PROVIDER("middle_layer_provider", "Middle Layer Provider:"),
        MIN("min", "Min:"),
        MINIMUM_SIZE("minimum_size", "Minimum Size:"),
        MUDDY_ROOTS_IN(Value.MUDDY_ROOTS_IN, "muddy_roots_in", "Muddy Root In:"),
        MUDDY_ROOTS_PROVIDER("muddy_roots_provider", "Muddy Root Provider:"),
        OFFSET("offset", "Offset:"),
        OUTER_LAYER_PROVIDER("outer_layer_provider", "Outer Layer Provider:"),
        OUTER_WALL_DISTANCE("outer_wall_distance", "Outer Wall Distance:"),
        OUTPUT_STATE("output_state", "Output State:"),
        OVERLAY_PROCESSORS("overlay_processors", "Overlay Processors:"),
        OVERLAY_STRUCTURES(Value.OVERLAY_STRUCTURE, "overlay_structures", "Overlay Structures:"),
        PLACEMENT("placement", "Placement:"),
        POINTED_BLOCK("pointed_block", "Pointed Block:"),
        POINT_OFFSET("point_offset", "Point Offset:"),
        POSITIONS(Value.POSITION, "positions", "Positions:"),
        PREDICATE("predicate", "Predicate:"),
        PREDICATES("predicates", "Predicates:"),
        PROPERTIES("properties", "Properties:"),
        PROVIDER("provider", "Provider:"),
        RADIUS(Value.RADIUS, "radius", "Radius:"),
        REACH("reach", "Reach:"),
        REPLACEABLE("replaceable", "Replaceable:"),
        REPLACEABLE_BLOCKS("replaceable_blocks", "Replaceable Blocks:"),
        REPLACE_STATE("replace_state", "Replace State:"),
        RIM("rim", "Rim:"),
        RIM_SIZE("rim_size", "Rim Size:"),
        ROOT_PLACER("root_placer", "Root Placer:"),
        ROOT_PROVIDER("root_provider", "Root Provider:"),
        ROOT_REPLACEABLE("root_replaceable", "Root Replaceable:"),
        ROOT_STATE_PROVIDER("root_state_provider", "Root State Provider:"),
        ROTATIONS(Value.ROTATION, "rotations",  "Rotations:"),
        ROTTABLE_BLOCKS(Value.ROTTABLE_BLOCK, "rottable_blocks", "Rottable Blocks:"),
        RULES("rules", "Rules:"),
        SECOND_BRANCH_START_OFFSET_FROM_TOP("second_branch_start_offset_from_top", "Second Branch Start Offset From Top:"),
        SIZE(Value.SIZE, "size", "Size:"),
        SOURCE("source", "Source:"),
        SPELEOTHEM_BLOCK_LAYER_THICKNESS("speleothem_layer_thickness", "Speleothem Layer Thickness:"),
        SPIKES("spikes", "Spikes:"),
        STALACTITE_BLUNTNESS("stalactite_bluntness", "Stalactite Bluntness:"),
        STALAGMITE_BLUNTNESS("stalagmite_bluntness", "Stalagmite Bluntness:"),
        STATE("state", "State:"),
        STATES("states", "States:"),
        STATE_PROVIDER("state_provider", "State Provider:"),
        STEM_PROVIDER("stem_provider", "Stem Provider:"),
        STEM_STATE("stem_state", "Stem State:"),
        STUMP_DECORATORS("stump_decorators", "Stump Decorators:"),
        TARGET("target", "Target:"),
        TARGET_CONDITION("target_condition", "Target Condition:"),
        TARGET_STATE("target_state", "Target State:"),
        TARGET_STATES("target_states", "Target States:"),
        TEMPLATES("templates", "Templates:"),
        THEN("then", "Then:"),
        TO_IGNORE(Value.TO_IGNORE, "to_ignore", "To Ignore:"),
        TO_PLACE("to_place", "To Place:"),
        TREE_FEATURE("tree_feature", "Tree Feature:"),
        TRUNK_HEIGHT("trunk_height", "Trunk Height:"),
        TRUNK_OFFSET_Y("trunk_offset_y", "Trunk Offset Y:"),
        TRUNK_PLACER("trunk_placer", "Trunk Placer:"),
        TRUNK_PROVIDER("trunk_provider", "Trunk Provider:"),
        VALID_BASE_STATE("valid_base_state", "Valid Base State:"),
        VALID_BLOCKS(Value.VALID_BLOCK, "valid_blocks", "Valid Blocks:"),
        VALUE("value", "Value:"),
        VALUES("values",  "Values:"),
        VEGETATION_FEATURE("vegetation_feature", "Vegetation Feature:"),
        WEIGHTED_LIST("weighted_list", "Weighted List:"),
        WETNESS("wetness", "Wetness:"),
        WIND_SPEED("wind_speed", "Wind Speed:"),
        XZ_RADIUS("xz_radius", "XZ Radius:"),
        XZ_SPREAD("xz_spread", "XZ Spread:"),
        Y_SPREAD("y_spread", "Y Spread:"),
        ;

        private final Translation translation;

        Branch(ITooltipKey s, String k, String e) {
            this.translation = new Translation(s.singular(), "awi.property.branch." + k, s.englishSingular(), e);
        }

        Branch(String k, String e) {
            this.translation = new Translation("awi.property.branch." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Placement implements ITooltipKey {
        ON_CEILING("on_ceiling", "On Ceiling"),
        ON_LAND("on_land", "On Land"),
        UNDERWATER("underwater", "Underwater"),
        ;

        private final Translation translation;

        Placement(String k, String e) {
            this.translation = new Translation("awi.property.placement." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum FeatureSize implements ITooltipKey {
        TWO_LAYERS("two_layers", "Two Layers:"),
        THREE_LAYERS("three_layers", "Three Layers:"),
        ;

        private final Translation translation;

        FeatureSize(String k, String e) {
            this.translation = new Translation("awi.property.feature_size." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum RootPlacer implements ITooltipKey {
        MANGROVE_ROOT("mangrove_root", "Mangrove Root:"),
        ;

        private final Translation translation;

        RootPlacer(String k, String e) {
            this.translation = new Translation("awi.property.feature_size." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum FoliagePlacer implements ITooltipKey {
        ACACIA("acacia", "Acacia:"),
        BLOB("blob", "Blob:"),
        BUSH("bush", "Bush:"),
        CHERRY("cherry", "Cherry:"),
        DARK_OAK("dark_oak", "Dark Oak:"),
        FANCY("fancy", "Fancy:"),
        MEGA_JUNGLE("mega_jungle", "Mega Jungle:"),
        MEGA_PINE("mega_pine", "Mega Pine:"),
        PINE("pine", "Pine:"),
        RANDOM_SPREAD("random_spread", "Random Spread:"),
        SPRUCE("spruce", "Spruce:"),
        ;

        private final Translation translation;

        FoliagePlacer(String k, String e) {
            this.translation = new Translation("awi.property.foliage_placer." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum TreeDecorator implements ITooltipKey {
        TRUNK_VINE("trunk_vine", "Trunk Vine"),
        LEAVE_VINE("leave_vine", "Leave Vine"),
        COCOA("cocoa", "Cocoa:"),
        BEEHIVE("beehive", "Beehive:"),
        ALTER_GROUND("alter_ground", "Alter Ground:"),
        ATTACHED_TO_LEAVES("attached_to_leaves", "Attached To Leaves:"),
        PALE_MOSS("pale_moss", "Pale Moss:"),
        CREAKING_HEART("creaking_heart", "Creaking Heart:"),
        PLACE_ON_GROUND("place_on_ground", "Place On Ground:"),
        ATTACHED_TO_LOGS("attached_to_logs", "Attached To Logs:"),
        ;

        private final Translation translation;

        TreeDecorator(String k, String e) {
            this.translation = new Translation("awi.property.tree_decorator." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum BlockStateProvider implements ITooltipKey {
        DUAL_NOISE_PROVIDER("dual_noise_provider", "Dual Noise Provider:"),
        NOISE_PROVIDER("noise_provider", "Noise Provider:"),
        NOISE_THRESHOLD("noise_threshold", "Noise Threshold:"),
        RANDOMIZED_INT_STATE("randomized_int", "Randomized Int State:"),
        ROTATED_BLOCK("rotated_block", "Rotated Block:"),
        RULE_BASED("rule_based",  "Rule Based:"),
        SIMPLE("simple", "Simple:"),
        WEIGHTED("weighted", "Weighted:"),
        ;

        private final Translation translation;

        BlockStateProvider(String k, String e) {
            this.translation = new Translation("awi.property.block_state_provider." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum TrunkPlacer implements ITooltipKey {
        BENDING_TRUNK("bending_trunk", "Bending Trunk:"),
        CHERRY("cherry", "Cherry:"),
        DARK_OAK("dark_oak", "Dark Oak:"),
        FANCY_TRUNK("fancy_trunk", "Fancy Trunk:"),
        FORKING_TRUNK("forking_trunk", "Forking Trunk:"),
        GIANT_TRUNK("giant_trunk", "Giant Trunk:"),
        MEGA_JUNGLE("mega_jungle", "Mega Jungle:"),
        STRAIGHT_TRUNK("straight_trunk", "Straight Trunk:"),
        UPWARD_BRANCHING_TRUNK("upward_branching_trunk", "Upward Branching Trunk:"),
        ;

        private final Translation translation;

        TrunkPlacer(String k, String e) {
            this.translation = new Translation("awi.property.trunk_placer." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum PlacementModifier implements ITooltipKey {
        BLOCK_PREDICATE_FILTER("block_predicate_filter", "Block Predicate Filter:"),
        COUNT_ON_EVERY_LAYER("count_on_every_layer", "Count On Every Layer:"),
        COUNT_PLACEMENT("count_placement", "Count Placement:"),
        ENVIRONMENT_SCAN_PLACEMENT("environment_scan_placement", "Environment Scan Placement:"),
        FIXED_PLACEMENT("fixed_placement", "Fixed Placement:"),
        HEIGHTMAP_PLACEMENT("heightmap_placement", "Heightmap Placement:"),
        HEIGHT_RANGE_PLACEMENT("height_range_placement", "Height Range Placement:"),
        NOISE_BASED_COUNT_PLACEMENT("noise_based_count_placement", "Noise Based Count Placement:"),
        NOISE_THRESHOLD_COUNT_PLACEMENT("noise_threshold_count_placement", "Noise Threshold Count Placement:"),
        RANDOM_OFFSET("random_offset", "Random Offset:"),
        RARITY_FILTER("rarity_filter", "Rarity Filter:"),
        SURFACE_RELATIVE_THRESHOLD_FILTER("surface_relative_threshold_filter", "Surface Relative Threshold Filter:"),
        SURFACE_WATER_DEPTH_FILTER("surface_water_depth_filter", "Surface Water Depth Filter:"),
        ;

        private final Translation translation;

        PlacementModifier(String k, String e) {
            this.translation = new Translation("awi.property.placement_modifier." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum IntProvider implements ITooltipKey {
        BIASED_TO_BOTTOM("biased_to_bottom", "Biased To Bottom:"),
        CLAMPED("clamped", "Clamped:"),
        CLAMPED_NORMAL("clamped_normal", "Clamped Normal:"),
        CONSTANT("constant", "Constant:"),
        TRAPEZOID("trapezoid", "Trapezoid:"),
        UNIFORM("uniform", "Uniform:"),
        WEIGHTED_LIST("weighted_list", "Weighted List:"),
        ;

        private final Translation translation;

        IntProvider(String k, String e) {
            this.translation = new Translation("awi.property.int_provider." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum FloatProvider implements ITooltipKey {
        CLAMPED_NORMAL("clamped_normal", "Clamped Normal:"),
        CONSTANT("constant", "Constant:"),
        TRAPEZOID("trapezoid", "Trapezoid:"),
        UNIFORM("uniform", "Uniform:"),
        ;

        private final Translation translation;

        FloatProvider(String k, String e) {
            this.translation = new Translation("awi.property.float_provider." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum HeightProvider implements ITooltipKey {
        BIASED_TO_BOTTOM("biased_to_bottom", "Biased To Bottom:"),
        CONSTANT("constant", "Constant:"),
        TRAPEZOID("trapezoid", "Trapezoid:"),
        UNIFORM("uniform", "Uniform:"),
        VERY_BIASED_TO_BOTTOM("very_biased_to_bottom", "Very Biased To Bottom:"),
        WEIGHTED_LIST("weighted_list", "Weighted List:"),
        ;

        private final Translation translation;

        HeightProvider(String k, String e) {
            this.translation = new Translation("awi.property.height_provider." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum BlockPredicate implements ITooltipKey {
        ALL_OF("all_of", "All Of:"),
        ANY_OF("any_of", "Any Of:"),
        HAS_STURDY_FACE("has_sturdy_face", "Has Sturdy Face:"),
        INSIDE_WORLD_BOUNDS("inside_world_bounds", "Inside World Bounds:"),
        MATCHING_BIOMES("matching_biomes",  "Matching Biomes:"),
        MATCHING_BLOCKS("matching_blocks", "Matching Blocks:"),
        MATCHING_BLOCK_TAG("matching_block_tag", "Matching Block Tag:"),
        MATCHING_FLUIDS("matching_fluids", "Matching Fluids:"),
        NOT("not", "Not:"),
        REPLACEABLE("replaceable", "Replaceable:"),
        SOLID("solid", "Solid:"),
        TRUE_BLOCK("true_block", "True Block"),
        UNOBSTRUCTED("unobstructed", "Unobstructed:"),
        WOULD_SURVIVE("would_survive", "Would Survive:"),
        ;

        private final Translation translation;

        BlockPredicate(String k, String e) {
            this.translation = new Translation("awi.property.block_predicate." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Kind implements ITooltipKey {
        BIASED_TO_BOTTOM("biased_to_bottom", "Biased To Bottom"),
        CLAMPED("clamped", "Clamped"),
        CLAMPED_NORMAL("clamped_normal", "Clamped Normal"),
        CONSTANT("constant", "Constant"),
        TRAPEZOID("trapezoid", "Trapezoid"),
        UNIFORM("uniform", "Uniform"),
        UNKNOWN("unknown", "Unknown"),
        VERY_BIASED_TO_BOTTOM("very_biased_to_bottom", "Very Biased To Bottom"),
        WEIGHTED("weighted", "Weighted"),
        ;

        private final Translation translation;

        Kind(String k, String e) {
            this.translation = new Translation("awi.property.kind." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum GenerationStep implements ITooltipKey {
        BASE_TERRAIN("base_terrain", "Base Terrain"),
        FLUID_SPRINGS("fluid_springs", "Fluid Springs"),
        LAKES("lakes", "Lakes"),
        LOCAL_MODIFICATIONS("local_modifications", "Local Modifications"),
        RAW_GENERATION("raw_generation", "Raw Generation"),
        STRONGHOLDS("strongholds", "Strongholds"),
        SURFACE_STRUCTURES("surface_structures", "Surface Structures"),
        TOP_LAYER_MODIFICATION("top_layer_modification", "Top Layer Modifications"),
        UNDERGROUND_DECORATION("underground_decoration", "Underground Decoration"),
        UNDERGROUND_ORES("underground_ores", "Underground Ores"),
        UNDERGROUND_STRUCTURES("underground_structures", "Underground Structures"),
        VEGETAL_DECORATION("vegetal_decoration", "Vegetation Decoration"),
        ;

        private final Translation translation;

        GenerationStep(String k, String e) {
            this.translation = new Translation("awi.enum.decoration_step." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }
}
