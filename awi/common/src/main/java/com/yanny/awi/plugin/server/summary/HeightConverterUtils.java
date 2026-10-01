package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.common.HeightFunctions;
import net.minecraft.world.level.levelgen.heightproviders.*;
import org.jetbrains.annotations.NotNull;

public class HeightConverterUtils {
    @NotNull
    public static NumberExpr getConstantHeight(IServerUtils ignoredUtils, ConstantHeight provider, ColumnContext ctx) {
        return NumberExpr.constant(ctx.resolveY(provider.getValue()));
    }

    @NotNull
    public static NumberExpr getUniformHeight(IServerUtils ignoredUtils, UniformHeight provider, ColumnContext ctx) {
        return NumberExpr.uniformInt(ctx.resolveY(provider.minInclusive), ctx.resolveY(provider.maxInclusive));
    }

    @NotNull
    public static NumberExpr getBiasedToBottomHeight(IServerUtils ignoredUtils, BiasedToBottomHeight provider, ColumnContext ctx) {
        return HeightFunctions.biasedToBottom(ctx.resolveY(provider.minInclusive), ctx.resolveY(provider.maxInclusive), provider.inner);
    }

    @NotNull
    public static NumberExpr getVeryBiasedToBottomHeight(IServerUtils ignoredUtils, VeryBiasedToBottomHeight provider, ColumnContext ctx) {
        return HeightFunctions.veryBiasedToBottom(ctx.resolveY(provider.minInclusive), ctx.resolveY(provider.maxInclusive), provider.inner);
    }

    @NotNull
    public static NumberExpr getTrapezoidHeight(IServerUtils ignoredUtils, TrapezoidHeight provider, ColumnContext ctx) {
        return HeightFunctions.trapezoid(ctx.resolveY(provider.minInclusive), ctx.resolveY(provider.maxInclusive), provider.plateau);
    }

    @NotNull
    public static NumberExpr getWeightedListHeight(IServerUtils utils, WeightedListHeight provider, ColumnContext ctx) {
        return NumberExpr.weighted(provider.distribution.unwrap().stream()
                .map((e) -> new NumberExpr.WeightedEntry(e.weight(), utils.convertHeightProvider(utils, e.value(), ctx)))
                .toList());
    }
}
