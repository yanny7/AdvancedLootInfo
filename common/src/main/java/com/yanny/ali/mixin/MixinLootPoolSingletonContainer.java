package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootPoolSingletonContainer.class)
public interface MixinLootPoolSingletonContainer {
    @Accessor
    int getWeight();

    @Accessor
    int getQuality();

    @Accessor
    List<LootItemFunction> getFunctions();
}
