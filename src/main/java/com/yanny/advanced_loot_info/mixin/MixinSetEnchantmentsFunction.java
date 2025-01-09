package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SetEnchantmentsFunction.class)
public interface MixinSetEnchantmentsFunction {
    @Accessor
    Map<Enchantment, NumberProvider> getEnchantments();

    @Accessor
    boolean getAdd();
}
