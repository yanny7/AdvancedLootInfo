package com.yanny.awi.forge.gametest;

import com.yanny.awi.Utils;
import com.yanny.awi.forge.AwiMod;
import com.yanny.awi.manager.PluginManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Forge's GameTestServer never posts ServerStartingEvent, which starts the scan in production
@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameTestServerScan {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PluginManager.getInstance().registerServerEvent(event.getServer().overworld());
        AwiMod.SERVER.readWorldgenInfo(event.getServer().overworld());
    }
}
