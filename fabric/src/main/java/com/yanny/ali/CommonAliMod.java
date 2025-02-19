package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.Client;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.network.NetworkUtils;
import com.yanny.ali.network.Server;
import com.yanny.ali.registries.LootCategories;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class CommonAliMod implements ModInitializer {
    public static final DistHolder<Client, Server> INFO_PROPAGATOR;

    static {
        INFO_PROPAGATOR = NetworkUtils.registerLootInfoPropagator();
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(LootCategories.onResourceReload());
        FabricBusSubscriber.registerEvents();
        NetworkUtils.registerLootInfoPropagator();
        PluginManager.registerCommonEvent();
    }
}
