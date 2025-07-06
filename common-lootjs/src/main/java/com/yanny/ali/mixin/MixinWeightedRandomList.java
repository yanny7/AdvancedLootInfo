package com.yanny.ali.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedRandomList.class)
public interface MixinWeightedRandomList<T extends WeightedEntry> {
    @Accessor
    ImmutableList<T> getItems();

    @Accessor
    int getTotalWeight();
}
