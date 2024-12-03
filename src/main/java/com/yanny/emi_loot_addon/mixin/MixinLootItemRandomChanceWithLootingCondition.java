package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemRandomChanceWithLootingCondition.class)
public interface MixinLootItemRandomChanceWithLootingCondition {
    @Accessor
    float getPercent();

    @Accessor
    float getLootingMultiplier();
}
