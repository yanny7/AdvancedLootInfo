package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPoolEntryContainer.class)
public interface MixinLootPoolEntryContainer {
    @Accessor
    LootItemCondition[] getConditions();
}
