package com.yanny.awi.test;

import com.yanny.awi.plugin.server.TrunkPlacerTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.trunkplacers.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class TrunkPlacerTooltipTest {
    @Test
    public void testStraightTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getStraightTrunkPlacerTooltip(UTILS, new StraightTrunkPlacer(5, 2, 1)).build(), List.of(
                "Straight Trunk:",
                "  -> Base Height: 5",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1"
        ));
    }

    @Test
    public void testForkingTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getForkingTrunkPlacerTooltip(UTILS, new ForkingTrunkPlacer(5, 2, 1)).build(), List.of(
                "Forking Trunk:",
                "  -> Base Height: 5",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1"
        ));
    }

    @Test
    public void testGiantTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getGiantTrunkPlacerTooltip(UTILS, new GiantTrunkPlacer(13, 2, 5)).build(), List.of(
                "Giant Trunk:",
                "  -> Base Height: 13",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 5"
        ));
    }

    @Test
    public void testMegaJungleTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getMegaJungleTrunkPlacerTooltip(UTILS, new MegaJungleTrunkPlacer(10, 2, 19)).build(), List.of(
                "Mega Jungle:",
                "  -> Base Height: 10",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 19"
        ));
    }

    @Test
    public void testDarkOakTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getDarkOakTrunkPlacerTooltip(UTILS, new DarkOakTrunkPlacer(6, 2, 1)).build(), List.of(
                "Dark Oak:",
                "  -> Base Height: 6",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1"
        ));
    }

    @Test
    public void testFancyTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getFancyTrunkPlacerTooltip(UTILS, new FancyTrunkPlacer(5, 2, 1)).build(), List.of(
                "Fancy Trunk:",
                "  -> Base Height: 5",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1"
        ));
    }

    @Test
    public void testBendingTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getBendingTrunkPlacerTooltip(UTILS, new BendingTrunkPlacer(5, 2, 1, 3, ConstantInt.of(2))).build(), List.of(
                "Bending Trunk:",
                "  -> Base Height: 5",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1",
                "  -> Min Height For Leaves: 3",
                "  -> Bend Length:",
                "    -> Constant:",
                "      -> Value: 2"
        ));
    }

    @Test
    public void testUpwardBranchingTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getUpwardBranchingTrunkPlacerTooltip(UTILS, new UpwardsBranchingTrunkPlacer(
                5, 2, 1,
                ConstantInt.of(2),
                0.25f,
                ConstantInt.of(3),
                HolderSet.direct(Holder.direct(Blocks.OAK_LOG), Holder.direct(Blocks.SPRUCE_LOG))
        )).build(), List.of(
                "Upward Branching Trunk:",
                "  -> Base Height: 5",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1",
                "  -> Extra Branch Steps:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Branch Per-Log Chance: 0.25",
                "  -> Extra Branch Length:",
                "    -> Constant:",
                "      -> Value: 3",
                "  -> Can Grow Through:",
                "    -> Oak Log",
                "    -> Spruce Log"
        ));
    }

    @Test
    public void testCherryTrunkPlacerTooltip() {
        assertTooltip(TrunkPlacerTooltipUtils.getCherryTrunkPlacerTooltip(UTILS, new CherryTrunkPlacer(
                5, 2, 1,
                ConstantInt.of(4),
                ConstantInt.of(2),
                UniformInt.of(2, 4),
                ConstantInt.of(1)
        )).build(), List.of(
                "Cherry:",
                "  -> Base Height: 5",
                "  -> Height Rand A: 2",
                "  -> Height Rand B: 1",
                "  -> Branch Count:",
                "    -> Constant:",
                "      -> Value: 4",
                "  -> Branch Horizontal Length:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Branch Start Offset From Top:",
                "    -> Uniform:",
                "      -> Range: 2-4",
                "  -> Second Branch Start Offset From Top:",
                "    -> Uniform:",
                "      -> Range: 2-3",
                "  -> Branch End Offset From Top:",
                "    -> Constant:",
                "      -> Value: 1"
        ));
    }
}
