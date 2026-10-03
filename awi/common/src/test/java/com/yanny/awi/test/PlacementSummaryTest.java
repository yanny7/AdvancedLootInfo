package com.yanny.awi.test;

import com.mojang.serialization.MapCodec;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.language.Lang;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.HeightFunctions;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import com.yanny.awi.plugin.server.summary.PlacementContribution;
import com.yanny.awi.plugin.server.summary.PlacementSummary;
import com.yanny.awi.plugin.server.summary.PlacementSummaryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.*;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.feature.CuboidPlacement;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static com.yanny.aci.api.NumberExpr.*;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.*;

public class PlacementSummaryTest {
    private static final ColumnContext CTX = new ColumnContext(-64, 384);

    @Test
    public void testIntProviderConverters() {
        assertEquals(constant(3), UTILS.convertIntProvider(UTILS, ConstantInt.of(3), List.of()));
        assertEquals(uniformInt(2, 6), UTILS.convertIntProvider(UTILS, UniformInt.of(2, 6), List.of()));
        assertEquals(fn(NumberFunctions.BIASED_TO_BOTTOM, constant(0), constant(10)), UTILS.convertIntProvider(UTILS, BiasedToBottomInt.of(0, 10), List.of()));
        assertEquals(clamp(uniformInt(1, 10), constant(3), constant(7)), UTILS.convertIntProvider(UTILS, ClampedInt.of(UniformInt.of(1, 10), 3, 7), List.of()));
        assertEquals(
                fn(NumberFunctions.TRUNC, clamp(fn(NumberFunctions.NORMAL, constant(5), constant(2)), constant(0), constant(10))),
                UTILS.convertIntProvider(UTILS, ClampedNormalInt.of(5, 2, 0, 10), List.of())
        );
    }

    @Test
    public void testWeightedListIntRecurses() {
        WeightedList<IntProvider> distribution = WeightedList.<IntProvider>builder()
                .add(UniformInt.of(1, 2), 1)
                .add(ConstantInt.of(8), 3)
                .build();

        assertEquals(
                weighted(List.of(new WeightedEntry(1, uniformInt(1, 2)), new WeightedEntry(3, constant(8)))),
                UTILS.convertIntProvider(UTILS, new WeightedListInt(distribution), List.of())
        );
    }

    @Test
    public void testFloatProviderConverters() {
        assertEquals(constant(2), UTILS.convertFloatProvider(UTILS, ConstantFloat.of(2), List.of()));
        assertEquals(uniformFloat(1, 3), UTILS.convertFloatProvider(UTILS, UniformFloat.of(1, 3), List.of()));
        assertEquals(
                clamp(fn(NumberFunctions.NORMAL, constant(4.5), constant(1)), constant(2), constant(7)),
                UTILS.convertFloatProvider(UTILS, ClampedNormalFloat.of(4.5f, 1, 2, 7), List.of())
        );
        assertEquals(
                fn(NumberFunctions.TRAPEZOID_FLOAT, constant(1), constant(9), constant(2)),
                UTILS.convertFloatProvider(UTILS, TrapezoidFloat.of(1, 9, 2), List.of())
        );
    }

    @Test
    public void testHeightConverters() {
        assertEquals(constant(5), UTILS.convertHeightProvider(UTILS, ConstantHeight.of(VerticalAnchor.absolute(5)), CTX, List.of()));
        assertEquals(uniformInt(-64, 319), UTILS.convertHeightProvider(UTILS, UniformHeight.of(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0)), CTX, List.of()));
        assertEquals(HeightFunctions.biasedToBottom(10, 60, 1), UTILS.convertHeightProvider(UTILS, BiasedToBottomHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60), 1), CTX, List.of()));
        assertEquals(HeightFunctions.veryBiasedToBottom(10, 60, 8), UTILS.convertHeightProvider(UTILS, VeryBiasedToBottomHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60), 8), CTX, List.of()));
        assertEquals(HeightFunctions.trapezoid(40, 120, 20), UTILS.convertHeightProvider(UTILS, TrapezoidHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(120), 20), CTX, List.of()));
    }

    @Test
    public void testHeightFunctionsMatchVanillaBounds() {
        assertEquals(NumberInterval.closed(10, 59), HeightFunctions.biasedToBottom(10, 60, 1).bounds());
        assertEquals(NumberInterval.closed(40, 120), HeightFunctions.trapezoid(40, 120, 20).bounds());
        assertEquals(NumberInterval.closed(40, 120), HeightFunctions.trapezoid(40, 120, 100).bounds());
        assertTrue(HeightFunctions.trapezoid(40, 120, 20).mode().isPresent());
    }

    @Test
    public void testWeightedListHeightRecurses() {
        WeightedList<HeightProvider> distribution = WeightedList.<HeightProvider>builder()
                .add(ConstantHeight.of(VerticalAnchor.absolute(10)), 1)
                .add(ConstantHeight.of(VerticalAnchor.absolute(100)), 5)
                .build();

        assertEquals(
                weighted(List.of(new WeightedEntry(1, constant(10)), new WeightedEntry(5, constant(100)))),
                UTILS.convertHeightProvider(UTILS, new WeightedListHeight(distribution), CTX, List.of())
        );
    }

    @Test
    public void testWorldDependentCountIsOpaqueWithDetails() {
        //noinspection deprecation
        PlacementContribution everyLayer = UTILS.getPlacementContribution(UTILS, CountOnEveryLayerPlacement.of(5), CTX);
        PlacementContribution noise = UTILS.getPlacementContribution(UTILS, NoiseBasedCountPlacement.of(5, 1.5, 0.2), CTX);
        PlacementContribution threshold = UTILS.getPlacementContribution(UTILS, NoiseThresholdCountPlacement.of(0.5, 2, 8), CTX);

        assertEquals(opaque("minecraft:count_on_every_layer"), everyLayer.count());
        assertEquals(opaque("minecraft:noise_based_count"), noise.count());
        assertEquals(opaque("minecraft:noise_threshold_count"), threshold.count());
        assertNotNull(noise.countDetails());
    }

    @Test
    public void testHeightmapPlacementFallsBackToHeightmap() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR), CTX);

        assertNull(contribution.count());
        assertNull(contribution.chance());
        assertNull(contribution.height().height());
        assertEquals(Heightmap.Types.OCEAN_FLOOR, contribution.height().heightmap());
    }

    @Test
    public void testChancesCompound() {
        PlacementSummary summary = PlacementSummaryUtils.summarize(UTILS, List.of(RarityFilter.onAverageOnceEvery(4), RarityFilter.onAverageOnceEvery(5)), CTX);

        assertEquals(constant(0.05), summary.chance());
    }

    @Test
    public void testFirstCountAndHeightWin() {
        PlacementSummary summary = PlacementSummaryUtils.summarize(UTILS, List.of(
                CountPlacement.of(4),
                CountPlacement.of(9),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10)),
                HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR)
        ), CTX);

        assertEquals(constant(4), summary.count());
        assertEquals(uniformInt(0, 10), summary.height().height());
    }

    @Test
    public void testSummaryTooltip() {
        List<PlacementModifier> modifiers = List.of(
                CountPlacement.of(4),
                RarityFilter.onAverageOnceEvery(10),
                HeightRangePlacement.of(TrapezoidHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(120), 20))
        );

        assertTooltip(renderSummary(modifiers), List.of(
                "Attempts Per Chunk: 4",
                "Chance: 10%",
                "Height: 40 to 120  ~70 to 90 (2%)"
        ));
    }

    @Test
    public void testSummaryTooltipUniformCount() {
        assertTooltip(renderSummary(List.of(CountPlacement.of(UniformInt.of(2, 6)))), List.of(
                "Attempts Per Chunk: 2 to 6"
        ));
    }

    @Test
    public void testSummaryTooltipBiasedHeight() {
        List<PlacementModifier> modifiers = List.of(HeightRangePlacement.of(BiasedToBottomHeight.of(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16), 1)));

        assertTooltip(renderSummary(modifiers), List.of(
                "Height: −64 to 15  ~−64 (6%)"
        ));
    }

    @Test
    public void testSummaryTooltipHeightmapFallback() {
        assertTooltip(renderSummary(List.of(HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR))), List.of(
                "Height: Solid Ground, Ignores Water"
        ));
    }

    @Test
    public void testSummaryTooltipUnknownCountKeepsDetails() {
        assertTooltip(renderSummary(List.of(NoiseBasedCountPlacement.of(5, 1.5, 0.2))), List.of(
                "Attempts Per Chunk: ? (minecraft:noise_based_count)",
                "  -> Noise Based Count Placement:",
                "    -> Noise To Count Ratio: 5",
                "    -> Noise Factor: 1.5",
                "    -> Noise Offset: 0.2"
        ));
    }

    @Test
    public void testFailingPropagatorDropsOnlyTheSummary() {
        PluginManager.getInstance().serverRegistry.registerPlacementPropagator(BrokenPlacement.class, (u, m, c) -> {
            throw new IllegalStateException("broken propagator");
        });

        assertTooltip(renderSummary(List.of(new BrokenPlacement(), CountPlacement.of(3))), List.of());
    }

    @Test
    public void testConditionalChancesKeepTheirConditions() {
        TooltipNode first = TooltipBuilder.keyOnly(Lang.Value.CHANCE).build();
        TooltipNode second = TooltipBuilder.keyOnly(Lang.Value.HEIGHT).build();

        PluginManager.getInstance().serverRegistry.registerPlacementPropagator(ConditionalPlacement.class, (u, m, c) -> {
            NumberExpr chance = cond(List.of(new Branch(0, constant(m.chance))), constant(1));

            return PlacementContribution.ofChance(chance, List.of(m.condition));
        });

        PlacementSummary summary = PlacementSummaryUtils.summarize(UTILS, List.of(new ConditionalPlacement(0.5, first), new ConditionalPlacement(0.25, second)), CTX);

        assertEquals(mul(cond(List.of(new Branch(0, constant(0.5))), constant(1)), cond(List.of(new Branch(1, constant(0.25))), constant(1))), summary.chance());
        assertEquals(List.of(first, second), summary.chanceConditions());
    }

    private static TooltipNode renderSummary(List<PlacementModifier> modifiers) {
        return TooltipBuilder.branch((b) -> PlacementSummaryUtils.appendSummary(b, UTILS, modifiers, CTX)).build();
    }

    @Test
    public void testCuboidPlacementContributionIsOpaque() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, new CuboidPlacement(UniformInt.of(3, 5), ConstantInt.of(2), false, false), CTX);
        assertInstanceOf(Opaque.class, contribution.count());
        assertNotNull(contribution.countDetails());
    }

    @Test
    public void testRandomChancePlacementContribution() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, new RandomChancePlacement(0.9F), CTX);
        assertNull(contribution.count());
        assertNull(contribution.height());
        assertEquals(constant(0.9F), contribution.chance());
    }

    @Test
    public void testRandomlySelectedPlacementContributesNothingForOffsetsOnly() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, new RandomlySelectedPlacement(List.<PlacementModifier>of(
                OffsetPlacement.of(0, -1, 0),
                OffsetPlacement.of(-1, -1, 0)
        )), CTX);
        assertNull(contribution.count());
        assertNull(contribution.chance());
        assertNull(contribution.height());
    }

    @Test
    public void testRandomlySelectedPlacementWeightsNestedCounts() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, new RandomlySelectedPlacement(List.<PlacementModifier>of(
                CountPlacement.of(3),
                CountPlacement.of(UniformInt.of(6, 8))
        )), CTX);
        assertEquals(weighted(List.of(new WeightedEntry(1, constant(3)), new WeightedEntry(1, uniformInt(6, 8)))), contribution.count());
    }

    private static class BrokenPlacement implements PlacementModifier {
        @Override
        public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
            output.accept(origin);
        }

        @NotNull
        @Override
        public MapCodec<? extends PlacementModifier> codec() {
            return CountPlacement.CODEC;
        }
    }

    private static class ConditionalPlacement implements PlacementModifier {
        private final double chance;
        private final TooltipNode condition;

        private ConditionalPlacement(double chance, TooltipNode condition) {
            this.chance = chance;
            this.condition = condition;
        }

        @Override
        public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
            output.accept(origin);
        }

        @NotNull
        @Override
        public MapCodec<? extends PlacementModifier> codec() {
            return RarityFilter.CODEC;
        }
    }
}
