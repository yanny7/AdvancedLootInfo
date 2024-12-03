package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BonusLevelTableCondition.class)
public interface MixinBonusLevelTableCondition {
    @Accessor
    Enchantment getEnchantment();

    @Accessor
    float[] getValues();
}
