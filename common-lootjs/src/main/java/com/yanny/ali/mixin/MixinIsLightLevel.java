package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.IsLightLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IsLightLevel.class)
public interface MixinIsLightLevel {
    @Accessor
    int getMin();

    @Accessor
    int getMax();
}
