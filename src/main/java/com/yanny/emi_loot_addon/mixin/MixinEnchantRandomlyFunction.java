package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EnchantRandomlyFunction.class)
public interface MixinEnchantRandomlyFunction {
    @Accessor
    List<Enchantment> getEnchantments();
}
