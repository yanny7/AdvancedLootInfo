package com.yanny.ali.platform;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.platform.services.IPlatformHelper;
import com.yanny.ali.plugin.Plugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.nio.file.Path;
import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @Override
    public List<LootPool> getLootPools(LootTable table) {
        return List.of();
    }

    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new Plugin());
    }

    @Override
    public Path getConfiguration() {
        return null;
    }

    @Override
    public SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        return null;
    }
}
