package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(MinMaxBounds.class)
public interface MixinMinMaxBounds<T extends Number> {
    @Mixin(MinMaxBounds.Doubles.class)
    interface Doubles extends MixinMinMaxBounds<Double> {}

    @Mixin(MinMaxBounds.Ints.class)
    interface Ints extends MixinMinMaxBounds<Integer> {}

    @Nullable
    @Accessor
    T getMin();

    @Nullable
    @Accessor
    T getMax();
}
