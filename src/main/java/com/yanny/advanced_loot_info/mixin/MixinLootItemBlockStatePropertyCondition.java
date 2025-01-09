package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemBlockStatePropertyCondition.class)
public interface MixinLootItemBlockStatePropertyCondition {
    @Accessor
    Block getBlock();

    @Accessor
    StatePropertiesPredicate getProperties();
}
