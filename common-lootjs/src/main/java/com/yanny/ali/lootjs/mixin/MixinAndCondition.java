package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.ILootCondition;
import com.almostreliable.lootjs.loot.condition.AndCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AndCondition.class)
public interface MixinAndCondition {
    @Accessor
    ILootCondition[] getConditions();
}
