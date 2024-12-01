package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetPotionFunction.class)
public interface MixinSetPotionFunction {
    @Accessor
    Potion getPotion();
}
