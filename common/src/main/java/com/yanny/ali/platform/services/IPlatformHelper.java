package com.yanny.ali.platform.services;

import com.yanny.ali.manager.PluginHolder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface IPlatformHelper {
        List<LootPool> getLootPools(LootTable table);

        List<PluginHolder> getPlugins();
}