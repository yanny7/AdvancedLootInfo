package com.yanny.aci.api;

import java.util.function.BiFunction;

public interface ICoreServerRegistry<
        TServerUtils    extends ICoreServerUtils<?, ?, ?>,
        TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
        > {
    <T> void registerValueTooltip(Class<T> clazz, BiFunction<TServerUtils, T, TKeyTooltipNode> getter);
}
