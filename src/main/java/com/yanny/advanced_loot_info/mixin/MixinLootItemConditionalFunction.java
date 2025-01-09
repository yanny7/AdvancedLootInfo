package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemConditionalFunction.class)
public interface MixinLootItemConditionalFunction {
    @Accessor
    LootItemCondition[] getPredicates();
}
