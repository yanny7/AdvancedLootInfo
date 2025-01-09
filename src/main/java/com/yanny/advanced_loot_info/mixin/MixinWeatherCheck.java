package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeatherCheck.class)
public interface MixinWeatherCheck {
    @Accessor
    Boolean isIsRaining();

    @Accessor
    Boolean isIsThundering();
}
