package com.yanny.awi.forge;

import com.yanny.awi.Utils;
import com.yanny.awi.manager.PluginManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonBusSubscriber {
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
