package com.yanny.awi.test;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.test.utils.TestUtils;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.plugin.common.nodes.BaseTerrainNode;
import com.yanny.awi.plugin.common.nodes.BlockNode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseTerrainNodeTest {
    private static final BlockInfo ORANGE_BANDS = new BlockInfo(Blocks.ORANGE_TERRACOTTA, BlockInfo.StorageType.LAYERED,
            List.of(new RangeValue(57), new RangeValue(74)), 4, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of());
    private static final BlockInfo ORANGE_SURFACE = new BlockInfo(Blocks.ORANGE_TERRACOTTA, BlockInfo.StorageType.RELATIVE,
            List.of(new RangeValue(0, 8)), 0, BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of());
    private static final BlockInfo RED_SAND = new BlockInfo(Blocks.RED_SAND, BlockInfo.StorageType.RELATIVE,
            List.of(new RangeValue(0)), 0, BlockInfo.WaterConstraint.DRY, BlockInfo.Placement.FLOOR, List.of());

    @Test
    public void testNodeTooltip() {
        assertTooltip(new BaseTerrainNode(UTILS, Set.of(), Blocks.STONE, Fluids.WATER).getTooltip(), List.of(
                "Generation Step: Base Terrain"
        ));
    }

    @Test
    public void testOneSlotPerBlockAfterDefaults() {
        List<BlockNode> nodes = blockNodes(new BaseTerrainNode(UTILS, Set.of(RED_SAND, ORANGE_SURFACE, ORANGE_BANDS), Blocks.STONE, Fluids.WATER));

        assertEquals(List.of(Blocks.STONE, Blocks.WATER, Blocks.ORANGE_TERRACOTTA, Blocks.RED_SAND),
                nodes.stream().map(BaseTerrainNodeTest::block).toList());
        assertTooltip(nodes.get(0).getTooltip(), List.of("Default Block"));
        assertTooltip(nodes.get(1).getTooltip(), List.of("Default Fluid"));
        assertTooltip(nodes.get(3).getTooltip(), List.of("Depth Below Surface: 0 (On Land)"));
    }

    @Test
    public void testSameBlockEntriesShareOneSlot() {
        List<BlockNode> nodes = blockNodes(new BaseTerrainNode(UTILS, Set.of(ORANGE_SURFACE, ORANGE_BANDS), Blocks.AIR, Fluids.EMPTY));

        assertEquals(List.of(Blocks.ORANGE_TERRACOTTA), nodes.stream().map(BaseTerrainNodeTest::block).toList());
        assertEquals(List.of(
                "Layers At Y: 57, 74",
                "Layer Shift: ±4",
                "----------",
                "Depth Below Surface: 0-8"
        ), lines(nodes.get(0)));
    }

    @Test
    public void testDetectedDefaultsAreNotAddedTwice() {
        BlockInfo stone = new BlockInfo(Blocks.STONE, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0)), 0,
                BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of());
        BlockInfo water = new BlockInfo(Blocks.WATER, BlockInfo.StorageType.RELATIVE, List.of(new RangeValue(0)), 0,
                BlockInfo.WaterConstraint.ANY, BlockInfo.Placement.ANY, List.of());
        List<BlockNode> nodes = blockNodes(new BaseTerrainNode(UTILS, Set.of(stone, water), Blocks.STONE, Fluids.WATER));

        assertEquals(List.of(Blocks.STONE, Blocks.WATER), nodes.stream().map(BaseTerrainNodeTest::block).toList());
        nodes.forEach((node) -> assertTooltip(node.getTooltip(), List.of("Depth Below Surface: 0")));
    }

    @Test
    public void testAirAndEmptyFluidAddNothing() {
        List<BlockNode> nodes = blockNodes(new BaseTerrainNode(UTILS, Set.of(RED_SAND), Blocks.AIR, Fluids.EMPTY));

        assertEquals(List.of(Blocks.RED_SAND), nodes.stream().map(BaseTerrainNodeTest::block).toList());
    }

    private static List<BlockNode> blockNodes(BaseTerrainNode node) {
        return node.nodes().stream().map((IDataNode child) -> (BlockNode) child).toList();
    }

    private static List<String> lines(BlockNode node) {
        return CoreTooltipUtils.toComponents(node.getTooltip(), 0, true, TooltipStyle.DEFAULT).stream()
                .map(TestUtils::componentToPlainString).toList();
    }

    private static Block block(BlockNode node) {
        return node.getBlock().left().orElseThrow();
    }
}
