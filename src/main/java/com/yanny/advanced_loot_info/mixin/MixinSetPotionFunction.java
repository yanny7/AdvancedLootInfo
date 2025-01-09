package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetPotionFunction.class)
public interface MixinSetPotionFunction {
    @Accessor
    Potion getPotion();
}
