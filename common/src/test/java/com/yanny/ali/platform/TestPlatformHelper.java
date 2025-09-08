package com.yanny.ali.platform;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.platform.services.IPlatformHelper;
import com.yanny.ali.plugin.Plugin;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

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
}
