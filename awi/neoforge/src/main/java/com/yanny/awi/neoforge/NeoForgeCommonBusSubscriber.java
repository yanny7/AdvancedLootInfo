package com.yanny.awi.neoforge;

import com.yanny.awi.Utils;
import com.yanny.awi.manager.PluginManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class NeoForgeCommonBusSubscriber {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        PluginManager.getInstance().registerServerEvent(event.getServer().overworld());
        AwiMod.SERVER.readWorldgenInfo(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onServerStopping(@SuppressWarnings("unused") ServerStoppingEvent event) {
        PluginManager.getInstance().deregisterServerEvent();
    }
}
