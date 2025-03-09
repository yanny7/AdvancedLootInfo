package com.yanny.ali.mixin;

import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.functions.SetBannerPatternFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetBannerPatternFunction.class)
public interface MixinSetBannerPatternFunction {
    @Accessor
    BannerPatternLayers getPatterns();

    @Accessor
    boolean getAppend();
}
