package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.function.BiFunction;

public interface ICoreServerRegistry<TServerUtils extends ICoreServerUtils<?>> {
    <T> void registerValueTooltip(Class<T> clazz, BiFunction<TServerUtils, T, TooltipBuilder> getter);

    void registerCacheCleaner(Runnable cleaner);

    <T extends IntProvider> void registerIntProvider(Class<T> type, BiFunction<TServerUtils, T, NumberExpr> converter);

    <T extends FloatProvider> void registerFloatProvider(Class<T> type, BiFunction<TServerUtils, T, NumberExpr> converter);
}
