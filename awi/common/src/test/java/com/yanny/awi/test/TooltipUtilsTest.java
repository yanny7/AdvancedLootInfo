package com.yanny.awi.test;

import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.plugin.server.TooltipUtils;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.awi.test.TooltipTestSuite.UTILS;

public class TooltipUtilsTest {
    @Test
    public void testRelativeStorageBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(5)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Depth Below Surface: 5"
        ));
    }

    @Test
    public void testAbsoluteStorageBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.ABSOLUTE, List.of(new RangeValue(5), new RangeValue(10)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Absolute Y: 5, 10"
        ));
    }

    @Test
    public void testLayeredStorageSingleRangeBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.LAYERED, List.of(new RangeValue(7)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Layers At Y: 7"
        ));
    }

    @Test
    public void testLayeredStorageMultiRangeBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.LAYERED, List.of(new RangeValue(7), new RangeValue(9)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Layers At Y: 7, 9"
        ));
    }

    @Test
    public void testUnderwaterBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(3)), 0, BlockInfo.WaterConstraint.UNDERWATER, BlockInfo.Placement.FLOOR, List.of()
        )).build(), List.of(
                "Depth Below Surface: 3 (Underwater)"
        ));
    }

    @Test
    public void testDryBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(3)), 0, BlockInfo.WaterConstraint.DRY, BlockInfo.Placement.FLOOR, List.of()
        )).build(), List.of(
                "Depth Below Surface: 3 (On Land)"
        ));
    }

    @Test
    public void testCeilingBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(3)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.CEILING, List.of()
        )).build(), List.of(
                "Depth Below Surface: 3 (On Ceiling)"
        ));
    }

    @Test
    public void testLayerShiftBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.STONE, BlockInfo.StorageType.LAYERED, List.of(new RangeValue(7), new RangeValue(9)), 4, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Layers At Y: 7, 9",
                "Layer Shift: ±4"
        ));
    }

    @Test
    public void testAbsoluteStorageSingleRangeBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.BEDROCK, BlockInfo.StorageType.ABSOLUTE, List.of(new RangeValue(-64, -60)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Absolute Y: -64--60"
        ));
    }

    @Test
    public void testRelativeStorageRangeBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.DIRT, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0, 8)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Depth Below Surface: 0-8"
        ));
    }

    @Test
    public void testRelativeStorageShowsAllRanges() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.DIRT, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0, 2), new RangeValue(4, 5)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Depth Below Surface: 0-2, 4-5"
        ));
    }

    @Test
    public void testUnderwaterCeilingBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.SANDSTONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0, 7)), 0, BlockInfo.WaterConstraint.UNDERWATER, BlockInfo.Placement.CEILING, List.of()
        )).build(), List.of(
                "Depth Below Surface: 0-7 (Underwater, On Ceiling)"
        ));
    }

    @Test
    public void testLayeredDryWithShiftBlockInfoTooltip() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.DYED_TERRACOTTA.red(), BlockInfo.StorageType.LAYERED, List.of(new RangeValue(88, 92), new RangeValue(132, 142)), 4, BlockInfo.WaterConstraint.DRY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Layers At Y: 88-92, 132-142 (On Land)",
                "Layer Shift: ±4"
        ));
    }

    @Test
    public void testLongRangeListWrapsAndQualifiesTheLastLine() {
        List<RangeValue> levels = Stream.of(57, 74, 79, 82, 95, 97, 105, 109, 111, 116, 121, 123, 125, 142, 144, 158, 160, 166).map(RangeValue::new).toList();

        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.DYED_TERRACOTTA.orange(), BlockInfo.StorageType.LAYERED, levels, 4, BlockInfo.WaterConstraint.DRY, BlockInfo.Placement.ANY, List.of()
        )).build(), List.of(
                "Layers At Y:",
                "  57, 74, 79, 82, 95, 97, 105, 109, 111,",
                "  116, 121, 123, 125, 142, 144, 158, 160,",
                "  166 (On Land)",
                "Layer Shift: ±4"
        ));
    }

    @Test
    public void testSeveralEntriesInFixedOrder() {
        assertTooltip(TooltipUtils.getBlockInfosTooltip(UTILS, List.of(
                new BlockInfo(Blocks.SANDSTONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0, 7)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.CEILING, List.of()),
                new BlockInfo(Blocks.SANDSTONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(1, 8)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.FLOOR, List.of()),
                new BlockInfo(Blocks.SANDSTONE, BlockInfo.StorageType.ABSOLUTE, List.of(new RangeValue(-10, 0)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of()),
                new BlockInfo(Blocks.SANDSTONE, BlockInfo.StorageType.LAYERED, List.of(new RangeValue(70)), 4, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of())
        )).build(), List.of(
                "Layers At Y: 70",
                "Layer Shift: ±4",
                "----------",
                "Absolute Y: -10-0",
                "----------",
                "Depth Below Surface: 1-8",
                "----------",
                "Depth Below Surface: 0-7 (On Ceiling)"
        ));
    }

    @Test
    public void testHeightsLineAfterTheDepth() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.DYED_TERRACOTTA.white(), BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0, 4)), 0, BlockInfo.WaterConstraint.UNDERWATER, BlockInfo.Placement.ANY,
                List.of(new RangeValue(-63, 62))
        )).build(), List.of(
                "Depth Below Surface: 0-4 (Underwater)",
                "At Y: -63-62"
        ));
    }

    @Test
    public void testSeveralHeightRanges() {
        assertTooltip(TooltipUtils.getBlockInfoTooltip(UTILS, new BlockInfo(
                Blocks.DYED_TERRACOTTA.orange(), BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0, 8)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY,
                List.of(new RangeValue(-63, 76), new RangeValue(256, 319))
        )).build(), List.of(
                "Depth Below Surface: 0-8",
                "At Y: -63-76, 256-319"
        ));
    }
}
