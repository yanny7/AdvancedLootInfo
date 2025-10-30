package com.yanny.ali.platform.services;

import com.yanny.ali.api.IPlugin;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {
        List<LootPool> getLootPools(LootTable table);

        List<IPlugin> getPlugins();

        Path getConfiguration();
}