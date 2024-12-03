package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemRandomChanceCondition.class)
public interface MixinLootItemRandomChanceCondition {
    @Accessor
    float getProbability();
}
