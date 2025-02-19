package com.yanny.ali.platform.services;

import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface IPlatformHelper {
        List<LootPool> getLootPools(LootTable table);

        List<PluginHolder> getPlugins();

        DistHolder<AbstractClient, AbstractServer> getInfoPropagator();
}