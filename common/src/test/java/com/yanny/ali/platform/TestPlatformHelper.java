package com.yanny.ali.platform;

import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.platform.services.IPlatformHelper;
import com.yanny.ali.plugin.CommonPlugin;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @Override
    public List<LootPool> getLootPools(LootTable table) {
        return List.of();
    }

    @Override
    public List<PluginHolder> getPlugins() {
        return List.of(new PluginHolder("test", new CommonPlugin()));
    }

    @Override
    public DistHolder<AbstractClient, AbstractServer> getInfoPropagator() {
        return new DistHolder<>(null, null);
    }
}
