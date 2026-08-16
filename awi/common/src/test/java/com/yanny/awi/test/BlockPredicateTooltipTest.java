package com.yanny.awi.test;

import com.yanny.awi.plugin.server.BlockPredicateTooltipUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.*;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class BlockPredicateTooltipTest {
    @Test
    public void testMatchingBlocksPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getMatchingBlocksPredicateTooltip(UTILS, (MatchingBlocksPredicate) BlockPredicate.matchesBlocks(new Vec3i(1, 0, 0), Blocks.STONE, Blocks.DIRT)).build(), List.of(
                "Matching Blocks:",
                "  -> Blocks:",
                "    -> Stone",
                "    -> Dirt",
                "  -> Offset: [1,0,0]"
        ));
    }

    @Test
    public void testMatchingBlockTagPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getMatchingBlockTagPredicateTooltip(UTILS, (MatchingBlockTagPredicate) BlockPredicate.matchesTag(new Vec3i(0, 1, 0), BlockTags.WOOL)).build(), List.of(
                "Matching Block Tag:",
                "  -> Tag: minecraft:wool",
                "  -> Offset: [0,1,0]"
        ));
    }

    @Test
    public void testMatchingFluidsPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getMatchingFluidsPredicateTooltip(UTILS, (MatchingFluidsPredicate) BlockPredicate.matchesFluids(Fluids.WATER, Fluids.LAVA)).build(), List.of(
                "Matching Fluids:",
                "  -> Fluids:",
                "    -> minecraft:water",
                "    -> minecraft:lava"
        ));
    }

    @Test
    public void testHasSturdyFacePredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getHasSturdyFacePredicateTooltip(UTILS, new HasSturdyFacePredicate(new Vec3i(1, 2, 3), Direction.UP)).build(), List.of(
                "Has Sturdy Face:",
                "  -> Direction: UP",
                "  -> Offset: [1,2,3]"
        ));
    }

    @Test
    public void testSolidPredicateTooltip() {
        //noinspection deprecation
        assertTooltip(BlockPredicateTooltipUtils.getSolidPredicateTooltip(UTILS, new SolidPredicate(new Vec3i(0, -1, 0))).build(), List.of(
                "Solid:",
                "  -> Offset: [0,-1,0]"
        ));
    }

    @Test
    public void testReplaceablePredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getReplaceablePredicateTooltip(UTILS, new ReplaceablePredicate(new Vec3i(0, 1, 0))).build(), List.of(
                "Replaceable:",
                "  -> Offset: [0,1,0]"
        ));
    }

    @Test
    public void testWouldSurvivePredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getWouldSurvivePredicateTooltip(UTILS, (WouldSurvivePredicate) BlockPredicate.wouldSurvive(Blocks.STONE.defaultBlockState(), Vec3i.ZERO)).build(), List.of(
                "Would Survive:",
                "  -> State:",
                "    -> Block: Stone"
        ));
    }

    @Test
    public void testInsideWorldBoundsPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getInsideWorldBoundsPredicateTooltip(UTILS, new InsideWorldBoundsPredicate(new Vec3i(1, 2, 3))).build(), List.of(
                "Inside World Bounds:",
                "  -> Offset: [1,2,3]"
        ));
    }

    @Test
    public void testAnyOfPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getAnyOfPredicateTooltip(UTILS, (AnyOfPredicate) BlockPredicate.anyOf(BlockPredicate.solid(), BlockPredicate.replaceable())).build(), List.of(
                "Any Of:",
                "  -> Predicates:",
                "    -> Solid:",
                "    -> Replaceable:"
        ));
    }

    @Test
    public void testAllOfPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getAllOfPredicateTooltip(UTILS, (AllOfPredicate) BlockPredicate.allOf(BlockPredicate.insideWorld(Vec3i.ZERO), BlockPredicate.hasSturdyFace(Direction.UP))).build(), List.of(
                "All Of:",
                "  -> Predicates:",
                "    -> Inside World Bounds:",
                "    -> Has Sturdy Face:",
                "      -> Direction: UP"
        ));
    }

    @Test
    public void testNotPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getNotPredicateTooltip(UTILS, new NotPredicate(BlockPredicate.solid())).build(), List.of(
                "Not:",
                "  -> Predicate:",
                "    -> Solid:"
        ));
    }

    @Test
    public void testTrueBlockPredicateTooltip() {
        assertTooltip(BlockPredicateTooltipUtils.getTrueBlockPredicateTooltip(UTILS, TrueBlockPredicate.INSTANCE).build(), List.of(
                "True Block"
        ));
    }
}
