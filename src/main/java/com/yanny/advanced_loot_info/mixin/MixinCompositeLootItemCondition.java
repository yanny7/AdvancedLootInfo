package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeLootItemCondition.class)
public interface MixinCompositeLootItemCondition {
    @Accessor
    LootItemCondition[] getTerms();
}
