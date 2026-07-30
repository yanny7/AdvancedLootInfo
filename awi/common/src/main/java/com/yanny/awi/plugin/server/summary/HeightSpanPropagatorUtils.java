package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.levelgen.heightproviders.*;
import org.jetbrains.annotations.NotNull;

/**
 * One method per concrete {@link HeightProvider} type, producing a {@link HeightSpan} (absolute-Y range +
 * the "best band" where the block is most likely). Anchors are resolved via {@link ColumnContext} (no world
 * needed). {@link WeightedListHeight} recurses through {@code utils.getHeightSpan(...)} to find the best
 * band of its heaviest branch, so nested / modded providers resolve via their own propagator.
 *
 * <p>Band rule (locked): flat distributions get their exact flat interval; peaked (biased) ones collapse
 * to the single mode Y.
 */
public class HeightSpanPropagatorUtils {
    @NotNull
    public static HeightSpan getConstantHeight(IServerUtils ignoredUtils, ConstantHeight provider, ColumnContext ctx) {
        int y = ctx.resolveY(provider.getValue());
        RangeValue point = new RangeValue(y);
        return HeightSpan.of(point, point, Kind.CONSTANT);
    }

    @NotNull
    public static HeightSpan getUniformHeight(IServerUtils ignoredUtils, UniformHeight provider, ColumnContext ctx) {
        RangeValue range = new RangeValue(ctx.resolveY(provider.minInclusive), ctx.resolveY(provider.maxInclusive));
        // flat: the whole range is equally the "most likely" band
        return HeightSpan.of(range, range, Kind.UNIFORM);
    }

    @NotNull
    public static HeightSpan getTrapezoidHeight(IServerUtils ignoredUtils, TrapezoidHeight provider, ColumnContext ctx) {
        int min = ctx.resolveY(provider.minInclusive);
        int max = ctx.resolveY(provider.maxInclusive);
        int center = (min + max) / 2;
        RangeValue band = provider.plateau > 0
                ? new RangeValue(center - provider.plateau / 2f, center + provider.plateau / 2f) // flat plateau top
                : new RangeValue(center); // triangular ⇒ single mode
        return HeightSpan.of(new RangeValue(min, max), band, Kind.TRAPEZOID);
    }

    @NotNull
    public static HeightSpan getBiasedToBottomHeight(IServerUtils ignoredUtils, BiasedToBottomHeight provider, ColumnContext ctx) {
        int min = ctx.resolveY(provider.minInclusive);
        int max = ctx.resolveY(provider.maxInclusive);
        return HeightSpan.of(new RangeValue(min, max), new RangeValue(min), Kind.BIASED_TO_BOTTOM); // mode at bottom
    }

    @NotNull
    public static HeightSpan getVeryBiasedToBottomHeight(IServerUtils ignoredUtils, VeryBiasedToBottomHeight provider, ColumnContext ctx) {
        int min = ctx.resolveY(provider.minInclusive);
        int max = ctx.resolveY(provider.maxInclusive);
        return HeightSpan.of(new RangeValue(min, max), new RangeValue(min), Kind.VERY_BIASED_TO_BOTTOM); // mode at bottom
    }

    @NotNull
    public static HeightSpan getWeightedListHeight(IServerUtils utils, WeightedListHeight provider, ColumnContext ctx) {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        RangeValue bestBand = null;
        int bestWeight = -1;

        for (Weighted<HeightProvider> entry : provider.distribution.unwrap()) {
            HeightSpan span = utils.getHeightSpan(utils, entry.value(), ctx);
            min = Math.min(min, span.range().min());
            max = Math.max(max, span.range().max());

            int weight = entry.weight();
            if (weight > bestWeight && span.bestBand() != null) {
                bestWeight = weight;
                bestBand = span.bestBand(); // best band of the heaviest branch
            }
        }

        RangeValue range = new RangeValue(min, max);
        return HeightSpan.of(range, bestBand != null ? bestBand : range, Kind.WEIGHTED);
    }
}
