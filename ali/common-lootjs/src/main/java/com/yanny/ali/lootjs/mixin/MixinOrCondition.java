package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.ILootCondition;
import com.almostreliable.lootjs.loot.condition.OrCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OrCondition.class)
public interface MixinOrCondition {
    @Accessor
    ILootCondition[] getConditions();
}
