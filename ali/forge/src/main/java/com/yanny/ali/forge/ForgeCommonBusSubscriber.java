package com.yanny.ali.forge;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonBusSubscriber {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarted(ServerStartedEvent event) {
        PluginManager.getInstance().registerServerEvent(event.getServer().overworld());
        AliMod.SERVER.readLootTables(event.getServer().getLootData());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PluginManager.getInstance().deregisterServerEvent();
    }
}
