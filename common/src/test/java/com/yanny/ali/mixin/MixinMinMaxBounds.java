package com.yanny.ali.mixin;

import javax.annotation.Nullable;

public interface MixinMinMaxBounds<T extends Number> {
    interface Doubles extends MixinMinMaxBounds<Double> {}
    interface Ints extends MixinMinMaxBounds<Integer> {}

    @Nullable
    T getMin();

    @Nullable
    T getMax();
}
