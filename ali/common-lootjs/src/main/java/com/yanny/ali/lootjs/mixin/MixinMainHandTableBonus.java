package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.condition.MainHandTableBonus;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MainHandTableBonus.class)
public interface MixinMainHandTableBonus {
    @Accessor
    Enchantment getEnchantment();

    @Accessor
    float[] getValues();
}
