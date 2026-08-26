package com.yanny.awi.test;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.plugin.server.summary.*;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.*;
import net.minecraft.world.level.levelgen.placement.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.*;

public class PlacementSummaryTest {
    // overworld-like bounds: minY = -64, height (genDepth) = 384  ⇒  top = 319
    private static final ColumnContext CTX = new ColumnContext(-64, 384);

    @Test
    public void testConstantIntSpan() {
        CountSpan span = UTILS.getIntSpan(UTILS, ConstantInt.of(3));
        assertEquals(Kind.CONSTANT, span.kind());
        assertEquals("3", span.range().toIntString());
    }

    @Test
    public void testUniformIntSpan() {
        CountSpan span = UTILS.getIntSpan(UTILS, UniformInt.of(2, 6));
        assertEquals(Kind.UNIFORM, span.kind());
        assertEquals("2-6", span.range().toIntString());
    }

    @Test
    public void testClampedIntSpanRecurses() {
        // source uniform [1,10] clamped to [3,7]
        CountSpan span = UTILS.getIntSpan(UTILS, ClampedInt.of(UniformInt.of(1, 10), 3, 7));
        assertEquals(Kind.CLAMPED, span.kind());
        assertEquals("3-7", span.range().toIntString());
    }

    @Test
    public void testWeightedListIntSpanRecurses() {
        SimpleWeightedRandomList<IntProvider> distribution = SimpleWeightedRandomList.<IntProvider>builder()
                .add(ConstantInt.of(2), 1)
                .add(ConstantInt.of(8), 3)
                .build();
        CountSpan span = UTILS.getIntSpan(UTILS, new WeightedListInt(distribution));
        assertEquals(Kind.WEIGHTED, span.kind());
        assertEquals("2-8", span.range().toIntString()); // union of branch ranges
    }

    @Test
    public void testConstantHeightSpan() {
        HeightSpan span = UTILS.getHeightSpan(UTILS, ConstantHeight.of(VerticalAnchor.absolute(5)), CTX);
        assertEquals(Kind.CONSTANT, span.kind());
        assertEquals("5", span.range().toIntString());
        assertEquals("5", span.bestBand().toIntString());
    }

    @Test
    public void testUniformHeightSpanIsFlatBand() {
        HeightSpan span = UTILS.getHeightSpan(UTILS, UniformHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112)), CTX);
        assertEquals(Kind.UNIFORM, span.kind());
        assertEquals("-16-112", span.range().toIntString());
        assertEquals("-16-112", span.bestBand().toIntString()); // flat ⇒ whole range is the best band
    }

    @Test
    public void testTrapezoidHeightSpanBandIsPlateau() {
        HeightSpan span = UTILS.getHeightSpan(UTILS, TrapezoidHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(120), 20), CTX);
        assertEquals(Kind.TRAPEZOID, span.kind());
        assertEquals("40-120", span.range().toIntString());
        assertEquals("70-90", span.bestBand().toIntString()); // plateau band centered at 80, width 20
    }

    @Test
    public void testBiasedToBottomHeightSpanBandIsMode() {
        HeightSpan span = UTILS.getHeightSpan(UTILS, BiasedToBottomHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60), 1), CTX);
        assertEquals(Kind.BIASED_TO_BOTTOM, span.kind());
        assertEquals("10-60", span.range().toIntString());
        assertEquals("10", span.bestBand().toIntString()); // peaked ⇒ single mode at bottom
    }

    @Test
    public void testHeightAnchorsResolveAgainstColumnContext() {
        // aboveBottom(0) -> -64 ; belowTop(0) -> -64 + 384 - 1 = 319
        HeightSpan span = UTILS.getHeightSpan(UTILS, UniformHeight.of(VerticalAnchor.aboveBottom(0), VerticalAnchor.belowTop(0)), CTX);
        assertEquals("-64-319", span.range().toIntString());
    }

    @Test
    public void testWeightedListHeightSpanRecurses() {
        SimpleWeightedRandomList<HeightProvider> distribution = SimpleWeightedRandomList.<HeightProvider>builder()
                .add(ConstantHeight.of(VerticalAnchor.absolute(10)), 1)
                .add(ConstantHeight.of(VerticalAnchor.absolute(100)), 5)
                .build();
        HeightSpan span = UTILS.getHeightSpan(UTILS, new WeightedListHeight(distribution), CTX);
        assertEquals(Kind.WEIGHTED, span.kind());
        assertEquals("10-100", span.range().toIntString());     // union of branches
        assertEquals("100", span.bestBand().toIntString());     // best band = heaviest branch (weight 5 @ y=100)
    }

    @Test
    public void testCountOnEveryLayerIsUnknownTotal() {
        //noinspection deprecation
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, CountOnEveryLayerPlacement.of(5), CTX);
        // per-layer count is known (5) but the number of layers isn't ⇒ flagged uncertain
        assertEquals(Kind.UNKNOWN, contribution.count().kind());
        assertTrue(contribution.count().range().isUnknown());
        assertEquals("1[+???]", contribution.count().range().toIntString());
    }

    @Test
    public void testNoiseBasedCountIsUnknown() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, NoiseBasedCountPlacement.of(5, 1.5, 0.2), CTX);
        assertEquals(Kind.UNKNOWN, contribution.count().kind());
        assertEquals("1[+???]", contribution.count().range().toIntString()); // up to noiseToCountRatio
    }

    @Test
    public void testNoiseThresholdCountIsUnknown() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, NoiseThresholdCountPlacement.of(0.5, 2, 8), CTX);
        assertEquals(Kind.UNKNOWN, contribution.count().kind());
        assertEquals("1[+???]", contribution.count().range().toIntString()); // either belowNoise or aboveNoise
    }

    @Test
    public void testHeightmapPlacementFallsBackToHeightmap() {
        PlacementContribution contribution = UTILS.getPlacementContribution(UTILS, HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR), CTX);
        assertNull(contribution.count());
        assertNull(contribution.chancePercent());
        assertEquals(Kind.RELATIVE_TO_HEIGHTMAP, contribution.height().kind());
        assertEquals(Heightmap.Types.OCEAN_FLOOR, contribution.height().heightmap());
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
                "Height: 40-120 (Trapezoid), most likely 70-90"
        ));
    }

    @Test
    public void testSummaryTooltipCountShowsDistributionKind() {
        // non-constant, known count range ⇒ shown with its distribution kind, e.g. "2-6 (Uniform)"
        List<PlacementModifier> modifiers = List.of(CountPlacement.of(UniformInt.of(2, 6)));
        assertTooltip(renderSummary(modifiers), List.of(
                "Attempts Per Chunk: 2-6 (Uniform)"
        ));
    }

    @Test
    public void testSummaryTooltipHidesMostLikelyWhenEqualToRange() {
        // uniform ⇒ best band == full range ⇒ "Most Likely" would be redundant, so it's omitted
        List<PlacementModifier> modifiers = List.of(
                HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112)))
        );
        assertTooltip(renderSummary(modifiers), List.of(
                "Height: -16-112 (Uniform)"
        ));
    }

    @Test
    public void testSummaryTooltipHeightmapFallback() {
        List<PlacementModifier> modifiers = List.of(HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR));
        assertTooltip(renderSummary(modifiers), List.of(
                "Height: Solid Ground, Ignores Water"
        ));
    }

    @Test
    public void testSummaryTooltipUnknownCountRendersAsUnknown() {
        // noise-based count has no clean numeric value ⇒ show "Unknown", not "0-5[+???]", but nest the
        // modifier's own tooltip (header + parameters) so the reader can see which values it came from
        List<PlacementModifier> modifiers = List.of(NoiseBasedCountPlacement.of(5, 1.5, 0.2));
        assertTooltip(renderSummary(modifiers), List.of(
                "Attempts Per Chunk: Unknown",
                "  -> Noise Based Count Placement:",
                "    -> Noise To Count Ratio: 5",
                "    -> Noise Factor: 1.5",
                "    -> Noise Offset: 0.2"
        ));
    }

    private static com.yanny.aci.tooltip.TooltipNode renderSummary(List<PlacementModifier> modifiers) {
        return TooltipBuilder.branch((b) -> PlacementSummaryUtils.appendSummary(b, UTILS, modifiers, CTX)).build();
    }
}
