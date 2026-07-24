package com.yanny.awi.language;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class Lang {
    public enum FeatureConfiguration implements ITooltipKey {
        BLOCK_COLUMN("block_column", "Block Column:"),
        BLOCK_PILE("block_pile", "Block Pile:"),
        BLOCK_STATE("block_state", "Block State:"),
        COLUMN_FEATURE("column_feature", "Column Feature:"),
        COUNT("count", "Count:"),
        DELTA_FEATURE("delta_feature", "Delta Feature:"),
        DISK("disk", "Disk:"),
        DRIPSTONE_CLUSTER("dripstone_cluster", "Dripstone Cluster:"),
        END_GATEWAY("end_gateway", "End Gateway:"),
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
        POINTED_DRIPSTONE("pointed_dripstone", "Pointed Dripstone:"),
        PROBABILITY_FEATURE("probability_feature", "Probability Feature:"),
        RANDOM_BOOLEAN_FEATURE("random_boolean_feature", "Random Boolean Feature:"),
        RANDOM_FEATURE("random_feature", "Random Feature:"),
        RANDOM_PATCH("random_patch", "Random Patch:"),
        REPLACEABLE_BLOCK("replaceable_block", "Replaceable Block:"),
        REPLACEABLE_SPHERE("replaceable_sphere", "Replaceable Sphere:"),
        ROOT_SYSTEM("root_system", "Root System:"),
        SCULK_PATCH("sculk_patch", "Sculk Patch:"),
        SIMPLE_BLOCK("simple_block", "Simple Block:"),
        SIMPLE_RANDOM_FEATURES("simple_random_features", "Simple Random Features:"),
        SPIKE("spike", "Spike:"),
        SPRING("spring", "Spring:"),
        TREE("tree", "Tree:"),
        TWISTING_VINES("twisting_vines", "Twisting Vines:"),
        UNDERWATER_MAGMA("underwater_magma", "Underwater Magma:"),
        VEGETATION_PATCH("vegetation_patch", "Vegetation Patch:"),
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

    public enum Value implements ITooltipKey {
        ABOVE_BOTTOM("above_bottom", "Above Bottom: %s"),
        ABOVE_NOISE("above_noise", "Above Noise: %s"),
        ABSOLUTE_Y("absolute_y", "Absolute Y: %s"),
        ALLOWED_VERTICAL_WATER_FOR_TREE("allowed_vertical_water_for_tree", "Allowed Vertical Water For Tree: %s"),
        ALTERNATE_LAYER_CHANCE("alternate_layer_chance", "Alternate Layer Chance: %s"),
        AMOUNT_PER_CHARGE("amount_per_charge", "Amount Per Charge: %s"),
        BASE_CRACK_SIZE("base_crack_size", "Base Crack Size: %s"),
        BELOW_NOISE("below_noise", "Below Noise: %s"),
        BELOW_TOP("below_top", "Below Top: %s"),
        BLOCK("block", "Block: %s"),
        CAN_BE_PLACED_ON("can_be_placed_on", "Can be Placed On: %s"),
        CAN_PLACE_ON_CEILING("can_place_on_ceiling", "Can Place On Ceiling: %s"),
        CAN_PLACE_ON_FLOOR("can_place_on_floor", "Can Place On Floor: %s"),
        CAN_PLACE_ON_WALL("can_place_on_wall", "Can Place On Wall: %s"),
        CATALYST_CHANCE("catalyst_chance", "Catalyst Chance: %s"),
        CENTER_X("center_x", "Center X: %s"),
        CENTER_Z("center_z", "Center Z: %s"),
        CHANCE("chance", "Chance: %s"),
        CHANCE_OF_DIRECTIONAL_SPEED("chance_of_directional_speed", "Chance Of Directional Speed: %s"),
        CHANCE_OF_SPREADING("chance_of_spreading", "Chance Of Spreading: %s"),
        CHANCE_OF_SPREAD_RADIUS_2("chance_of_spread_radius_2", "Chance Of Spread Radius 2: %s"),
        CHANCE_OF_SPREAD_RADIUS_3("chance_of_spread_radius_3", "Chance Of Spread Radius 3: %s"),
        CHANCE_OF_TALLER_DRIPSTONE("chance_of_taller_dripstone", "Chance Of Taller Dripstone: %s"),
        CHANCE_RADIUS("chance_radius", "Chance Radius: %s"),
        CHARGE_COUNT("charge_count", "Charge Count: %s"),
        COLUMN_RADIUS("column_radius", "Column Radius: %s"),
        COUNT("count", "Count: %s"),
        CRACK_POINT_OFFSET("crack_point_offset", "Crack Point Offset: %s"),
        CRYSTAL_BEAM_TARGET("crystal_beam_target", "Crystal Beam Target: %s"),
        DEFAULT_BLOCK("default_block", "Default Block: %s"),
        DEFAULT_FLUID("default_fluid", "Default Fluid: %s"),
        DENSITY("density", "Density: %s"),
        DEPTH("depth", "Depth: %s"),
        DEPTH_BELOW_SURFACE("depth_below_surface", "Depth Below Surface: %s"),
        DIRECTION("direction", "Direction: %s"),
        DIRECTION_OF_SEARCH("direction_of_search", "Direction Of Search: %s"),
        DISCARD_CHANCE_ON_AIR_EXPOSURE("discard_chance_on_air_exposure", "Discard Chance On Air Exposure: %s"),
        DISTRIBUTION_POINTS("distribution_points", "Distribution Points: %s"),
        EDGE_CHANCE("edge_chance", "Edge Chance: %s"),
        EXACT("exact", "Exact: %s"),
        EXIT("exit", "Exit: %s"),
        EXTRA_BOTTOM_BLOCK_CHANCE("extra_bottom_block_chance", "Extra Bottom Block Chance: %s"),
        EXTRA_EDGE_COLUMN_CHANCE("extra_edge_column_chance", "Extra Edge Column Chance: %s"),
        EXTRA_RARE_GROWTHS("extra_rare_growths", "Extra Rare Growths: %s"),
        FEATURE("feature", "Feature: %s"),
        FILLING("filling", "Filling: %s"),
        FLOOR_RANGE_SEARCH("floor_range_search", "Floor Range Search: %s"),
        FLUID("fluid",  "Fluid: %s"),
        FOLIAGE_RADIUS("foliage_radius", "Foliage Radius: %s"),
        FORCE_DIRT("force_dirt", "Force Dirt: %s"),
        FOSSIL_STRUCTURE("fossil_structure", "Fossil Structure: %s"),
        GENERATE_CRACK_CHANCE("generate_crack_chance", "Generate Crack Chance: %s"),
        GENERATION_STEP("generation_step", "Generation Step: %s"),
        GROWTH_ROUNDS("growth_rounds", "Growth Rounds: %s"),
        HALF_HEIGHT("half_height", "Half Height: %s"),
        HANGING_ROOT_PLACEMENT_ATTEMPTS("hanging_root_placement_attempts", "Hanging Root Placement Attempts: %s"),
        HANGING_ROOT_RADIUS("hanging_root_radius", "Hanging Root Radius: %s"),
        HANGING_ROOT_VERTICAL_SPAN("hanging_root_vertical_span", "Hanging Root Vertical Span: %s"),
        HEIGHT("height", "Height: %s"),
        HEIGHTMAP("heightmap", "Heightmap: %s"),
        HEIGHT_BIAS_RADIUS("height_bias_radius", "Height Bias Radius: %s"),
        HEIGHT_DEVIATION("height_deviation", "Height Deviation: %s"),
        HEIGHT_SCALE("height_scale", "Height Scale: %s"),
        HOLE_COUNT("hole_count", "Hole Count: %s"),
        IGNORE_VINES("ignore_vines", "Ignore Vines: %s"),
        INNER("inner", "Inner: %s"),
        INNER_LAYER("inner_layer", "Inner Layer: %s"),
        INVALID_BLOCKS_THRESHOLD("invalid_blocks_threshold", "Invalid Blocks Threshold: %s"),
        IS_CRYSTAL_VULNERABLE("is_crystal_vulnerable", "Is Crystal Vulnerable: %s"),
        IS_GUARDED("is_guarded", "Is Guarded: %s"),
        ITEM("item", "Item: %s"),
        LAYER_AT_Y("layer_at_y", "Layer At Y: %s"),
        LAYER_THICKNESS("layer_thickness", "Layer Thickness: %s"),
        MAX_EMPTY_CORNERS_ALLOWED("max_empty_corners_allowed", "Max Empty Corners Allowed: %s"),
        MAX_GEN_OFFSET("max_gen_offset", "Max Gen Offset: %s"),
        MAX_HEIGHT("max_height", "Max Height: %s"),
        MAX_HEIGHT_DIFF("max_height_diff", "Max Height Diff: %s"),
        MAX_STEPS("max_steps", "Max Steps: %s"),
        MAX_WATER_DEPTH("max_water_depth", "Max Water Depth: %s"),
        MIDDLE_LAYER("middle_layer", "Middle Layer: %s"),
        MIN_BLUNTNESS_FOR_WIND("min_bluntness_for_wind", "Min Bluntness For Wind: %s"),
        MIN_GEN_OFFSET("min_gen_offset", "Min Gen Offset: %s"),
        MIN_RADIUS_FOR_WIND("min_radius_for_wind", "Min Radius For Wind: %s"),
        NOISE_FACTOR("noise_factor", "Noise Factor: %s"),
        NOISE_LEVEL("noise_level", "Noise Level: %s"),
        NOISE_MULTIPLIER("noise_multiplier", "Noise Multiplier: %s"),
        NOISE_OFFSET("noise_offset", "Noise Offset: %s"),
        NOISE_TO_COUNT_RATIO("noise_to_count_ratio", "Noise To Count Ratio: %s"),
        OFFSET("offset", "Offset: %s"),
        OUTER_LAYER("outer_layer", "Outer Layer: %s"),
        OUTER_WALL_DISTANCE("outer_wall_distance", "Outer Wall Distance: %s"),
        OVERLAY_STRUCTURE("overlay_structure", "Overlay Structure: %s"),
        PLACEMENT("placement", "Placement: %s"),
        PLACEMENT_RADIUS_AROUND_FLOOR("placement_radius_around_floor", "Placement Radius Around Floor: %s"),
        PLACE_BLOCK("place_block", "Place Block: %s"),
        PLANTED("planted", "Planted: %s"),
        PLATEAU("plateau", "Plateau: %s"),
        POINT_OFFSET("point_offset", "Point Offset: %s"),
        POTENTIAL_PLACEMENT_CHANCE("potential_placement_chance", "Potential Placement Chance: %s"),
        PRIORITIZE_TIP("prioritize_tip", "Prioritize Tip: %s"),
        PROBABILITY("probability", "Probability: %s"),
        PROBABILITY_PER_POSITION("probability_per_position", "Probability Per Position: %s"),
        RADIUS("radius", "Radius: %s"),
        RADIUS_TO_HEIGHT_RATIO("radius_to_height_ratio", "Radius To Height Ratio: %s"),
        RANGE("range", "Range: %s"),
        REACH("reach", "Reach: %s"),
        REPLACEABLE("replaceable", "Replaceable: %s"),
        REQUIRED_VERTICAL_SPACE_FOR_TREE("required_vertical_space_for_tree", "Required Vertical Space For Tree: %s"),
        REQUIRES_BLOCK_BELOW("requires_block_below", "Requires Block Below: %s"),
        REQUIRE_ALTERNATE_LAYER("require_alternate_layer", "Require Alternate Layer: %s"),
        RIM_SIZE("rim_size", "Rim Size: %s"),
        ROCK_COUNT("rock_count", "Rock Count: %s"),
        ROOT_COLUMN_MAX_HEIGHT("root_column_max_height", "Root Column Max Height: %s"),
        ROOT_PLACEMENT_ATTEMPTS("root_placement_attempts", "Root Placement Attempts: %s"),
        ROOT_RADIUS("root_radius", "Root Radius: %s"),
        ROOT_REPLACEABLE("root_replaceable", "Root Replaceable: %s"),
        SEARCH_RANGE("search_range", "Search Range: %s"),
        SEA_LEVEL("sea_level", "Sea Level: %s"),
        SIZE("size", "Size: %s"),
        SPREAD_ATTEMPTS("spread_attempts", "Spread Attempts: %s"),
        SPREAD_HEIGHT("spread_height", "Spread Height: %s"),
        SPREAD_ROUNDS("spread_rounds", "Spread Rounds: %s"),
        SPREAD_WIDTH("spread_width", "Spread Width: %s"),
        STALACTITE_BLUNTNESS("stalactite_bluntness", "Stalactite Bluntness: %s"),
        STALAGMITE_BLUNTNESS("stalagmite_bluntness", "Stalagmite Bluntness: %s"),
        STEP("step",  "Step: %s"),
        SURFACE("surface", "Surface: %s"),
        TAG("tag", "Tag: %s"),
        TOTAL_WEIGHT("total_weight", "Total Weight: %s"),
        TRIES("tries", "Tries: %s"),
        VALID_BLOCK("valid_block", "Valid Block: %s"),
        VEGETATION_CHANCE("vegetation_chance", "Vegetation Chance: %s"),
        VERTICAL_RANGE("vertical_range", "Vertical Range: %s"),
        WEIGHT("weight", "Weight: %s"),
        WETNESS("wetness", "Wetness: %s"),
        WIND_SPEED("wind_speed", "Wind Speed: %s"),
        XZ_RADIUS("xz_radius", "XZ Radius: %s"),
        XZ_SPREAD("xz_spread", "XZ Spread: %s"),
        Y_SPREAD("y_spread", "Y Spread: %s"),
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
        ABSOLUTE_Y(Value.ABSOLUTE_Y, "absolute_y", "Absolute Y:"),
        ALLOWED_PLACEMENT("allowed_placement", "Allowed Placement:"),
        ALLOWED_SEARCH_CONDITION("allowed_search_condition", "Allowed Search Condition:"),
        ALLOWED_TREE_POSITION("allowed_tree_position", "Allowed Tree Position:"),
        ALTERNATE_INNER_LAYER_PROVIDER("alternate_inner_layer_provider", "Alternate Inner Layer Provider:"),
        BARRIER("barrier", "Barrier:"),
        BLOCKS(Value.BLOCK, "blocks", "Blocks:"),
        CANNOT_REPLACE("cannot_replace", "Cannot Replace:"),
        CAN_BE_PLACED_ON(Value.CAN_BE_PLACED_ON, "can_be_placed_on", "Can Be Placed On:"),
        CAP_PROVIDER("cap_provider", "Cap Provider:"),
        CONFIG("config", "Config:"),
        CONFIGURED_FEATURE("configured_feature", "Configured Feature:"),
        CONTENTS("contents", "Contents:"),
        DATA("data", "Data:"),
        DECORATORS("decorators", "Decorators:"),
        DECOR_STATE("decor_state", "Decor State:"),
        DEFAULT_FEATURE("default_feature", "Default Feature:"),
        DIRT_PROVIDER("dirt_provider", "Dirt Provider:"),
        ENTRY("entry", "Entry:"),
        FALLBACK("fallback", "Fallback:"),
        FEATURE("feature", "Feature:"),
        FEATURES("features", "Features:"),
        FEATURE_FALSE("feature_false", "Feature False:"),
        FEATURE_TRUE("feature_true", "Feature True:"),
        FILLING_PROVIDER("filling_provider", "Filling Provider:"),
        FLUID("fluid", "Fluid:"),
        FLUIDS(Value.FLUID, "fluids", "Fluids:"),
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
        HEIGHT("height", "Height:"),
        IF_TRUE("if_true", "If True:"),
        INNER_LAYER_PROVIDER("inner_layer_provider", "Inner Layer Provider:"),
        INNER_PLACEMENTS("inner_placements", "Inner Placements:"),
        INVALID_BLOCKS("invalid_blocks", "Invalid Blocks:"),
        ITEMS(Value.ITEM, "items", "Items:"),
        LAYERS("layers", "Layers:"),
        LAYERS_AT_Y(Value.LAYER_AT_Y, "layers_at_y", "Layers At Y:"),
        MAX("max", "Max:"),
        MIDDLE_LAYER_PROVIDER("middle_layer_provider", "Middle Layer Provider:"),
        MIN("min", "Min:"),
        MINIMUM_SIZE("minimum_size", "Minimum Size:"),
        OUTER_LAYER_PROVIDER("outer_layer_provider", "Outer Layer Provider:"),
        OVERLAY_PROCESSORS("overlay_processors", "Overlay Processors:"),
        OVERLAY_STRUCTURES(Value.OVERLAY_STRUCTURE, "overlay_structures", "Overlay Structures:"),
        PLACEMENT("placement", "Placement:"),
        PREDICATE("predicate", "Predicate:"),
        PREDICATES("predicates", "Predicates:"),
        PROPERTIES("properties", "Properties:"),
        REPLACEABLE_BLOCKS("replaceable_blocks", "Replaceable Blocks:"),
        REPLACE_STATE("replace_state", "Replace State:"),
        RIM("rim", "Rim:"),
        ROOT_PLACER("root_placer", "Root Placer:"),
        ROOT_STATE_PROVIDER("root_state_provider", "Root State Provider:"),
        RULES("rules", "Rules:"),
        SPIKES("spikes", "Spikes:"),
        STATE("state", "State:"),
        STATE_PROVIDER("state_provider", "StateProvider:"),
        STEM_PROVIDER("stem_provider", "Stem Provider:"),
        STEM_STATE("stem_state", "Stem State:"),
        TARGET("target", "Target:"),
        TARGET_CONDITION("target_condition", "Target Condition:"),
        TARGET_STATE("target_state", "Target State:"),
        TARGET_STATES("target_states", "Target States:"),
        THEN("then", "Then:"),
        TO_PLACE("to_place", "To Place:"),
        TREE_FEATURE("tree_feature", "Tree Feature:"),
        TRUNK_PLACER("trunk_placer", "Trunk Placer:"),
        TRUNK_PROVIDER("trunk_provider", "Trunk Provider:"),
        VALID_BASE_STATE("valid_base_state", "Valid Base State:"),
        VALID_BLOCKS(Value.VALID_BLOCK, "valid_blocks", "Valid Blocks:"),
        VEGETATION_FEATURE("vegetation_feature", "Vegetation Feature:"),
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
            this.translation = new Translation("awi.enum.placement." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum PlacementModifier implements ITooltipKey {
        BLOCK_PREDICATE("block_predicate", "Block predicate:"),
        CARVING_MASK_PLACEMENT("carving_mask_placement", "Carving Mask Placement:"),
        COUNT_ON_EVERY_LAYER("count_on_every_layer", "Count On Every Layer:"),
        COUNT_PLACEMENT("count_placement", "Count Placement:"),
        ENVIRONMENT_SCAN_PLACEMENT("environment_scan_placement", "Environment Scan Placement:"),
        HEIGHTMAP_PLACEMENT("heightmap_placement", "Heightmap Placement:"),
        HEIGHT_RANGE_PLACEMENT("height_range_placement", "Height Range Placement:"),
        NOISE_BASED_COUNT_PLACEMENT("noise_based_count_placement", "Noise Based Count Placement:"),
        NOISE_THRESHOLD_COUNT_PLACEMENT("noise_threshold_count_placement", "Noise Threshold Count Placement:"),
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
        MATCHING_BLOCKS("matching_blocks", "Matching Blocks:"),
        MATCHING_BLOCK_TAG("matching_block_tag", "Matching Block Tag:"),
        MATCHING_FLUIDS("matching_fluids", "Matching Fluids:"),
        NOT("not", "Not:"),
        REPLACEABLE("replaceable", "Replaceable:"),
        SOLID("solid", "Solid:"),
        TRUE_BLOCK("true_block", "True Block"),
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
