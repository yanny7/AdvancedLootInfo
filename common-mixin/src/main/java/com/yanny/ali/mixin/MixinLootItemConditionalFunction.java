package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootItemConditionalFunction.class)
public interface MixinLootItemConditionalFunction {
    @Accessor
    List<LootItemCondition> getPredicates();
}
