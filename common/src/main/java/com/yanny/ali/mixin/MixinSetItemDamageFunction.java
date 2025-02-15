package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetItemDamageFunction.class)
public interface MixinSetItemDamageFunction {
    @Accessor
    NumberProvider getDamage();

    @Accessor
    boolean getAdd();
}
