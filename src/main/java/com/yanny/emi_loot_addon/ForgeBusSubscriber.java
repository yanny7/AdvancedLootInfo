package com.yanny.emi_loot_addon;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EmiLootMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusSubscriber {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        EmiLootMod.INFO_PROPAGATOR.server().setLootDataManager(event.getServer().getLootData());
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        EmiLootMod.INFO_PROPAGATOR.server().sendMessage(event.getEntity());
    }
}
