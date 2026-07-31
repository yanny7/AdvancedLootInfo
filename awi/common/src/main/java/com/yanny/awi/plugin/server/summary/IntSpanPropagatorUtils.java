package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.valueproviders.*;
import org.jetbrains.annotations.NotNull;

/**
 * One method per concrete {@link IntProvider} type, producing a {@link CountSpan} (range + distribution
 * kind). Registered in {@code Plugin.registerServer}. Recursive types (clamped/weighted) delegate back
 * through {@code utils.getIntSpan(...)} so nested / modded providers resolve via their own propagator.
 */
public class IntSpanPropagatorUtils {
    @NotNull
    public static CountSpan getConstantInt(IServerUtils ignoredUtils, ConstantInt provider) {
        return new CountSpan(new RangeValue(provider.value()), Kind.CONSTANT);
    }

    @NotNull
    public static CountSpan getUniformInt(IServerUtils ignoredUtils, UniformInt provider) {
        return new CountSpan(new RangeValue(provider.minInclusive(), provider.maxInclusive()), Kind.UNIFORM);
    }

    @NotNull
    public static CountSpan getBiasedToBottomInt(IServerUtils ignoredUtils, BiasedToBottomInt provider) {
        return new CountSpan(new RangeValue(provider.minInclusive(), provider.maxInclusive()), Kind.BIASED_TO_BOTTOM);
    }

    @NotNull
    public static CountSpan getClampedNormalInt(IServerUtils ignoredUtils, ClampedNormalInt provider) {
        return new CountSpan(new RangeValue(provider.minInclusive(), provider.maxInclusive()), Kind.CLAMPED_NORMAL);
    }

    @NotNull
    public static CountSpan getClampedInt(IServerUtils utils, ClampedInt provider) {
        // recurse into the wrapped source, then clamp it to the outer bounds
        CountSpan source = utils.getIntSpan(utils, provider.source);
        RangeValue clamped = source.range().clamp(provider.minInclusive(), provider.maxInclusive());
        return new CountSpan(clamped, Kind.CLAMPED);
    }

    @NotNull
    public static CountSpan getWeightedListInt(IServerUtils utils, WeightedListInt provider) {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for (Weighted<IntProvider> entry : provider.distribution.unwrap()) {
            RangeValue range = utils.getIntSpan(utils, entry.value()).range();
            min = Math.min(min, range.min());
            max = Math.max(max, range.max());
        }

        return new CountSpan(new RangeValue(min, max), Kind.WEIGHTED);
    }

    @NotNull
    public static CountSpan getTrapezoidInt(IServerUtils ignoredUtils, TrapezoidInt provider) {
        return new CountSpan(new RangeValue(provider.minInclusive(), provider.maxInclusive()), Kind.TRAPEZOID);

    }
}
