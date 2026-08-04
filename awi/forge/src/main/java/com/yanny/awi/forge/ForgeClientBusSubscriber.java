package com.yanny.awi.forge;

import com.yanny.awi.Utils;
import com.yanny.awi.manager.PluginManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientBusSubscriber {
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        PluginManager.getInstance().clientRegistry.loggingIn(AwiMod.CHANNEL.isRemotePresent(event.getConnection()));
    }

    @SubscribeEvent
    public static void onLoggingOut(@SuppressWarnings("unused") ClientPlayerNetworkEvent.LoggingOut event) {
        PluginManager.getInstance().clientRegistry.loggingOut();
    }
}
