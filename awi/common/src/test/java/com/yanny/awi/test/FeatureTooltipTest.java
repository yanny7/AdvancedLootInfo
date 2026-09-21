package com.yanny.awi.test;

import com.yanny.aci.test.utils.TestUtils;
import com.yanny.awi.test.utils.FeatureInstances;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeatureTooltipTest {
    private static final Map<String, Feature> FEATURES = FeatureInstances.features();

    @Test
    public void testEveryRegisteredFeatureTypeIsCovered() {
        Set<String> registered = BuiltInRegistries.FEATURE_TYPE.keySet().stream()
                .map(Identifier::toString)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> covered = FEATURES.entrySet().stream()
                .map((entry) -> Identifier.withDefaultNamespace(entry.getKey()).toString())
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(registered, covered);
    }

    /** Each entry's key has to name the type its instance really is, or the tooltip under it tests the wrong feature. */
    @Test
    public void testEveryInstanceMatchesItsKey() {
        FEATURES.forEach((id, feature) ->
                assertEquals(Identifier.withDefaultNamespace(id), BuiltInRegistries.FEATURE_TYPE.getKey(feature.codec()), id));
    }

    private static void assertTooltip(Feature feature, List<String> expected) {
        TestUtils.assertTooltip(UTILS.getFeatureTooltip(UTILS, feature).build(), expected);
    }

    @Test
    public void testBambooTooltip() {
        assertTooltip(FEATURES.get("bamboo"), List.of(
                "Bamboo:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testBlockBlobTooltip() {
        assertTooltip(FEATURES.get("block_blob"), List.of(
                "Block Blob:",
                "  -> State:",
                "    -> Block: Magma Block",
                "  -> Can Place On:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testBlockColumnTooltip() {
        assertTooltip(FEATURES.get("block_column"), List.of(
                "Block Column:",
                "  -> Layers:",
                "    -> Height:",
                "      -> Constant:",
                "        -> Value: 2",
                "    -> State:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Cave Vines",
                "          -> Properties:",
                "            -> age: 0",
                "            -> berries: false",
                "  -> Direction: Down",
                "  -> Allowed Placement:",
                "    -> Matching Block Tag:",
                "      -> Tag: minecraft:air",
                "  -> Prioritize Tip: true"
        ));
    }

    @Test
    public void testBlockPileTooltip() {
        assertTooltip(FEATURES.get("block_pile"), List.of(
                "Block Pile:",
                "  -> State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Hay Bale",
                "        -> Properties:",
                "          -> axis: y"
        ));
    }

    @Test
    public void testBlueIceTooltip() {
        assertTooltip(FEATURES.get("blue_ice"), List.of(
                "Blue Ice:"
        ));
    }

    @Test
    public void testBonusChestTooltip() {
        assertTooltip(FEATURES.get("bonus_chest"), List.of(
                "Bonus Chest:"
        ));
    }

    @Test
    public void testChorusPlantTooltip() {
        assertTooltip(FEATURES.get("chorus_plant"), List.of(
                "Chorus Plant:"
        ));
    }

    @Test
    public void testCoralClawTooltip() {
        assertTooltip(FEATURES.get("coral_claw"), List.of(
                "Coral Claw:",
                "  -> Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testCoralTreeTooltip() {
        assertTooltip(FEATURES.get("coral_tree"), List.of(
                "Coral Tree:",
                "  -> Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testDeltaFeatureTooltip() {
        assertTooltip(FEATURES.get("delta_feature"), List.of(
                "Delta Feature:",
                "  -> Contents:",
                "    -> Block: Lava",
                "    -> Properties:",
                "      -> level: 0",
                "  -> Rim:",
                "    -> Block: Magma Block",
                "  -> Size:",
                "    -> Constant:",
                "      -> Value: 4",
                "  -> Rim Size:",
                "    -> Uniform:",
                "      -> Range: 1-2"
        ));
    }

    @Test
    public void testDiskTooltip() {
        assertTooltip(FEATURES.get("disk"), List.of(
                "Disk:",
                "  -> State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Sand",
                "  -> Target:",
                "    -> Solid:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 3",
                "  -> Half Height: 2"
        ));
    }

    @Test
    public void testEndGatewayTooltip() {
        assertTooltip(FEATURES.get("end_gateway"), List.of(
                "End Gateway:",
                "  -> Exit: [1,2,3]",
                "  -> Exact: true"
        ));
    }

    @Test
    public void testEndIslandTooltip() {
        assertTooltip(FEATURES.get("end_island"), List.of(
                "End Island:"
        ));
    }

    @Test
    public void testEndPlatformTooltip() {
        assertTooltip(FEATURES.get("end_platform"), List.of(
                "End Platform:"
        ));
    }

    @Test
    public void testEndPodiumTooltip() {
        assertTooltip(FEATURES.get("end_podium"), List.of(
                "End Podium:",
                "  -> Active: true"
        ));
    }

    @Test
    public void testEndSpikeTooltip() {
        assertTooltip(FEATURES.get("end_spike"), List.of(
                "End Spike:",
                "  -> Spikes:",
                "    -> Center X: 1",
                "    -> Center Z: 2",
                "    -> Radius: 5",
                "    -> Height: 30",
                "    -> Is Guarded: true",
                "  -> Is Crystal Invulnerable: true",
                "  -> Crystal Beam Target: [1,2,3]"
        ));
    }

    @Test
    public void testFallenTreeTooltip() {
        assertTooltip(FEATURES.get("fallen_tree"), List.of(
                "Fallen Tree:",
                "  -> Trunk Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Log",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Log Length:",
                "    -> Constant:",
                "      -> Value: 5",
                "  -> Stump Decorators:",
                "    -> Trunk Vine",
                "  -> Log Decorators:",
                "    -> Cocoa:",
                "      -> Probability: 0.5"
        ));
    }

    @Test
    public void testFillLayerTooltip() {
        assertTooltip(FEATURES.get("fill_layer"), List.of(
                "Fill Layer:",
                "  -> Height: 4",
                "  -> State:",
                "    -> Block: Stone"
        ));
    }

    @Test
    public void testFossilTooltip() {
        assertTooltip(FEATURES.get("fossil"), List.of(
                "Fossil:",
                "  -> Fossil Structure: minecraft:fossil/spine_1",
                "  -> Overlay Structure: minecraft:fossil_spine_1",
                "  -> Fossil Processors:",
                "    -> Block Ignore:",
                "      -> To Ignore: Air",
                "  -> Overlay Processors:",
                "    -> Block Ignore:",
                "      -> To Ignore: Air",
                "  -> Max Empty Corners Allowed: 3"
        ));
    }

    @Test
    public void testFreezeTopLayerTooltip() {
        assertTooltip(FEATURES.get("freeze_top_layer"), List.of(
                "Freeze Top Layer:"
        ));
    }

    @Test
    public void testGeodeTooltip() {
        assertTooltip(FEATURES.get("geode"), List.of(
                "Geode:",
                "  -> Geode Block Settings:",
                "    -> Filling Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Air",
                "    -> Inner Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Block of Amethyst",
                "    -> Alternate Inner Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Budding Amethyst",
                "    -> Middle Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Calcite",
                "    -> Outer Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Smooth Basalt",
                "    -> Inner Placements:",
                "      -> Block: Amethyst Cluster",
                "      -> Properties:",
                "        -> facing: up",
                "        -> waterlogged: false",
                "    -> Cannot Replace:",
                "      -> Tag: minecraft:wool",
                "    -> Invalid Blocks:",
                "      -> Tag: minecraft:logs",
                "  -> Geode Layer Settings:",
                "    -> Filling: 1.7",
                "    -> Inner Layer: 2.2",
                "    -> Middle Layer: 3.2",
                "    -> Outer Layer: 4.2",
                "  -> Geode Crack Settings:",
                "    -> Generate Crack Chance: 1.0",
                "    -> Base Crack Size: 2.0",
                "    -> Crack Point Offset: 2",
                "  -> Potential Placement Chance: 0.35",
                "  -> Alternate Layer Chance: 0.0",
                "  -> Require Alternate Layer: true",
                "  -> Outer Wall Distance:",
                "    -> Constant:",
                "      -> Value: 5",
                "  -> Distribution Points:",
                "    -> Constant:",
                "      -> Value: 4",
                "  -> Point Offset:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Min Gen Offset: -16",
                "  -> Max Gen Offset: 16",
                "  -> Noise Multiplier: 0.05",
                "  -> Invalid Blocks Threshold: 1"
        ));
    }

    @Test
    public void testHugeBrownMushroomTooltip() {
        assertTooltip(FEATURES.get("huge_brown_mushroom"), List.of(
                "Huge Brown Mushroom:",
                "  -> Cap Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Brown Mushroom Block",
                "        -> Properties:",
                "          -> down: true",
                "          -> east: true",
                "          -> north: true",
                "          -> south: true",
                "          -> up: true",
                "          -> west: true",
                "  -> Stem Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Mushroom Stem",
                "        -> Properties:",
                "          -> down: true",
                "          -> east: true",
                "          -> north: true",
                "          -> south: true",
                "          -> up: true",
                "          -> west: true",
                "  -> Foliage Radius: 3",
                "  -> Can Place On:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testHugeFungusTooltip() {
        assertTooltip(FEATURES.get("huge_fungus"), List.of(
                "Huge Fungus:",
                "  -> Valid Base State:",
                "    -> Block: Crimson Nylium",
                "  -> Stem State:",
                "    -> Block: Crimson Stem",
                "    -> Properties:",
                "      -> axis: y",
                "  -> Hat State:",
                "    -> Block: Nether Wart Block",
                "  -> Decor State:",
                "    -> Block: Shroomlight",
                "  -> Replaceable Blocks:",
                "    -> Solid:",
                "  -> Planted: true"
        ));
    }

    @Test
    public void testHugeRedMushroomTooltip() {
        assertTooltip(FEATURES.get("huge_red_mushroom"), List.of(
                "Huge Red Mushroom:",
                "  -> Cap Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Red Mushroom Block",
                "        -> Properties:",
                "          -> down: true",
                "          -> east: true",
                "          -> north: true",
                "          -> south: true",
                "          -> up: true",
                "          -> west: true",
                "  -> Stem Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Mushroom Stem",
                "        -> Properties:",
                "          -> down: true",
                "          -> east: true",
                "          -> north: true",
                "          -> south: true",
                "          -> up: true",
                "          -> west: true",
                "  -> Foliage Radius: 2",
                "  -> Can Place On:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testIcebergTooltip() {
        assertTooltip(FEATURES.get("iceberg"), List.of(
                "Iceberg:",
                "  -> State:",
                "    -> Block: Packed Ice"
        ));
    }

    @Test
    public void testLakeTooltip() {
        assertTooltip(FEATURES.get("lake"), List.of(
                "Lake:",
                "  -> Fluid:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Water",
                "        -> Properties:",
                "          -> level: 0",
                "  -> Barrier:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Stone",
                "  -> Can Place Feature:",
                "    -> Solid:",
                "  -> Can Replace With Air Or Fluid:",
                "    -> Solid:",
                "  -> Can Replace With Barrier:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testLargeDripstoneTooltip() {
        assertTooltip(FEATURES.get("large_dripstone"), List.of(
                "Large Dripstone:",
                "  -> Replaceable Blocks:",
                "    -> Tag: minecraft:wool",
                "  -> Search Range: 30",
                "  -> Column Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Height Scale:",
                "    -> Constant:",
                "      -> Value: 0.4",
                "  -> Radius To Height Ratio: 0.2",
                "  -> Stalactite Bluntness:",
                "    -> Constant:",
                "      -> Value: 0.1",
                "  -> Stalagmite Bluntness:",
                "    -> Constant:",
                "      -> Value: 0.2",
                "  -> Wind Speed:",
                "    -> Constant:",
                "      -> Value: 0.3",
                "  -> Min Radius For Wind: 3",
                "  -> Min Bluntness For Wind: 0.4"
        ));
    }

    @Test
    public void testMonsterRoomTooltip() {
        assertTooltip(FEATURES.get("monster_room"), List.of(
                "Monster Room:"
        ));
    }

    @Test
    public void testMultifaceGrowthTooltip() {
        assertTooltip(FEATURES.get("multiface_growth"), List.of(
                "Multiface Growth:",
                "  -> Place Block: Glow Lichen",
                "  -> Search Range: 20",
                "  -> Can Place On Floor: true",
                "  -> Can Place On Ceiling: true",
                "  -> Can Place On Wall: true",
                "  -> Chance Of Spreading: 0.5",
                "  -> Can Be Placed On:",
                "    -> Tag: minecraft:wool"
        ));
    }

    @Test
    public void testNetherrackReplaceBlobsTooltip() {
        assertTooltip(FEATURES.get("netherrack_replace_blobs"), List.of(
                "Netherrack Replace Blobs:",
                "  -> Target State:",
                "    -> Block: Netherrack",
                "  -> Replace State:",
                "    -> Block: Blackstone",
                "  -> Radius:",
                "    -> Uniform:",
                "      -> Range: 3-5"
        ));
    }

    @Test
    public void testNoOpTooltip() {
        assertTooltip(FEATURES.get("no_op"), List.of(
                "No Op:"
        ));
    }

    @Test
    public void testOreTooltip() {
        assertTooltip(FEATURES.get("ore"), List.of(
                "Ore:",
                "  -> Target States:",
                "    -> State:",
                "      -> Block: Furnace",
                "      -> Properties:",
                "        -> facing: north",
                "        -> lit: false",
                "    -> Target:",
                "      -> Always True",
                "  -> Size: 5",
                "  -> Discard Chance On Air Exposure: 0.5"
        ));
    }

    @Test
    public void testOverlayTooltip() {
        assertTooltip(FEATURES.get("overlay"), List.of(
                "Overlay:",
                "  -> Features:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testProjectedRandomPatchySquareTooltip() {
        assertTooltip(FEATURES.get("projected_random_patchy_square"), List.of(
                "Projected Random Patchy Square:",
                "  -> Block:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Moss Carpet",
                "  -> Project Through:",
                "    -> Solid:",
                "  -> Size:",
                "    -> Constant:",
                "      -> Value: 3",
                "  -> Max Projection Height: 5"
        ));
    }

    @Test
    public void testRandomBooleanSelectorTooltip() {
        assertTooltip(FEATURES.get("random_boolean_selector"), List.of(
                "Random Boolean Selector:",
                "  -> Feature True:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:",
                "  -> Feature False:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testRandomNeighborSpreadTooltip() {
        assertTooltip(FEATURES.get("random_neighbor_spread"), List.of(
                "Random Neighbor Spread:",
                "  -> Block:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Glowstone",
                "  -> Accepted Neighbors:",
                "    -> Tag: minecraft:wool",
                "  -> Can Replace:",
                "    -> Solid:",
                "  -> Attempts:",
                "    -> Constant:",
                "      -> Value: 1500",
                "  -> XZ Offset:",
                "    -> Uniform:",
                "      -> Range: 0-12",
                "  -> Y Offset:",
                "    -> Uniform:",
                "      -> Range: 0-12"
        ));
    }

    @Test
    public void testRandomSelectorTooltip() {
        assertTooltip(FEATURES.get("random_selector"), List.of(
                "Random Selector:",
                "  -> Features:",
                "    -> Feature:",
                "      -> Feature:",
                "        -> No Op:",
                "      -> Placement:",
                "        -> Block Predicate Filter:",
                "          -> Solid:",
                "    -> Chance: 0.25",
                "  -> Default Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testReplaceSingleBlockTooltip() {
        assertTooltip(FEATURES.get("replace_single_block"), List.of(
                "Replace Single Block:",
                "  -> Replacements:",
                "    -> State:",
                "      -> Block: Emerald Ore",
                "    -> Target:",
                "      -> Always True"
        ));
    }

    @Test
    public void testRootSystemTooltip() {
        assertTooltip(FEATURES.get("root_system"), List.of(
                "Root System:",
                "  -> Tree Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:",
                "  -> Required Vertical Space For Tree: 3",
                "  -> Level Test Distance: 2",
                "  -> Max Level Deviation: 1",
                "  -> Root Radius: 4",
                "  -> Root Replaceable:",
                "    -> Tag: minecraft:wool",
                "  -> Root State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Mangrove Roots",
                "        -> Properties:",
                "          -> waterlogged: false",
                "  -> Root Placement Attempts: 5",
                "  -> Root Column Max Height: 6",
                "  -> Hanging Root Radius: 7",
                "  -> Hanging Root Vertical Span: 8",
                "  -> Hanging Root State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Hanging Roots",
                "        -> Properties:",
                "          -> waterlogged: false",
                "  -> Hanging Root Placement Attempts: 9",
                "  -> Allowed Vertical Water For Tree: 10",
                "  -> Allowed Tree Position:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testScatteredOreTooltip() {
        assertTooltip(FEATURES.get("scattered_ore"), List.of(
                "Scattered Ore:",
                "  -> Target States:",
                "    -> State:",
                "      -> Block: Gold Ore",
                "    -> Target:",
                "      -> Always True",
                "  -> Size: 3",
                "  -> Discard Chance On Air Exposure: 0.0"
        ));
    }

    @Test
    public void testSculkPatchTooltip() {
        assertTooltip(FEATURES.get("sculk_patch"), List.of(
                "Sculk Patch:",
                "  -> Charge Count: 2",
                "  -> Amount Per Charge: 3",
                "  -> Spread Attempts: 4",
                "  -> Growth Rounds: 5",
                "  -> Spread Rounds: 6"
        ));
    }

    @Test
    public void testSequenceTooltip() {
        assertTooltip(FEATURES.get("sequence"), List.of(
                "Sequence:",
                "  -> Features:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testSimpleBlockTooltip() {
        assertTooltip(FEATURES.get("simple_block"), List.of(
                "Simple Block:",
                "  -> To Place:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Seagrass",
                "  -> Schedule Tick: true"
        ));
    }

    @Test
    public void testSimpleRandomSelectorTooltip() {
        assertTooltip(FEATURES.get("simple_random_selector"), List.of(
                "Simple Random Selector:",
                "  -> Features:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testSingleBlockPillarTooltip() {
        assertTooltip(FEATURES.get("single_block_pillar"), List.of(
                "Single Block Pillar:",
                "  -> Block:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Basalt",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Can Replace:",
                "    -> Solid:",
                "  -> Direction: Up",
                "  -> Chance To Continue: 0.75",
                "  -> Cap Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:"
        ));
    }

    @Test
    public void testSpeleothemTooltip() {
        assertTooltip(FEATURES.get("speleothem"), List.of(
                "Speleothem:",
                "  -> Base Block:",
                "    -> Block: Dripstone Block",
                "  -> Pointed Block:",
                "    -> Block: Pointed Dripstone",
                "    -> Properties:",
                "      -> thickness: tip",
                "      -> vertical_direction: up",
                "      -> waterlogged: false",
                "  -> Replaceable Blocks:",
                "    -> Tag: minecraft:wool",
                "  -> Chance Of Taller Generation: 0.2",
                "  -> Chance Of Directional Spread: 0.7",
                "  -> Chance Of Spread Radius 2: 0.5",
                "  -> Chance Of Spread Radius 3: 0.5"
        ));
    }

    @Test
    public void testSpeleothemClusterTooltip() {
        assertTooltip(FEATURES.get("speleothem_cluster"), List.of(
                "Speleothem Cluster:",
                "  -> Base Block:",
                "    -> Block: Dripstone Block",
                "  -> Pointed Block:",
                "    -> Block: Pointed Dripstone",
                "    -> Properties:",
                "      -> thickness: tip",
                "      -> vertical_direction: up",
                "      -> waterlogged: false",
                "  -> Replaceable Blocks:",
                "    -> Tag: minecraft:wool",
                "  -> Floor-Ceiling Search Range: 12",
                "  -> Height:",
                "    -> Uniform:",
                "      -> Range: 3-6",
                "  -> Radius:",
                "    -> Uniform:",
                "      -> Range: 2-8",
                "  -> Max Stalagmite/Stalactite Diff: 1",
                "  -> Height Deviation: 3",
                "  -> Speleothem Layer Thickness:",
                "    -> Constant:",
                "      -> Value: 1",
                "  -> Density:",
                "    -> Constant:",
                "      -> Value: 0.4",
                "  -> Wetness:",
                "    -> Constant:",
                "      -> Value: 0.3",
                "  -> Chance At Max Center Distance: 0.1",
                "  -> Max Edge Distance For Chance: 4",
                "  -> Max Center Distance For Height Bias: 5"
        ));
    }

    @Test
    public void testSpikeTooltip() {
        assertTooltip(FEATURES.get("spike"), List.of(
                "Spike:",
                "  -> State:",
                "    -> Block: Obsidian",
                "  -> Can Place On:",
                "    -> Solid:",
                "  -> Can Replace:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testSpringFeatureTooltip() {
        assertTooltip(FEATURES.get("spring_feature"), List.of(
                "Spring Feature:",
                "  -> State:",
                "    -> Fluid: minecraft:water",
                "    -> Properties:",
                "      -> falling: true",
                "  -> Requires Block Below: true",
                "  -> Rock Count: 4",
                "  -> Hole Count: 1",
                "  -> Valid Blocks:",
                "    -> Tag: minecraft:wool"
        ));
    }

    @Test
    public void testSteppedColumnClusterTooltip() {
        assertTooltip(FEATURES.get("stepped_column_cluster"), List.of(
                "Stepped Column Cluster:",
                "  -> Block:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Basalt",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Continue Through:",
                "    -> Solid:",
                "  -> Can Replace:",
                "    -> Solid:",
                "  -> Cannot Place On:",
                "    -> Tag: minecraft:wool",
                "  -> Cluster Reach:",
                "    -> Constant:",
                "      -> Value: 4",
                "  -> Column Count:",
                "    -> Uniform:",
                "      -> Range: 1-3",
                "  -> Column Reach:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Height:",
                "    -> Uniform:",
                "      -> Range: 1-5"
        ));
    }

    @Test
    public void testTemplateTooltip() {
        assertTooltip(FEATURES.get("template"), List.of(
                "Template:",
                "  -> Templates:",
                "    -> Total Weight: 2",
                "    -> Items:",
                "      -> Weight: 2",
                "      -> Value:",
                "        -> Template: minecraft:trail_ruins/tower/tower_1",
                "        -> Rotations:",
                "          -> None",
                "          -> Clockwise 90",
                "  -> Processors:",
                "    -> Block Ignore:",
                "      -> To Ignore: Air"
        ));
    }

    @Test
    public void testTreeTooltip() {
        assertTooltip(FEATURES.get("tree"), List.of(
                "Tree:",
                "  -> Trunk Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Log",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Trunk Placer:",
                "    -> Straight Trunk:",
                "      -> Base Height: 4",
                "      -> Height Rand A: 2",
                "      -> Height Rand B: 0",
                "  -> Foliage Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Leaves",
                "        -> Properties:",
                "          -> distance: 7",
                "          -> persistent: false",
                "          -> waterlogged: false",
                "  -> Foliage Placer:",
                "    -> Blob:",
                "      -> Radius:",
                "        -> Constant:",
                "          -> Value: 2",
                "      -> Offset:",
                "        -> Constant:",
                "          -> Value: 0",
                "      -> Height: 3",
                "  -> Root Placer:",
                "    -> Mangrove Root:",
                "      -> Trunk Offset Y:",
                "        -> Constant:",
                "          -> Value: 1",
                "      -> Root Provider:",
                "        -> Simple:",
                "          -> State:",
                "            -> Block: Mangrove Roots",
                "            -> Properties:",
                "              -> waterlogged: false",
                "      -> Mangrove Root Placement:",
                "        -> Can Grow Through:",
                "          -> Tag: minecraft:wool",
                "        -> Muddy Root In:",
                "          -> Tag: minecraft:wool",
                "        -> Muddy Root Provider:",
                "          -> Simple:",
                "            -> State:",
                "              -> Block: Muddy Mangrove Roots",
                "              -> Properties:",
                "                -> axis: y",
                "        -> Max Root Width: 8",
                "        -> Max Root Length: 15",
                "        -> Random Skew Chance: 0.2",
                "  -> Minimum Size:",
                "    -> Two Layers:",
                "      -> Limit: 1",
                "      -> Lower Size: 0",
                "      -> Upper Size: 1",
                "  -> Decorators:",
                "    -> Trunk Vine",
                "  -> Ignore Vines: true",
                "  -> Below Trunk Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Dirt"
        ));
    }

    @Test
    public void testUnderwaterMagmaTooltip() {
        assertTooltip(FEATURES.get("underwater_magma"), List.of(
                "Underwater Magma:",
                "  -> Floor Range Search: 5",
                "  -> Placement Radius Around Floor: 3",
                "  -> Probability Per Position: 0.5"
        ));
    }

    @Test
    public void testVegetationPatchTooltip() {
        assertTooltip(FEATURES.get("vegetation_patch"), List.of(
                "Vegetation Patch:",
                "  -> Replaceable:",
                "    -> Tag: minecraft:wool",
                "  -> Ground State:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Moss Block",
                "  -> Vegetation Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:",
                "  -> Surface: Floor",
                "  -> Depth:",
                "    -> Constant:",
                "      -> Value: 1",
                "  -> Extra Bottom Block Chance: 0.0",
                "  -> Vertical Range: 5",
                "  -> Vegetation Chance: 0.8",
                "  -> XZ Radius:",
                "    -> Uniform:",
                "      -> Range: 1-2",
                "  -> Extra Edge Column Chance: 0.7"
        ));
    }

    @Test
    public void testVinesTooltip() {
        assertTooltip(FEATURES.get("vines"), List.of(
                "Vines:"
        ));
    }

    @Test
    public void testVoidStartPlatformTooltip() {
        assertTooltip(FEATURES.get("void_start_platform"), List.of(
                "Void Start Platform:"
        ));
    }

    @Test
    public void testWaterloggedVegetationPatchTooltip() {
        assertTooltip(FEATURES.get("waterlogged_vegetation_patch"), List.of(
                "Waterlogged Vegetation Patch:",
                "  -> Replaceable:",
                "    -> Tag: minecraft:wool",
                "  -> Ground State:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Clay",
                "  -> Vegetation Feature:",
                "    -> Feature:",
                "      -> No Op:",
                "    -> Placement:",
                "      -> Block Predicate Filter:",
                "        -> Solid:",
                "  -> Surface: Floor",
                "  -> Depth:",
                "    -> Constant:",
                "      -> Value: 1",
                "  -> Extra Bottom Block Chance: 0.0",
                "  -> Vertical Range: 5",
                "  -> Vegetation Chance: 0.8",
                "  -> XZ Radius:",
                "    -> Uniform:",
                "      -> Range: 1-2",
                "  -> Extra Edge Column Chance: 0.7"
        ));
    }

    @Test
    public void testWeightedRandomSelectorTooltip() {
        assertTooltip(FEATURES.get("weighted_random_selector"), List.of(
                "Weighted Random Selector:",
                "  -> Features:",
                "    -> Total Weight: 3",
                "    -> Items:",
                "      -> Weight: 3",
                "      -> Value:",
                "        -> Feature:",
                "          -> No Op:",
                "        -> Placement:",
                "          -> Block Predicate Filter:",
                "            -> Solid:"
        ));
    }
}
