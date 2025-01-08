package com.yanny.emi_loot_addon.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.storage.loot.functions.SetBannerPatternFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetBannerPatternFunction.class)
public interface MixinSetBannerPatternFunction {
    @Accessor
    List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns();

    @Accessor
    boolean getAppend();
}
