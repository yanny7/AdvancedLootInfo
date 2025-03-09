package com.yanny.ali;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber(modid = Utils.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeBusSubscriber {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        AliMod.INFO_PROPAGATOR.server().readLootTables(event.getServer().reloadableRegistries(), event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        AliMod.INFO_PROPAGATOR.server().syncLootTables(event.getEntity());
    }
}
