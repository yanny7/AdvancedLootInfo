package com.yanny.awi.test;

import com.yanny.awi.plugin.server.BlockStateProviderTooltipUtils;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class BlockStateProviderTooltipTest {
    @Test
    public void testSimpleStateProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getSimpleStateProviderTooltip(UTILS, BlockStateProvider.simple(Blocks.STONE)).build(), List.of(
                "Simple:",
                "  -> State:",
                "    -> Block: Stone"
        ));
    }

    @Test
    public void testWeightedStateProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getWeightedStateProviderTooltip(UTILS, new WeightedStateProvider(
                WeightedList.<net.minecraft.world.level.block.state.BlockState>builder()
                        .add(Blocks.STONE.defaultBlockState(), 2)
                        .add(Blocks.DIRT.defaultBlockState(), 1)
        )).build(), List.of(
                "Weighted:",
                "  -> Weighted List:",
                "    -> Total Weight: 3",
                "    -> Items:",
                "      -> Entry:",
                "        -> Weight: 2",
                "        -> Value:",
                "          -> Block: Stone",
                "      -> Entry:",
                "        -> Weight: 1",
                "        -> Value:",
                "          -> Block: Dirt"
        ));
    }

    @Test
    public void testNoiseThresholdProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getNoiseThresholdProviderTooltip(UTILS, new NoiseThresholdProvider(
                1L,
                new NormalNoise.NoiseParameters(-3, List.of(1.0)),
                1.0f,
                0.5f,
                0.25f,
                Blocks.DIRT.defaultBlockState(),
                List.of(Blocks.STONE.defaultBlockState()),
                List.of(Blocks.GRANITE.defaultBlockState())
        )).build(), List.of(
                "Noise Threshold:",
                "  -> Threshold: 0.5",
                "  -> High Chance: 0.25",
                "  -> Default State:",
                "    -> Block: Dirt",
                "  -> Low States:",
                "    -> Block: Stone",
                "  -> High States:",
                "    -> Block: Granite"
        ));
    }

    @Test
    public void testNoiseProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getNoiseProviderTooltip(UTILS, new NoiseProvider(
                1L,
                new NormalNoise.NoiseParameters(-3, List.of(1.0)),
                1.0f,
                List.of(Blocks.STONE.defaultBlockState(), Blocks.DIRT.defaultBlockState())
        )).build(), List.of(
                "Noise Provider:",
                "  -> States:",
                "    -> Block: Stone",
                "    -> Block: Dirt"
        ));
    }

    @Test
    public void testDualNoiseProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getDualNoiseProviderTooltip(UTILS, new DualNoiseProvider(
                new net.minecraft.util.InclusiveRange<>(1, 4),
                new NormalNoise.NoiseParameters(-3, List.of(1.0)),
                1.0f,
                1L,
                new NormalNoise.NoiseParameters(-3, List.of(1.0)),
                1.0f,
                List.of(Blocks.STONE.defaultBlockState())
        )).build(), List.of(
                "Dual Noise Provider:",
                "  -> States:",
                "    -> Block: Stone"
        ));
    }

    @Test
    public void testRotatedBlockProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getRotatedBlockProviderTooltip(UTILS, new RotatedBlockProvider(Blocks.STONE)).build(), List.of(
                "Rotated Block:",
                "  -> Block: Stone"
        ));
    }

    @Test
    public void testRandomizedIntStateProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getRandomizedIntStateProviderTooltip(UTILS, new RandomizedIntStateProvider(
                BlockStateProvider.simple(Blocks.STONE),
                "age",
                net.minecraft.util.valueproviders.ConstantInt.of(1)
        )).build(), List.of(
                "Randomized Int State:",
                "  -> Source:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Stone",
                "  -> Property Name: age",
                "  -> Values:",
                "    -> Constant:",
                "      -> Value: 1"
        ));
    }

    @Test
    public void testRuleBasedStateProviderTooltip() {
        assertTooltip(BlockStateProviderTooltipUtils.getRuleBasedStateProviderTooltip(UTILS, RuleBasedStateProvider.ifTrueThenProvide(
                BlockPredicate.matchesBlocks(Blocks.DIRT),
                Blocks.DIAMOND_BLOCK
        )).build(), List.of(
                "Rule Based:",
                "  -> Rules:",
                "    -> If True:",
                "      -> Matching Blocks:",
                "        -> Block: Dirt",
                "    -> Then:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Block of Diamond"
        ));
    }
}
