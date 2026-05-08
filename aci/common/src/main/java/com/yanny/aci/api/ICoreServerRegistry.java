package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipBuilder;

import java.util.function.BiFunction;

public interface ICoreServerRegistry<TServerUtils extends ICoreServerUtils<?>> {
    <T> void registerValueTooltip(Class<T> clazz, BiFunction<TServerUtils, T, TooltipBuilder> getter);
}
