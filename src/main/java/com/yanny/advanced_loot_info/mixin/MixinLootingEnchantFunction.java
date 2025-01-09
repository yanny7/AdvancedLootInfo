package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootingEnchantFunction.class)
public interface MixinLootingEnchantFunction {
    @Accessor
    NumberProvider getValue();

    @Accessor
    int getLimit();
}
