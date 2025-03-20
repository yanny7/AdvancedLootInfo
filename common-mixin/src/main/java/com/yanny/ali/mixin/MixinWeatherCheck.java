package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeatherCheck.class)
public interface MixinWeatherCheck {
    @Nullable
    @Accessor
    Boolean isIsRaining();

    @Nullable
    @Accessor
    Boolean isIsThundering();
}
