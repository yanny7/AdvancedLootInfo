package com.yanny.ali.neoforge;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = Utils.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NeoForgeCommonBusSubscriber {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        PluginManager.registerServerEvent();
        AliMod.SERVER.readLootTables(event.getServer().reloadableRegistries(), event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PluginManager.deregisterServerEvent();
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        AliMod.SERVER.syncLootTables(event.getEntity());
    }
}
