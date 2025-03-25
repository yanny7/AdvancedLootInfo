package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public interface MixinLootPoolSingletonContainer {
    int getWeight();

    int getQuality();

    LootItemFunction[] getFunctions();
}
