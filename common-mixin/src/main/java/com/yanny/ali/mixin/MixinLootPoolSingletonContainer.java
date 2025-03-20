package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPoolSingletonContainer.class)
public interface MixinLootPoolSingletonContainer {
    @Accessor
    int getWeight();

    @Accessor
    int getQuality();

    @Accessor
    LootItemFunction[] getFunctions();
}
