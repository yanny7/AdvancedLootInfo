package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import net.minecraft.util.valueproviders.*;
import org.jetbrains.annotations.NotNull;

public class CommonNumberProviders<
        TServerUtils    extends ICoreServerUtils<TServerUtils>,
        TServerRegistry extends ICoreServerRegistry<TServerUtils>
        > {
    public void registerAll(TServerRegistry registry) {
        registry.registerIntProvider(ConstantInt.class, this::getConstantInt);
        registry.registerIntProvider(UniformInt.class, this::getUniformInt);
        registry.registerIntProvider(BiasedToBottomInt.class, this::getBiasedToBottomInt);
        registry.registerIntProvider(ClampedInt.class, this::getClampedInt);
        registry.registerIntProvider(ClampedNormalInt.class, this::getClampedNormalInt);
        registry.registerIntProvider(WeightedListInt.class, this::getWeightedListInt);
        registry.registerIntProvider(TrapezoidInt.class, this::getTrapezoidInt);

        registry.registerFloatProvider(ConstantFloat.class, this::getConstantFloat);
        registry.registerFloatProvider(UniformFloat.class, this::getUniformFloat);
        registry.registerFloatProvider(ClampedNormalFloat.class, this::getClampedNormalFloat);
        registry.registerFloatProvider(TrapezoidFloat.class, this::getTrapezoidFloat);
    }

    @NotNull
    private NumberExpr getConstantInt(TServerUtils utils, ConstantInt provider) {
        return NumberExpr.constant(provider.value());
    }

    @NotNull
    private NumberExpr getUniformInt(TServerUtils utils, UniformInt provider) {
        return NumberExpr.uniformInt(provider.minInclusive(), provider.maxInclusive());
    }

    @NotNull
    private NumberExpr getBiasedToBottomInt(TServerUtils utils, BiasedToBottomInt provider) {
        return NumberExpr.fn(NumberFunctions.BIASED_TO_BOTTOM, NumberExpr.constant(provider.minInclusive()), NumberExpr.constant(provider.maxInclusive()));
    }

    @NotNull
    private NumberExpr getClampedInt(TServerUtils utils, ClampedInt provider) {
        return NumberExpr.clamp(
                utils.convertIntProvider(utils, provider.source()),
                NumberExpr.constant(provider.minInclusive()),
                NumberExpr.constant(provider.maxInclusive())
        );
    }

    @NotNull
    private NumberExpr getClampedNormalInt(TServerUtils utils, ClampedNormalInt provider) {
        return NumberExpr.fn(NumberFunctions.TRUNC, NumberExpr.clamp(
                NumberExpr.fn(NumberFunctions.NORMAL, NumberExpr.constant(provider.mean()), NumberExpr.constant(provider.deviation())),
                NumberExpr.constant(provider.minInclusive()),
                NumberExpr.constant(provider.maxInclusive())
        ));
    }

    @NotNull
    private NumberExpr getWeightedListInt(TServerUtils utils, WeightedListInt provider) {
        return NumberExpr.weighted(provider.distribution.unwrap().stream()
                .map((e) -> new NumberExpr.WeightedEntry(e.weight(), utils.convertIntProvider(utils, e.value())))
                .toList());
    }

    @NotNull
    private NumberExpr getConstantFloat(TServerUtils utils, ConstantFloat provider) {
        return NumberExpr.constant(provider.value());
    }

    @NotNull
    private NumberExpr getUniformFloat(TServerUtils utils, UniformFloat provider) {
        return NumberExpr.uniformFloat(provider.min(), provider.max());
    }

    @NotNull
    private NumberExpr getClampedNormalFloat(TServerUtils utils, ClampedNormalFloat provider) {
        return NumberExpr.clamp(
                NumberExpr.fn(NumberFunctions.NORMAL, NumberExpr.constant(provider.mean()), NumberExpr.constant(provider.deviation())),
                NumberExpr.constant(provider.min()),
                NumberExpr.constant(provider.max())
        );
    }

    @NotNull
    private NumberExpr getTrapezoidFloat(TServerUtils utils, TrapezoidFloat provider) {
        return NumberExpr.fn(
                NumberFunctions.TRAPEZOID_FLOAT,
                NumberExpr.constant(provider.min()),
                NumberExpr.constant(provider.max()),
                NumberExpr.constant(provider.plateau())
        );
    }

    @NotNull
    private NumberExpr getTrapezoidInt(TServerUtils utils, TrapezoidInt provider) {
        return NumberExpr.fn(
                NumberFunctions.TRAPEZOID_INT,
                NumberExpr.constant(provider.minInclusive()),
                NumberExpr.constant(provider.maxInclusive()),
                NumberExpr.constant(provider.plateau())
        );
    }
}
