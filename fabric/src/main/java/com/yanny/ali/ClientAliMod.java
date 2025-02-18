package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.registries.LootCategories;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class ClientAliMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PluginManager.registerClientEvent();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(LootCategories.onResourceReload());
    }
}
