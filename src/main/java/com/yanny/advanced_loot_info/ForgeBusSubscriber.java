package com.yanny.advanced_loot_info;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AdvancedLootInfoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusSubscriber {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        AdvancedLootInfoMod.INFO_PROPAGATOR.server().setLootDataManager(event.getServer().getLootData());
        //TODO do loot parsing there!
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        AdvancedLootInfoMod.INFO_PROPAGATOR.server().syncLootTables(event.getEntity());
    }
}
