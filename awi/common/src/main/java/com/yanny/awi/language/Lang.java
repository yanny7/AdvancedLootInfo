package com.yanny.awi.language;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class Lang {
    public enum FeatureConfiguration implements ITooltipKey {
        COUNT("count", "Count:"),
        ORE("ore", "Ore:"),
        BLOCK_COLUMN("block_column", "Block Column:"),
        BLOCK_PILE("block_pile", "Block Pile:"),
        BLOCK_STATE("block_state", "Block State:"),
        COLUMN_FEATURE("column_feature", "Column Feature:"),
        DELTA_FEATURE("delta_feature", "Delta Feature:"),
        DISK("disk", "Disk:"),
        DRIPSTONE_CLUSTER("dripstone_cluster", "Dripstone Cluster:"),
        END_GATEWAY("end_gateway", "End Gateway:"),
        GEODE("geode", "Geode:"),
        HUGE_MUSHROOM_FEATURE("huge_mushroom_feature", "Huge Mushroom Feature:"),
        LARGE_DRIPSTONE("large_dripstone", "Large Dripstone:"),
        LAYERED("layered", "Layered:"),
        MULTIFACE_GROWTH("multiface_growth", "Multiface Growth:"),
        NETHER_FOREST_VEGETATION("nether_forest_vegetation", "Nether Forest Vegetation:"),
        NONE_FEATURE("none_feature", "None Feature:"),
        POINTED_DRIPSTONE("pointed_dripstone", "Pointed Dripstone:"),
        RANDOM_BOOLEAN_FEATURE("random_boolean_feature", "Random Boolean Feature:"),
        RANDOM_FEATURE("random_feature", "Random Feature:"),
        REPLACEABLE_BLOCK("replaceable_block", "Replaceable Block:"),
        RANDOM_PATCH("random_patch", "Random Patch:"),
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
        PROBABILITY_FEATURE("probability_feature", "Probability Feature:"),
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
        HEIGHT("height", "Height: %s"),
        REACH("reach", "Reach: %s"),
        ABSOLUTE_Y("absolute_y", "Absolute Y: %s"),
        BLOCK("block", "Block: %s"),
        EXIT("exit", "Exit: %s"),
        EXACT("exact", "Exact: %s"),
        RIM_SIZE("rim_size", "Rim Size: %s"),
        RADIUS("radius", "Radius: %s"),
        COUNT("count", "Count: %s"),
        HALF_HEIGHT("half_height", "Half Height: %s"),
        DEFAULT_BLOCK("default_block", "Default Block: %s"),
        DEFAULT_FLUID("default_fluid", "Default Fluid: %s"),
        DEPTH_BELOW_SURFACE("depth_below_surface", "Depth Below Surface: %s"),
        DISCARD_CHANCE_ON_AIR_EXPOSURE("discard_chance_on_air_exposure", "Discard Chance On Air Exposure: %s"),
        GENERATION_STEP("generation_step", "Generation Step: %s"),
        HEIGHTMAP("heightmap", "Heightmap: %s"),
        LAYER_AT_Y("layer_at_y", "Layer At Y: %s"),
        MAX_WATER_DEPTH("max_water_depth", "Max Water Depth: %s"),
        PLACEMENT("placement", "Placement: %s"),
        CHANCE("chance", "Chance: %s"),
        RANGE("range", "Range: %s"),
        SEA_LEVEL("sea_level", "Sea Level: %s"),
        SIZE("size", "Size: %s"),
        NOISE_TO_COUNT_RATIO("noise_to_count_ratio", "Noise To Count Ratio: %s"),
        NOISE_FACTOR("noise_factor", "Noise Factor: %s"),
        NOISE_OFFSET("noise_offset", "Noise Offset: %s"),
        NOISE_LEVEL("noise_level", "Noise Level: %s"),
        BELOW_NOISE("below_noise", "Below Noise: %s"),
        ABOVE_NOISE("above_noise", "Above Noise: %s"),
        DIRECTION_OF_SEARCH("direction_of_search", "Direction Of Search: %s"),
        MAX_STEPS("max_steps", "Max Steps: %s"),
        STEP("step",  "Step: %s"),
        ABOVE_BOTTOM("above_bottom", "Above Bottom: %s"),
        BELOW_TOP("below_top", "Below Top: %s"),
        INNER("inner", "Inner: %s"),
        PLATEAU("plateau", "Plateau: %s"),
        TOTAL_WEIGHT("total_weight", "Total Weight: %s"),
        ITEM("item", "Item: %s"),
        OFFSET("offset", "Offset: %s"),
        TAG("tag", "Tag: %s"),
        FLUID("fluid",  "Fluid: %s"),
        DIRECTION("direction", "Direction: %s"),
        FEATURE("feature", "Feature: %s"),
        WEIGHT("weight", "Weight: %s"),
        PRIORITIZE_TIP("prioritize_tip", "Prioritize Tip: %s"),
        SEARCH_RANGE("search_range", "Search Range: %s"),
        MAX_HEIGHT_DIFF("max_height_diff", "Max Height Diff: %s"),
        HEIGHT_DEVIATION("height_deviation", "Height Deviation: %s"),
        LAYER_THICKNESS("layer_thickness", "Layer Thickness: %s"),
        DENSITY("density", "Density: %s"),
        WETNESS("wetness", "Wetness: %s"),
        EDGE_CHANCE("edge_chance", "Edge Chance: %s"),
        CHANCE_RADIUS("chance_radius", "Chance Radius: %s"),
        HEIGHT_BIAS_RADIUS("height_bias_radius", "Height Bias Radius: %s"),
        POTENTIAL_PLACEMENT_CHANCE("potential_placement_chance", "Potential Placement Chance: %s"),
        ALTERNATE_LAYER_CHANCE("alternate_layer_chance", "Alternate Layer Chance: %s"),
        REQUIRE_ALTERNATE_LAYER("require_alternate_layer", "Require Alternate Layer: %s"),
        OUTER_WALL_DISTANCE("outer_wall_distance", "Outer Wall Distance: %s"),
        DISTRIBUTION_POINTS("distribution_points", "Distribution Points: %s"),
        POINT_OFFSET("point_offset", "Point Offset: %s"),
        MIN_GEN_OFFSET("min_gen_offset", "Min Gen Offset: %s"),
        MAX_GEN_OFFSET("max_gen_offset", "Max Gen Offset: %s"),
        NOISE_MULTIPLIER("noise_multiplier", "Noise Multiplier: %s"),
        INVALID_BLOCKS_THRESHOLD("invalid_blocks_threshold", "Invalid Blocks Threshold: %s"),
        FILLING("filling", "Filling: %s"),
        INNER_LAYER("inner_layer", "Inner Layer: %s"),
        MIDDLE_LAYER("middle_layer", "Middle Layer: %s"),
        OUTER_LAYER("outer_layer", "Outer Layer: %s"),
        GENERATE_CRACK_CHANCE("generate_crack_chance", "Generate Crack Chance: %s"),
        BASE_CRACK_SIZE("base_crack_size", "Base Crack Size: %s"),
        CRACK_POINT_OFFSET("crack_point_offset", "Crack Point Offset: %s"),
        FOLIAGE_RADIUS("foliage_radius", "Foliage Radius: %s"),
        COLUMN_RADIUS("column_radius", "Column Radius: %s"),
        HEIGHT_SCALE("height_scale", "Height Scale: %s"),
        RADIUS_TO_HEIGHT_RATIO("radius_to_height_ratio", "Radius To Height Ratio: %s"),
        STALACTITE_BLUNTNESS("stalactite_bluntness", "Stalactite Bluntness: %s"),
        STALAGMITE_BLUNTNESS("stalagmite_bluntness", "Stalagmite Bluntness: %s"),
        WIND_SPEED("wind_speed", "Wind Speed: %s"),
        MIN_RADIUS_FOR_WIND("min_radius_for_wind", "Min Radius For Wind: %s"),
        MIN_BLUNTNESS_FOR_WIND("min_bluntness_for_wind", "Min Bluntness For Wind: %s"),
        PLACE_BLOCK("place_block", "Place Block: %s"),
        CAN_PLACE_ON_FLOOR("can_place_on_floor", "Can Place On Floor: %s"),
        CAN_PLACE_ON_CEILING("can_place_on_ceiling", "Can Place On Ceiling: %s"),
        CAN_PLACE_ON_WALL("can_place_on_wall", "Can Place On Wall: %s"),
        CHANCE_OF_SPREADING("chance_of_spreading", "Chance Of Spreading: %s"),
        SPREAD_WIDTH("spread_width", "Spread Width: %s"),
        SPREAD_HEIGHT("spread_height", "Spread Height: %s"),
        PROBABILITY("probability", "Probability: %s"),
        TRIES("tries", "Tries: %s"),
        XZ_SPREAD("xz_spread", "XZ Spread: %s"),
        Y_SPREAD("y_spread", "Y Spread: %s"),
        REQUIRED_VERTICAL_SPACE_FOR_TREE("required_vertical_space_for_tree", "Required Vertical Space For Tree: %s"),
        ROOT_RADIUS("root_radius", "Root Radius: %s"),
        ROOT_REPLACEABLE("root_replaceable", "Root Replaceable: %s"),
        ROOT_PLACEMENT_ATTEMPTS("root_placement_attempts", "Root Placement Attempts: %s"),
        ROOT_COLUMN_MAX_HEIGHT("root_column_max_height", "Root Column Max Height: %s"),
        HANGING_ROOT_RADIUS("hanging_root_radius", "Hanging Root Radius: %s"),
        HANGING_ROOT_VERTICAL_SPAN("hanging_root_vertical_span", "Hanging Root Vertical Span: %s"),
        HANGING_ROOT_PLACEMENT_ATTEMPTS("hanging_root_placement_attempts", "Hanging Root Placement Attempts: %s"),
        ALLOWED_VERTICAL_WATER_FOR_TREE("allowed_vertical_water_for_tree", "Allowed Vertical Water For Tree: %s"),
        CHARGE_COUNT("charge_count", "Charge Count: %s"),
        AMOUNT_PER_CHARGE("amount_per_charge", "Amount Per Charge: %s"),
        SPREAD_ATTEMPTS("spread_attempts", "Spread Attempts: %s"),
        GROWTH_ROUNDS("growth_rounds", "Growth Rounds: %s"),
        SPREAD_ROUNDS("spread_rounds", "Spread Rounds: %s"),
        EXTRA_RARE_GROWTHS("extra_rare_growths", "Extra Rare Growths: %s"),
        CATALYST_CHANCE("catalyst_chance", "Catalyst Chance: %s"),
        CHANCE_OF_TALLER_DRIPSTONE("chance_of_taller_dripstone", "Chance Of Taller Dripstone: %s"),
        CHANCE_OF_DIRECTIONAL_SPEED("chance_of_directional_speed", "Chance Of Directional Speed: %s"),
        CHANCE_OF_SPREAD_RADIUS_2("chance_of_spread_radius_2", "Chance Of Spread Radius 2: %s"),
        CHANCE_OF_SPREAD_RADIUS_3("chance_of_spread_radius_3", "Chance Of Spread Radius 3: %s"),
        REQUIRES_BLOCK_BELOW("requires_block_below", "Requires Block Below: %s"),
        ROCK_COUNT("rock_count", "Rock Count: %s"),
        HOLE_COUNT("hole_count", "Hole Count: %s"),
        IGNORE_VINES("ignore_vines", "Ignore Vines: %s"),
        FORCE_DIRT("force_dirt", "Force Dirt: %s"),
        MAX_HEIGHT("max_height", "Max Height: %s"),
        FLOOR_RANGE_SEARCH("floor_range_search", "Floor Range Search: %s"),
        PLACEMENT_RADIUS_AROUND_FLOOR("placement_radius_around_floor", "Placement Radius Around Floor: %s"),
        PROBABILITY_PER_POSITION("probability_per_position", "Probability Per Position: %s"),
        REPLACEABLE("replaceable", "Replaceable: %s"),
        SURFACE("surface", "Surface: %s"),
        DEPTH("depth", "Depth: %s"),
        EXTRA_BOTTOM_BLOCK_CHANCE("extra_bottom_block_chance", "Extra Bottom Block Chance: %s"),
        VERTICAL_RANGE("vertical_range", "Vertical Range: %s"),
        VEGETATION_CHANCE("vegetation_chance", "Vegetation Chance: %s"),
        XZ_RADIUS("xz_radius", "XZ Radius: %s"),
        EXTRA_EDGE_COLUMN_CHANCE("extra_edge_column_chance", "Extra Edge Column Chance: %s"),
        IS_CRYSTAL_VULNERABLE("is_crystal_vulnerable", "Is Crystal Vulnerable: %s"),
        CRYSTAL_BEAM_TARGET("crystal_beam_target", "Crystal Beam Target: %s"),
        CENTER_X("center_x", "Center X: %s"),
        CENTER_Z("center_z", "Center Z: %s"),
        IS_GUARDED("is_guarded", "Is Guarded: %s"),
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
        STATE_PROVIDER("state_provider", "StateProvider:"),
        ABSOLUTE_Y(Value.ABSOLUTE_Y, "absolute_y", "Absolute Y:"),
        PROPERTIES("properties", "Properties:"),
        PREDICATE("predicate", "Predicate:"),
        HEIGHT("height", "Height:"),
        MIN("min", "Min:"),
        CONTENTS("contents", "Contents:"),
        RIM("rim", "Rim:"),
        MAX("max", "Max:"),
        LAYERS_AT_Y(Value.LAYER_AT_Y, "layers_at_y", "Layers At Y:"),
        PLACEMENT("placement", "Placement:"),
        STATE("state", "State:"),
        TARGET("target", "Target:"),
        TARGET_STATES("target_states", "Target States:"),
        TARGET_CONDITION("target_condition", "Target Condition:"),
        ALLOWED_SEARCH_CONDITION("allowed_search_condition", "Allowed Search Condition:"),
        ITEMS(Value.ITEM, "items", "Items:"),
        BLOCKS(Value.BLOCK, "blocks", "Blocks:"),
        FLUIDS(Value.FLUID, "fluids", "Fluids:"),
        PREDICATES("predicates", "Predicates:"),
        CONFIGURED_FEATURE("configured_feature", "Configured Feature:"),
        DATA("data", "Data:"),
        ENTRY("entry", "Entry:"),
        LAYERS("layers", "Layers:"),
        ALLOWED_PLACEMENT("allowed_placement", "Allowed Placement:"),
        GEODE_BLOCK_SETTINGS("geode_block_settings", "Geode Block Settings:"),
        GEODE_LAYER_SETTINGS("geode_layer_settings", "Geode Layer Settings:"),
        GEODE_CRACK_SETTINGS("geode_crack_settings", "Geode Crack Settings:"),
        FILLING_PROVIDER("filling_provider", "Filling Provider:"),
        INNER_LAYER_PROVIDER("inner_layer_provider", "Inner Layer Provider:"),
        ALTERNATE_INNER_LAYER_PROVIDER("alternate_inner_layer_provider", "Alternate Inner Layer Provider:"),
        MIDDLE_LAYER_PROVIDER("middle_layer_provider", "Middle Layer Provider:"),
        OUTER_LAYER_PROVIDER("outer_layer_provider", "Outer Layer Provider:"),
        INNER_PLACEMENTS("inner_placements", "Inner Placements:"),
        CANNOT_REPLACE("cannot_replace", "Cannot Replace:"),
        INVALID_BLOCKS("invalid_blocks", "Invalid Blocks:"),
        CAP_PROVIDER("cap_provider", "Cap Provider:"),
        STEM_PROVIDER("stem_provider", "Stem Provider:"),
        CAN_BE_PLACED_ON("can_be_placed_on", "Can Be Placed On:"),
        FEATURE("feature", "Feature:"),
        FEATURE_TRUE("feature_true", "Feature True:"),
        FEATURE_FALSE("feature_false", "Feature False:"),
        FEATURES("features", "Features:"),
        DEFAULT_FEATURE("default_feature", "Default Feature:"),
        TARGET_STATE("target_state", "Target State:"),
        REPLACE_STATE("replace_state", "Replace State:"),
        TREE_FEATURE("tree_feature", "Tree Feature:"),
        ROOT_STATE_PROVIDER("root_state_provider", "Root State Provider:"),
        HANGING_ROOT_STATE_PROVIDER("hanging_root_state_provider", "Hanging Root State Provider:"),
        ALLOWED_TREE_POSITION("allowed_tree_position", "Allowed Tree Position:"),
        TO_PLACE("to_place", "To Place:"),
        SPIKES("spikes", "Spikes:"),
        VALID_BLOCKS("valid_blocks", "Valid Blocks:"),
        TRUNK_PROVIDER("trunk_provider", "Trunk Provider:"),
        DIRT_PROVIDER("dirt_provider", "Dirt Provider:"),
        TRUNK_PLACER("trunk_placer", "Trunk Placer:"),
        FOLIAGE_PROVIDER("foliage_provider", "Foliage Provider:"),
        FOLIAGE_PLACER("foliage_placer", "Foliage Placer:"),
        ROOT_PLACER("root_placer", "Root Placer:"),
        MINIMUM_SIZE("minimum_size", "Minimum Size:"),
        DECORATORS("decorators", "Decorators:"),
        GROUND_STATE("ground_state", "Ground State:"),
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
        UNDERWATER("underwater", "Underwater"),
        ON_LAND("on_land", "On Land"),
        ON_CEILING("on_ceiling", "On Ceiling"),
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
        COUNT_PLACEMENT("count_placement", "Count Placement:"),
        HEIGHTMAP_PLACEMENT("heightmap_placement", "Heightmap Placement:"),
        HEIGHT_RANGE_PLACEMENT("height_range_placement", "Height Range Placement:"),
        RARITY_FILTER("rarity_filter", "Rarity Filter:"),
        SURFACE_RELATIVE_THRESHOLD_FILTER("surface_relative_threshold_filter", "Surface Relative Threshold Filter:"),
        SURFACE_WATER_DEPTH_FILTER("surface_water_depth_filter", "Surface Water Depth Filter:"),
        NOISE_BASED_COUNT_PLACEMENT("noise_based_count_placement", "Noise Based Count Placement:"),
        COUNT_ON_EVERY_LAYER("count_on_every_layer", "Count On Every Layer:"),
        NOISE_THRESHOLD_COUNT_PLACEMENT("noise_threshold_count_placement", "Noise Threshold Count Placement:"),
        ENVIRONMENT_SCAN_PLACEMENT("environment_scan_placement", "Environment Scan Placement:"),
        CARVING_MASK_PLACEMENT("carving_mask_placement", "Carving Mask Placement:"),
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
        CONSTANT("constant", "Constant:"),
        UNIFORM("uniform", "Uniform:"),
        BIASED_TO_BOTTOM("biased_to_bottom", "Biased To Bottom:"),
        VERY_BIASED_TO_BOTTOM("very_biased_to_bottom", "Very Biased To Bottom:"),
        WEIGHTED_LIST("weighted_list", "Weighted List:"),
        TRAPEZOID("trapezoid", "Trapezoid:"),
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
        MATCHING_BLOCKS("matching_blocks", "Matching Blocks:"),
        MATCHING_BLOCK_TAG("matching_block_tag", "Matching Block Tag:"),
        MATCHING_FLUIDS("matching_fluids", "Matching Fluids:"),
        HAS_STURDY_FACE("has_sturdy_face", "Has Sturdy Face:"),
        SOLID("solid", "Solid:"),
        REPLACEABLE("replaceable", "Replaceable:"),
        WOULD_SURVIVE("would_survive", "Would Survive:"),
        INSIDE_WORLD_BOUNDS("inside_world_bounds", "Inside World Bounds:"),
        ANY_OF("any_of", "Any Of:"),
        ALL_OF("all_of", "All Of:"),
        NOT("not", "Not:"),
        TRUE_BLOCK("true_block", "True Block"),
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
        RAW_GENERATION("raw_generation", "Raw Generation"),
        LAKES("lakes", "Lakes"),
        LOCAL_MODIFICATIONS("local_modifications", "Local Modifications"),
        UNDERGROUND_STRUCTURES("underground_structures", "Underground Structures"),
        SURFACE_STRUCTURES("surface_structures", "Surface Structures"),
        STRONGHOLDS("strongholds", "Strongholds"),
        UNDERGROUND_ORES("underground_ores", "Underground Ores"),
        UNDERGROUND_DECORATION("underground_decoration", "Underground Decoration"),
        FLUID_SPRINGS("fluid_springs", "Fluid Springs"),
        VEGETAL_DECORATION("vegetal_decoration", "Vegetation Decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification", "Top Layer Modifications"),
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
