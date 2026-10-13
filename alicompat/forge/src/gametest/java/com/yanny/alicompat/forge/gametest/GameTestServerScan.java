package com.yanny.alicompat.forge.gametest;

import com.yanny.ali.forge.AliMod;
import com.yanny.ali.manager.PluginManager;
import com.yanny.alicompat.Utils;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Forge's GameTestServer never posts ServerStartingEvent, which starts ALI's scan in production
@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameTestServerScan {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PluginManager.getInstance().registerServerEvent(event.getServer().overworld());
        AliMod.SERVER.readLootTables(event.getServer().reloadableRegistries());
    }
}
