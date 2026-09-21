package com.yanny.awi.test;

import com.yanny.awi.plugin.server.RuleTestTooltipUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class RuleTestTooltipTest {
    @Test
    public void testAlwaysTrueTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getAlwaysTrueTestTooltip(UTILS, AlwaysTrueTest.INSTANCE).build(), List.of(
                "Always True"
        ));
    }

    @Test
    public void testBlockMatchTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getBlockMatchTestTooltip(UTILS, new BlockMatchTest(Blocks.STONE)).build(), List.of(
                "Block Match:",
                "  -> Block: Stone"
        ));
    }

    @Test
    public void testBlockStateMatchTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getBlockStateMatchTestTooltip(UTILS, new BlockStateMatchTest(Blocks.STONE.defaultBlockState())).build(), List.of(
                "Block State Match:",
                "  -> State:",
                "    -> Block: Stone"
        ));
    }

    @Test
    public void testTagMatchTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getTagMatchTestTooltip(UTILS, new TagMatchTest(BlockTags.WOOL)).build(), List.of(
                "Tag Match:",
                "  -> Tag: minecraft:wool"
        ));
    }

    @Test
    public void testRandomBlockMatchTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getRandomBlockMatchTestTooltip(UTILS, new RandomBlockMatchTest(Blocks.STONE, 0.5f)).build(), List.of(
                "Random Block Match:",
                "  -> Block: Stone",
                "  -> Probability: 0.5"
        ));
    }

    @Test
    public void testRandomBlockStateMatchTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getRandomBlockStateMatchTestTooltip(UTILS, new RandomBlockStateMatchTest(Blocks.STONE.defaultBlockState(), 0.5f)).build(), List.of(
                "Random Block State Match:",
                "  -> State:",
                "    -> Block: Stone",
                "  -> Probability: 0.5"
        ));
    }

    @Test
    public void testAllOfRuleTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getAllOfRuleTestTooltip(UTILS, new AllOfRuleTest(List.of(new BlockMatchTest(Blocks.STONE), new TagMatchTest(BlockTags.WOOL)))).build(), List.of(
                "All Of:",
                "  -> Rules:",
                "    -> Block Match:",
                "      -> Block: Stone",
                "    -> Tag Match:",
                "      -> Tag: minecraft:wool"
        ));
    }

    @Test
    public void testAnyOfRuleTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getAnyOfRuleTestTooltip(UTILS, new AnyOfRuleTest(List.of(new BlockMatchTest(Blocks.STONE), new TagMatchTest(BlockTags.WOOL)))).build(), List.of(
                "Any Of:",
                "  -> Rules:",
                "    -> Block Match:",
                "      -> Block: Stone",
                "    -> Tag Match:",
                "      -> Tag: minecraft:wool"
        ));
    }

    @Test
    public void testNotRuleTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getNotRuleTestTooltip(UTILS, new NotRuleTest(new BlockMatchTest(Blocks.STONE))).build(), List.of(
                "Not:",
                "  -> Rule:",
                "    -> Block Match:",
                "      -> Block: Stone"
        ));
    }

    @Test
    public void testHeightMatchTestTooltip() {
        assertTooltip(RuleTestTooltipUtils.getHeightMatchTestTooltip(UTILS, new HeightMatchTest(-16, 48)).build(), List.of(
                "Height Match:",
                "  -> Min: -16",
                "  -> Max: 48"
        ));
    }
}
