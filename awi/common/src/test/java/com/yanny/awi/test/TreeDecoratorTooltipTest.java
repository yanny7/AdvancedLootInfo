package com.yanny.awi.test;

import com.yanny.awi.plugin.server.TreeDecoratorTooltipUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class TreeDecoratorTooltipTest {
    @Test
    public void testTrunkVineDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getTrunkVineDecoratorTooltip(UTILS, TrunkVineDecorator.INSTANCE).build(), List.of(
                "Trunk Vine"
        ));
    }

    @Test
    public void testLeaveVineDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getLeaveVineDecoratorTooltip(UTILS, new LeaveVineDecorator(0.25f)).build(), List.of(
                "Leave Vine",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testPaleMossDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getPaleMossDecoratorTooltip(UTILS, new PaleMossDecorator(0.25f, 0.35f, 0.45f)).build(), List.of(
                "Pale Moss:",
                "  -> Leaves Probability: 0.25",
                "  -> Trunk Probability: 0.35",
                "  -> Ground Probability: 0.45"
        ));
    }

    @Test
    public void testCreakingHeartDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getCreakingHeartDecoratorTooltip(UTILS, new CreakingHeartDecorator(0.25f)).build(), List.of(
                "Creaking Heart:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testCocoaDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getCocoaDecoratorTooltip(UTILS, new CocoaDecorator(0.2f)).build(), List.of(
                "Cocoa:",
                "  -> Probability: 0.2"
        ));
    }

    @Test
    public void testBeehiveDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getBeehiveDecoratorTooltip(UTILS, new BeehiveDecorator(0.05f)).build(), List.of(
                "Beehive:",
                "  -> Probability: 0.05"
        ));
    }

    @Test
    public void testAlterGroundDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getAlterGroundDecoratorTooltip(UTILS, new AlterGroundDecorator(BlockStateProvider.simple(Blocks.PODZOL))).build(), List.of(
                "Alter Ground:",
                "  -> Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Podzol",
                "        -> Properties:",
                "          -> snowy: false"
        ));
    }

    @Test
    public void testAttachedToLeavesDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getAttachedToLeavesDecoratorTooltip(UTILS, new AttachedToLeavesDecorator(
                0.1f,
                2,
                1,
                BlockStateProvider.simple(Blocks.CHERRY_LEAVES),
                2,
                List.of(Direction.UP, Direction.DOWN)
        )).build(), List.of(
                "Attached To Leaves:",
                "  -> Probability: 0.1",
                "  -> Exclusion Radius XZ: 2",
                "  -> Exclusion Radius Y: 1",
                "  -> Block Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Cherry Leaves",
                "        -> Properties:",
                "          -> distance: 7",
                "          -> persistent: false",
                "          -> waterlogged: false",
                "  -> Required Empty Blocks: 2",
                "  -> Directions:",
                "    -> Up",
                "    -> Down"
        ));
    }

    @Test
    public void testPlaceOnGroundDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getPlaceOnGroundDecoratorTooltip(UTILS, new PlaceOnGroundDecorator(
                3,
                2,
                1,
                BlockStateProvider.simple(Blocks.CHERRY_LEAVES)
        )).build(), List.of(
                "Place On Ground:",
                "  -> Tries: 3",
                "  -> Radius: 2",
                "  -> Height: 1",
                "  -> Block Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Cherry Leaves",
                "        -> Properties:",
                "          -> distance: 7",
                "          -> persistent: false",
                "          -> waterlogged: false"
        ));
    }

    @Test
    public void testAttachedToLogsDecoratorTooltip() {
        assertTooltip(TreeDecoratorTooltipUtils.getAttachedToLogsDecoratorTooltip(UTILS, new AttachedToLogsDecorator(
                0.1f,
                BlockStateProvider.simple(Blocks.CHERRY_LEAVES),
                List.of(Direction.UP, Direction.DOWN)
        )).build(), List.of(
                "Attached To Logs:",
                "  -> Probability: 0.1",
                "  -> Block Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Cherry Leaves",
                "        -> Properties:",
                "          -> distance: 7",
                "          -> persistent: false",
                "          -> waterlogged: false",
                "  -> Directions:",
                "    -> UP",
                "    -> DOWN"
        ));
    }
}
