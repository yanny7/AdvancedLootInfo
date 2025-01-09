package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InvertedLootItemCondition.class)
public interface MixinInvertedLootItemCondition {
    @Accessor
    LootItemCondition getTerm();
}
