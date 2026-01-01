package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeServerBusSubscriber {
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
