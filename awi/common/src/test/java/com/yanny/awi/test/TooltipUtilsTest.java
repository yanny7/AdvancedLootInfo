package com.yanny.awi.test;

import com.yanny.aci.api.RangeValue;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.plugin.server.TooltipUtils;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class TooltipUtilsTest {
    @Test
    public void testRelativeStorageBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.RELATIVE, List.of(new RangeValue(5)), NodeUtils.WaterConstraint.ANY, NodeUtils.Placement.ANY
        )).build(), List.of(
                "Depth Below Surface: 5"
        ));
    }

    @Test
    public void testAbsoluteStorageBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.ABSOLUTE, List.of(new RangeValue(5), new RangeValue(10)), NodeUtils.WaterConstraint.ANY, NodeUtils.Placement.ANY
        )).build(), List.of(
                "Absolute Y:",
                "  -> 5",
                "  -> 10"
        ));
    }

    @Test
    public void testLayeredStorageSingleRangeBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.LAYERED, List.of(new RangeValue(7)), NodeUtils.WaterConstraint.ANY, NodeUtils.Placement.ANY
        )).build(), List.of(
                "Layer At Y: 7"
        ));
    }

    @Test
    public void testLayeredStorageMultiRangeBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.LAYERED, List.of(new RangeValue(7), new RangeValue(9)), NodeUtils.WaterConstraint.ANY, NodeUtils.Placement.ANY
        )).build(), List.of(
                "Layers At Y:",
                "  -> 7",
                "  -> 9"
        ));
    }

    @Test
    public void testUnderwaterBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.RELATIVE, List.of(new RangeValue(3)), NodeUtils.WaterConstraint.UNDERWATER, NodeUtils.Placement.FLOOR
        )).build(), List.of(
                "Depth Below Surface: 3",
                "Placement: Underwater"
        ));
    }

    @Test
    public void testDryBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.RELATIVE, List.of(new RangeValue(3)), NodeUtils.WaterConstraint.DRY, NodeUtils.Placement.FLOOR
        )).build(), List.of(
                "Depth Below Surface: 3",
                "Placement: On Land"
        ));
    }

    @Test
    public void testCeilingBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new NodeUtils.BlockInfo(
                Blocks.STONE, NodeUtils.StorageType.RELATIVE, List.of(new RangeValue(3)), NodeUtils.WaterConstraint.ANY, NodeUtils.Placement.CEILING
        )).build(), List.of(
                "Depth Below Surface: 3",
                "Placement: On Ceiling"
        ));
    }
}
