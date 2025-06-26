package com.yanny.ali.api;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface ICommonUtils {
    List<LootPool> getLootPools(LootTable lootTable);
}
