package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public interface MixinLootPoolSingletonContainer {
    int getWeight();

    int getQuality();

    List<LootItemFunction> getFunctions();
}
