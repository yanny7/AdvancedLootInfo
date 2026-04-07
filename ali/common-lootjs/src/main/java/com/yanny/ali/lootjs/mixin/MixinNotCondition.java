package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.ILootCondition;
import com.almostreliable.lootjs.loot.condition.NotCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NotCondition.class)
public interface MixinNotCondition {
    @Accessor
    ILootCondition getCondition();
}
