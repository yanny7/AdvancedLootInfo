package com.yanny.awi.language;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class Lang {
    public enum FeatureConfiguration implements ITooltipKey {
        COUNT("count", "Count:"),
        ORE("ore", "Ore:"),
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
        ABSOLUTE_Y("absolute_y", "Absolute Y: %s"),
        BLOCK("block", "Block: %s"),
        COUNT("count", "Count: %s"),
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
        PROPERTIES("properties", "Properties:"),
        PREDICATE("predicate", "Predicate:"),
        HEIGHT("height", "Height:"),
        MIN("min", "Min:"),
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
        TRUE_BLOCK("true_block", "True Block:"),
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
