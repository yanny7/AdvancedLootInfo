package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ApplyBonusCount.BinomialWithBonusCount.class)
public interface MixinBinomialWithBonusCount {
    @Accessor
    int getExtraRounds();
}
