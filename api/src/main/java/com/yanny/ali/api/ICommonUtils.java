package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICommonUtils {
    @Nullable
    LootTable getLootTable(ResourceLocation resourceLocation);

    List<LootPool> getLootPools(LootTable lootTable);
}
