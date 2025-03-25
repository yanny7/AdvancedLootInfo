package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CompositeLootItemCondition.class)
public interface MixinCompositeLootItemCondition {
    @Accessor
    List<LootItemCondition> getTerms();
}
