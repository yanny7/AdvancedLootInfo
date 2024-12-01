package com.yanny.emi_loot_addon;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = EmiLootMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusSubscriber {
    @SubscribeEvent
    public static void onServerStarting(@NotNull ServerStartingEvent event) {
        EmiLootMod.INFO_PROPAGATOR.server().setLootDataManager(event.getServer().getLootData());
    }

    @SubscribeEvent
    public static void onPlayerLogIn(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        EmiLootMod.INFO_PROPAGATOR.server().sendMessage(event.getEntity());
    }
}
